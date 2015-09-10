package reql.dsl

import reql.protocol.ReqlResponseWithError

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions

/**
 * Base trait for any classes who want to implement
 * hi-level RethinkDB access. 
 * @tparam Data JSON AST base trait or primitive
 */
trait ReqlContext[Data] extends ReqlEntryPoint {

  import ReqlContext._

  implicit val runContext = this

  //---------------------------------------------------------------------------
  //
  //  Internal types
  //
  //---------------------------------------------------------------------------

  sealed trait ParsedResponse

  object ParsedResponse {

    trait Error extends ParsedResponse {
      def tpe: ReqlResponseWithError
      def text: String
    } 

    trait Atom extends ParsedResponse {
      def data: Data
    }

    trait Sequence extends ParsedResponse {
      def xs: Seq[Data]
      def partial: Boolean
    }
  }

  /**
   * Parse response. Implement this in parser.
   * @param data JSON part of response
   */
  def parseResponse(data: Array[Byte]): ParsedResponse

  def runCursorQuery[U](query: ReqlArg)(f: CursorCb[Data]): Unit

  def runAtomQuery[U](query: ReqlArg)(f: AtomCb[Data]): Unit

  //---------------------------------------------------------------------------
  //
  //  run() method providers
  //
  //---------------------------------------------------------------------------

  implicit def toRunOps(x: ReqlArg): RunReqlOps[Data] = {
    new RunReqlOps(x)
  }

}

object ReqlContext {

  type AtomCb[Data] = Either[ReqlQueryException, Data] ⇒ _

  type CursorCb[Data] = Cursor[Data] ⇒ _

  final class RunReqlOps[Data](val self: ReqlArg) extends AnyVal {

    def runA[U](f: Either[ReqlQueryException, Data] ⇒ U)(implicit c: ReqlContext[Data]): Unit = {
      c.runAtomQuery(self)(f)
    }

    def runA(implicit c: ReqlContext[Data]): Future[Data] = {
      val p = Promise[Data]()
      c.runAtomQuery[Unit](self) { res: Either[ReqlQueryException, Data] ⇒
        res match {
          case Right(value) ⇒ p.success(value)
          case Left(value) ⇒
            val exception = ThrowableReqlQueryException(value, self)
            p.failure(exception)
        }
      }
      p.future
    }

    def runC[U](f: Cursor[Data] ⇒ U)(implicit c: ReqlContext[Data]): Unit = {
      c.runCursorQuery(self)(f)
    }
  }

}
