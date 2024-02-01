package module3

import zio.Has
import zio.Task
import userService.{User, UserID}
import zio.{ZLayer, ULayer}

package object userDAO {

    /**
     * Реализовать сервис с двумя методами
     *  1. list - список всех пользователей
     *  2. findBy - поиск по User ID
     */

    //1
    type UserDAO = Has[UserDAO.Service]

    object UserDAO{
      // 2
      trait Service{
        def list(): Task[List[User]]
        def findBy(id: UserID): Task[Option[User]]
      }

      //3
      val live: ULayer[UserDAO] = ZLayer.succeed(new Service {
        override def list(): Task[List[User]] = ???

        override def findBy(id: UserID): Task[Option[User]] = ???
      })
    }


}
