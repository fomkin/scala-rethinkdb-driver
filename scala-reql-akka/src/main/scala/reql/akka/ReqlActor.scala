package reql.akka

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import reql.dsl.{Cursor, ReqlArg, ReqlContext}

import scala.annotation.tailrec
import scala.collection.mutable

trait ReqlActor[Data] extends Actor with ReqlContext[Data] {

  import ReqlActor._
  import ReqlContext._
  import ReqlTcpConnection._

  /**
   * Time to wait for response from RethinkDB  
   * @return
   */
  def queryTimeout: Timeout

  /**
   * The actor implements ReqlTcpConnection protocol. It can be
   * ReqlTcp connection itself or router for connection pool.
   * @return
   */
  def dbConnection: ActorRef

  //---------------------------------------------------------------------------
  //
  //  ReqlContext implementation
  //
  //---------------------------------------------------------------------------

  def runCursorQuery[U](query: ReqlArg, f: CursorCb[Data]): Unit = {
    import context.dispatcher
    val message = StartQuery(query, Some(self))
    dbConnection.ask(message)(queryTimeout).mapTo[Long] foreach { token ⇒
      self ! RegisterCursorCb(token, f)
    }
  }

  def runAtomQuery[U](query: ReqlArg, f: AtomCb[Data]): Unit = {
    import context.dispatcher
    val message = StartQuery(query, Some(self))
    dbConnection.ask(message)(queryTimeout).mapTo[Long] foreach { token ⇒
      self ! RegisterAtomCb(token, f)
    }
  }
  
  //---------------------------------------------------------------------------
  //
  //  Private zone
  //
  //---------------------------------------------------------------------------
  
  private[this] case class RegisterAtomCb(token: Long, cb: AtomCb[Data])

  private[this] case class RegisterCursorCb(token: Long, cb: CursorCb[Data])

  private[this] class CursorImpl(token: Long) extends Cursor[Data] {

    sealed trait CursorState

    case object Idle extends CursorState

    case class AwaitNext(f: Option[Data] ⇒ _) extends CursorState

    case class AwaitFinish(f: Seq[Data] ⇒ _) extends CursorState

    case class Foreach(f: Option[Data] ⇒ _) extends CursorState

    var state: CursorState = Idle

    var data = List.empty[Data]

    var closed = false

    def next[U](f: (Option[Data]) => U): Unit = {
      checkIsIdle()
      if (closed) {
        f(None)
      }
      else {
        data match {
          case x :: xs ⇒
            data = xs
            f(Some(x))
          case Nil ⇒
            state = AwaitNext(f)
        }
        dbConnection ! ContinueQuery(token)
      }
    }

    def close(): Unit = {
      if (!closed) dbConnection ! StopQuery(token)
      else throw CursorException("Cursor already closed")
    }

    def force[U](f: Seq[Data] ⇒ U): Unit = {
      checkIsIdle()
      if (closed) f(data.reverse)
      else state = AwaitFinish(f)
    }

    def foreach[U](f: Option[Data] ⇒ U): Unit = {
      checkIsIdle()
      data.foreach(x ⇒ f(Some(x)))
      data = Nil
      state = Foreach(f)
      dbConnection ! ContinueQuery(token)
    }

    def checkIsIdle(): Unit = {
      if (state != Idle) {
        throw CursorException(s"Cursor already activated: $state")
      }
    }

    def append(value: Data, close: Boolean): Unit = {
      // Before apply
      if (close) closed = true
      // Apply
      state match {
        case AwaitNext(f) ⇒ f(Some(value))
        case Foreach(f) ⇒
          f(Some(value))
          if (!close) {
            dbConnection ! ContinueQuery(token)
          }
        case _ ⇒ data ::= value
      }
      // After apply
      if (close) {
        state match {
          case AwaitNext(f) ⇒ f(None)
          case Foreach(f) ⇒ f(None)
          case AwaitFinish(f) ⇒ f(data.reverse)
          case _ ⇒ // Do nothing
        }
      }
    }
  }

  private[this] val atomCallbacks = mutable.Map.empty[Long, AtomCb[Data]]

  private[this] val cursorCallbacks = mutable.Map.empty[Long, CursorCb[Data]]
  
  private[this] val activeCursors = mutable.Map.empty[Long, CursorImpl]

  @tailrec
  private[this] def appendSequenceToCursor(cursor: CursorImpl, tail: List[Data]): Unit = tail match {
    case Nil ⇒ // Do nothing
    case x :: Nil ⇒ cursor.append(x, close = true)
    case x :: xs ⇒
      cursor.append(x, close = false)
      appendSequenceToCursor(cursor, xs)
  }

  private[this] def registerCursorForPartialResponse(token: Long): CursorImpl = {
    val cursor = new CursorImpl(token)
    cursorCallbacks.get(token).foreach(cb ⇒ cb(cursor))
    cursor
  }

  override def unhandled(message: Any): Unit = {
    message match {
      case ReqlTcpConnection.Response(token, rawData) ⇒
        println("Reponse received")
        parseResponse(rawData) match {
          case pr: ParsedResponse.Atom ⇒
            println(pr)
            atomCallbacks.remove(token).foreach(cb ⇒ cb(pr.data))
            dbConnection ! ForgetQuery(token)
          case pr: ParsedResponse.Sequence if pr.partial ⇒
            println(pr)
            val cursor = activeCursors.getOrElseUpdate(
              token, registerCursorForPartialResponse(token))
            pr.xs.foreach(cursor.append(_, close = false))
          case pr: ParsedResponse.Sequence if !pr.partial ⇒
            println(pr)
            activeCursors.get(token) match {
              case Some(cursor) ⇒ appendSequenceToCursor(cursor, pr.xs.toList)
              case None ⇒ cursorCallbacks.get(token) foreach { cb ⇒
                val cursor = new CursorImpl(token)
                appendSequenceToCursor(cursor, pr.xs.toList)
                cb(cursor)
              }
            }
            dbConnection ! ForgetQuery(token)
          case pr: ParsedResponse.Error ⇒
            println(pr)
            // TODO
        }
      case RegisterCursorCb(token, f) ⇒ 
        cursorCallbacks(token) = f
      case RegisterAtomCb(token, f) ⇒ 
        atomCallbacks(token) = f
      case _ ⇒
        super.unhandled(message)
    }
  }
}

object ReqlActor {
  case class CursorException(message: String) extends Exception(message)
}
