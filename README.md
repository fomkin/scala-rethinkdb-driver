### Scala RethinkDB Driver

The idea is to create pure and simple RethinkDB API implementation which
can be run inside different Scala environments. It means that protocol
implementation and query eDSL are not depend on thirdparty libraries.
It may allow to use library inside Akka, Scalaz and Scala.js based projects.

***Work in progress***

Implementation details

 * Reql eDSL generation
 * Abstract connection
 * Function and Pathspecs are not implemented now
 * Type safe API is not planned
