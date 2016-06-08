
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object BoolOps {


}

final class BoolOps(val self: Bool) extends AnyVal {
  
  /**

   */
         
  def or(b: Bool): Bool = new Bool {
    lazy val json = s"""[66,[${extractJson(self)}, ${extractJson(b)}]]"""
    override def toString: String = self.toString + "." + s"or(b = ${b.toString})"

  }
       
  
  /**

   */
         
  def and(b: Bool): Bool = new Bool {
    lazy val json = s"""[67,[${extractJson(self)}, ${extractJson(b)}]]"""
    override def toString: String = self.toString + "." + s"and(b = ${b.toString})"

  }
       
}
         