package reql.dsl

import reql.protocol.ReqlResponseWithError

sealed trait ReqlQueryException

object ReqlQueryException {

  case class ReqlIllegalUsage(text: String)
    extends ReqlQueryException

  case class ReqlErrorResponse(tpe: ReqlResponseWithError, text: String)
    extends ReqlQueryException

  /**
   * End of cursor
   */
  case object End extends ReqlQueryException
} 
