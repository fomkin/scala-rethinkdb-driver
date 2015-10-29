package reql.dsl

import scala.annotation.switch
import scala.language.implicitConversions

trait ReqlTypesConversions {

  private def escape(sb: StringBuilder, s: String, unicode: Boolean): Unit = {
    sb.append('"')
    var i = 0
    val len = s.length
    while (i < len) {
      (s.charAt(i): @switch) match {
        case '"' => sb.append("\\\"")
        case '\\' => sb.append("\\\\")
        case '\b' => sb.append("\\b")
        case '\f' => sb.append("\\f")
        case '\n' => sb.append("\\n")
        case '\r' => sb.append("\\r")
        case '\t' => sb.append("\\t")
        case c =>
          if (c < ' ' || (c > '~' && unicode)) sb.append("\\u%04x" format c.toInt)
          else sb.append(c)
      }
      i += 1
    }
    sb.append('"')
  }

  implicit def toStr(value: String): types.Str = new types.Str {
    override def toString = value
    val json = {
      val sb = new StringBuilder()
      escape(sb, value, unicode = true)
      sb.mkString
    }
  }

  implicit def toNum(value: Int): types.Num = new types.Num {
    override def toString = value.toString
    val json = value.toString
  }

  implicit def toNum(value: Float): types.Num = new types.Num {
    override def toString = value.toString
    val json = value.toString
  }

  implicit def toNum(value: Double): types.Num = new types.Num {
    override def toString = value.toString
    val json = value.toString
  }

  implicit def toBool(value: Boolean): types.Bool = new types.Bool {
    override def toString = value.toString
    val json = value.toString
  }
}
