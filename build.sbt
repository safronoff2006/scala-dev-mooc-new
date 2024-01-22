
scalaVersion := "2.13.8"

name := "scala-dev-mooc-2023-09"
organization := "ru.otus"
version := "1.0"

libraryDependencies += Dependencies.scalaTest
libraryDependencies ++= Dependencies.cats
libraryDependencies ++= Dependencies.zio
libraryDependencies ++= Dependencies.zioConfig
libraryDependencies ++= Dependencies.fs2
libraryDependencies ++= Dependencies.http4s

scalacOptions += "-Ymacro-annotations"
