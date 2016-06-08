
package reql.dsl

import reql.dsl.types._

object ReqlTopLevelApi {
























}

// Generated code. Do not modify|
class ReqlTopLevelApi {
  
  /**

   */
         
  def error(message: Str): Error = new Error {
    lazy val json = s"""[12,[${extractJson(message)}]]"""
    override def toString: String = s"error(message = ${message.toString})"

  }
       
  
  /**

   */
         
  def uuid: Datum = new Datum {
    lazy val json = s"""[169,[]]"""
    override def toString: String = s"uuid()"

  }
       
  
  /**

   */
         
  def implicitVar: Datum = new Datum {
    lazy val json = s"""[13,[]]"""
    override def toString: String = s"implicitVar()"

  }
       
  
  /**

   */
         
  def http(url: Str): Obj with Str = new Obj with Str {
    lazy val json = s"""[153,[${extractJson(url)}]]"""
    override def toString: String = s"http(url = ${url.toString})"

  }
       
  
  /**

   */
         
  def eq(a: Datum, b: Datum): Bool = new Bool {
    lazy val json = s"""[17,[${extractJson(a)}, ${extractJson(b)}]]"""
    override def toString: String = s"eq(a = ${a.toString}, b = ${b.toString})"

  }
       
  
  /**

   */
         
  def ne(a: Datum, b: Datum): Bool = new Bool {
    lazy val json = s"""[18,[${extractJson(a)}, ${extractJson(b)}]]"""
    override def toString: String = s"ne(a = ${a.toString}, b = ${b.toString})"

  }
       
  
  /**

   */
         
  def lt(a: Datum, b: Datum): Bool = new Bool {
    lazy val json = s"""[19,[${extractJson(a)}, ${extractJson(b)}]]"""
    override def toString: String = s"lt(a = ${a.toString}, b = ${b.toString})"

  }
       
  
  /**

   */
         
  def gt(a: Datum, b: Datum): Bool = new Bool {
    lazy val json = s"""[21,[${extractJson(a)}, ${extractJson(b)}]]"""
    override def toString: String = s"gt(a = ${a.toString}, b = ${b.toString})"

  }
       
  
  /**

   */
         
  def not(a: Bool): Bool = new Bool {
    lazy val json = s"""[23,[${extractJson(a)}]]"""
    override def toString: String = s"not(a = ${a.toString})"

  }
       
  
  /**

   */
         
  def branch(condition: Bool, thn: Top, els: Top): AnyType = new AnyType {
    lazy val json = s"""[65,[${extractJson(condition)}, ${extractJson(thn)}, ${extractJson(els)}]]"""
    override def toString: String = s"branch(condition = ${condition.toString}, thn = ${thn.toString}, els = ${els.toString})"

  }
       
  
  /**

   */
         
  def or(a: Bool, b: Bool): Bool = new Bool {
    lazy val json = s"""[66,[${extractJson(a)}, ${extractJson(b)}]]"""
    override def toString: String = s"or(a = ${a.toString}, b = ${b.toString})"

  }
       
  
  /**

   */
         
  def and(a: Bool, b: Bool): Bool = new Bool {
    lazy val json = s"""[67,[${extractJson(a)}, ${extractJson(b)}]]"""
    override def toString: String = s"and(a = ${a.toString}, b = ${b.toString})"

  }
       
  
  /**

   */
         
  def add(values: Num*): Num = new Num {
    lazy val json = s"""[24,[${values.map(extractJson).mkString(", ")}]]"""
    override def toString: String = s"add(values = [${values.mkString(",")}])"

  }
       
  
  /**

   */
         
  def sub(values: Num*): Num = new Num {
    lazy val json = s"""[25,[${values.map(extractJson).mkString(", ")}]]"""
    override def toString: String = s"sub(values = [${values.mkString(",")}])"

  }
       
  
  /**

   */
         
  def dbCreate(name: Str): Obj = new Obj {
    lazy val json = s"""[57,[${extractJson(name)}]]"""
    override def toString: String = s"dbCreate(name = ${name.toString})"

  }
       
  
  /**

   */
         
  def dbList: Arr = new Arr {
    lazy val json = s"""[59,[]]"""
    override def toString: String = s"dbList()"

  }
       
  
  /**
   * 
   * Reference a database.
   * Example: Explicitly specify a database for a query.
   * {{{
   * r.db('heroes').table('marvel').run(conn, callback)
   * }}}
   *       
   */
         
