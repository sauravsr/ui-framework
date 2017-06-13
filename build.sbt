name := """play-reactive-mongo-db"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.2"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


scalacOptions in ThisBuild ++= Seq("-feature", "-language:postfixOps")