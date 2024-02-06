package module3
/*
import zio.{Has, RIO, URLayer, ZIO, ZLayer, console}
import zio.macros.accessible
import emailService.{Email, EmailAddress, EmailService, Html}
import module3.userDAO.UserDAO
import zio.console.Console

package object userService {

  /**
   * Реализовать сервис с одним методом
   * notifyUser, принимает id пользователя в качестве аргумента и шлет ему уведомление
   * при реализации использовать UserDAO и EmailService
   */
  // 1
  type UserService = Has[UserService.Service]

  @accessible
  object UserService{
    //2
    trait Service{
      def notifyUser(id: UserID): RIO[EmailService with Console, Unit]
    }

    class ServiceImpl(userDAO: UserDAO.Service) extends Service{
      override def notifyUser(id: UserID): RIO[EmailService with Console, Unit] = for{
        user <- userDAO.findBy(id).some.orElseFail(new Throwable("User not found"))
        email = Email(user.email, Html("Hello here"))
//        emailService <- ZIO.environment[EmailService].map(_.get)
//        _ <- ZIO.accessM[EmailService with Console](_.get.sendEmail(email))
        _ <- EmailService.sendEmail(email)
      } yield ()
    }

    // 3
    val live: URLayer[UserDAO, UserService] =
      ZLayer.fromService[UserDAO.Service, UserService.Service](uDao =>
      new ServiceImpl(uDao)
    )
  }



}
*/