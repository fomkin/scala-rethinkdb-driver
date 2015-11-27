sealed trait Top

object Top extends Top {

  val all = Seq(
    Top.Arr, Top.Database, Top.Datum, Top.Error, Top.Function,
    Top.Ordering, Top.Pathspec, Top.Sequence, Top.Datum.Bool,
    Top.Datum.Null, Top.Datum.Num, Top.Datum.Obj, Top.Datum.SingleSelection,
    Top.Datum.Str, Top.Sequence.Stream, Top.Sequence.StreamSelection,
    Top.Sequence.Table, Top.PseudoType.Time, Top.PseudoType.Binary
  )

  sealed trait AnyType extends Top
  object AnyType extends AnyType

  sealed trait Datum extends AnyType

  sealed trait Sequence extends Top

  case object Arr extends Datum with Sequence

  case object Database extends Top

  case class FunctionArg(argsCount: Int) extends AnyType

  case object Function extends AnyType

  case object Ordering extends Top

  case object Pathspec extends Top

  case object Error extends Top

  object Datum extends Datum {

    case object Null extends Datum

    case object Bool extends Datum

    case object Num extends Datum

    sealed trait Str extends Datum

    case object Str extends Str

    sealed trait Obj

    case object Obj extends Datum with Obj

    case object SingleSelection extends Datum with Obj

    val Arr = Top.Arr
  }

  object Sequence extends Sequence {

    val Arr = Top.Arr

    sealed trait Stream extends Sequence

    case object Stream extends Stream

    sealed trait StreamSelection extends Stream

    case object StreamSelection extends StreamSelection

    sealed trait Table extends StreamSelection

    case object Table extends Table
  }

  object PseudoType {

    case object Time extends Top

    case object Binary extends Top
  }
}

case class module(termType: Int, dataTypes: Seq[Top], name: String, funcs: Seq[fun], doc: Option[String])

object module {
  def apply(name: String, termType: Int)(dataTypes: Top*)(funcs: fun*): module = {
    module(termType, dataTypes, name, funcs, None)
  }
  def apply(name: String, termType: Int, doc: String)(dataTypes: Top*)(funcs: fun*): module = {
    module(termType, dataTypes, name, funcs, Some(doc))
  }
}

case class fun(customName: Option[String], dependency: Option[Top], args: Seq[ArgOrOpt])

object fun {

  def apply(customName: String, dependency: Top)(args: ArgOrOpt*): fun = {
    fun(Some(customName), Some(dependency), args)
  }

  def apply(customName: String)(args: ArgOrOpt*): fun = {
    fun(Some(customName), None, args)
  }

  def apply(dependency: Top)(args: ArgOrOpt*): fun = {
    fun(None, Some(dependency), args)
  }

  def apply(args: ArgOrOpt*): fun = fun(None, None, args)
}

sealed trait ArgOrOpt {

  val name: String

  val tpe: Top

  def isMulti = false
}

case class arg(name: String, tpe: Top) extends ArgOrOpt

case class multiarg(name: String, tpe: Top) extends ArgOrOpt {
  override def isMulti: Boolean = true
}

case class opt(name: String, tpe: Top) extends ArgOrOpt