assemblySettings

name := "predictor"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  "com.typesafe.slick" %% "slick" % "2.1.0-RC2", // TODO: use stable version when available
  "mysql" % "mysql-connector-java" % "5.1.30",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
)
    