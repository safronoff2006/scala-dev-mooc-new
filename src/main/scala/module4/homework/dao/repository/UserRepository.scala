package module4.homework.dao.repository

import io.getquill.context.ZioJdbc._
import module4.homework.dao.entity._
import module4.phoneBook.db
import zio.{Has, ULayer, ZLayer}


object UserRepository{


    val dc = db.Ctx
    import dc._

    type UserRepository = Has[Service]

    trait Service{
        def findUser(userId: UserId): QIO[Option[User]]
        def createUser(user: User): QIO[User]
        def createUsers(users: List[User]): QIO[List[User]]
        def updateUser(user: User): QIO[Unit]
        def deleteUser(user: User): QIO[Unit]
        def findByLastName(lastName: String): QIO[List[User]]
        def list(): QIO[List[User]]
        def userRoles(userId: UserId): QIO[List[Role]]
        def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit]
        def listUsersWithRole(roleCode: RoleCode): QIO[List[User]]
        def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]]
    }

    class ServiceImpl extends Service{

        lazy val userSchema = quote {
            querySchema[User](""""User"""")
        }


        lazy val roleSchema = quote {
            querySchema[Role](""""Role"""")
        }

        lazy val userToRoleSchema = quote {
            querySchema[UserToRole](""""UserToRole"""")
        }

        def findUser(userId: UserId): QIO[Option[User]] = run(
            userSchema.filter(_.id == lift(userId.id)).sortBy(_.id).take(1))
          .map(_.headOption)
        
        def createUser(user: User): QIO[User] = run(userSchema.insert(lift(user))).as(user)

        def createUsers(users: List[User]): QIO[List[User]] = run(
            liftQuery(users).foreach { u =>
              userSchema.insert(u)
            }).as(users)

        def updateUser(user: User): QIO[Unit] = run(
            userSchema.filter(_.id == lift(user.id)).update(lift(user))
        ).unit

        def deleteUser(user: User): QIO[Unit] = run(
            userSchema.filter(_.id == lift(user.id)).delete
        ).unit

        def findByLastName(lastName: String): QIO[List[User]] = run(
            userSchema.filter(_.lastName == lift(lastName))
        )

        def list(): QIO[List[User]] = run(
            userSchema
        )


        def userRoles(userId: UserId): QIO[List[Role]] = run(
          userToRoleSchema.filter(_.userId == lift(userId.id)).join(roleSchema).on(_.roleId == _.code).map{ par => par._2 }
        )
        
        def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit] = run(
            userToRoleSchema.insert(lift(UserToRole(roleCode.code, userId.id)))
        ).unit


        def listUsersWithRole(roleCode: RoleCode): QIO[List[User]] = run(
            userToRoleSchema.filter(_.roleId == lift(roleCode.code)).join(userSchema).on(_.userId == _.id).map{ par => par._2}
        )


        def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]] = run(
            roleSchema.filter(_.code == lift(roleCode.code))
        ).map(_.headOption)

    }

    val live: ULayer[UserRepository] = ZLayer.succeed( new ServiceImpl )
}