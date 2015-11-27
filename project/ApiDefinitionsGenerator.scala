import scala.annotation.tailrec
import scala.io.Source
import spray.json._
import DefaultJsonProtocol._

object ApiDefinitionsGenerator {

  def modules(): Seq[module] = {
    //todo: read term_info.json from java driver
    val jsonFilename = "term_info.json"

    val jsonStr = Source.fromFile(jsonFilename).mkString
    val jsonAst = jsonStr.parseJson
    val map = jsonAst.convertTo[Map[String, JsValue]]
    val modules = map.keys collect {
      case mathKey @ ("EQ" | "NE" | "LT" | "GT" |
                  "BRANCH" | "OR" | "AND" |
                  "ADD" | "SUB") =>
        mathModules.get(mathKey) match {
          case Some(m: module) => m
        }
      case key =>
        genModule(key, map.get(key))
    }
    modules.toSeq
  }

  def genModule(name: String, moduleValue: Option[JsValue]): module = {
    moduleValue match {
      case Some(m: JsValue) =>
        val moduleMap = m.convertTo[Map[String, JsValue]]
        val id = moduleMap.get("id") match {
          case Some(jsObj: JsValue) => jsObj.convertTo[Double].toInt
          case None => throw JsonParseException()
        }
        val includeInTypes = genTopTypes(moduleMap.getOrElse("include_in", JsObject()))
        val optArgs = if (moduleMap.contains("optargs")) {
          genOptArgs(moduleMap.getOrElse("optargs", JsObject()))
        }
        else {
          List[ArgOrOpt]()
        }
        val signatures = moduleMap.getOrElse("signatures", JsObject())
        val functions = if (moduleMap.contains("signatures")) {
          genFunctions(signatures, optArgs, includeInTypes)
        }
        else {
          List[fun]()
        }
        module(name.toLowerCase, id)(nameToType(name))(functions:_*)
      case None => throw JsonParseException()
    }
  }

  def genTopTypes(includeIn: JsValue): List[Top] = {
    includeIn.convertTo[List[String]] collect {
      case topTypeStr: String => strToTopType(topTypeStr)
    }
  }

  def genOptArgs(optArgs: JsValue): List[ArgOrOpt] = {
    val map = optArgs.convertTo[Map[String, JsValue]]
    val result = map.keys collect {
      case name =>
        // todo: handle complex optargs
        val argType = try {
          map(name).convertTo[String]
        }
        catch {
          case e: Exception =>
            println("Complex optArg type: " + map(name))
            "T_EXPR"
        }
        opt(name, strToTopType(argType))
    }
    result.toList
  }

  def genFunctions(signaturesJson: JsValue,
                   optArgs: List[ArgOrOpt],
                   includeInTypes: List[Top]): List[fun] = {
    val signatures = signaturesJson.convertTo[List[List[JsValue]]]

    signatures collect {
      case emptySignature if emptySignature.isEmpty => fun()
      case signature =>
        @tailrec
        def genArgs(acc: List[ArgOrOpt], argNumber: Int, tl: List[JsValue]): List[ArgOrOpt] = tl match {
          case Nil => acc
          case (argTypeJs: JsString) :: tale =>
            val argType = argTypeJs.convertTo[String]
            val argument = argType match {
              case "*" =>
                multiarg("x", strToTopType("*"))
              case _ =>
                arg("arg" + argNumber, strToTopType(argType))
            }

            genArgs(argument.asInstanceOf[ArgOrOpt] :: acc, argNumber + 1, tale)
          case _ :: tale =>
            genArgs(arg("func" + argNumber, Top.AnyType) :: acc, argNumber + 1, tale)
        }

        val args = genArgs(Nil, 0, signature).reverse
        val argsOrOpts = (args.toSeq ++ optArgs).asInstanceOf[Seq[ArgOrOpt]]

        if (includeInTypes.contains(argsOrOpts.head)) {
          val tpe = argsOrOpts.head.tpe
          fun(tpe)(argsOrOpts diff List(0): _*)
        }
        else fun(argsOrOpts: _*)

    }
  }

