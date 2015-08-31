package reql

package object dsl extends BaseOps with OpsImplicits with TypesImplicits {
  type Json = pushka.Ast
  val Json = pushka.Ast
}
