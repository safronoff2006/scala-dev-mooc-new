package http4sdsl

import cats.data.{EitherT, Kleisli, OptionT, ReaderT}
import cats.effect.IO
import cats.effect.unsafe.implicits.global


object catshttp4dsl {
  def getUserName: IO[Option[String]] = IO.pure(Some("sdf"))
  def getId(name: String): IO[Option[Int]] = IO.raiseError(new Exception("sdf"))
  def getPermissions(int: Int): IO[Option[String]] = IO.pure(Some("Permissions"))

/*  def compose[F[_]: Functor, G[_]: Functor] : Functor[X=>F[G[X]]] = new Functor[X=> F[G[X]]] {
    override def map[A, B](fa:F[G[A]])(f:A=>B): F[G[B]] =
      Functor[F].map(ga => Functor[G].map(....))???
  }
*/


  def main(args: Array[String]) : Unit = {
    implicit  val ec = scala.concurrent.ExecutionContext.Implicits.global

   /* val res = for {
      username <- getUserName
      id <- getId(username)
    } yield ()*/

    val res: OptionT[IO, (String, Int, String)] = for {
      username <- OptionT(getUserName)
      id <- OptionT(getId(username))
      permissions <- OptionT(getPermissions(id))
    } yield (username, id, permissions)

 //   println(res.value.unsafeRunSync())

    def getUserName1: IO[Option[String]] = IO.pure(Some("sdf"))
    def getId1(name: String): IO[Int] = IO.pure(42)
    def getPermissions1(int: Int): IO[Option[String]] = IO.pure(Some("Permissions"))

    val res1: OptionT[IO, (String, Int, String)] = for {
      username <- OptionT(getUserName1)
      id <-  OptionT.liftF(getId1(username))
      permissions <- OptionT(getPermissions1(id))
    } yield (username,id, permissions)

//    println(res1.value.unsafeRunSync())

    //EitherT
    sealed trait UserServiceError
    case class PermissionDenied(msg: String) extends UserServiceError

    def getUserName2(userid: Int): EitherT[IO, UserServiceError, String] =
      EitherT.pure("sdf")

    def getUserAddress(userId:Int): EitherT[IO, UserServiceError, String] = {
      EitherT.fromEither(Right("bla bla bla"))
    }

    def getProfile(id:Int) = for {
      name <- getUserName2(id)
      address <- getUserAddress(id)
    } yield (name, address)

    println(getProfile(2).value.unsafeRunSync())

    /*
    val f: Int => Option[String] =  ???
    val kleisli = Kleisli(f)
    val g: String => Option[Double] = ???
    val composed = kleisli.andThen(g)

    val result = composed.run(3)
    */

    //ReaderT
    trait ConnectionPool
    case class Environment(cp: ConnectionPool)

    def getUserAlias(id: Int): ReaderT[IO, Environment, String] = ReaderT(cp => IO.pure("zdjkf"))
    def getComment(id: Int): ReaderT[IO, Environment, String] = ReaderT.liftF(IO.pure("sjhdgvjaes"))
    def updateComment(id:Int, text:String): ReaderT[IO,Environment,Unit] = ReaderT.liftF(IO.println("updated"))


    val result = for {
      a <- getUserAlias(1)
      b <- getComment(1)
      _ <- updateComment(1, "adf")
    } yield (a,b)
    println(result(Environment(new ConnectionPool {})).unsafeRunSync())





  }


}