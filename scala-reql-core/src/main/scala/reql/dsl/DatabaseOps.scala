
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object DatabaseOps {

  trait Table0 extends Table {
    def optargs(readMode: Str = EmptyOption, identifierFormat: Str = EmptyOption): Table
  }


  trait TableCreate0 extends Obj {
    def optargs(primaryKey: Str = EmptyOption, shards: Num = EmptyOption, replicas: Datum = EmptyOption, primaryReplicaTag: Str = EmptyOption): Obj
  }




}

final class DatabaseOps(val self: Database) extends AnyVal {
import DatabaseOps._
  
  /**
   * 
   * Select all documents in a table. This command can be chained with other commands to do further processing on the data.
   * Example: Return all documents in the table 'marvel' of the default database.
   * {{{
   * r.table('marvel').run(conn, callback)
   * }}}
   *       
   */
         
  def table(name: Str): Table0 = new Table0 {
    lazy val json = s"""[15,[${extractJson(self)}, ${extractJson(name)}]]"""
    override def toString: String = self.toString + "." + s"table(name = ${name.toString})"
    def optargs(readMode: Str = EmptyOption, identifierFormat: Str = EmptyOption): Table = new Table {
      val json = s"""[15,[${extractJson(self)}, ${extractJson(name)}],{${val opts = Map( "read_mode" -> readMode, "identifier_format" -> identifierFormat); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"table(name = ${name.toString}, readMode = ${readMode.toString}, identifierFormat = ${identifierFormat.toString})"
    }
         
  }
       
  
  /**

   */
         
  def tableCreate(name: Str): TableCreate0 = new TableCreate0 {
    lazy val json = s"""[60,[${extractJson(self)}, ${extractJson(name)}]]"""
    override def toString: String = self.toString + "." + s"tableCreate(name = ${name.toString})"
    def optargs(primaryKey: Str = EmptyOption, shards: Num = EmptyOption, replicas: Datum = EmptyOption, primaryReplicaTag: Str = EmptyOption): Obj = new Obj {
      val json = s"""[60,[${extractJson(self)}, ${extractJson(name)}],{${val opts = Map( "primary_key" -> primaryKey, "shards" -> shards, "replicas" -> replicas, "primary_replica_tag" -> primaryReplicaTag); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"tableCreate(name = ${name.toString}, primaryKey = ${primaryKey.toString}, shards = ${shards.toString}, replicas = ${replicas.toString}, primaryReplicaTag = ${primaryReplicaTag.toString})"
    }
         
  }
       
  
  /**

   */
         
  def tableDrop(name: Str): Obj = new Obj {
    lazy val json = s"""[61,[${extractJson(self)}, ${extractJson(name)}]]"""
    override def toString: String = self.toString + "." + s"tableDrop(name = ${name.toString})"

  }
       
  
  /**

   */
         
  def waitToBeReady: Obj = new Obj {
    lazy val json = s"""[177,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"waitToBeReady()"

  }
       
  
  /**

   */
         
  def tableList: Arr = new Arr {
    lazy val json = s"""[62,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"tableList()"

  }
       
}
         