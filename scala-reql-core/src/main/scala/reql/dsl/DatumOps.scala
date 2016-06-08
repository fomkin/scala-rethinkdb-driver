
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object DatumOps {
















}

final class DatumOps(val self: Datum) extends AnyVal {
  
  /**

   */
         
  def eq(and: Datum): Bool = new Bool {
    lazy val json = s"""[17,[${extractJson(self)}, ${extractJson(and)}]]"""
    override def toString: String = self.toString + "." + s"eq(and = ${and.toString})"

  }
       
  
  /**

   */
         
  def ===(and: Datum): Bool = new Bool {
    lazy val json = s"""[17,[${extractJson(self)}, ${extractJson(and)}]]"""
    override def toString: String = self.toString + "." + s"===(and = ${and.toString})"

  }
       
  
  /**

   */
         
  def ne(and: Datum): Bool = new Bool {
    lazy val json = s"""[18,[${extractJson(self)}, ${extractJson(and)}]]"""
    override def toString: String = self.toString + "." + s"ne(and = ${and.toString})"

  }
       
  
  /**

   */
         
  def !===(and: Datum): Bool = new Bool {
    lazy val json = s"""[18,[${extractJson(self)}, ${extractJson(and)}]]"""
    override def toString: String = self.toString + "." + s"!===(and = ${and.toString})"

  }
       
  
  /**

   */
         
  def lt(thn: Datum): Bool = new Bool {
    lazy val json = s"""[19,[${extractJson(self)}, ${extractJson(thn)}]]"""
    override def toString: String = self.toString + "." + s"lt(thn = ${thn.toString})"

  }
       
  
  /**

   */
         
  def <(thn: Datum): Bool = new Bool {
    lazy val json = s"""[19,[${extractJson(self)}, ${extractJson(thn)}]]"""
    override def toString: String = self.toString + "." + s"<(thn = ${thn.toString})"

  }
       
  
  /**

   */
         
  def gt(thn: Datum): Bool = new Bool {
    lazy val json = s"""[21,[${extractJson(self)}, ${extractJson(thn)}]]"""
    override def toString: String = self.toString + "." + s"gt(thn = ${thn.toString})"

  }
       
  
  /**

   */
         
  def >(thn: Datum): Bool = new Bool {
    lazy val json = s"""[21,[${extractJson(self)}, ${extractJson(thn)}]]"""
    override def toString: String = self.toString + "." + s">(thn = ${thn.toString})"

  }
       
  
  /**

   */
         
  def ||(b: Datum): Bool = new Bool {
    lazy val json = s"""[66,[${extractJson(self)}, ${extractJson(b)}]]"""
    override def toString: String = self.toString + "." + s"||(b = ${b.toString})"

  }
       
  
  /**

   */
         
  def &&(b: Datum): Bool = new Bool {
    lazy val json = s"""[67,[${extractJson(self)}, ${extractJson(b)}]]"""
    override def toString: String = self.toString + "." + s"&&(b = ${b.toString})"

  }
       
  
  /**
   * Get a subset of an object by selecting some attributes to discard, or
   * map that over a sequence.  (Both unpick and without, polymorphic.)
   *       
   */
         
  def without(objects: Str*): AnyType = new AnyType {
    lazy val json = s"""[34,[${extractJson(self)}, ${objects.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"without(objects = [${objects.mkString(",")}])"

  }
       
  
  /**

   */
         
  def merge(objects: Datum*): AnyType = new AnyType {
    lazy val json = s"""[35,[${extractJson(self)}, ${objects.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"merge(objects = [${objects.mkString(",")}])"

  }
       
  
  /**

   */
         
  def merge(f: (Var) => Function): AnyType = new AnyType {
    lazy val json = s"""[35,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"merge(f = ${f.toString})"

  }
       
  
  /**

   */
         
  def apply(field: Str): AnyType = new AnyType {
    lazy val json = s"""[170,[${extractJson(self)}, ${extractJson(field)}]]"""
    override def toString: String = self.toString + "." + s"apply(field = ${field.toString})"

  }
       
  
  /**

   */
         
  def apply(i: Num): AnyType = new AnyType {
    lazy val json = s"""[170,[${extractJson(self)}, ${extractJson(i)}]]"""
    override def toString: String = self.toString + "." + s"apply(i = ${i.toString})"

  }
       
  
  /**

   */
         
  def default(value: Top): AnyType = new AnyType {
    lazy val json = s"""[92,[${extractJson(self)}, ${extractJson(value)}]]"""
    override def toString: String = self.toString + "." + s"default(value = ${value.toString})"

  }
       
}
         