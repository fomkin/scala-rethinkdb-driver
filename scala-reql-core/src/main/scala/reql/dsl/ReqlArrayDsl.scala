package reql.dsl

import reql.dsl.types.Arr

class ReqlArrayDsl {

  val empty = new Arr {
    val json = s"[2, []]"

    override def toString = "[]"
  }

  def apply(args: ReqlArg*): Arr = new Arr {
    override def toString = "[" + args.mkString(", ") + "]"

    val json = s"[2, [${args.map(_.json).mkString(",")}]]"
  }
}
