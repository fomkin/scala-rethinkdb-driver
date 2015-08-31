package reql.protocol

import java.nio.charset.StandardCharsets
import java.nio.{ByteBuffer, ByteOrder}
import java.util.concurrent.atomic.AtomicLong

import pushka.annotation.pushka
import pushka.json.{read => readJson, write => writeJson, _}
import reql.dsl.Json

import scala.annotation.{switch, tailrec}

/**
 * See specification at
 * http://www.rethinkdb.com/docs/writing-drivers/
 */
trait ReqlConnection {

  import ReqlConnection._

  //---------------------------------------------------------------------------
  //
  // Public API
  //
  //---------------------------------------------------------------------------

  /**
   * @param data Prepared query
   * @return Query unique token
   */
  def startQuery(data: Json): Long = {
    val token = tokenFactory.incrementAndGet()
    val json = Json.Arr(Seq(
      Json.Num(ReqlQueryType.Start.value),
      data
    ))
    val jsonBuffer = ByteBuffer.wrap(writeJson(json).getBytes("UTF-8"))
    send(token, jsonBuffer)
    token
  }

  def continueQuery(token: Long) = send(token, ContinueBuffer)

  def stopQuery(token: Long) = send(token, StopBuffer)

  //---------------------------------------------------------------------------
  //
  // Internal API
  //
  //---------------------------------------------------------------------------

  protected def sendBytes(data: ByteBuffer): Unit

  protected def onFatalError(message: String): Unit

  protected def onResponse(queryToken: Long,
                           responseType: ReqlResponseType,
                           json: Json): Unit

  /**
   * Process data from RethinkDB server when connection
   * was established and handshake was successful.
   * Decode packets from server and run callback.
   * @param data Raw data from connection
   */
  protected def processData(data: ByteBuffer): Unit = {
    @tailrec
    def loop(): Unit = {
      // Enough for header
      if (buffer.limit >= HeaderSize) {
        val queryToken = buffer.getLong(0)
        val responseLength = buffer.getInt(8)
        val messageSize = HeaderSize + responseLength
        // Enough for body
        if (buffer.limit >= messageSize) {
          val jsonBuffer = ByteBuffer.allocate(responseLength)
          val tailSize = buffer.limit - messageSize
          buffer.position(HeaderSize)
          processResponse(queryToken, jsonBuffer.put(buffer))
          if (tailSize > 0) {
            val newBuffer = ByteBuffer.allocate(tailSize)
            buffer.position(messageSize)
            buffer = newBuffer.put(buffer)
            loop()
          }
          else {
            buffer.clear()
          }
        }
      }
    }
    // Update buffer
    buffer.position(0)
    buffer = ByteBuffer.allocate(buffer.limit + data.limit).
      order(ByteOrder.LITTLE_ENDIAN).
      put(buffer).
      put(data)
    // Process data
    (state: @switch) match {
      case Processing ⇒ loop()
      case Handshake if buffer.get(buffer.limit - 1) == 0 ⇒
        val asciiString = new String(buffer.array(), StandardCharsets.US_ASCII)
        asciiString.trim match {
          case "SUCCESS" ⇒ state = Processing
          case res if res.startsWith("ERROR: ") ⇒
            val s = asciiString.stripPrefix("ERROR: ")
            onFatalError(s"Handshake fails with: '$s'")
          case s ⇒ onFatalError("Unexpected handshake result: '$s'")
        }
    }
  }

  /**
   * Prepare data for handshake
   */
  protected def createHandshakeBuffer(authKey: Option[String],
                                      version: ReqlVersion = ReqlVersion.V04,
                                      protocol: ReqlProtocol = ReqlProtocol.JSON): ByteBuffer = {
    val authKeyBuffer = authKey.fold(ByteBuffer.allocate(4)) { key ⇒
      val buffer = ByteBuffer.allocate(key.length + 4).
        order(ByteOrder.LITTLE_ENDIAN).
        putInt(key.length).
        put(key.getBytes("ASCII"))
      buffer.position(0)
      buffer
    }
    val buffer = ByteBuffer.allocate(authKeyBuffer.limit + 8).
      order(ByteOrder.LITTLE_ENDIAN).
      putInt(version.value).
      put(authKeyBuffer).
      putInt(protocol.value)
    buffer.position(0)
    buffer
  }

  //---------------------------------------------------------------------------
  //
  //  Private API
  //
  //---------------------------------------------------------------------------

  private[this] var state: Int = Handshake

  private[this] val tokenFactory = new AtomicLong()

  private[this] var buffer: ByteBuffer = ByteBuffer.allocate(0)

  private[this] def send(token: Long, data: ByteBuffer): Unit = {
    val buffer = ByteBuffer.
      allocate(HeaderSize + data.limit).
      order(ByteOrder.LITTLE_ENDIAN).
      putLong(token).
      putInt(data.limit).
      put(data)
    buffer.position(0)
    sendBytes(buffer)
  }

  private[this] def processResponse(token: Long, data: ByteBuffer): Unit = {

    import ReqlResponseType._
    val utfString = new String(data.array(), StandardCharsets.UTF_8)
    val responseWrapper = readJson[ResponseWrapper](utfString)

    onResponse(
      queryToken = token,
      json = responseWrapper.r,
      responseType = (responseWrapper.t: @switch) match {
        case ClientError.value ⇒ ClientError
        case CompileError.value ⇒ CompileError
        case RuntimeError.value ⇒ RuntimeError
        case SuccessAtom.value ⇒ SuccessAtom
        case SuccessPartial.value ⇒ SuccessPartial
        case SuccessSequence.value ⇒ SuccessSequence
        case WaitComplete.value ⇒ WaitComplete
      }
    )
  }
}

private object ReqlConnection {

  @pushka
  case class ResponseWrapper(t: Int, r: Json)

  val Handshake = 0

  val Processing = 1

  val HeaderSize = 12

  val ContinueBuffer = ByteBuffer.wrap("[2]".getBytes("UTF-8"))

  val StopBuffer = ByteBuffer.wrap("[3]".getBytes("UTF-8"))
}

