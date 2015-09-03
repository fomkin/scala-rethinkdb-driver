package reql.dsl

trait Cursor[+Data] extends {

  def next[U](f: Either[ReqlQueryException, Data] ⇒ U): Unit

  def foreach[U](f: Either[ReqlQueryException, Data] ⇒ U): Unit

  def force[U](f: Either[ReqlQueryException, Seq[Data]] ⇒ U): Unit

  def close(): Unit
}
