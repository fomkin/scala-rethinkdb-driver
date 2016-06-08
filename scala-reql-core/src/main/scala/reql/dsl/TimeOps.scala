
package reql.dsl

import reql.dsl.types._

// Generated code. Do not modify
object TimeOps {














}

final class TimeOps(val self: Time) extends AnyVal {
  
  /**
   * Returns seconds since epoch in UTC given a time.
   */
         
  def toEpochTime: Num = new Num {
    lazy val json = s"""[102,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"toEpochTime()"

  }
       
  
  /**
   * 
   * Puts a time into an ISO 8601 timezone.
   *       
   */
         
  def inTimezone(timezone: Str): Datum = new Datum {
    lazy val json = s"""[104,[${extractJson(self)}, ${extractJson(timezone)}]]"""
    override def toString: String = self.toString + "." + s"inTimezone(timezone = ${timezone.toString})"

  }
       
  
  /**
   * 
   * a.during(b, c) returns whether a is in the range [b, c]
   *       
   */
         
  def during(left: Time, right: Time): Bool = new Bool {
    lazy val json = s"""[105,[${extractJson(self)}, ${extractJson(left)}, ${extractJson(right)}]]"""
    override def toString: String = self.toString + "." + s"during(left = ${left.toString}, right = ${right.toString})"

  }
       
  
  /**
   * 
   * Retrieves the date portion of a time.
   *       
   */
         
  def date: Time = new Time {
    lazy val json = s"""[106,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"date()"

  }
       
  
  /**
   * 
   * x.time_of_day == x.date - x
   *       
   */
         
  def timeOfDay: Num = new Num {
    lazy val json = s"""[126,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"timeOfDay()"

  }
       
  
  /**
   * 
   * Returns the timezone of a time.
   *       
   */
         
  def timezone: Str = new Str {
    lazy val json = s"""[127,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"timezone()"

  }
       
  
  /**

   */
         
  def year: Num = new Num {
    lazy val json = s"""[128,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"year()"

  }
       
  
  /**

   */
         
  def month: Num = new Num {
    lazy val json = s"""[129,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"month()"

  }
       
  
  /**

   */
         
  def day: Num = new Num {
    lazy val json = s"""[130,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"day()"

  }
       
  
  /**

   */
         
  def dayOfWeek: Num = new Num {
    lazy val json = s"""[131,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"dayOfWeek()"

  }
       
  
  /**

   */
         
  def dayOfYear: Num = new Num {
    lazy val json = s"""[132,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"dayOfYear()"

  }
       
  
  /**

   */
         
  def hours: Num = new Num {
    lazy val json = s"""[133,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"hours()"

  }
       
  
  /**

   */
         
  def minutes: Num = new Num {
    lazy val json = s"""[134,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"minutes()"

  }
       
  
  /**

   */
         
  def seconds: Num = new Num {
    lazy val json = s"""[135,[${extractJson(self)}]]"""
    override def toString: String = self.toString + "." + s"seconds()"

  }
       
}
         