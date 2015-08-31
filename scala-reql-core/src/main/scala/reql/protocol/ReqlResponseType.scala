package reql.protocol

sealed abstract class ReqlResponseType(val value: Int)

object ReqlResponseType {

  /**
   * Query returned a single RQL datatype. 
   */
  case object SuccessAtom extends ReqlResponseType(1)

  /**
   * Query returned a sequence of RQL datatypes.
   */
  case object SuccessSequence extends ReqlResponseType(2)

  /**
   * Query returned a partial sequence of RQL
   * datatypes.  If you send a [CONTINUE] query with
   * the same token as this response, you will get
   * more of the sequence.  Keep sending [CONTINUE]
   * queries until you get back [SUCCESS_SEQUENCE].
   */
  case object SuccessPartial extends ReqlResponseType(3)

  /**
   * A [NOREPLY_WAIT] query completed.
   */
  case object WaitComplete extends ReqlResponseType(4)

  // These response types indicate failure.

  /**
   * Means the client is buggy.  An example is if the
   * client sends a malformed protobuf, or tries to
   * send [CONTINUE] for an unknown token.
   */
  case object ClientError extends ReqlResponseType(16)

  /**
   * Means the query failed during parsing or type
   * checking.  For example, if you pass too many
   * arguments to a function.
   */
  case object CompileError extends ReqlResponseType(17)

  /**
   * Means the query failed at runtime.  An example is
   * if you add together two values from a table, but
   * they turn out at runtime to be booleans rather
   * than numbers.
   */
  case object RuntimeError extends ReqlResponseType(18)

}