  def nameToType(name: String): Top = {
    name match {
      case "DB" => Top.Database
      case "TABLE" => Top.Sequence.Table
      case _ => Top.AnyType
    }
  }
  def strToTopType(str: String): Top = str match {
    case "T_EXPR" => Top.AnyType
    case "T_TABLE" => Top.Sequence.Table
    case "T_TOP_LEVEL" => Top
    case "T_NUM" => Top.Datum.Num
    case "T_BOOL" => Top.Datum.Bool
    case "T_ARRAY" => Top.Datum.Arr
    case "T_STR" => Top.Datum.Str
    case "T_OBJECT" => Top.Datum.Obj
    case "T_DB" => Top.Database
    case "T_FUNC1" => Top.FunctionArg(1)
    case "T_FUNC2" => Top.FunctionArg(2)
    case "T_FUNC0" => Top.FunctionArg(0)
    // todo: implement functions type with unknown arguments count
    case "T_FUNCX" => Top.AnyType
    case "*" => Top.Datum
    case "E_RESULT_FORMAT" => Top.Datum.Str
    case "E_HTTP_METHOD" => Top.Datum.Str
    case _ =>
      println("Unknown include_in type: " + str)
      Top.AnyType
  }

  // todo: implement math operations
  def mathModules = {
     Map[String, module](
      //-------------------------------------------------------------------------
      //
      //  Math
      //
      //-------------------------------------------------------------------------
      "EQ" ->
      // EQ  = 17; // DATUM... -> BOOL
      module(termType = 17, name = "eq")(Top.Datum.Bool)(
        fun(arg("a", Top.Datum), arg("b", Top.Datum)),
        fun(Top.Datum)(arg("and", Top.Datum)),
        fun("===", Top.Datum)(arg("and", Top.Datum))
      ),
      "NE" ->
      // NE  = 18; // DATUM... -> BOOL
      module(termType = 18, name = "ne")(Top.Datum.Bool)(
        fun(arg("a", Top.Datum), arg("b", Top.Datum)),
        fun(Top.Datum)(arg("and", Top.Datum)),
        fun("!===", Top.Datum)(arg("and", Top.Datum))
      ),

     "LT" ->
      //LT  = 19; // DATUM... -> BOOL
      module(termType = 19, name = "lt")(Top.Datum.Bool)(
        fun(arg("a", Top.Datum), arg("b", Top.Datum)),
        fun(Top.Datum)(arg("thn", Top.Datum)),
        fun("<", Top.Datum)(arg("thn", Top.Datum))
      ),

     "GT" ->
      //GT  = 21; // DATUM... -> BOOL
      module(termType = 21, name = "gt")(Top.Datum.Bool)(
        fun(arg("a", Top.Datum), arg("b", Top.Datum)),
        fun(Top.Datum)(arg("thn", Top.Datum)),
        fun(">", Top.Datum)(arg("thn", Top.Datum))
      ),

     "BRANCH" ->
      // Executes its first argument, and returns its second argument if it
      // got [true] or its third argument if it got [false] (like an `if`
      // statement).
      //BRANCH  = 65; // BOOL, Top, Top -> Top
      module(termType = 65, name = "branch")(Top.AnyType)(
        fun(arg("condition", Top.Datum.Bool), arg("thn", Top), arg("els", Top))
      ),

     "OR" ->
      //OR = 66; // BOOL... -> BOOL
      module(termType = 66, name = "or")(Top.Datum.Bool)(
        fun(arg("a", Top.Datum.Bool), arg("b", Top.Datum.Bool)),
        fun(Top.Datum.Bool)(arg("b", Top.Datum.Bool)),
        fun("||", Top.Datum)(arg("b", Top.Datum))
      ),

     "AND" ->
      //  AND     = 67; // BOOL... -> BOOL
      module(termType = 67, name = "and")(Top.Datum.Bool)(
        fun(arg("a", Top.Datum.Bool), arg("b", Top.Datum.Bool)),
        fun(Top.Datum.Bool)(arg("b", Top.Datum.Bool)),
        fun("&&", Top.Datum)(arg("b", Top.Datum))
      ),

     "ADD" ->
      // SUB = 25; // NUMBER... -> NUMBER
      module(termType = 24, name = "add")(Top.Datum.Num)(
        fun(multiarg("values", Top.Datum.Num)),
        fun(Top.Datum.Num)(arg("value", Top.Datum.Num)),
        fun("+", Top.Datum.Num)(arg("value", Top.Datum.Num))
      ),

     "SUB" ->
      // SUB = 25; // NUMBER... -> NUMBER
      module(termType = 25, name = "sub")(Top.Datum.Num)(
        fun(multiarg("values", Top.Datum.Num)),
        fun(Top.Datum.Num)(arg("value", Top.Datum.Num)),
        fun("-", Top.Datum.Num)(arg("value", Top.Datum.Num))
      )
    )
  }
}

case class JsonParseException(message: String = "error in json file") extends RuntimeException