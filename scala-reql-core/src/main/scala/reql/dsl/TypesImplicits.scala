package reql.dsl

import scala.language.implicitConversions

trait TypesImplicits {

  implicit def toStr(value: String): types.Str = new types.Str {
    val json = s""""$value""""
  }

  implicit def toNum(value: Int): types.Num = new types.Num {
    val json = value.toString
  }

  implicit def toNum(value: Float): types.Num = new types.Num {
    val json = value.toString
  }

  implicit def toNum(value: Double): types.Num = new types.Num {
    val json = value.toString
  }

  implicit def toBool(value: Boolean): types.Bool = new types.Bool {
    val json = value.toString
  }
}
