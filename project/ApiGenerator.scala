import sbt.File
import sbt._

class ApiGenerator(modules: Seq[module]) extends (File ⇒ Seq[File]) {

  private[this] def nameToCamelCase(name: String, isClass: Boolean = false): String = {
    val parts = name.split("_").zipWithIndex map {
      case (part, i) if i == 0 && !isClass ⇒ part
      case (part, _) ⇒ part.charAt(0).toUpper + part.substring(1)
    }
    parts.mkString
  }

  private[this] def topToName(t: Top): String = t match {
    case Top.Arr ⇒ "Arr"
    case Top.Database ⇒ "Database"
    case Top.Datum ⇒ "Datum"
    case Top.Error ⇒ "Error"
    case Top.Function ⇒ "Function"
    case Top.Ordering ⇒ "Ordering"
    case Top.Pathspec ⇒ "Pathspec"
    case Top.Sequence ⇒ "Sequence"
    // --
    case Top.Datum.Bool ⇒ "Bool"
    case Top.Datum.Field ⇒ "Field"
    case Top.Datum.Null ⇒ "Null"
    case Top.Datum.Num ⇒ "Num"
    case Top.Datum.Obj ⇒ "Obj"
    case Top.Datum.SingleSelection ⇒ "SingleSelection"
    case Top.Datum.Str ⇒ "Str"
    // --
    case Top.Sequence.Stream ⇒ "Stream"
    case Top.Sequence.StreamSelection ⇒ "StreamSelection"
    case Top.Sequence.Table ⇒ "Table"
    // --
    case Top.PseudoType.Time ⇒ "Time"
    case Top.PseudoType.Binary ⇒ "Binary"

  }

  private[this] def genFunc(module: module, func: fun, hasDep: Boolean) = {
    // Header
    val funcName = nameToCamelCase(func.customName.getOrElse(module.name), isClass = false)
    val argsDef = {
      val xs = func.args.map { arg ⇒
        val name = nameToCamelCase(arg.name)
        val tpe = topToName(arg.tpe)
        s"$name: $tpe"
      }
      xs.mkString(", ")
    }
    // Body
    val args = {
      val xs = func.args.collect {
        case arg(name, _) ⇒
          val ccName = nameToCamelCase(name)
          "${" + ccName + ".json}"
      }
      if (hasDep) {
        if (xs.isEmpty) "${self.json}"
        else "${self.json}, " + xs.mkString(", ")
      }
      else xs.mkString(", ")
    }
    val opts = {
      val xs = func.args.collect {
        case opt(name, _) ⇒
          val ccName = "${" + nameToCamelCase(name) + ".json}"
          s""""$name" : $ccName"""
      }
      xs.mkString(", ")
    }
    val doc = {
      val lines = module.doc.fold("") { s ⇒
        s.split("\n").map("   * " + _).mkString("\n")
      }
      s"""
         |  /**
         |$lines
         |   */
       """.stripMargin
    }
    val ret = module.dataTypes.map(topToName).mkString(" with ")
    s"""
       |  $doc
       |  def $funcName($argsDef): $ret = new $ret {
       |    val json = s\"\""[
       |      ${module.termType},
       |      [$args],
       |      {$opts}
       |    ]\"\""
       |  }
     """.stripMargin
  }

  private[this] def genOps() = {
    Top.all map { tpe ⇒
      val someTpe = Some(tpe)
      val tpeName = topToName(tpe)
      val className = tpeName + "Ops"
      val funDefs = {
        val xs = modules flatMap { module ⇒
          module.funcs.
            filter(fun ⇒ fun.dependency == someTpe).
            map(genFunc(module, _, hasDep = true))
        }
        xs.mkString("\n")
      }
      ReqlFile(className + ".scala",
        s"""
           |package reql.dsl
           |
           |import reql.dsl.types._
           |
           |final class $className(val self: $tpeName) extends AnyVal {
           |$funDefs
           |}
         """.stripMargin
      )
    }
  }

  private[this] def genImplicits() = {
    val defs = {
      val xs = Top.all map { tpe ⇒
        val name = topToName(tpe)
        val nameOps = name + "Ops"
        s"  implicit def To$nameOps(x: $name): $nameOps = new $nameOps(x)"
      }
      xs.mkString("\n")
    }
    ReqlFile("OpsImplicits.scala",
      s"""
         |package reql.dsl
         |
         |import reql.dsl.types._
         |
         |trait OpsImplicits {
         |$defs
         |}
     """.stripMargin
    )
  }

  private[this] def genBaseOps() = {
    val funDefs = {
      val xs = modules flatMap { module ⇒
        module.funcs.
          filter(fun ⇒ fun.dependency.isEmpty).
          map(genFunc(module, _, hasDep = false))
      }
      xs.mkString("\n")
    }
    ReqlFile("BaseOps.scala",
      s"""
         |package reql.dsl
         |
         |import reql.dsl.types._
         |
         |class BaseOps {
         |$funDefs
         |}
     """.stripMargin
    )
  }

  def apply(dir: File): Seq[File] = {
    val packageDir = dir / "reql" / "dsl"
    (genBaseOps() :: genImplicits() :: genOps().toList) map { rf ⇒
      val file = packageDir / rf.name
      IO.write(file, rf.content)
      file
    }
  }
}

object ApiGenerator extends ApiGenerator(ApiDefinitions.modules)

case class ReqlFile(name: String, content: String)
