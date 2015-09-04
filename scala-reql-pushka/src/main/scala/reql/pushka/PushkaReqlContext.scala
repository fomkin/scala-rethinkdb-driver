package reql.pushka

import java.nio.charset.StandardCharsets

import pushka.annotation._
import pushka.json._
import pushka.{Ast, PushkaException}
import reql.dsl.ReqlContext
import reql.protocol.{ReqlResponseType, ReqlResponseWithError}

trait PushkaReqlContext extends ReqlContext[Ast] {

  import ReqlResponseType._

  @pushka
  case class PRes(t: Int, r: Ast)

  case class Atom(data: Ast) extends ParsedResponse.Atom

  case class Sequence(xs: Seq[Ast], partial: Boolean) extends ParsedResponse.Sequence

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
}
