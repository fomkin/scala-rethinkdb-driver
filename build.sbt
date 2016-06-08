val commonSettings = Seq(
  organization := "com.github.fomkin",
  version := "0.3.5-SNAPSHOT",
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-language:postfixOps",
    "-language:implicitConversions"
  ),
  publishTo := {
    isSnapshot.value match {
      case true => Some("iDecide Snapshots" at "https://nexus.flexis.ru/content/repositories/snapshots")
      case false => Some("iDecide Releases" at "https://nexus.flexis.ru/content/repositories/releases")
    }
  }
)

val `scala-reql-core` = crossProject.crossType(CrossType.Pure).
  settings(commonSettings:_*).
  settings(sourceGenerators in Compile <+= (sourceDirectory in Compile).map { f ⇒ ApiGenerator(f) } )

lazy val `scala-reql-core-js` = `scala-reql-core`.js
lazy val `scala-reql-core-jvm` = `scala-reql-core`.jvm

val `scala-reql-pushka` = crossProject.crossType(CrossType.Pure).
  dependsOn(`scala-reql-core`).
  settings(commonSettings:_*).
  settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies += "com.github.fomkin" %%% "pushka-json" % "0.4.1"
  )

lazy val `scala-reql-pushka-js` = `scala-reql-pushka`.js
lazy val `scala-reql-pushka-jvm` = `scala-reql-pushka`.jvm

lazy val `scala-reql-akka` = project.
  dependsOn(`scala-reql-core-jvm`).
  settings(commonSettings:_*).
  settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.2"
    )
  )

lazy val `scala-reql-akka-test` = project.
  dependsOn(`scala-reql-akka`, `scala-reql-pushka-jvm`).
  settings(commonSettings:_*).
  settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0-RC1" % "test"
  )

publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))

publishArtifact := false
