package reql.dsl

import reql.dsl.types.{Arr, Obj, Datum}

import scala.language.{dynamics, implicitConversions}

trait ReqlEntryPoint extends ReqlTermOpsConversions with ReqlTypesConversions {

  /**
   * The top-level ReQL namespace.
   */
  val rethinkdb = new ReqlTopLevelApi()

  /**
   * The top-level ReQL namespace. Alias to [[rethinkdb]]
   */
  val r = rethinkdb
    
  val Null = new Datum {
    override def toString = "null"
    val json = "null"
  }  
  
  /**
   * Use it to create RethinkDB objects.
   *
   * val message = document(
   *   author = document(
   *     firstName = "John"
   *     lastName = "Doe"
   *   ),
   *   timestamp = r.now(),
   *   text = "Hello world"
   * )
   */
  val document = new ReqlDocumentDsl()
  
  /**
   * Use it to create Datum.
   *
   * val zoo = document(
   *   manager = "John Doe", 
   *   animals = list("Cat", "Dog", "Duck")   
   * )
   */
  val array = new ReqlArrayDsl()
  
  implicit def toTableSpecialOps(table: types.Table): TableSpecialOps = {
    new TableSpecialOps(table)
  }
}
