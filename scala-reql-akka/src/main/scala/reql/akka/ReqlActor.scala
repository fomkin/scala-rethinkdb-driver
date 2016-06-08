package reql.akka

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.util.Timeout
import reql.dsl.{Cursor, ReqlArg, ReqlContext, ReqlQueryException}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

trait ReqlActor[Data] extends Actor with ReqlContext[Data] with ActorLogging {

  import ReqlActor._
  import ReqlContext._
  import RethinkDbConnectionActor._

  /**
   * Time to wait for response from RethinkDB
   *
   * @return
   */
  def queryTimeout: Timeout

  /**
   * The actor implements ReqlTcpConnection protocol. It can be
   * ReqlTcp connection itself or router for connection pool.
   *
   * @return
   */
  def dbConnection: ActorRef

  //---------------------------------------------------------------------------
  //
  //  ReqlContext implementation
  //
  //---------------------------------------------------------------------------

  def runCursorQuery[U](query: ReqlArg)(f: CursorCb[Data]): Unit = {
    self ! Message.RunCursorQuery(query, f)
  }

  def runAtomQuery[U](query: ReqlArg)(f: AtomCb[Data]): Unit = {
    self ! Message.RunAtomQuery(query, f)
  }

  //---------------------------------------------------------------------------
  //
  //  Private zone
  //
  //---------------------------------------------------------------------------

  private[this] class CursorImpl(token: Long) extends Cursor[Data] {

    sealed trait CursorState

    case object Idle extends CursorState

    case class Next(f: Either[ReqlQueryException, Data] ⇒ _) extends CursorState

    case class Force(f: Either[ReqlQueryException, Seq[Data]] ⇒ _) extends CursorState

    case class Foreach(f: Either[ReqlQueryException, Data] ⇒ _) extends CursorState

    case class Failed(e: ReqlQueryException) extends CursorState

    var state: CursorState = Idle

    var data = List.empty[Data]

    var closed = false

    def next[U](f: Either[ReqlQueryException, Data] ⇒ U): Unit = {
      checkFail(whenFail = e ⇒ f(Left(e))) {
        if (closed) {
          f(Left(ReqlQueryException.End))
        }
        else {
          data match {
            case x :: xs ⇒
              data = xs
              f(Right(x))
            case Nil ⇒
              state = Next(f)
          }
          dbConnection ! ContinueQuery(token)
        }
      }
    }

    def close(): Unit = {
      if (!closed) {
        closed = true
        dbConnection ! StopQuery(token)
      }
      else throw CursorException("Cursor already closed")
    }

    def force[U](f: Either[ReqlQueryException, Seq[Data]] ⇒ U): Unit = {
      checkFail(whenFail = e ⇒ f(Left(e))) {
        if (closed) f(Right(data.reverse))
        else state = Force(f)
      }
    }

    def foreach[U](f: Either[ReqlQueryException, Data] ⇒ U): Unit = {
      checkFail(whenFail = e ⇒ f(Left(e))) {
        if (data != Nil) {
          data.foreach(x ⇒ f(Right(x)))
          data = Nil
        }
        if (!closed) {
          state = Foreach(f)
          dbConnection ! ContinueQuery(token)
        }
      }
    }

    /**
     * Check cursor is idle and not failed
     */
    def checkFail[U1, U2](whenFail: ReqlQueryException ⇒ U1)(whenNot: ⇒ U2): Unit = {
      state match {
        case Failed(e) ⇒ whenFail(e)
        case Idle ⇒ whenNot
        case _ ⇒
          val msg = s"Cursor already activated: $state"
          val e = ReqlQueryException.ReqlIllegalUsage(msg)
          whenFail(e)
      }
    }

    def fail(exception: ReqlQueryException): Unit = state match {
      case Idle ⇒ state = Failed(exception)
      case Foreach(f) ⇒ f(Left(exception))
      case Next(f) ⇒ f(Left(exception))
      case Force(f) ⇒ f(Left(exception))
      case Failed(_) ⇒ // We cant fail twice!
    }

    def append(value: Data, close: Boolean): Unit = {
      // Before apply
      if (close) closed = true
      // Apply
      state match {
        case Next(f) ⇒ f(Right(value))
        case Foreach(f) ⇒
          f(Right(value))
          if (!close) {
            dbConnection ! ContinueQuery(token)
          }
        case _ ⇒ data ::= value
      }
      // After apply
      if (close) {
        state match {
          case Next(f) ⇒ f(Left(ReqlQueryException.End))
          case Foreach(f) ⇒ f(Left(ReqlQueryException.End))
          case Force(f) ⇒ f(Right(data.reverse))
          case _ ⇒ // Do nothing
        }
      }
    }
  }

