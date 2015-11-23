
import scala.collection.mutable.ArrayBuffer
import scala.util.parsing.json.JSON
import scala.io.Source
object ApiDefinitionsGenerator {

  def modules(): Seq[module] = {
    //todo: read term_info.json from java driver
    val jsonFilename = "term_info.json"
    var modules = ArrayBuffer[module]()

    var jsonString = ""
    for (line <- Source.fromFile(jsonFilename).getLines()) {
      jsonString += line
    }

    val json = JSON.parseFull(jsonString)

    json match {
      case Some(map: Map[String, Any]) =>
        // todo: implement math operations
        val math = Array[String]("EQ", "NE", "LT", "GT", "BRANCH", "OR", "AND", "ADD", "SUB", "MATCH", "OBJECT")
        for(key <- map.keys) {
          if (!math.contains(key)) {
            modules += genModule(map.get(key), key)
          }
        }
      case _ => println("ERROR in json parse")
    }

    modules.append(ApiDefinitions.modules:_*)
    println(modules.length)
    modules
  }

  def genModule(moduleAny: Any, name: String): module = {
    moduleAny match {
      case Some(dictMap: Map[String, Any]) =>
        val id = dictMap.get("id") match {
          case Some(i: Double) => i.toInt
          case _ => println("error")
            0
        }

        val includeInTypes = genTopTypes(dictMap.get("include_in"))

        val optArgs = genOptArgs(dictMap.get("optargs"))

        val functions = genFunctions(dictMap.get("signatures"), optArgs, includeInTypes)

        module(name.toLowerCase, id)(nameToType(name))(functions:_*)
      case _ => throw new JsonParseException
    }
  }
  def genTopTypes(includeIn: Any): ArrayBuffer[Top] = {
    includeIn match {
      case Some(tops: List[String]) =>
        var result = ArrayBuffer[Top]()
        tops.foreach(t => result += strToTopType(t))
        result
      case _ => throw new JsonParseException
    }
  }

  def genOptArgs(optArgs: Any): ArrayBuffer[ArgOrOpt] = {
    optArgs match {
      case Some(map: Map[String, Any]) =>
        var result = ArrayBuffer[ArgOrOpt]()

        // todo: handle complex optargs
        for ((name, argType) <- map) {
          argType match {
            case s: String =>
              result += opt(name, strToTopType(s))
            case _ =>
              println("Complex optarg: " + argType)
          }
        }
        result
      case None => ArrayBuffer[ArgOrOpt]()
      case _ => throw new JsonParseException
    }
  }

  def genFunctions(signaturesAny: Any,
                   optArgs: ArrayBuffer[ArgOrOpt],
                   includeInTypes: ArrayBuffer[Top]): ArrayBuffer[fun] = {

    signaturesAny match {
      case Some(signatures: List[List[Any]]) =>
        var functions = ArrayBuffer[fun]()
        for (signature <- signatures) {
          //todo: implement signatures with function type
          if (!signature.contains("T_FUNC0") && !signature.contains("T_FUNC1") && !signature.contains("T_FUNC2") && !signature.contains("T_FUNCX")) {
            if (signature.isEmpty) {
              functions += fun()
            }
            else {
              var args = ArrayBuffer[ArgOrOpt]()

              var argCount = 0
              for (argType <- signature)
                argType match {
                  case "*" =>
                    args += multiarg("x", strToTopType("*"))
                  case argType: String =>
                    args += arg("arg" + argCount, strToTopType(argType))
                    argCount += 1
                  case _ => println("NON string arg:" + argType)
                }

              args.append(optArgs: _*)

              if (includeInTypes.contains(args.head.tpe)) {
                val tpe = args.head.tpe
                args.remove(0)
                functions += fun(tpe)(args:_*)
              }
              else {
                functions += fun(args:_*)
              }
            }
          }
        }
        functions
      case None => ArrayBuffer[fun]()
      case _ => throw new JsonParseException
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
    case "T_FUNCX" =>
      // todo: implement functions type with unknown arguments count
      Top.FunctionArg(0)
    case "*" => Top.Datum
    case "E_RESULT_FORMAT" => Top.Datum.Str
    case "E_HTTP_METHOD" => Top.Datum.Str


    case _ =>
      println("Unknown include_in type: " + str)
      Top.AnyType
  }
}

case class JsonParseException(message: String = "error in json file") extends RuntimeException