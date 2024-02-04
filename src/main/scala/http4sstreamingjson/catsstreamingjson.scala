package http4sstreamingjson

import cats.effect
import cats.effect.{IO, IOApp, Resource}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Request, Response, Uri}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import http4smiddleware.Restfull


object HttpClient {
  val builder: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  val request = Request[IO](uri = Uri.fromString("http://localhost:8080/hello").toOption.get)

  //1
/*  val result: Resource[IO, Response[IO]] = for {
    client <- builder
    response <- client.run(request)
  } yield response
  */

  //2
/*  val result: Resource[IO, String] = for {
    client <- builder
    response <-  effect.Resource.eval(client.expect[String](request))
  } yield response */

  val result = builder.use(
    client => client.run(request).use(
      resp => if (!resp.status.isSuccess)
        resp.body.compile.to(Array).map(new String(_))
      else
        IO("Error")
    )
  )
}

object mainServer extends IOApp.Simple {
  def run(): IO[Unit] = {
    // for 1 and 2
/*    for {
      fiber <- Restfull.serverSessionsAuthServerClear.use(_ => IO.never).start
      _ <- HttpClient.result.use(IO.println)
      _ <- fiber.join
    } yield()*/

    for {
      _ <- Restfull.serverSessionsAuthServerClear.use(_ => HttpClient.result.flatMap(IO.println) *> IO.never)
    } yield()

  }

}
