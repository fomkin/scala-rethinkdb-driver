import sbt.MainLogging

import scala.collection.mutable
import scala.util.parsing.json.JSON
import scala.io.Source

object ApiDefinitionsGenerator {

  def getModules(): Seq[module] = {
    val jsonFilename = "term_info.json"
    var modules = List[module]()

    var jsonString = ""
    for (line <- Source.fromFile(jsonFilename).getLines()) {
      jsonString += line
    }

    val json = JSON.parseFull(jsonString)

    json match {
      case Some(map: Map[String, Any]) =>
        val math = Array[String]("EQ", "NE", "LT", "GT", "BRANCH", "OR", "AND", "ADD", "SUB")
        for(key <- map.keys) {
          if (!math.contains(key)) {
            modules = genModule(map.get(key), key) :: modules
          }
        }
      case _ => println("ERROR in json parse")
    }

    modules = modules ::: ApiDefinitions.modules.toList
    println(modules.length)

    var set = mutable.Set[Int]()
    for (m <- modules) {
      set.add(m.termType)
      println("moduleName" + m.name)
      println(m.funcs)
      println(m.dataTypes)
      println()
    }
    println("setCount:" + set.count(p => true))
    return modules
  }

  def genModule(moduleAny: Any, name: String): module = {
    moduleAny match {
      case Some(dictMap: Map[String, Any]) =>
        val id = dictMap.get("id") match {
          case Some(i: Double) => i.toInt
          case _ => println("error")
            0
        }

        var topTypes = genTopTypes(dictMap.get("include_in"))

        //if (topTypes.length > 1)  topTypes = List[Top](Top.AnyType)

        val optArgs = genOptArgs(dictMap.get("optargs"))

        val functions = genFunctions(dictMap.get("signatures"), optArgs, id)
        module.createModule(name.toLowerCase, id, topTypes, functions)
      case _ => throw new JsonParseException
    }
  }
  def genTopTypes(includeIn: Any): List[Top] = {
    includeIn match {
      case Some(tops: List[String]) =>
        var result = List[Top]()
        tops.foreach(t => result = strToTopType(t) :: result)
        result
      case _ => throw new JsonParseException
    }
  }

  def genOptArgs(optArgs: Any): List[ArgOrOpt] = {
    optArgs match {
      case Some(map: Map[String, Any]) =>
        var result = List[ArgOrOpt]()

        // todo: handle complex optargs
        for ((name, argType) <- map) {
          argType match {
            case Some(s: String) => result = opt(name, strToTopType(s)) :: result
            case _ => println("Complex optarg: " + argType)
          }
        }
        return result
      case None => List[ArgOrOpt]()
      case _ => throw new JsonParseException
    }

  }

  def genFunctions(signaturesAny: Any, optArgs: List[ArgOrOpt], id: Int): List[fun] = {

    signaturesAny match {
      case Some(signatures: List[List[Any]]) =>
        var functions = List[fun]()
        for (signature <- signatures) {
          if (!signature.contains("T_FUNC1") && !signature.contains("T_FUNC0") && !signature.contains("T_FUNC2")) {
            if (signature.isEmpty) {
              functions = fun() :: functions
            }
            else {
              var args = List[ArgOrOpt]()
              for (i <- 1 until signature.length)
                signature(i) match {
                  case "*" => args = multiarg("x", strToTopType("*")) :: args
                  case argType: String => args = arg("arg" + i.toString, strToTopType(argType)) :: args
                  case _ => println("NON string arg:" + signature(i))
                }
              signature.head match {
                case s: String =>
                  functions = fun.createFun(strToTopType(s), (optArgs ::: args).reverse) :: functions
                case _ => println("non string arg:" + signature(0))
              }
            }
          }
        }
        if (id == 14) {
          functions = List[fun](fun(arg("name", Top.Datum.Str)))
        }
        functions
      case None => List[fun]()
      case _ => throw new JsonParseException
    }
  }
  def strToTopType(str: String): Top = {
    str match {
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
      case "T_FUNCX" => {
        // todo: implement functions type with unknown arguments count
        Top.FunctionArg(0)
      }
      case "*" => Top.Datum
      case "E_RESULT_FORMAT" => Top.Datum.Str
      case "E_HTTP_METHOD" => Top.Datum.Str


      case _ => {
        println("Unknown include_in type: " + str)
        Top.AnyType
      }
    }
  }
}

case class JsonParseException(message: String = "error in json file") extends RuntimeException