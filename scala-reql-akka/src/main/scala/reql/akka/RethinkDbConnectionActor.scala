package reql.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import reql.dsl.ReqlArg
import reql.protocol.RethinkDbConnection

import scala.collection.mutable
import scala.concurrent.duration._

object RethinkDbConnectionActor {

  // Events

  sealed trait ReqlConnectionEvent
  case class Response(token: Long, data: Array[Byte])
      extends ReqlConnectionEvent
  case object ConnectionClosed extends ReqlConnectionEvent

  //------------------------------------------------
  // Commands
  //------------------------------------------------

  sealed trait ReqlConnectionCommand {
    def token: Long
  }

  case class ForgetQuery(token: Long) extends ReqlConnectionCommand
  case class ContinueQuery(token: Long) extends ReqlConnectionCommand
  case class StopQuery(token: Long) extends ReqlConnectionCommand
  case class StartQuery(token: Long, query: ReqlArg)
      extends ReqlConnectionCommand

  def props(remote: InetSocketAddress,
            authKey: Option[String] = None,
            reconnectDelay: FiniteDuration = 5 seconds) = {
    Props(classOf[RethinkDbConnectionActor],
          remote,
          authKey,
          reconnectDelay)
  }
}

/**
  * Establish TCP connection to RethinkDB. Use RethinkDbConnectionActor
  * protocol to execute queries and process results
  *
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
class RethinkDbConnectionActor(remote: InetSocketAddress,
                        authKey: Option[String],
                        reconnectDelay: FiniteDuration)
    extends Actor
    with ActorLogging {

  import Tcp._
  import RethinkDbConnectionActor._
  import context.{dispatcher, system}

  var pendingCommands = List.empty[(ReqlConnectionCommand, ActorRef)]
  var workers = List.empty[ActorRef]
  var connectionAttempt = 0

  def establishConnection(firstTime: Boolean = false): Unit = {
    connectionAttempt += 1
    // Every attempt have double delay than previous
    val delay =
      if (firstTime) 0 seconds
      else reconnectDelay * connectionAttempt
    log.info(s"Try to establish RethinkDB connection in $delay")
    system.scheduler.scheduleOnce(delay) {
      IO(Tcp) ! Connect(remote)
    }
  }

  def receive = {
    case command: ReqlConnectionCommand =>
      if (workers.nonEmpty) passCommand(command, sender())
      else pendingCommands ::= (command, sender())
    case CommandFailed(_: Connect) =>
      log.error("Unable to connected.")
      establishConnection()
    case _: Connected =>
      val tcpConnection = sender()
      val worker = {
        val props = RethinkDbConnectionWorkerActor.props(
            authKey,
            tcpConnection
        )
        system.actorOf(props, "connection-worker")
      }
      // Reset connection attempts counter
      connectionAttempt = 0
      // Append worker
      context.watch(worker)
      workers = worker :: workers
      for ((command, sender) <- pendingCommands) {
        worker.tell(command, sender)
      }
      pendingCommands = Nil
    case Terminated(child) =>
      if (workers.contains(child)) {
        workers = workers.filterNot(_ == child)
        establishConnection()
      }
  }

  def passCommand(command: ReqlConnectionCommand, sender: ActorRef): Unit = {
    workers foreach { worker =>
      worker.tell(command, sender)
    }
  }

  // Try to connect to RethinkDB
  establishConnection(firstTime = true)
}

object RethinkDbConnectionWorkerActor {
  def props(authKey: Option[String], tcpConnection: ActorRef): Props = {
    Props(classOf[RethinkDbConnectionWorkerActor], authKey, tcpConnection)
  }
}

private class RethinkDbConnectionWorkerActor(
    authKey: Option[String], tcpConnection: ActorRef)
    extends Actor
    with ActorLogging
    with RethinkDbConnection {

  import Tcp._
  import RethinkDbConnectionActor._

  case class Ack(id: Long) extends Event
  case class SendBytes(data: ByteString)

  val mapping = mutable.Map.empty[Long, ActorRef]
  val storage = new mutable.Queue[ByteString]()

  var closing = false

  def processCommand(command: ReqlConnectionCommand, sender: ActorRef): Unit = {
    command match {
      case StartQuery(token, query) =>
        mapping.put(token, sender)
        startQuery(token, query)
      case StopQuery(token) =>
        mapping.remove(token)
        stopQuery(token)
      case ContinueQuery(token) =>
        continueQuery(token)
      case ForgetQuery(token) =>
        mapping.remove(token)
    }
  }

  var counter: Long = 0
  def receive: Receive = {
    case command: ReqlConnectionCommand =>
      processCommand(command, sender())
    case SendBytes(data) if storage.isEmpty =>
      storage.enqueue(data)
      tcpConnection ! Write(data, Ack(counter))
    case SendBytes(data) => storage.enqueue(data)
    case ack: Ack => acknowledge(ack)
    case Received(data) => processData(data.toByteBuffer)
    case PeerClosed =>
      log.info("Connection closed by peer")
      mapping.values.foreach(_ ! ConnectionClosed)
      context stop self
    case Terminated(x) ⇒ log.error(s"$x is terminated")
  }

  private def acknowledge(ack: Ack): Unit = {
    if (ack.id == counter) {
      storage.dequeue()
      counter += 1

      storage.headOption match {
        case Some(nextData) ⇒ tcpConnection ! Write(nextData, Ack(counter))
        case None if closing ⇒
          log.info("Connection closed by peer")
          mapping.values.foreach(_ ! ConnectionClosed)
          context stop self
        case _ ⇒
      }
    } else if (ack.id > counter) {
      log.warning(s"Expected ack with id ${ack.id}, but got $counter. Ignoring.")
    } // Ignore old queries acknowledge: ack.id < counter
  }

  protected def sendBytes(bytes: ByteBuffer): Unit = {
    bytes.position(0)
    val data = ByteString(bytes)
    self ! SendBytes(data)
  }

  protected def onFatalError(message: String): Unit = {
    log.error(message)
    context.stop(self)
  }

  protected def onResponse(token: Long, data: Array[Byte]): Unit = {
    mapping.get(token) match {
      case Some(receiver) =>
        receiver ! Response(token, data)
      case None =>
        // Sometimes https://github.com/rethinkdb/rethinkdb/issues/3296#issuecomment-79975399 is occurred
        log.warning(s"Received response for forgotten message: $token, ${new String(data, StandardCharsets.UTF_8)}")
    }
  }

  tcpConnection ! Register(self, keepOpenOnPeerClosed = true)
  sendBytes(createHandshakeBuffer(authKey))
  context watch tcpConnection
}
