package reql.akka

import java.net.InetSocketAddress
import java.nio.ByteBuffer

import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import reql.dsl.ReqlArg
import reql.protocol._

import scala.concurrent.duration._

class ReqlTcpConnection(remote: InetSocketAddress, authKey: Option[String])

  extends Actor with ReqlConnection {

  /**
   * _1: Token.
   * _2: Receiver of response
   * _3: Request body
   */
  type PendingQuery = (Long, ActorRef, ReqlArg)

  import ReqlTcpConnection._
  import context.{dispatcher, system}

  establishConnection()

  //---------------------------------------------------------------------------
  //
  // State
  //
  //---------------------------------------------------------------------------

  val queries = scala.collection.mutable.Map.empty[Long, ActorRef]

  var pendingQueries = List.empty[PendingQuery]

  var tcpConnection = Option.empty[ActorRef]

  //---------------------------------------------------------------------------
  //
  // Receiving
  //
  //---------------------------------------------------------------------------

  def receive: Receive = connecting

  def connecting: Receive = {
    case StartQuery(token, query) ⇒
      val sndr = sender()
      context watch sndr
      pendingQueries ::=(token, sndr, query)
    case c: Tcp.Connected ⇒
      reset()
      val handshakeBuffer = createHandshakeBuffer(authKey)
      val connection = sender()
      this.tcpConnection = Some(connection)
      context watch connection
      context become operating
      connection ! Tcp.Register(self)
      connection ! Tcp.ResumeReading
      connection ! Tcp.Write(ByteString(handshakeBuffer), ReqlTcpConnection.Ack)
      // Send pending queries
      pendingQueries foreach {
        case (token, receiver, json) ⇒
          queries(token) = receiver
          startQuery(token, json)
      }
      pendingQueries = Nil
    case Tcp.CommandFailed(_: Tcp.Connect) ⇒
      context.system.scheduler.scheduleOnce(ReconnectInterval) {
        establishConnection()
      }
  }

  def operating: Receive = {
    case StartQuery(token, query) ⇒
      val receiver = sender()
      context watch receiver
      queries(token) = receiver
      startQuery(token, query)
    case StopQuery(queryToken) ⇒
      stopQuery(queryToken)
      queries.remove(queryToken) foreach { refToUnwatch ⇒
        tryToUnwatchListener(refToUnwatch)
      }
    case ForgetQuery(token) ⇒
      queries.remove(token).foreach(tryToUnwatchListener)
    case ContinueQuery(token) ⇒
      continueQuery(token)
    case Tcp.Received(data) ⇒
      processData(data.asByteBuffer)
    case ReqlTcpConnection.Ack ⇒
      tcpConnection foreach (_ ! Tcp.ResumeReading)
    case _: Tcp.ConnectionClosed =>
      tcpConnection foreach context.unwatch
      tcpConnection = None
      for ((token, (receiver)) ← queries) {
        receiver ! ConnectionClosed
      }
      establishConnection()
      context.unbecome()
  }

  override def unhandled(message: Any): Unit = message match {
    case Terminated(subject) ⇒
      queries.retain((k, v) ⇒ v != subject)
      pendingQueries = pendingQueries filter {
        case (sender, receiver, _) ⇒ receiver != subject
      }
    case _ ⇒ super.unhandled(message)
  }

  def tryToUnwatchListener(refToUnwatch: ActorRef): Unit = {
    val exists = queries.exists { case (k, v) ⇒ v == refToUnwatch }
    if (!exists) context unwatch refToUnwatch
  }

  def establishConnection(): Unit = {
    IO(Tcp) ! Tcp.Connect(remote, pullMode = true)
  }

  //---------------------------------------------------------------------------
  //
  // ReqlConnection implementation
  //
  //---------------------------------------------------------------------------

  protected def sendBytes(data: ByteBuffer): Unit = {
    tcpConnection foreach (_ ! Tcp.Write(ByteString(data), ReqlTcpConnection.Ack))
  }

  protected def onFatalError(message: String): Unit = {
    context stop self
  }

  protected def onResponse(queryToken: Long, data: Array[Byte]): Unit = {
    queries.get(queryToken) foreach { ref ⇒
      ref ! ReqlTcpConnection.Response(queryToken, data)
    }
  }
}

object ReqlTcpConnection {

  def props(remote: InetSocketAddress = new InetSocketAddress("localhost", 28015),
            authKey: Option[String] = None): Props = {
    Props(classOf[ReqlTcpConnection], remote, authKey)
  }

  private[reql] val ReconnectInterval = 2 seconds

  private[reql] case object Ack extends Tcp.Event

  //------------------------------------------------
  // Events
  //------------------------------------------------

  sealed trait ReqlConnectionEvent

  case class Response(token: Long, data: Array[Byte])
    extends ReqlConnectionEvent

  case object ConnectionClosed extends ReqlConnectionEvent

  //------------------------------------------------
  // Commands
  //------------------------------------------------

  sealed trait ReqlConnectionCommand

  case class ContinueQuery(token: Long)
    extends ReqlConnectionCommand

  case class StopQuery(token: Long)
    extends ReqlConnectionCommand

  case class StartQuery(token: Long, query: ReqlArg)
    extends ReqlConnectionCommand

  case class ForgetQuery(token: Long)
    extends ReqlConnectionCommand

}
