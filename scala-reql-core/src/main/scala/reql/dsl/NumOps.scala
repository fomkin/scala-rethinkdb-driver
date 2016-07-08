
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object NumOps {




}

final class NumOps(val self: Num) extends AnyVal {
import NumOps._
  
  /**

   */
         
  def add(value: Num): Num = new Num {
    lazy val json = s"""[24,[${extractJson(self)}, ${extractJson(value)}]]"""
    override def toString: String = self.toString + "." + s"add(value = ${value.toString})"

  }
       
  
  /**

   */
         
  def +(value: Num): Num = new Num {
    lazy val json = s"""[24,[${extractJson(self)}, ${extractJson(value)}]]"""
    override def toString: String = self.toString + "." + s"+(value = ${value.toString})"

  }
       
  
  /**

   */
         
  def sub(value: Num): Num = new Num {
    lazy val json = s"""[25,[${extractJson(self)}, ${extractJson(value)}]]"""
    override def toString: String = self.toString + "." + s"sub(value = ${value.toString})"

  }
       
  
  /**

   */
         
  def -(value: Num): Num = new Num {
    lazy val json = s"""[25,[${extractJson(self)}, ${extractJson(value)}]]"""
    override def toString: String = self.toString + "." + s"-(value = ${value.toString})"

  }
       
}
         