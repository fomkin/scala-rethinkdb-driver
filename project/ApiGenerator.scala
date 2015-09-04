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
    case Top.FunctionArg(n) ⇒
      val args = (0 until n).map(_ ⇒ "Var").mkString(",")
      s"($args) => Function"
    case Top.Function ⇒ "Function"
    case Top.Arr ⇒ "Arr"
    case Top.Database ⇒ "Database"
    case Top.Datum ⇒ "Datum"
    case Top.Error ⇒ "Error"
    case Top.Ordering ⇒ "Ordering"
    case Top.Pathspec ⇒ "Pathspec"
    case Top.Sequence ⇒ "Sequence"
    // --
    case Top.Datum.Bool ⇒ "Bool"
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
    // --
    case Top.AnyType ⇒ "AnyType"
  }

  private[this] def genFunc(module: module, func: fun, hasDep: Boolean) = {
    // Header
    val funcName = nameToCamelCase(func.customName.getOrElse(module.name), isClass = false)
    val argsDef = {
      val xs = func.args.map { arg ⇒
        val name = nameToCamelCase(arg.name)
        val tpe = topToName(arg.tpe)
        if (arg.isMulti) {
          arg.tpe match {
            case _: Top.FunctionArg ⇒ s"$name: ($tpe)*"
            case _ ⇒ s"$name: $tpe*"
          }
        }
        else s"$name: $tpe"
      }
      xs.mkString(", ")
    }
    // Body
    val args = {
      val xs = func.args.collect {
        case arg(name, _) ⇒
          val ccName = nameToCamelCase(name)
          "${extractJson(" + ccName + ")}"
        case multiarg(name, _) ⇒
          val ccName = nameToCamelCase(name)
          "${" + ccName + ".map(extractJson).mkString(\", \")}"
      }
      if (hasDep) {
        if (xs.isEmpty) "${extractJson(self)}"
        else "${extractJson(self)}, " + xs.mkString(", ")
      }
      else xs.mkString(", ")
    }
    val opts = {
      val xs = func.args.collect {
        case opt(name, _) ⇒
          val ccName = "${extractJson(" + nameToCamelCase(name) + ")}"
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
//    val varFunDef = if (func.args.nonEmpty) {
//      val varArgsDef = {
//        val xs = func.args map { arg ⇒
//          val name = nameToCamelCase(arg.name)
//          if (arg.isMulti) s"$name: Var*"
//          else s"$name: Var"
//        }
//        xs.mkString(", ")
//      }
//      s"""
//         |  def $funcName($varArgsDef): $ret = new $ret {
//         |    val json = s\"\""[${module.termType},[$args],{$opts}]\"\""
//         |  }
//       """
//    } else {
//      ""
//    }
    s"""
       |$doc
       |  def $funcName($argsDef): $ret = new $ret {
       |    val json = s\"\""[${module.termType},[$args],{$opts}]\"\""
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
        s"  implicit def to$nameOps(x: $name): $nameOps = new $nameOps(x)"
      }
      xs.mkString("\n")
    }
    ReqlFile("ReqlTermOpsConversions.scala",
      s"""
         |package reql.dsl
         |
         |import reql.dsl.types._
         |
         |trait ReqlTermOpsConversions {
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
    ReqlFile("ReqlTopLevelApi.scala",
      s"""
         |package reql.dsl
         |
         |import reql.dsl.types._
         |
         |class ReqlTopLevelApi {
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
