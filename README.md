### Scala RethinkDB Driver

The idea is to create pure and simple RethinkDB API implementation which
can be run inside different Scala environments. It means that protocol
implementation and query eDSL are not depend on thirdparty libraries.
It may allow to use library inside Akka, Scalaz and Scala.js based projects.

Implementation details

 * Functions support
 * Cursors support
 * Full asynchronous
 * Zero-dependency core 
 * Type safe API is not planned

Not implemented

 * Part of API
 * Pathspecs
 
### Akka and Pushka example

Add dependencies to your project. Note that you 
cant download `scala-reql-akka` from Maven Central. You
should clone this repository and make `sbt publish-local`
first.
 
```scala
libraryDependencies ++= Seq(
  "com.github.fomkin" %% "scala-reql-akka" % "0.1.0-SNAPSHOT",
  "com.github.fomkin" %% "pushka-json" % "0.2.0",
)
```

Create actor system and connection to RethinkDB
 
```scala
class Queries(val dbConnection: ActorRef) 
  extends ReqlActor[Ast] with PushkaReqlContext {

  // Timeout for queries execution
  val queryTimeout: Timeout = 2 seconds

  r.db("test").table("animals").get("cow").runA {
    case Right(cow) => // work with cow
    case Left(error) => // process error  
  }
}

val system = new ActorSystem("rethinkdb-client")
val dbConnection = system.actorOf(Props[ReqlTcpConnection])
```

There are three different ways to run query

Run atomic. Do not scare modify actors state
inside lambda. It runs in same thread as
receive.

```scala
r.db("test").table("animals").get("cow").runA {
  case Right(cow) => // work with cow
  case Left(error) => // process error  
}
```

Run cursor. Like atomic cursors lambda runs
in same thread as receive.

```scala
r.db("test").table("animals").changes().runC { cursor =>
  cursor.foreach { animal => 
    // Work with update
  }
}
```

Run atomic and get future. 

```scala
r.db("test").table("animals").get("cow").runA pipeTo sender()
```
