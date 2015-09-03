package reql.dsl

trait Cursor[+Data] extends {

  def next[U](f: Option[Data] ⇒ U): Unit

  def foreach[U](f: Option[Data] ⇒ U): Unit

  def force[U](f: Seq[Data] ⇒ U): Unit

  def close(): Unit
}
