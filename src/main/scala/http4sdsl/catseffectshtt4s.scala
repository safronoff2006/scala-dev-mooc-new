package catshttp4sdsl

import cats.effect.{IO, IOApp}
import org.http4s.{Http, HttpRoutes}
import org.http4s.Method.GET
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.{Host, Port}
import org.http4s.server.Router


object Restfull{
  val service: HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "hello" / name => Ok("sadfknj")
    }

  val serviceOne: HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "hello1"/ name => Ok("111")
    }

  val serviceTwo: HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "hello2"/ name => Ok(name)
    }

  val router = Router("/" -> serviceOne, "/api" -> serviceTwo )

  val httpApp: Http[IO, IO] = service.orNotFound
  val server = for {
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(httpApp).build
  } yield  s

  val server1 = for {
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(router.orNotFound).build
  } yield s
}

object mainServer extends  IOApp.Simple {
  def run(): IO[Unit] = {
    Restfull.server1.use(_ => IO.never)

  }
}