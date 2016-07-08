
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object SequenceOps {




  trait Filter0 extends Sequence {
    def optargs(default: Datum = EmptyOption): Sequence
  }


  trait Filter1 extends Sequence {
    def optargs(default: Datum = EmptyOption): Sequence
  }















  trait Changes0 extends Stream {
    def optargs(includeInitial: Bool = EmptyOption, includeStates: Bool = EmptyOption): Stream
  }




  trait OrderBy0 extends Sequence {
    def optargs(index: Str = EmptyOption): Sequence
  }


  trait OrderBy1 extends Sequence {
    def optargs(index: Ordering = EmptyOption): Sequence
  }




}

final class SequenceOps(val self: Sequence) extends AnyVal {
import SequenceOps._
  
  /**

   */
         
  def isEmpty: Bool = new Bool {
    lazy val json = s"""[86,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"isEmpty()"

  }
       
  
  /**

   */
         
  def pluck(cols: Str*): AnyType = new AnyType {
    lazy val json = s"""[33,[${extractJson(self)}, ${cols.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"pluck(cols = [${cols.mkString(",")}])"

  }
       
  
  /**
   * 
   * Transform each element of one or more sequences by applying a mapping function to them. If map is run with two or more sequences, it will iterate for as many items as there are in the shortest sequence.
   * Example: Return the first five squares.
   * {{{
   * r.expr([1, 2, 3, 4, 5]).map(function (val) {
   *     return val.mul(val);
   * }).run(conn, callback);
   * }}}
   * // Result passed to callback
   * [1, 4, 9, 16, 25]
   *       
   */
         
  def map(f: (Var) => Function): Sequence = new Sequence {
    lazy val json = s"""[38,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"map(f = ${f.toString})"

  }
       
  
  /**
   * Filter a sequence with either a function or a shortcut
   * object (see API docs for details).  The body of FILTER is
   * wrapped in an implicit `.default(false)`, and you can
   * change the default value by specifying the `default`
   * optarg.  If you make the default `r.error`, all errors
   * caught by `default` will be rethrown as if the `default`
   * did not exist.
   *       
   */
         
  def filter(f: (Var) => Function): Filter0 = new Filter0 {
    lazy val json = s"""[39,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"filter(f = ${f.toString})"
    def optargs(default: Datum = EmptyOption): Sequence = new Sequence {
      val json = s"""[39,[${extractJson(self)}, ${extractJson(f)}],{${val opts = Map( "default" -> default); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"filter(f = ${f.toString}, default = ${default.toString})"
    }
         
  }
       
  
  /**
   * Filter a sequence with either a function or a shortcut
   * object (see API docs for details).  The body of FILTER is
   * wrapped in an implicit `.default(false)`, and you can
   * change the default value by specifying the `default`
   * optarg.  If you make the default `r.error`, all errors
   * caught by `default` will be rethrown as if the `default`
   * did not exist.
   *       
   */
         
  def filter(x: Datum): Filter1 = new Filter1 {
    lazy val json = s"""[39,[${extractJson(self)}, ${extractJson(x)}]]"""
    override def toString: String = self.toString + "." + s"filter(x = ${x.toString})"
    def optargs(default: Datum = EmptyOption): Sequence = new Sequence {
      val json = s"""[39,[${extractJson(self)}, ${extractJson(x)}],{${val opts = Map( "default" -> default); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"filter(x = ${x.toString}, default = ${default.toString})"
    }
         
  }
       
  
  /**
   * Map a function over a sequence and then concatenate the results together.
   */
         
  def concatMap(f: (Var) => Function): Sequence = new Sequence {
    lazy val json = s"""[40,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"concatMap(f = ${f.toString})"

  }
       
  
  /**

   */
         
  def limit(count: Num): Sequence = new Sequence {
    lazy val json = s"""[71,[${extractJson(self)}, ${extractJson(count)}]]"""
    override def toString: String = self.toString + "." + s"limit(count = ${count.toString})"

  }
       
  
  /**

   */
         
  def skip(count: Num): Sequence = new Sequence {
    lazy val json = s"""[70,[${extractJson(self)}, ${extractJson(count)}]]"""
    override def toString: String = self.toString + "." + s"skip(count = ${count.toString})"

  }
       
  
  /**

   */
         
  def group(field: Str*): Function with Obj = new Function with Obj {
    lazy val json = s"""[144,[${extractJson(self)}, ${field.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"group(field = [${field.mkString(",")}])"

  }
       
  
  /**

   */
         
  def groupF(f: ((Var) => Function)*): Function with Obj = new Function with Obj {
    lazy val json = s"""[144,[${extractJson(self)}, ${f.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"groupF(f = [${f.mkString(",")}])"

  }
       
  
  /**

   */
         
  def sum(field: Str*): Function with Obj = new Function with Obj {
    lazy val json = s"""[145,[${extractJson(self)}, ${field.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"sum(field = [${field.mkString(",")}])"

  }
       
  
  /**

   */
         
  def sumF(f: ((Var) => Function)*): Function with Obj = new Function with Obj {
    lazy val json = s"""[145,[${extractJson(self)}, ${f.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"sumF(f = [${f.mkString(",")}])"

  }
       
  
  /**

   */
         
  def avg(field: Str*): Function with Obj = new Function with Obj {
    lazy val json = s"""[146,[${extractJson(self)}, ${field.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"avg(field = [${field.mkString(",")}])"

  }
       
  
  /**

   */
         
  def avgF(f: ((Var) => Function)*): Function with Obj = new Function with Obj {
    lazy val json = s"""[146,[${extractJson(self)}, ${f.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"avgF(f = [${f.mkString(",")}])"

  }
       
  
  /**

   */
         
  def min(field: Str*): Function with Obj = new Function with Obj {
    lazy val json = s"""[147,[${extractJson(self)}, ${field.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"min(field = [${field.mkString(",")}])"

  }
       
  
  /**

   */
         
  def minF(f: ((Var) => Function)*): Function with Obj = new Function with Obj {
    lazy val json = s"""[147,[${extractJson(self)}, ${f.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"minF(f = [${f.mkString(",")}])"

  }
       
  
  /**

   */
         
  def max(field: Str*): Function with Obj = new Function with Obj {
    lazy val json = s"""[148,[${extractJson(self)}, ${field.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"max(field = [${field.mkString(",")}])"

  }
       
  
  /**

   */
         
  def maxF(f: ((Var) => Function)*): Function with Obj = new Function with Obj {
    lazy val json = s"""[148,[${extractJson(self)}, ${f.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"maxF(f = [${f.mkString(",")}])"

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
         
  def contains(x: Datum*): Bool = new Bool {
    lazy val json = s"""[93,[${extractJson(self)}, ${x.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"contains(x = [${x.mkString(",")}])"

  }
       
  
  /**

   */
         
  def contains(f: (Var) => Function): Bool = new Bool {
    lazy val json = s"""[93,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"contains(f = ${f.toString})"

  }
       
  
  /**

   */
         
  def orderBy(field: Str*): OrderBy0 = new OrderBy0 {
    lazy val json = s"""[41,[${extractJson(self)}, ${field.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"orderBy(field = [${field.mkString(",")}])"
    def optargs(index: Str = EmptyOption): Sequence = new Sequence {
      val json = s"""[41,[${extractJson(self)}, ${field.map(extractJson).mkString(", ")}],{${val opts = Map( "index" -> index); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"orderBy(field = ${field.toString}, index = ${index.toString})"
    }
         
  }
       
  
  /**

   */
         
  def orderBy(ordering: Ordering*): OrderBy1 = new OrderBy1 {
    lazy val json = s"""[41,[${extractJson(self)}, ${ordering.map(extractJson).mkString(", ")}]]"""
    override def toString: String = self.toString + "." + s"orderBy(ordering = [${ordering.mkString(",")}])"
    def optargs(index: Ordering = EmptyOption): Sequence = new Sequence {
      val json = s"""[41,[${extractJson(self)}, ${ordering.map(extractJson).mkString(", ")}],{${val opts = Map( "index" -> index); opts.filter(_._2 != EmptyOption).map{ case (k,v) => "\""+k+"\":"+extractJson(v)}.mkString(",") }}]"""
      override def toString: String = self.toString + "." + s"orderBy(ordering = ${ordering.toString}, index = ${index.toString})"
    }
         
  }
       
  
  /**

   */
         
  def count: Num = new Num {
    lazy val json = s"""[43,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"count()"

  }
       
  
  /**

   */
         
  def count(value: Datum): Num = new Num {
    lazy val json = s"""[43,[${extractJson(self)}, ${extractJson(value)}]]"""
    override def toString: String = self.toString + "." + s"count(value = ${value.toString})"

  }
       
  
  /**

   */
         
  def count(f: (Var) => Function): Num = new Num {
    lazy val json = s"""[43,[${extractJson(self)}, ${extractJson(f)}]]"""
    override def toString: String = self.toString + "." + s"count(f = ${f.toString})"

  }
       
}
         