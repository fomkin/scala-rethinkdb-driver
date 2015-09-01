package reql.akka

import java.net.InetSocketAddress
import java.nio.ByteBuffer

import akka.actor.{Actor, ActorRef, Terminated}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import reql.dsl.{ReqlArg, Json}
import reql.protocol._

class ReqlTcpConnection(remote: InetSocketAddress = new InetSocketAddress("localhost", 28015),
                        authKey: Option[String] = None)

  extends Actor with ReqlConnection {

  /**
   * _1: Sender of query. Receiver of response
   * _2: Request body
   */
  type Query = (ActorRef, Json)

  import ReqlTcpConnection._
  import context.system

  IO(Tcp) ! Tcp.Connect(remote, pullMode = true)

  //---------------------------------------------------------------------------
  //
  // State
  //
  //---------------------------------------------------------------------------

  val queries = scala.collection.mutable.Map.empty[Long, ActorRef]

  var pendingQueries = List.empty[Query]

  var tcpConnection = Option.empty[ActorRef]

  //---------------------------------------------------------------------------
  //
  // Receiving
  //
  //---------------------------------------------------------------------------

  def receive: Receive = connecting

  def connecting: Receive = {
    case StartQuery(arg) ⇒
      context watch sender()
      pendingQueries ::= (sender(), arg.ast)
    case c: Tcp.Connected ⇒
      val handshakeBuffer = createHandshakeBuffer(authKey)
      val connection = sender()
      this.tcpConnection = Some(connection)
      context watch connection
      context become operating
      connection ! Tcp.Register(self)
      connection ! Tcp.Write(ByteString(handshakeBuffer), ReqlTcpConnection.Ack)
      // Send pending queries
      pendingQueries foreach {
        case (sender, json) ⇒
          queries(startQuery(json)) = sender
      }
      pendingQueries = Nil
    case Tcp.CommandFailed(_: Tcp.Connect) ⇒
      context stop self
  }

  def operating: Receive = {
    case c: StartQuery ⇒
      queries(startQuery(c.data.ast)) = sender()
      context watch sender()
    case StopQuery(queryToken) ⇒
      queries.remove(queryToken) foreach { refToUnwatch ⇒
        tryToUnwatchListener(refToUnwatch)
      }
    case Tcp.Received(data) ⇒
      processData(data.asByteBuffer)
    case ReqlTcpConnection.Ack ⇒
      tcpConnection foreach (_ ! Tcp.ResumeReading)
  }

  override def unhandled(message: Any): Unit = message match {
    case Terminated(tcp) if tcpConnection.contains(tcp) ⇒
      context stop self
    case Terminated(subject) ⇒
      queries.retain((k, v) ⇒ v != subject)
      pendingQueries = pendingQueries filter {
        case (ref, _) ⇒ ref != subject
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

  protected def onResponse(queryToken: Long, responseType: ReqlResponseType, json: Json): Unit = {
    queries.get(queryToken) foreach { ref ⇒
      ref ! Response(queryToken, responseType, json)
      if (responseType != ReqlResponseType.SuccessPartial) {
        queries.remove(queryToken)
        tryToUnwatchListener(ref)
      } else {
        continueQuery(queryToken)
      }
    }
  }
}

object ReqlTcpConnection {

  private[reql] case object Ack extends Tcp.Event

  sealed trait ReqlConnectionEvent

  sealed trait ReqlConnectionCommand

  case class Response(queryToken: Long,
                      reqlResponseType: ReqlResponseType,
                      json: Json) extends ReqlConnectionEvent

  case class StopQuery(queryToken: Long) extends ReqlConnectionCommand

  case class StartQuery(data: ReqlArg) extends ReqlConnectionCommand
}
