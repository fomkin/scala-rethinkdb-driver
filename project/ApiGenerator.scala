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

  private[this] def genFuncs(module: module, hasDep: Boolean) = {
    module.funcs.zipWithIndex map { case (func, i) ⇒
      // Header
      val funcName = nameToCamelCase(func.customName.getOrElse(module.name), isClass = false)
      val argsDef = func.args.collect {
        case arg@multiarg(_, _: Top.FunctionArg) ⇒
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
        val xs = func.args.collect { case x: opt ⇒ x }
        val genMap = s"""val opts = Map(${xs.map(x ⇒ s""" "${x.name}" -> ${nameToCamelCase(x.name)}""").mkString(",")})"""
        val genPrinter = """opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}"""
        "${" + s"""$genMap; $genPrinter.mkString(",") """ + "}"
      }
      val ret = module.dataTypes.map(topToName).mkString(" with ")
      val complexRet = {
        if (optsDef.nonEmpty) nameToCamelCase(module.name, isClass = true) + i
        else ret
      }
      val toStrWithoutOpts = {
        val xs = func.args.collect {
          case x: arg =>
            val ccName = nameToCamelCase(x.name)
            ccName + " = ${" + ccName + ".toString}"
          case x: multiarg =>
            val ccName = nameToCamelCase(x.name)
            ccName + " = [${" + ccName + ".mkString(\",\")}]"
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
          ccName + " = ${" + ccName + ".toString}"
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
      val withOps = if (optsDef.nonEmpty) {
        val optsDefStr = optsDef.mkString(", ")
        s"""    def optargs($optsDefStr): $ret = new $ret {
           |      val json = s\"\""[${module.termType},[$args],{$opts}]\"\""
           |      override def toString: String = $toStrDep$toStrWithOpts
           |    }
         """.stripMargin
      } else {
        ""
      }
      val argsDefStr = {
        if (argsDef.isEmpty) ""
        else "(" + argsDef.mkString(", ") + ")"
      }
      s"""  $doc
          |  def $funcName$argsDefStr: $complexRet = new $complexRet {
          |    lazy val json = s\"\""[${module.termType},[$args]]\"\""
          |    override def toString: String = $toStrDep$toStrWithoutOpts
          |$withOps
          |  }
       """.stripMargin
    }    
  }

  private[this] def genFuncResultTypes(module: module) = {
    module.funcs.zipWithIndex map { case (func, i) ⇒
      val complexRet = nameToCamelCase(module.name, isClass = true) + i
      val ret = module.dataTypes.map(topToName).mkString(" with ")
      val optsDef = func.args.collect {
        case arg: opt ⇒
          val name = nameToCamelCase(arg.name)
          val tpe = topToName(arg.tpe)
          s"$name: $tpe = EmptyOption"
      }
      if (optsDef.nonEmpty) {
        s"""
           |  trait $complexRet extends $ret {
           |    def optargs(${optsDef.mkString(", ")}): $ret
           |  }
           |""".stripMargin
      } else ""
    }
  }

  private[this] def genOps() = {
    Top.all map { tpe ⇒
      val someTpe = Some(tpe)
      val tpeName = topToName(tpe)
      val className = tpeName + "Ops"
      val (funTypes, funDefs) = {
        val pureModules = modules map { module ⇒
          val newFuncs = module.funcs.filter(fun ⇒ fun.dependency == someTpe)
          module.copy(funcs = newFuncs)
        }
        val funTypes = pureModules.flatMap(genFuncResultTypes)
        val funDefs = pureModules.flatMap(genFuncs(_, hasDep = true))
        (funTypes.mkString("\n"), funDefs.mkString("\n"))
      }
      ReqlFile(className + ".scala",
        s"""
           |package reql.dsl
           |
           |import reql.dsl.types._
           |
           |// Generated code. Do not modify
           |object $className {
           |$funTypes
           |}
           |
           |final class $className(val self: $tpeName) extends AnyVal {
           |import $className._
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
    val (funTypes, funDefs) = {
      val pureModules = modules map { module ⇒
        val newFuncs = module.funcs.filter(fun ⇒ fun.dependency.isEmpty)
        module.copy(funcs = newFuncs)
      }
      val funTypes = pureModules.flatMap(genFuncResultTypes)
      val funDefs = pureModules.flatMap(genFuncs(_, hasDep = false))
      (funTypes.mkString("\n"), funDefs.mkString("\n"))
    }
    ReqlFile("ReqlTopLevelApi.scala",
      s"""
         |package reql.dsl
         |
         |import reql.dsl.types._
         |
         |object ReqlTopLevelApi {
         |$funTypes
         |}
         |
         |// Generated code. Do not modify|
         |class ReqlTopLevelApi {
         |import ReqlTopLevelApi._
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

object ApiGenerator extends ApiGenerator(ApiDefinitionsGenerator.modules())

case class ReqlFile(name: String, content: String)
