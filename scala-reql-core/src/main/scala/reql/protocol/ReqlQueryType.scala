package reql.protocol

sealed abstract class ReqlQueryType(val value: Int)

object ReqlQueryType {

  /**
   * Start a new query.
   */
  case object Start extends ReqlQueryType(1)

  /**
   * Continue a query that returned [SUCCESS_PARTIAL]
   */
  case object Continue extends ReqlQueryType(2)

  /**
   * Stop a query partway through executing.
   */
  case object Stop extends ReqlQueryType(3)

  /**
   * Wait for noreply operations to finish.
   */
  case object NoReplyWait extends ReqlQueryType(4)
}