  def db(name: Str): Database = new Database {
    lazy val json = s"""[14,[${extractJson(name)}]]"""
    override def toString: String = s"db(name = ${name.toString})"

  }
       
  
  /**

   */
         
  def applyFunction(f: (Var) => Function, value: Datum): Datum = new Datum {
    lazy val json = s"""[64,[${extractJson(f)}, ${extractJson(value)}]]"""
    override def toString: String = s"applyFunction(f = ${f.toString}, value = ${value.toString})"

  }
       
  
  /**

   */
         
  def now: Time = new Time {
    lazy val json = s"""[103,[]]"""
    override def toString: String = s"now()"

  }
       
  
  /**
   * 
   * time(year, month, day[, hour, minute, second], timezone)
   * 
   * Create a time object for a specific time.
   * 
   * A few restrictions exist on the arguments:
   * 
   *   * year is an integer between 1400 and 9,999.
   *   * month is an integer between 1 and 12.
   *   * day is an integer between 1 and 31.
   *   * hour is an integer.
   *   * minutes is an integer.
   *   * seconds is a double. Its value will be rounded to three decimal places (millisecond-precision).
   *   * timezone can be 'Z' (for UTC) or a string with the format ±[hh]:[mm].
   * 
   * Example:
   * 
   * Update the birthdate of the user "John" to November 3rd, 1986 UTC.
   * {{{
   * r.table("user").get("John").update({birthdate: r.time(1986, 11, 3, 'Z')})
   *     .run(conn, callback)
   * }}}
   *       
   */
         
  def time(year: Num, month: Num, day: Num, timezone: Str): Time = new Time {
    lazy val json = s"""[136,[${extractJson(year)}, ${extractJson(month)}, ${extractJson(day)}, ${extractJson(timezone)}]]"""
    override def toString: String = s"time(year = ${year.toString}, month = ${month.toString}, day = ${day.toString}, timezone = ${timezone.toString})"

  }
       
  
  /**
   * 
   * time(year, month, day[, hour, minute, second], timezone)
   * 
   * Create a time object for a specific time.
   * 
   * A few restrictions exist on the arguments:
   * 
   *   * year is an integer between 1400 and 9,999.
   *   * month is an integer between 1 and 12.
   *   * day is an integer between 1 and 31.
   *   * hour is an integer.
   *   * minutes is an integer.
   *   * seconds is a double. Its value will be rounded to three decimal places (millisecond-precision).
   *   * timezone can be 'Z' (for UTC) or a string with the format ±[hh]:[mm].
   * 
   * Example:
   * 
   * Update the birthdate of the user "John" to November 3rd, 1986 UTC.
   * {{{
   * r.table("user").get("John").update({birthdate: r.time(1986, 11, 3, 'Z')})
   *     .run(conn, callback)
   * }}}
   *       
   */
         
  def time(year: Num, month: Num, day: Num, hour: Num, minutes: Num, seconds: Num, timezone: Str): Time = new Time {
    lazy val json = s"""[136,[${extractJson(year)}, ${extractJson(month)}, ${extractJson(day)}, ${extractJson(hour)}, ${extractJson(minutes)}, ${extractJson(seconds)}, ${extractJson(timezone)}]]"""
    override def toString: String = s"time(year = ${year.toString}, month = ${month.toString}, day = ${day.toString}, hour = ${hour.toString}, minutes = ${minutes.toString}, seconds = ${seconds.toString}, timezone = ${timezone.toString})"

  }
       
  
  /**

   */
         
  def asc(field: Str): Ordering = new Ordering {
    lazy val json = s"""[73,[${extractJson(field)}]]"""
    override def toString: String = s"asc(field = ${field.toString})"

  }
       
  
  /**

   */
         
  def desc(field: Str): Ordering = new Ordering {
    lazy val json = s"""[74,[${extractJson(field)}]]"""
    override def toString: String = s"desc(field = ${field.toString})"

  }
       
  
  /**

   */
         
  def args(value: Datum): Datum = new Datum {
    lazy val json = s"""[154,[${extractJson(value)}]]"""
    override def toString: String = s"args(value = ${value.toString})"

  }
       
}
     