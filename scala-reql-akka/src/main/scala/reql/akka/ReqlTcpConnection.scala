package reql.akka

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Props, Actor, ActorRef, Terminated}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import reql.dsl.ReqlArg
import reql.protocol._

class ReqlTcpConnection(remote: InetSocketAddress, authKey: Option[String])

  extends Actor with ReqlConnection {

  /**
   * _1: Sender of query.
   * _2: Receiver of response
   * _3: Request body
   */
  type PendingQuery = (ActorRef, ActorRef, ReqlArg)

  import ReqlTcpConnection._
  import context.system

  IO(Tcp) ! Tcp.Connect(remote, pullMode = true)

  //---------------------------------------------------------------------------
  //
  // State
  //
  //---------------------------------------------------------------------------

  val tokenFactory = new AtomicLong()

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
    case StartQuery(query, optReceiver) ⇒
      val sndr = sender()
      val receiver = optReceiver.getOrElse(sndr)
      context watch receiver
      pendingQueries ::= (sndr, receiver, query)
    case c: Tcp.Connected ⇒
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
        case (sender, receiver, json) ⇒
          val token = tokenFactory.incrementAndGet()
          sender ! token
          startQuery(token, json)
          // Tel asker about query token
          queries(token) = receiver
      }
      pendingQueries = Nil
    case Tcp.CommandFailed(_: Tcp.Connect) ⇒
      context stop self
  }

  def operating: Receive = {
    case StartQuery(query, optReceiver) ⇒
      val receiver = optReceiver.getOrElse(sender())
      val token = tokenFactory.incrementAndGet()
      sender() ! token
      startQuery(token, query)
      context watch receiver
      queries(token) = receiver
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
  }

  override def unhandled(message: Any): Unit = message match {
    case Terminated(tcp) if tcpConnection.contains(tcp) ⇒
      context stop self
    case x @ Terminated(subject) ⇒
      queries.retain((k, v) ⇒ v != subject)
      pendingQueries = pendingQueries filter {
        case (sender, receiver, _) ⇒ receiver != subject
      }
    case x ⇒ super.unhandled(x)
  }

  def tryToUnwatchListener(refToUnwatch: ActorRef): Unit = {
    val exists = queries.exists { case (k, v) ⇒ v == refToUnwatch }
    if (!exists) context unwatch refToUnwatch
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

  private[reql] case object Ack extends Tcp.Event

  //------------------------------------------------
  // Events
  //------------------------------------------------

  sealed trait ReqlConnectionEvent

  case class Response(token: Long, data: Array[Byte])
    extends ReqlConnectionEvent

  //------------------------------------------------
  // Commands
  //------------------------------------------------

  sealed trait ReqlConnectionCommand

  case class ContinueQuery(token: Long)
    extends ReqlConnectionCommand

  case class StopQuery(token: Long)
    extends ReqlConnectionCommand

  case class StartQuery(query: ReqlArg, receiver: Option[ActorRef] = None)
    extends ReqlConnectionCommand

  case class ForgetQuery(token: Long)
    extends ReqlConnectionCommand

}
