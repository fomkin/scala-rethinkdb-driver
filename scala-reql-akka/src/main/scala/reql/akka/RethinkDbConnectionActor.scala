package reql.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import java.nio.ByteBuffer

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
  import context.system
  import context.dispatcher

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
        system.actorOf(props)
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

  case object Ack extends Event
  case class SendBytes(data: ByteString)

  val mapping = mutable.Map.empty[Long, ActorRef]
  var storage = Vector.empty[ByteString]
  var stored = 0L
  var transferred = 0L
  var closing = false

  val maxStored = 10000000L
  val highWatermark = maxStored * 5 / 10
  val lowWatermark = maxStored * 3 / 10
  var suspended = false

  def processCommand(command: ReqlConnectionCommand, sender: ActorRef): Unit = {
    command match {
      case StartQuery(token, query) =>
        mapping.put(command.token, sender)
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

  val writing: Receive = {
    case command: ReqlConnectionCommand =>
      processCommand(command, sender())
    case SendBytes(data) =>
      // When some data sent, push
      // all new outgoing packets to
      // storage
      buffer(data)
    case Received(data) => processData(data.toByteBuffer)
    case Ack => acknowledge()
    case PeerClosed => closing = true
  }

  val pending: Receive = {
    case command: ReqlConnectionCommand =>
      processCommand(command, sender())
    case SendBytes(data) =>
      buffer(data)
      tcpConnection ! Write(data, Ack)
      context.become(writing, discardOld = false)
    case Received(data) =>
      processData(data.toByteBuffer)
    case PeerClosed =>
      log.info("Connection closed by peer")
      mapping.values.foreach(_ ! ConnectionClosed)
      context stop self
  }

  def receive: Receive = pending

  private def buffer(data: ByteString): Unit = {
    storage :+= data
    stored += data.size

    if (stored > maxStored) {
      log.warning(s"Drop connection to Rethinkdb (buffer overrun)")
      context stop self
    } else if (stored > highWatermark) {
      log.debug(s"Suspending reading")
      tcpConnection ! SuspendReading
      suspended = true
    }
  }

  private def acknowledge(): Unit = {
    require(storage.nonEmpty, "storage was empty")

    val size = storage(0).size
    stored -= size
    transferred += size
    storage = storage drop 1

    if (suspended && stored < lowWatermark) {
      log.debug("Resuming reading")
      tcpConnection ! ResumeReading
      suspended = false
    }

    if (storage.isEmpty) {
      if (closing) {
        log.info("Connection closed by peer")
        mapping.values.foreach(_ ! ConnectionClosed)
        context stop self
      } else context.unbecome()
    } else {
      tcpConnection ! Write(storage(0), Ack)
    }
  }

  protected def sendBytes(bytes: ByteBuffer): Unit = {
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
      case None => log.warning("Received response for forgotten message")
    }
  }

  tcpConnection ! Register(self, keepOpenOnPeerClosed = true)
  sendBytes(createHandshakeBuffer(authKey))
  context watch tcpConnection
}
