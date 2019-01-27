name := "fs2"

version := "0.1"

scalaVersion := "2.12.8"

resolvers += Resolver.sonatypeRepo("snapshots")

val http4sVersion = "0.20.0-M5"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification")

libraryDependencies += "co.fs2" %% "fs2-core" % "1.0.1"
libraryDependencies += "co.fs2" %% "fs2-io" % "1.0.2"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.1.0"

libraryDependencies += "org.http4s" %% "http4s-dsl" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-blaze-server" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % http4sVersion
