package reql.pushka

import java.nio.charset.StandardCharsets

import pushka.annotation._
import pushka.json._
import pushka.{Ast, PushkaException}
import reql.dsl.{types, ReqlEntryPoint, ReqlContext}
import reql.dsl.types.Datum
import reql.protocol.{ReqlResponseType, ReqlResponseWithError}

import scala.language.implicitConversions

trait PushkaReqlContext extends ReqlContext[Ast] {

  import ReqlResponseType._
  import PushkaReqlContext._
  import ReqlContext._

  @pushka
  case class PRes(t: Int, r: Ast)

  case class Atom(data: Ast) extends ParsedResponse.Atom[Ast]

  case class Sequence(xs: Seq[Ast], partial: Boolean) extends ParsedResponse.Sequence[Ast]

  case class Error(tpe: ReqlResponseWithError, text: String) extends ParsedResponse.Error
  
  def parseResponse(data: Array[Byte]): ParsedResponse = {
    read[PRes](new String(data, StandardCharsets.UTF_8)) match {
      case PRes(ClientError.value, ast: Ast.Arr) ⇒ Error(ClientError, pushka.read[String](ast.value.head))
      case PRes(CompileError.value, ast: Ast.Arr) ⇒ Error(CompileError, pushka.read[String](ast.value.head))
      case PRes(RuntimeError.value, ast: Ast.Arr) ⇒ Error(RuntimeError, pushka.read[String](ast.value.head))
      case PRes(SuccessAtom.value, ast: Ast.Arr) ⇒ Atom(ast.value.head)
      case PRes(SuccessPartial.value, ast: Ast.Arr) ⇒ Sequence(ast.value.toSeq, partial = true)
      case PRes(SuccessSequence.value, ast: Ast.Arr) ⇒ Sequence(ast.value.toSeq, partial = false)
      case response ⇒ throw new PushkaException(s"Unexpected response: $response")
    }
  }
  
  implicit def toAstOps(x: Ast): AstOps = new AstOps(x)
}

object PushkaReqlContext extends ReqlEntryPoint {

  def astToDatum(value: Ast): Datum = value match {
    case Ast.Obj(m) ⇒
      document fromMap {
        m map {
          case (k, v) ⇒ (k, astToDatum(v))
        }
      }
    case Ast.Str(s) ⇒ toStr(s)
    case Ast.Num(n) if n.contains('.') ⇒ toNum(n.toDouble)
    case Ast.Num(n) ⇒ toNum(n.toInt)
    case Ast.Arr(xs) ⇒ array(xs.toSeq.map(astToDatum):_*)
    case Ast.False ⇒ toBool(value = false)
    case Ast.True ⇒ toBool(value = true)
    case Ast.Null ⇒ Null
  }

  final class AstOps(val self: Ast) extends AnyVal {
    def toDatum: Datum = astToDatum(self)
    def toObj: types.Obj = self match {
      case Ast.Obj(m) ⇒
        document fromMap {
          m map {
            case (k, v) ⇒ (k, astToDatum(v))
          }
        }
      case _ ⇒ 
        throw new ClassCastException(s"$self cannot be converted to reql.dsl.types.Obj")
    }
  }

}
