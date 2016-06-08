package reql

import org.scalatest._
import reql.pushka.PushkaReqlContext

import scala.concurrent.Future
import scala.concurrent.duration._

abstract class ReqlContextSpec(protected val contextFactory: ReqlContextFactory)
  extends AsyncFreeSpecLike with BeforeAndAfterAll {

  def withContext(f: PushkaReqlContext ⇒ Future[Assertion]): Future[Assertion] = {
    implicit val timeout = 5.seconds
    contextFactory.run(f)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    contextFactory.init()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    contextFactory.destroy()
  }
}
