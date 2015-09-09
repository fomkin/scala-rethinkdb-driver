import sbt.{File, _}

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
    case Top ⇒ "Top"
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
    val argsDef = func.args.collect {
      case arg @ multiarg(_, _: Top.FunctionArg) ⇒
        val name = nameToCamelCase(arg.name)
        val tpe = topToName(arg.tpe)
        s"$name: ($tpe)*"
      case arg: multiarg ⇒
        val name = nameToCamelCase(arg.name)
        val tpe = topToName(arg.tpe)
        s"$name: $tpe*"
      case arg: arg ⇒
        val name = nameToCamelCase(arg.name)
        val tpe = topToName(arg.tpe)
        s"$name: $tpe"
    }
    val optsDef = func.args.collect {
      case arg: opt ⇒
        val name = nameToCamelCase(arg.name)
        val tpe = topToName(arg.tpe)
        s"$name: $tpe = EmptyOption"
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
//      val xs = func.args.zipWithIndex.collect {
//        case (opt(name, _), i) ⇒
//          val ccName = nameToCamelCase(name)
//          val comma = if (i < func.args.length - 1) """ + "," """ else ""
//          val x = s""" "$name:" + extractJson($ccName)""" + comma
//          "${if("+ccName+" != EmptyOption)" + x + """ else ""}"""
//      }
//      xs.mkString
      //      val xs = func.args.collect { case opt(name, _) ⇒ nameToCamelCase(name) }
      //      "{" + s"""Seq(${xs.mkString(",")}).map(extractJson).mkString(",")""" + "}"
      val xs = func.args.collect { case x: opt ⇒ x }
      val genMap = s"""val opts = Map(${xs.map(x ⇒ s""" "${x.name}" -> ${nameToCamelCase(x.name)}""").mkString(",")})"""
      val genPrinter = """opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}"""
      "${" + s"""$genMap; $genPrinter.mkString(",") """ + "}"
    }
    val ret = module.dataTypes.map(topToName).mkString(" with ")

    val toStrWithoutOpts = {
      val xs = func.args.collect {
        case x: arg =>
          val ccName = nameToCamelCase(x.name)
          ccName + " = ${"+ccName+".toString}"
        case x: multiarg =>
          val ccName = nameToCamelCase(x.name)
          ccName + " = [${"+ccName+".mkString(\",\")}]"
      }
      "s\"" + funcName + "(" + xs.mkString(", ") + ")" + "\""
    }

    val toStrDep = {
      if (hasDep) """self.toString + "." + """
      else ""
    }
    
    val toStrWithOpts = {
      val xs = func.args.map { x =>
        val ccName = nameToCamelCase(x.name)
        ccName + " = ${"+ccName+".toString}"
      }
      "s\"" + funcName + "(" + xs.mkString(", ") + ")" + "\""
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

    val withoutOps = {
      val argsDefStr = {
        if (argsDef.isEmpty) ""
        else "(" + argsDef.mkString(", ") + ")"
      }
      s"""  def $funcName$argsDefStr: $ret = new $ret {
         |    // Without opts
         |    val json = s\"\""[${module.termType},[$args]]\"\""
         |    override def toString: String = $toStrDep$toStrWithoutOpts
         |  }
     """.stripMargin
    }
    val withOps = if (optsDef.nonEmpty) {
      val argsDefStr = argsDef.foldLeft("")(_ + _ + ", ")
      val optsDefStr = optsDef.mkString(", ")
      s"""  def $funcName($argsDefStr$optsDefStr): $ret = new $ret {
         |    // With opts
         |    val json = s\"\""[${module.termType},[$args],{$opts}]\"\""
         |    override def toString: String = $toStrDep$toStrWithOpts
         |  }
       """.stripMargin
    } else {
      ""
    }
    s"""$doc
       |$withoutOps
       |$withOps
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
           |// Generated code. Do not modify
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
         |// Generated code. Do not modify
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
         |// Generated code. Do not modify|
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
