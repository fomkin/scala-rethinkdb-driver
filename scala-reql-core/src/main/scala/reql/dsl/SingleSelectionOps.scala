
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object SingleSelectionOps {

  trait Update0 extends Obj {
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }


  trait Update1 extends Obj {
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }


  trait Replace0 extends Obj {
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }


  trait Replace1 extends Obj {
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj
  }


  trait Changes0 extends Stream {
    def optargs(includeInitial: Bool = EmptyOption, includeStates: Bool = EmptyOption): Stream
  }


}

final class SingleSelectionOps(val self: SingleSelection) extends AnyVal {
import SingleSelectionOps._
  
  /**
   * Updates all the rows in a selection.  Calls its Function with the row
   * to be updated, and then merges the result of that call.
   *       
   */
         
  def update(f: (Var) => Function): Update0 = new Update0 {
    lazy val json = s"""[53,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"update(f = ${f.toString})"
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[53,[${extractJson(self)}, ${extractJson(f)}],{${val opts = Map( "non_atomic" -> nonAtomic, "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"update(f = ${f.toString}, nonAtomic = ${nonAtomic.toString}, durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
  
  /**
   * Updates all the rows in a selection.  Calls its Function with the row
   * to be updated, and then merges the result of that call.
   *       
   */
         
  def update(obj: Obj): Update1 = new Update1 {
    lazy val json = s"""[53,[${extractJson(self)}, ${extractJson(obj)}]]"""
    override def toString: String = self.toString + "." + s"update(obj = ${obj.toString})"
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[53,[${extractJson(self)}, ${extractJson(obj)}],{${val opts = Map( "non_atomic" -> nonAtomic, "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"update(obj = ${obj.toString}, nonAtomic = ${nonAtomic.toString}, durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
  
  /**
   * Replaces all the rows in a selection.  Calls its Function with the row
   * to be replaced, and then discards it and stores the result of that call.
   *       
   */
         
  def replace(f: (Var) => Function): Replace0 = new Replace0 {
    lazy val json = s"""[55,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"replace(f = ${f.toString})"
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[55,[${extractJson(self)}, ${extractJson(f)}],{${val opts = Map( "non_atomic" -> nonAtomic, "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"replace(f = ${f.toString}, nonAtomic = ${nonAtomic.toString}, durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
  
  /**
   * Replaces all the rows in a selection.  Calls its Function with the row
   * to be replaced, and then discards it and stores the result of that call.
   *       
   */
         
  def replace(obj: Obj): Replace1 = new Replace1 {
    lazy val json = s"""[55,[${extractJson(self)}, ${extractJson(obj)}]]"""
    override def toString: String = self.toString + "." + s"replace(obj = ${obj.toString})"
    def optargs(nonAtomic: Bool = EmptyOption, durability: Str = EmptyOption, returnChanges: Bool = EmptyOption): Obj = new Obj {
      val json = s"""[55,[${extractJson(self)}, ${extractJson(obj)}],{${val opts = Map( "non_atomic" -> nonAtomic, "durability" -> durability, "return_changes" -> returnChanges); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"replace(obj = ${obj.toString}, nonAtomic = ${nonAtomic.toString}, durability = ${durability.toString}, returnChanges = ${returnChanges.toString})"
    }
         
  }
       
  
  /**

   */
         
  def changes: Changes0 = new Changes0 {
    lazy val json = s"""[152,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"changes()"
    def optargs(includeInitial: Bool = EmptyOption, includeStates: Bool = EmptyOption): Stream = new Stream {
      val json = s"""[152,[${extractJson(self)}],{${val opts = Map( "include_initial" -> includeInitial, "include_states" -> includeStates); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"changes(includeInitial = ${includeInitial.toString}, includeStates = ${includeStates.toString})"
    }
         
  }
       
  
  /**

   */
         
  def delete: Obj = new Obj {
    lazy val json = s"""[54,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"delete()"

  }
       
}
         