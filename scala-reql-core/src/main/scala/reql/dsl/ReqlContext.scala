package reql.dsl

import reql.dsl.types.{AtomResultQuery, CursorResultQuery}
import reql.protocol.ReqlResponseWithError

import scala.language.implicitConversions

/**
 * Base trait for any classes who want to implement
 * hi-level RethinkDB access. 
 * @tparam Data JSON AST base trait or primitive
 */
trait ReqlContext[Data] {

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

  def runCursorQuery[U](query: ReqlArg, f: CursorCb[Data]): Unit

  def runAtomQuery[U](query: ReqlArg, f: AtomCb[Data]): Unit

  //---------------------------------------------------------------------------
  //
  //  run() method providers
  //
  //---------------------------------------------------------------------------

  implicit def toCursorOps(x: CursorResultQuery): CursorResultQueryOps[Data] = {
    new CursorResultQueryOps(x)
  }

  implicit def toAtomOps(x: AtomResultQuery): AtomResultQueryOps[Data] = {
    new AtomResultQueryOps(x)
  }

}

object ReqlContext {

  type AtomCb[Data] = Either[ReqlQueryException, Data] ⇒ _

  type CursorCb[Data] = Cursor[Data] ⇒ _

  final class CursorResultQueryOps[Data](val self: CursorResultQuery) extends AnyVal {
    def run[U](f: Cursor[Data] ⇒ U)(implicit c: ReqlContext[Data]): Unit = {
      c.runCursorQuery(self, f)
    }
  }

  final class AtomResultQueryOps[Data](val self: AtomResultQuery) extends AnyVal {
    def run[U](f: Either[ReqlQueryException, Data] ⇒ U)(implicit c: ReqlContext[Data]): Unit = {
      c.runAtomQuery(self, f)
    }
  }

}
