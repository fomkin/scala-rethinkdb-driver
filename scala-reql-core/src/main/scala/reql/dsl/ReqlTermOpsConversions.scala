
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
trait ReqlTermOpsConversions {
  implicit def toArrOps(x: Arr): ArrOps = new ArrOps(x)
  implicit def toDatabaseOps(x: Database): DatabaseOps = new DatabaseOps(x)
  implicit def toDatumOps(x: Datum): DatumOps = new DatumOps(x)
  implicit def toErrorOps(x: Error): ErrorOps = new ErrorOps(x)
  implicit def toFunctionOps(x: Function): FunctionOps = new FunctionOps(x)
  implicit def toOrderingOps(x: Ordering): OrderingOps = new OrderingOps(x)
  implicit def toPathspecOps(x: Pathspec): PathspecOps = new PathspecOps(x)
  implicit def toSequenceOps(x: Sequence): SequenceOps = new SequenceOps(x)
  implicit def toBoolOps(x: Bool): BoolOps = new BoolOps(x)
  implicit def toNullOps(x: Null): NullOps = new NullOps(x)
  implicit def toNumOps(x: Num): NumOps = new NumOps(x)
  implicit def toObjOps(x: Obj): ObjOps = new ObjOps(x)
  implicit def toSingleSelectionOps(x: SingleSelection): SingleSelectionOps = new SingleSelectionOps(x)
  implicit def toStrOps(x: Str): StrOps = new StrOps(x)
  implicit def toStreamOps(x: Stream): StreamOps = new StreamOps(x)
  implicit def toStreamSelectionOps(x: StreamSelection): StreamSelectionOps = new StreamSelectionOps(x)
  implicit def toTableOps(x: Table): TableOps = new TableOps(x)
  implicit def toTimeOps(x: Time): TimeOps = new TimeOps(x)
  implicit def toBinaryOps(x: Binary): BinaryOps = new BinaryOps(x)
}
     