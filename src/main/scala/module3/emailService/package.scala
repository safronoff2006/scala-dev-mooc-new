package module3
/*
import zio.Has
import zio.{UIO, URIO}
import zio.{ULayer, ZLayer}
import zio.console
import zio.ZIO
import zio.console.Console
import zio.macros.accessible


package object emailService {

    /**
     * Реализовать Сервис с одним методом sendEmail,
     * который будет принимать Email и отправлять его
     */

      type Foo = String
    // 1
    type EmailService = Has[EmailService.Service]

    object EmailService{

      // 2
      trait Service{
        def sendEmail(e: Email): URIO[zio.console.Console, Unit]
      }


      // 3
      val live: ULayer[EmailService] = ZLayer.succeed(new Service {
        override def sendEmail(e: Email): URIO[Console, Unit] =
          zio.console.putStrLn(e.toString)
      })

      def sendEmail(e: Email): URIO[EmailService with zio.console.Console, Unit] =
        ZIO.accessM(_.get.sendEmail(e))
    }


}
*/