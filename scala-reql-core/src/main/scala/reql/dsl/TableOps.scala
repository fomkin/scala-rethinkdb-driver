
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object TableOps {


  trait IndexCreate0 extends Obj {
    def optargs(multi: Bool = EmptyOption, geo: Bool = EmptyOption): Obj
  }


  trait IndexCreate1 extends Obj {
    def optargs(multi: Bool = EmptyOption, geo: Bool = EmptyOption): Obj
  }





  trait Insert0 extends Obj {
    def optargs(conflict: Str = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }


  trait Insert1 extends Obj {
    def optargs(conflict: Str = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }

}

final class TableOps(val self: Table) extends AnyVal {
import TableOps._
  
  /**

   */
         
  def waitToBeReady: Obj = new Obj {
    lazy val json = s"""[177,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"waitToBeReady()"

  }
       
  
  /**

   */
         
  def indexCreate(name: Str): IndexCreate0 = new IndexCreate0 {
    lazy val json = s"""[75,[${extractJson(self)}, ${extractJson(name)}]]"""
    override def toString: String = self.toString + "." + s"indexCreate(name = ${name.toString})"
    def optargs(multi: Bool = EmptyOption, geo: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[75,[${extractJson(self)}, ${extractJson(name)}],{${val opts = Map( "multi" -> multi, "geo" -> geo); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"indexCreate(name = ${name.toString}, multi = ${multi.toString}, geo = ${geo.toString})"
    }
         
  }
       
  
  /**

   */
         
  def indexCreate(name: Str, f: (Var) => Function): IndexCreate1 = new IndexCreate1 {
    lazy val json = s"""[75,[${extractJson(self)}, ${extractJson(name)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"indexCreate(name = ${name.toString}, f = ${f.toString})"
    def optargs(multi: Bool = EmptyOption, geo: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[75,[${extractJson(self)}, ${extractJson(name)}, ${extractJson(f)}],{${val opts = Map( "multi" -> multi, "geo" -> geo); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"indexCreate(name = ${name.toString}, f = ${f.toString}, multi = ${multi.toString}, geo = ${geo.toString})"
    }
         
  }
       
  
  /**

   */
         
  def indexWait(indexies: Str*): Arr = new Arr {
    lazy val json = s"""[140,[${extractJson(self)}, ${indexies.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"indexWait(indexies = [${indexies.mkString(",")}])"

  }
       
  
  /**

   */
         
  def indexList: Arr = new Arr {
    lazy val json = s"""[77,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"indexList()"

  }
       
  
  /**

   */
         
  def get(key: Datum): SingleSelection = new SingleSelection {
    lazy val json = s"""[16,[${extractJson(self)}, ${extractJson(key)}]]"""
    override def toString: String = self.toString + "." + s"get(key = ${key.toString})"

  }
       
  
  /**
   * Inserts into a table.  If `conflict` is replace, overwrites
   * entries with the same primary key.  If `conflict` is
   * update, does an update on the entry.  If `conflict` is
   * error, or is omitted, conflicts will trigger an error.
   * {{{
   * r.table("posts").insert({
   *     id: 1,
   *     title: "Lorem ipsum",
   *     content: "Dolor sit amet"
   * }).run(conn, callback)
   * }}}
   *       
   */
         
  def insert(data: Obj): Insert0 = new Insert0 {
    lazy val json = s"""[56,[${extractJson(self)}, ${extractJson(data)}]]"""
    override def toString: String = self.toString + "." + s"insert(data = ${data.toString})"
    def optargs(conflict: Str = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[56,[${extractJson(self)}, ${extractJson(data)}],{${val opts = Map( "conflict" -> conflict, "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"insert(data = ${data.toString}, conflict = ${conflict.toString}, durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
  
  /**
   * Inserts into a table.  If `conflict` is replace, overwrites
   * entries with the same primary key.  If `conflict` is
   * update, does an update on the entry.  If `conflict` is
   * error, or is omitted, conflicts will trigger an error.
   * {{{
   * r.table("posts").insert({
   *     id: 1,
   *     title: "Lorem ipsum",
   *     content: "Dolor sit amet"
   * }).run(conn, callback)
   * }}}
   *       
   */
         
  def insert(batch: Sequence): Insert1 = new Insert1 {
    lazy val json = s"""[56,[${extractJson(self)}, ${extractJson(batch)}]]"""
    override def toString: String = self.toString + "." + s"insert(batch = ${batch.toString})"
    def optargs(conflict: Str = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[56,[${extractJson(self)}, ${extractJson(batch)}],{${val opts = Map( "conflict" -> conflict, "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"insert(batch = ${batch.toString}, conflict = ${conflict.toString}, durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
}
         