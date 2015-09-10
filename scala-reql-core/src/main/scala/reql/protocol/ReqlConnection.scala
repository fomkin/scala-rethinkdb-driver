package reql.protocol

import java.nio.charset.StandardCharsets
import java.nio.{ByteBuffer, ByteOrder}

import reql.dsl.ReqlArg

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
  def startQuery(token: Long, data: ReqlArg): Long = {
    val json = s"[${ReqlQueryType.Start.value}, ${data.json}]"
    val jsonBuffer = ByteBuffer.wrap(json.getBytes("UTF-8"))
    send(token, jsonBuffer)
    token
  }

  def continueQuery(token: Long) = {
    send(token, ContinueBuffer)
  }

  def stopQuery(token: Long) = send(token, StopBuffer)

  //---------------------------------------------------------------------------
  //
  // Internal API
  //
  //---------------------------------------------------------------------------

  protected def sendBytes(data: ByteBuffer): Unit

  protected def onFatalError(message: String): Unit

  protected def onResponse(token: Long, data: Array[Byte]): Unit

  /**
   * Process data from RethinkDB server when connection
   * was established and handshake was successful.
   * Decode packets from server and run callback.
   * @param data Raw data from connection
   */
  protected def processData(data: ByteBuffer): Unit = {
    // Update buffer
    buffer.position(0)
    buffer = ByteBuffer.allocate(buffer.capacity + data.capacity).
      put(buffer).
      put(data)
    processData()
  }

  @tailrec
  final protected def processData(): Unit = {
    buffer.position(0)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    // Process data
    (state: @switch) match {
      case Processing ⇒
        // Enough for header
        if (buffer.capacity >= HeaderSize) {
          val queryToken = buffer.getLong(0)
          val responseLength = buffer.getInt(8)
          val messageSize = HeaderSize + responseLength
          // Enough for body
          if (buffer.capacity >= messageSize) {
            val jsonBytes = new Array[Byte](responseLength)
            buffer.position(HeaderSize)
            buffer.get(jsonBytes)
            // Truncate buffer
            buffer.position(messageSize)
            buffer = buffer.slice()
            // Process response
            onResponse(queryToken, jsonBytes)
            processData()
          }
        }
      case Handshake ⇒
        val zeroByteIndex = 0 until buffer.capacity indexWhere { i ⇒
          buffer.get(i) == 0
        }
        if (zeroByteIndex > -1) {
          val messageArray = new Array[Byte](zeroByteIndex)
          val messageSize = zeroByteIndex + 1
          buffer.position(0)
          buffer.get(messageArray, 0, zeroByteIndex)
          buffer.position(messageSize)
          buffer = buffer.slice()
          val asciiString = new String(
            messageArray,
            StandardCharsets.US_ASCII
          )
          asciiString match {
            case "SUCCESS" ⇒
              state = Processing
              processData()
            case res if res.startsWith("ERROR: ") ⇒
              val s = asciiString.stripPrefix("ERROR: ")
              onFatalError(s"Handshake fails with: '$s'")
            case s ⇒ onFatalError(s"Unexpected handshake result: '$s'")
          }
        }
    }
  }

  /**
   * Prepare data for handshake
   */
  protected def createHandshakeBuffer(authKey: Option[String],
                                      version: ReqlVersion = ReqlVersion.V04,
                                      protocol: ReqlProtocol = ReqlProtocol.Json): ByteBuffer = {
    val authKeyBuffer = authKey.fold(ByteBuffer.allocate(4)) { key ⇒
      val buffer = ByteBuffer.allocate(key.length + 4).
        order(ByteOrder.LITTLE_ENDIAN).
        putInt(key.length).
        put(key.getBytes("ASCII"))
      buffer.position(0)
      buffer
    }
    val buffer = ByteBuffer.allocate(authKeyBuffer.capacity + 8).
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

  private[this] var buffer: ByteBuffer = ByteBuffer.allocate(0)

  private[this] def send(token: Long, data: ByteBuffer): Unit = {
    data.position(0)
    val buffer = ByteBuffer.
      allocate(HeaderSize + data.capacity).
      order(ByteOrder.LITTLE_ENDIAN).
      putLong(token).
      putInt(data.capacity).
      put(data)
    buffer.position(0)
    sendBytes(buffer)
  }
}

private object ReqlConnection {

  val Handshake = 0

  val Processing = 1

  val HeaderSize = 12

  val ContinueBuffer = ByteBuffer.wrap("[2]".getBytes("UTF-8"))

  val StopBuffer = ByteBuffer.wrap("[3]".getBytes("UTF-8"))
}
