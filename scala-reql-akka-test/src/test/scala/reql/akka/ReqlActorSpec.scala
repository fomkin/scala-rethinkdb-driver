package reql.akka

import reql.ReqlContextSpec

import scala.concurrent.Future

class ReqlActorSpec extends ReqlContextSpec(AkkaReqlContextFactory) {
  "test" in withContext { context ⇒
    import context._
    import context.toRunOps

    context.runAtomQuery()
    Future.successful {
      assert(true == true)
    }
  }
}
