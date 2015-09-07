package reql.dsl

import reql.dsl.types.{Arr, Obj, Datum}

import scala.language.{dynamics, implicitConversions}

trait ReqlEntryPoint extends ReqlTermOpsConversions with ReqlTypesConversions {

  /**
   * The top-level ReQL namespace.
   */
  val rethinkdb = new ReqlTopLevelApi()

  /**
   * The top-level ReQL namespace. Alias to [[rethinkdb]]
   */
  val r = rethinkdb
    
  val Null = new Datum {
    val json = "null"
  }  
  
  /**
   * Use it to create RethinkDB objects.
   *
   * val message = document(
   *   author = document(
   *     firstName = "John"
   *     lastName = "Doe"
   *   ),
   *   timestamp = r.now(),
   *   text = "Hello world"
   * )
   */
  val document = new Dynamic {
    
    val empty = new Obj {
      val json = s"[3, [], {}]"
    }
    
    def applyDynamicNamed(method: String)(args: (String, ReqlArg)*): Obj = new Obj {
      val json = {
        val optArgs = {
          val xs = args map {
            case (k, v) ⇒ s""""$k": ${v.json}"""
          }
          xs.mkString(",")
        }
        s"[3, [], {$optArgs}]"
      }
    }
  }

  /**
   * Use it to create Datum.
   *
   * val zoo = document(
   *   manager = "John Doe", 
   *   animals = list("Cat", "Dog", "Duck")   
   * )
   */
  val list = new Dynamic {
    def applyDynamic(method: String)(args: ReqlArg*): Arr = new Arr {
      val json = s"[2, [${args.map(_.json).mkString(",")}]]"
    }
  }
}
