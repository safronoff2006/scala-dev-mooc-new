package module3
/*
import module3.emailService.EmailService
import module3.userDAO.UserDAO
import module3.userService.{UserID, UserService}
import zio.console.Console
import zio.{Has, ZIO, ZLayer}


object buildingZIOServices{

  val app: ZIO[UserService with EmailService with Console, Throwable, Unit] =
    UserService.notifyUser(UserID(10))

  val env: ZLayer[Any, Throwable, UserService with EmailService] =
    UserDAO.live >>> UserService.live ++ EmailService.live

  def main(args: Array[String]): Unit = {
    zio.Runtime.default.unsafeRun(app.provideSomeLayer[Console](env))
  }
}

 */