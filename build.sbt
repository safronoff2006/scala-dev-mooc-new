
scalaVersion := "2.13.12"

name := "scala-dev-mooc-2023-09"
organization := "ru.otus"
version := "1.0"

libraryDependencies += Dependencies.scalaTest
libraryDependencies ++= Dependencies.cats
libraryDependencies ++= Dependencies.zio
libraryDependencies ++= Dependencies.zioConfig
libraryDependencies ++= Dependencies.fs2
libraryDependencies ++= Dependencies.http4s
libraryDependencies += Dependencies.zioHttp
libraryDependencies += Dependencies.liquibase
libraryDependencies += Dependencies.postgres
libraryDependencies += Dependencies.logback
libraryDependencies ++= Dependencies.quill
libraryDependencies ++= Dependencies.testContainers
libraryDependencies ++= Dependencies.circe

scalacOptions += "-Ymacro-annotations"
