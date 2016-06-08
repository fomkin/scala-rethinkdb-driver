
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object ObjOps {



}

final class ObjOps(val self: Obj) extends AnyVal {
  
  /**

   */
         
  def getField(fieldName: Str): AnyType = new AnyType {
    lazy val json = s"""[31,[${extractJson(self)}, ${extractJson(fieldName)}]]"""
    override def toString: String = self.toString + "." + s"getField(fieldName = ${fieldName.toString})"

  }
       
  
  /**

   */
         
  def hasFields(fields: Str*): Bool = new Bool {
    lazy val json = s"""[32,[${extractJson(self)}, ${fields.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"hasFields(fields = [${fields.mkString(",")}])"

  }
       
  
  /**

   */
         
  def pluck(cols: Str*): AnyType = new AnyType {
    lazy val json = s"""[33,[${extractJson(self)}, ${cols.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"pluck(cols = [${cols.mkString(",")}])"

  }
       
}
         