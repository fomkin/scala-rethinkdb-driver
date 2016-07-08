package reql.akka

import reql.ReqlContextSpec

class ReqlActorSpec extends ReqlContextSpec(AkkaReqlContextFactory) {
  "test" in withContext { context ⇒
    import context._

    val query = r.db("test").tableList
    query.runC.map { xs ⇒
      println(s"Tables is: $xs")
      assert(true)
    }
  }
}
