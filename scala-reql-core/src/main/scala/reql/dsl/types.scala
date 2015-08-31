package reql.dsl

object types {

  trait Top extends ReqlArg

  trait Datum extends Top

  trait Sequence extends Top

  trait Arr extends Datum with Sequence

  trait Database extends Top

  trait Function extends Top

  trait Ordering extends Top

  trait Pathspec extends Top

  trait Error extends Top

  trait Null extends Datum

  trait Bool extends Datum

  trait Num extends Datum

  trait Str extends Datum

  trait Field extends Str

  trait Obj extends Datum

  trait SingleSelection extends Datum with Obj

  trait Stream extends Sequence

  trait StreamSelection extends Stream

  trait Table extends StreamSelection
}
