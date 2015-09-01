package reql.dsl

import reql.dsl.types.{Arr, Obj}

import scala.language.dynamics

class DatumDsl extends Dynamic {

  def applyDynamic(method: String)(args: ReqlArg*): Arr = new Arr {
    val json = s"[2, [${args.map(_.json).mkString(",")}]]"
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
