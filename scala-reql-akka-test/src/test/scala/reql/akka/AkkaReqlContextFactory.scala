package reql.akka

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import pushka.Ast
import reql.ReqlContextFactory
import reql.pushka.PushkaReqlContext

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

object AkkaReqlContextFactory extends ReqlContextFactory {

  private lazy val system: ActorSystem = ActorSystem("akka-reql-context-factory")
  private lazy val dbConnection: ActorRef = {
    val addr = extractAddress(System.getenv("RETHINKDB_SERVER"))
    system.actorOf(RethinkDbConnectionActor.props(addr))
  }

  override def init(): Unit = {
    dbConnection
  }

  def run[T](f: PushkaReqlContext ⇒ Future[T])(implicit ct: ClassTag[T], timeout: FiniteDuration): Future[T] = {
    val worker = system.actorOf(Props(classOf[TestReqlContext], dbConnection))
    worker.ask(Run(f))(timeout).mapTo[T]
  }

  override def destroy(): Unit = {
    system.stop(dbConnection)
    system.terminate()
  }

  private def extractAddress(s: String): InetSocketAddress = {
    val Array(hostname, port) = s.split(":")
    new InetSocketAddress(hostname, port.toInt)
  }

  private case class Run(f: PushkaReqlContext ⇒ Future[Any])
  private class TestReqlContext(val dbConnection: ActorRef) extends ReqlActor[Ast] with PushkaReqlContext {

    import context.dispatcher

    override def queryTimeout: Timeout = ???
    override def receive: Receive = {
      case Run(f) ⇒ f(this) pipeTo sender()
    }
  }
}