  private[this] object Message {

    case class RunCursorQuery(query: ReqlArg, callback: CursorCb[Data])

    case class RunAtomQuery(query: ReqlArg, callback: AtomCb[Data])

  }

  private[this] val atomCallbacks = mutable.Map.empty[Long, AtomCb[Data]]

  private[this] val cursorCallbacks = mutable.Map.empty[Long, CursorCb[Data]]

  private[this] val activeCursors = mutable.Map.empty[Long, CursorImpl]

  @tailrec
  private[this] def appendSequenceToCursorAndClose(cursor: CursorImpl, tail: List[Data]): Unit = tail match {
    case Nil ⇒ cursor.close()
    case x :: Nil ⇒ cursor.append(x, close = true)
    case x :: xs ⇒
      cursor.append(x, close = false)
      appendSequenceToCursorAndClose(cursor, xs)
  }

  private[this] def registerCursorForPartialResponse(token: Long): CursorImpl = {
    val cursor = new CursorImpl(token)
    if (cursorCallbacks.get(token).isEmpty) {
      log.warning("Haven't callback for cursor with token {}", token)
    }
    cursor
  }

  @tailrec
  private def createToken(exists: Long ⇒ Boolean): Long = {
    Random.nextLong() match {
      case token if !exists(token) ⇒ token
      case _ ⇒ createToken(exists)
    }
  }

  override def unhandled(message: Any): Unit = {
    message match {
      case Message.RunCursorQuery(query, callback) ⇒
        val token = createToken(cursorCallbacks.contains)
        val message = StartQuery(token, query)
        cursorCallbacks(token) = callback
        dbConnection ! message
      case Message.RunAtomQuery(query, callback) ⇒
        val token = createToken(atomCallbacks.contains)
        val message = StartQuery(token, query)
        atomCallbacks(token) = callback
        dbConnection ! message
      case RethinkDbConnectionActor.Response(token, rawData) ⇒
        parseResponse(rawData) match {
          case pr: ParsedResponse.Atom[Data] ⇒
            atomCallbacks.remove(token).foreach(cb ⇒ cb(Right(pr.data)))
            dbConnection ! ForgetQuery(token)
          case pr: ParsedResponse.Sequence[Data] if pr.partial ⇒
            val cursor = activeCursors.getOrElseUpdate(token, registerCursorForPartialResponse(token))
            pr.xs.foreach(cursor.append(_, close = false))
            dbConnection ! ContinueQuery(token)
          case pr: ParsedResponse.Sequence[Data] if !pr.partial ⇒
            activeCursors.get(token) match {
              case Some(cursor) ⇒
                appendSequenceToCursorAndClose(cursor, pr.xs.toList)
                cursorCallbacks.remove(token) foreach { cb ⇒ cb(cursor) }
              case None ⇒ cursorCallbacks.get(token) match {
                case Some(cb) ⇒
                  val cursor = new CursorImpl(token)
                  appendSequenceToCursorAndClose(cursor, pr.xs.toList)
                  cb(cursor)
                case None ⇒
                  log.warning("Haven't a callback for non-partial response. Token is {}", token)
              }
            }
            dbConnection ! ForgetQuery(token)
          case pr: ParsedResponse.Error ⇒
            val ex = ReqlQueryException.ReqlErrorResponse(pr.tpe, pr.text)
            dbConnection ! ForgetQuery(token)
            activeCursors.remove(token) match {
              case Some(cursor) ⇒
                cursor.fail(ex)
                cursorCallbacks.remove(token)
              case None ⇒
                cursorCallbacks.remove(token) match {
                  case Some(cb) ⇒
                    val cursor = new CursorImpl(token)
                    cursor.fail(ex)
                    cb(cursor)
                  case None ⇒
                    atomCallbacks.remove(token) match {
                      case Some(cb) ⇒ cb(Left(ex))
                      case None ⇒ log.warning("Haven't a callback for error response. Token is {}", token)
                    }
                }
            }
        }
      case RethinkDbConnectionActor.ConnectionClosed ⇒
        activeCursors foreach {
          case (k, v) ⇒
            v.fail(ReqlQueryException.ConnectionError)
        }
        atomCallbacks foreach {
          case (k, v) ⇒
            v(Left(ReqlQueryException.ConnectionError))
        }
        cursorCallbacks foreach {
          case (k, v) ⇒
            val cursor = new CursorImpl(k)
            cursor.fail(ReqlQueryException.ConnectionError)
            v(cursor)
        }
        cursorCallbacks.clear()
        atomCallbacks.clear()
        activeCursors.clear()
      case _ ⇒
        super.unhandled(message)
    }
  }
}

object ReqlActor {
  case class CursorException(message: String) extends Exception(message)
}
