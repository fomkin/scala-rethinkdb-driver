package reql

import reql.pushka.PushkaReqlContext

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

trait ReqlContextFactory {
  def init(): Unit
  def destroy(): Unit

  def run[T](f: PushkaReqlContext ⇒ Future[T])(implicit ct: ClassTag[T], timeout: FiniteDuration): Future[T]
}
