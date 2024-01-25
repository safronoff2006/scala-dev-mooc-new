package http4smiddleware

import cats.effect.{IO, IOApp, Ref, Resource}
import org.http4s.{AuthedRequest, AuthedRoutes, Http, HttpRoutes, Method, Request, Status, Uri}
import org.http4s.Method.GET
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.{Host, Port}
import org.http4s.server.{AuthMiddleware, HttpMiddleware, Router}
import cats.Functor
import cats.data.{Kleisli, OptionT}
import cats.implicits.toSemigroupKOps
import org.typelevel.ci.CIStringSyntax

object Restfull {
  val service: HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "hello" / name => Ok("sadfknj")
    }

  val serviceOne: HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "hello1" / name => Ok("111")
    }

  val serviceTwo: HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "hello2" / name => Ok(name)
    }

  val router = addResponseHeaderMiddleware(Router("/" -> addResponseHeaderMiddleware(serviceOne), "/api" -> addResponseHeaderMiddleware(serviceTwo)))

  def routerSessions(sessions: Sessions[IO]) =
    addResponseHeaderMiddleware(Router("/" -> serviceSessions(sessions)))

  def routerSessionsAuth(sessions: Sessions[IO]) =
  // <+>
    addResponseHeaderMiddleware(Router("/" -> (loginService(sessions) <+> serviceAuth(sessions)(serviceHello))))

  def routerSessionsAuthClear(sessions: Sessions[IO]) =
    addResponseHeaderMiddleware(Router("/" -> (loginService(sessions) <+> serviceAuthMiddleware(sessions)(serviceHelloAuth))))


  //1. middleware in TG
  def addResponseHeaderMiddleware[F[_] : Functor](
                                                   routes: HttpRoutes[F]
                                                 ): HttpRoutes[F] = Kleisli { req =>
    val maybeResponse = routes(req)

    //    maybeResponse.map(resp => resp.putHeaders("X-Otux" -> "Hello"))

    maybeResponse.map {
      case Status.Successful(resp) => resp.putHeaders("X-Otux" -> "Hello")
      case other => other
    }
  }

  //2 Sessions
  type Sessions[F[_]] = Ref[F, Set[String]]

  def serviceSessions(sessions: Sessions[IO]): HttpRoutes[IO] =
    HttpRoutes.of {
      case r@GET -> Root / "hello" =>
        r.headers.get(ci"X-User-Name") match {
          case Some(values) =>
            val name = values.head.value
            sessions.get.flatMap(users =>
              if (users.contains(name)) Ok(s"Hello, $name")
              else Forbidden("no access")
            )
          case None => Forbidden("no access")
        }
      case PUT -> Root / "login" / name =>
        sessions.update(set => set + name).flatMap(_ => Ok("done"))

    }

  // Auth
  def serviceAuth(sessions: Sessions[IO]): HttpMiddleware[IO] =
    routes =>
      Kleisli { req =>
        req.headers.get(ci"X-User-Name") match {
          case Some(values) =>
            val name = values.head.value

            for {
              users <- OptionT.liftF(sessions.get)
              results <-
                if (users.contains(name)) routes(req)
                else OptionT.liftF(Forbidden("no access"))
            } yield results
          case None => OptionT.liftF(Forbidden("no access"))
        }
      }

  def serviceHello: HttpRoutes[IO] =
    HttpRoutes.of {
      case r@GET -> Root / "hello" =>
        r.headers.get(ci"X-User-Name") match {
          case Some(values) =>
            val name = values.head.value
            Ok(s"Hellol $name")
          case None => Forbidden("No access")
        }
    }

  def loginService(sessions: Sessions[IO]): HttpRoutes[IO] =
    HttpRoutes.of {
      case PUT -> Root / "login" / name =>
        sessions.update(set => set + name).flatMap(_ => Ok("done"))
    }


  val httpApp: Http[IO, IO] = service.orNotFound
  val server = for {
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(httpApp).build
  } yield s

  val server1 = for {
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(router.orNotFound).build
  } yield s

  val serverSessionsServer = for {
    sessions <- Resource.eval(Ref.of[IO, Set[String]](Set.empty))
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(routerSessions(sessions).orNotFound).build
  } yield s

  val serverSessionsAuthServer = for {
    sessions <- Resource.eval(Ref.of[IO, Set[String]](Set.empty))
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(routerSessionsAuth(sessions).orNotFound).build
  } yield s


  // clean refactoring, authmiddleware
  final case class User(name: String)

  def serviceHelloAuth: AuthedRoutes[User, IO] = AuthedRoutes.of {
    case GET -> Root / "hello" as user =>
      Ok(s"Hello, ${user.name}")
  }

  def serviceAuthMiddleware(sessions: Sessions[IO]): AuthMiddleware[IO, User] =
    autherRoutes =>
      Kleisli { req =>
        req.headers.get(ci"X-User-Name") match {
          case Some(values) =>
            val name = values.head.value

            for {
              users <- OptionT.liftF(sessions.get)
              results <-
                if (users.contains(name)) autherRoutes(AuthedRequest(User(name), req))
                else OptionT.liftF(Forbidden("no access"))
            } yield {
              results
            }
          case None => OptionT.liftF(Forbidden("no access"))
        }
      }

  val serverSessionsAuthServerClear = for {
    sessions <- Resource.eval(Ref.of[IO, Set[String]](Set.empty))
    s <- EmberServerBuilder
      .default[IO]
      .withPort(Port.fromInt(8080).get)
      .withHost(Host.fromString("localhost").get)
      .withHttpApp(routerSessionsAuthClear(sessions).orNotFound).build
  } yield s


}

object mainServer extends  IOApp.Simple {
  def run(): IO[Unit] = {
    //Restfull.server1.use(_ => IO.never)
    //Restfull.serverSessionsServer.use( _ => IO.never)
//    Restfull.serverSessionsAuthServer.use( _ => IO.never)
    Restfull.serverSessionsAuthServerClear.use( _ => IO.never)
  }
}


//tests
object  Test extends IOApp.Simple {

  def run: IO[Unit] = {
    val service = Restfull.serviceHelloAuth

    for {
      result <- service(AuthedRequest(Restfull.User("abc"), Request(method = Method.GET,
        uri = Uri.fromString("/hello").toOption.get))).value
      _ <- result match {
        case Some(resp) =>
          resp.bodyText.compile.last.flatMap(body => IO.println(resp.status.isSuccess) *>
          IO.println(body))
        case None => IO.println("fail")
      }

    }yield()


  }

}