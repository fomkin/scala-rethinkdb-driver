package reql

import scala.language.implicitConversions

package object dsl extends ReqlTermOpsConversions with ReqlTypesConversions {

  /**
   * The top-level ReQL namespace.
   */
  val rethinkdb = new ReqlTopLevelApi()

  /**
   * The top-level ReQL namespace. Alias to [[rethinkdb]]
   */
  val r = rethinkdb

  /**
   * Use it to create Datum.
   *
   * val message = datum(
   *   author = datum(
   *     firstName = "John"
   *     lastName = "Doe"
   *   ),
   *   timestamp = r.now(),
   *   text = "Hello world"
   * )
   */
  val datum = new DatumDsl()
}
