
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object StreamSelectionOps {

  trait Update0 extends Obj {
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }


  trait Delete0 extends Obj {
    def optargs(durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }

}

final class StreamSelectionOps(val self: StreamSelection) extends AnyVal {
import StreamSelectionOps._
  
  /**
   * Updates all the rows in a selection.  Calls its Function with the row
   * to be updated, and then merges the result of that call.
   *       
   */
         
  def update(obj: Obj): Update0 = new Update0 {
    lazy val json = s"""[53,[${extractJson(self)}, ${extractJson(obj)}]]"""
    override def toString: String = self.toString + "." + s"update(obj = ${obj.toString})"
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[53,[${extractJson(self)}, ${extractJson(obj)}],{${val opts = Map( "non_atomic" -> nonAtomic, "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"update(obj = ${obj.toString}, nonAtomic = ${nonAtomic.toString}, durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
  
  /**

   */
         
  def delete: Delete0 = new Delete0 {
    lazy val json = s"""[54,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"delete()"
    def optargs(durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[54,[${extractJson(self)}],{${val opts = Map( "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"delete(durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
}
         