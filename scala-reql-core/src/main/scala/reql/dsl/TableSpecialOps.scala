package reql.dsl

import types._

// GET_ALL = 78; // Table, DATUM..., {index:!STRING} => ARRAY
final class TableSpecialOps(val self: Table) extends AnyVal {

  private def getArgs(keys: Seq[Datum]): String = {
    (self.json :: keys.map(extractJson).toList).mkString(",")
  }
  
  def getAll(index: Str)(keys: Datum*): Arr = new Arr {
    override def toString = self.toString + ".getAll(index = " + index + ", " + keys.mkString(", ") + ")"
    val json = {
      val args = getArgs(keys)
      s"""[78,[$args],{"index": ${extractJson(index)}}]"""
    }
  }

  def getAll(keys: Datum*): Arr = new Arr {
    override def toString = self.toString + ".getAll(" + keys.mkString(", ") + ")"
    val json = {
      val args = getArgs(keys)
      s"[78,[$args]]"
    }
  }

}
