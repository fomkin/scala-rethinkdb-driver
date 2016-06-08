
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object ArrOps {




}

final class ArrOps(val self: Arr) extends AnyVal {
  
  /**

   */
         
  def append(x: Datum): Arr = new Arr {
    lazy val json = s"""[29,[${extractJson(self)}, ${extractJson(x)}]]"""
    override def toString: String = self.toString + "." + s"append(x = ${x.toString})"

  }
       
  
  /**

   */
         
  def prepend(x: Datum): Arr = new Arr {
    lazy val json = s"""[80,[${extractJson(self)}, ${extractJson(x)}]]"""
    override def toString: String = self.toString + "." + s"prepend(x = ${x.toString})"

  }
       
  
  /**

   */
         
  def setUnion(x: Arr): Arr = new Arr {
    lazy val json = s"""[90,[${extractJson(self)}, ${extractJson(x)}]]"""
    override def toString: String = self.toString + "." + s"setUnion(x = ${x.toString})"

  }
       
  
  /**

   */
         
  def setDifference(x: Arr): Arr = new Arr {
    lazy val json = s"""[91,[${extractJson(self)}, ${extractJson(x)}]]"""
    override def toString: String = self.toString + "." + s"setDifference(x = ${x.toString})"

  }
       
}
         