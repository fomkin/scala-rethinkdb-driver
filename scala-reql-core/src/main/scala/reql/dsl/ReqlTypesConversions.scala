package reql.dsl

import scala.language.implicitConversions

trait ReqlTypesConversions {

  implicit def toStr(value: String): types.Str = new types.Str {
    override def toString = value
    val json = s""""$value""""
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
