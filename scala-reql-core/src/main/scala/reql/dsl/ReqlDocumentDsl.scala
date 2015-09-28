package reql.dsl

import reql.dsl.types.Obj
import scala.language.dynamics

final class ReqlDocumentDsl extends Dynamic {

  val empty = new Obj {
    val json = s"[3, [], {}]"
    override def toString = "{}" 
  }

  
  def fromSeq(args: Seq[(String, ReqlArg)]): Obj = new Obj {
    override def toString = {
      val s = args.map { case (k, v) ⇒ s"$k: $v" }
      s"{${s.mkString(", ")}}"
    }
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

  def fromMap(m: Map[String, ReqlArg]): Obj = fromSeq(m.toSeq)

  def applyDynamicNamed(method: String)(args: (String, ReqlArg)*): Obj = {
    fromSeq(args)
  }

}
