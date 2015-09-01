package reql

package object dsl extends OpsImplicits with TypesImplicits {
  val r = new BaseOps()
  val json = new DatumDsl()
}
