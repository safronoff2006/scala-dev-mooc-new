package module4.homework.services


import module4.homework.dao.entity.{Role, RoleCode, User, UserId}
import module4.homework.dao.repository.UserRepository
import module4.homework.dao.repository.UserRepository.UserRepository
import module4.phoneBook.db
import zio.macros.accessible
import zio.{Has, RIO, URLayer, ZIO, ZLayer}

import java.sql.SQLException
import javax.sql.DataSource


@accessible
object UserService{
    type UserService = Has[Service]

    trait Service{
        def listUsers(): RIO[db.DataSource, List[User]]
        def listUsersDTO(): RIO[db.DataSource, List[UserDTO]]
        def addUserWithRole(user: User, roleCode: RoleCode): RIO[db.DataSource, UserDTO]
        def listUsersWithRole(roleCode: RoleCode): RIO[db.DataSource, List[UserDTO]]

    }

    class Impl(userRepo: UserRepository.Service) extends Service{
        val dc = db.Ctx
        import dc._
        

        def listUsers(): RIO[db.DataSource, List[User]] =
        userRepo.list()


        def listUsersDTO(): RIO[db.DataSource, List[UserDTO]] = this.listUsers().flatMap {
              listusers =>
                 val listofZio: List[ZIO[Has[DataSource], SQLException, UserDTO]] =  listusers.map{
                     user => userRepo.userRoles(UserId(user.id))
                       .map{
                         listroles =>
                            listroles.foldLeft(Set[Role]())((acc,role) => acc + role)
                        }
                       .map( setroles  =>  UserDTO(user,setroles))
                 }

                ZIO.collectAll(listofZio)

          }
            

      def addUserWithRole(user: User, roleCode: RoleCode): RIO[db.DataSource,  UserDTO] = for {
          dto: UserDTO <- userRepo.findRoleByCode(roleCode).flatMap {
            optRole =>
              val optUserDTO: Option[UserDTO] = optRole.map(role => UserDTO(user, Set(role)))
              ZIO.fromOption(optUserDTO).orElseFail(new Throwable("Role not found"))

          }
          _ <- transaction(
            for {
              _ <- userRepo.createUser(user)
              _ <- userRepo.insertRoleToUser(roleCode, UserId(user.id))
            } yield ()
          )

        } yield dto

//RIO[db.DataSource, List[UserDTO]]
      def listUsersWithRole(roleCode: RoleCode): RIO[db.DataSource, List[UserDTO]] = {
       val zio: ZIO[Has[DataSource], Serializable, List[UserDTO]] = for {

          role <- userRepo.findRoleByCode(roleCode).some

          list <- userRepo.listUsersWithRole(roleCode).map {
            users =>
              users.map {
                user => UserDTO(user, Set(role))
              }
          }

        } yield list

       zio.orElseFail(new Throwable("Role not found"))
      }


        
        
    }

    val live: URLayer[UserRepository, UserService] = ZLayer.fromService[UserRepository.Service, UserService.Service](uRepo =>
      new Impl(uRepo)
    )
}

case class UserDTO(user: User, roles: Set[Role])