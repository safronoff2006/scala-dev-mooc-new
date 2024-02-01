package module4.phoneBook.dao.repositories

import io.getquill.context.ZioJdbc._
import module4.phoneBook.dao.entities._
import module4.phoneBook.db
import zio.{Has, ULayer, ZLayer}
import io.getquill.Ord

object PhoneRecordRepository {
  val ctx = db.Ctx
  import ctx._

  type PhoneRecordRepository = Has[Service]

  trait Service{
      def find(phone: String): QIO[Option[PhoneRecord]]
      def list(): QIO[List[PhoneRecord]]
      def insert(phoneRecord: PhoneRecord): QIO[Unit]
      def update(phoneRecord: PhoneRecord): QIO[Unit]
      def delete(id: String): QIO[Unit]
  }

  class Impl extends Service{
    val phoneRecordSchema = quote{
      querySchema[PhoneRecord]("""PhoneRecord""")
    }

    val addressSchema = quote{
      querySchema[Address]("""Address""")
    }

    // SELECT x1."id", x1."phone", x1."fio", x1."addressId" FROM PhoneRecord x1 WHERE x1."phone" = ?
    // SELECT x1."id", x1."phone", x1."fio", x1."addressId" FROM PhoneRecord x1 WHERE x1."phone" = ? 
    // ORDER BY x1."phone" ASC NULLS FIRST LIMIT 1
    def find(phone: String): QIO[Option[PhoneRecord]] = 
      run(phoneRecordSchema.filter(_.phone == lift(phone)).sortBy(_.phone).take(1)).map(_.headOption)
    
    // SELECT x."id", x."phone", x."fio", x."addressId" FROM PhoneRecord x
    def list(): QIO[List[PhoneRecord]] = 
      run(phoneRecordSchema)
    
      // INSERT INTO PhoneRecord ("id","phone","fio","addressId") VALUES (?, ?, ?, ?)
    def insert(phoneRecord: PhoneRecord): QIO[Unit] = 
      run(phoneRecordSchema.insert(lift(phoneRecord))).unit

       def insert(phoneRecords: List[PhoneRecord]): QIO[Unit] = 
        run(liftQuery(phoneRecords).foreach{ p =>
            phoneRecordSchema.insert(p)
        }).unit
    
    
      // UPDATE PhoneRecord SET "id" = ?, "phone" = ?, "fio" = ?, "addressId" = ?
      // UPDATE PhoneRecord SET "id" = ?, "phone" = ?, "fio" = ?, "addressId" = ? WHERE "id" = ?
    def update(phoneRecord: PhoneRecord): QIO[Unit] = 
      run(phoneRecordSchema.filter(_.id == lift(phoneRecord.id)).update(lift(phoneRecord))).unit
    
    // DELETE FROM PhoneRecord WHERE "id" = ?
    def delete(id: String): QIO[Unit] = 
      run(phoneRecordSchema.filter(_.id == lift(id)).delete).unit

      // SELECT x7."id", x7."zipCode", x7."streetAddress" FROM PhoneRecord x6, Address x7 
      // WHERE x6."phone" = ? AND x7."id" = x6."addressId"
    run(
        phoneRecordSchema.filter(_.phone == lift("")).flatMap{ p =>
            addressSchema.filter(_.id == p.addressId)
        }

    )

    //SELECT p."id", p."phone", p."fio", p."addressId", address."id", address."zipCode", address."streetAddress" 
    // FROM PhoneRecord p, Address address WHERE address."id" = p."addressId"
    run(
      for{
          p <- phoneRecordSchema
          address <- addressSchema if (address.id == p.addressId)
      } yield (p, address)
    )

    // applicative join
    // SELECT x8."id", x8."phone", x8."fio", x8."addressId", x9."id", x9."zipCode", x9."streetAddress" 
    // FROM PhoneRecord x8 
    // INNER JOIN Address x9 ON x8."addressId" = x9."id"
    run(
      phoneRecordSchema.join(addressSchema).on(_.addressId == _.id)
      .filter(_._1.phone == lift(""))
    )

    // flat join
    // SELECT p."id", p."phone", p."fio", p."addressId", address."id", address."zipCode", address."streetAddress" 
    // FROM PhoneRecord p, Address address
    run(
      for{
          p <- phoneRecordSchema
          address <- addressSchema.join(_.id == p.addressId)
      } yield (p, address)
    )
  }
 
  val live: ULayer[PhoneRecordRepository] = ???
}
