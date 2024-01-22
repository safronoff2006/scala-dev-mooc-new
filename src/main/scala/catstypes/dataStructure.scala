package catstypes

import cats.data.{Chain, Ior, Kleisli, NonEmptyChain, NonEmptyList, OptionT, Validated, ValidatedNec}
import cats.implicits._

import scala.concurrent.Future
import scala.util.Try

object  dataStructure{
  // Chain
  val emply: Chain[Int] = Chain[Int]()
  val empty2: Chain[Int] = Chain.empty[Int]
  val ch2 = Chain(1)
  val ch3 = Chain.one(1)
  val ch4 = Chain.fromSeq(1::2::3::Nil)
  //operators
  val ch5 = ch2 :+ 2 // append, const time
  val ch6 = 3 +: ch2 // prepend, const time
  val r = ch2.headOption
  ch3.map(_+1)
  ch3.flatMap(x=>Chain.one(x+1))

  //nonemptychain
  val nec = NonEmptyChain(1)
  val nec1 = NonEmptyChain.one(1)
  val nec2: Option[NonEmptyChain[Int]] = NonEmptyChain.fromSeq(1::2::3::Nil)
  val r2: Int = nec.head

  // NonEmptyList
  val nel1 = NonEmptyList(1, List())
  val nel2 = NonEmptyList.one(1)
  val nel3 = NonEmptyList.fromList(1::Nil)
}

object validation{
  type EmailValidationError = String
  type NameValidationError = String
  type AgeValidationError = String

  type Name = String
  type Email = String
  type Age = Int
  case class UserDTO(email: String, name:String, age:Int)
  case class User(email: String, name:String, age:Int)


  def emailValidatorE: Either[EmailValidationError, Email] = Left("Not valid email")
  def userNameValidatorE: Either[NameValidationError, Name] = Left("Not valid name")
  def ageValidatorE: Either[AgeValidationError, Age] = Right(30)

  //проблема - коротокое замыкание
  def validateUserDataE(userDto: UserDTO): Either[String, User] = for {
    email <- emailValidatorE
    name <- userNameValidatorE
    age <- ageValidatorE
  } yield  User(email, name, age)

  // cats validation
  val v1 = Validated.valid[String, String]("foo")
  val v2 = Validated.invalid[String, String]("foo")


  def emailValidatorV: Validated[EmailValidationError, Email] = "email not valid".invalid[String]
  def userNameValidatorV: Validated[NameValidationError, Name] = "name not valid".invalid[String]
  def userAgeValidatorV: Validated[AgeValidationError, Age] = 30.valid[String]

  /*
  def validateUserDataV(userDto: UserDTO): Validated[String, User] = for {
    email <- emailValidatorV
    name <- userNameValidatorV
    age <- userAgeValidatorV
  } yield User(email, name, age)
    */

  def validateUserDataV(userDto: UserDTO): Validated[String, String] =
    emailValidatorV combine userNameValidatorV combine userAgeValidatorV.map(_.toString)

  // improvement
  def validateUserDataV2(userDto: UserDTO): ValidatedNec[String, User] = (
    emailValidatorV.toValidatedNec,
    userNameValidatorV.toValidatedNec,
    userAgeValidatorV.toValidatedNec
  ).mapN {
    (email, name, age) =>
      User(email, name, age)
  }


  //Ior
  val u: User = User("a","b", 30)

  lazy val ior: Ior[String, User] = Ior.Left("")
  lazy val ior1: Ior[String, User] = Ior.right(u)
  lazy val ior2: Ior[String, User] = Ior.both("warning", u)

  def emailValidatorI = Ior.both("email ???", "sdf@sdf.de")
  def usernameValidatorI = Ior.both("name ???", "Mustermann")
  def userageValidatorI = 30.rightIor[String]

  def validateUserDataI(userDto: UserDTO): Ior[String, User] = for {
    email <- emailValidatorI
    name <- usernameValidatorI
    age <- userageValidatorI
  } yield User(email, name ,age)

  def validateUserDataI2(userDto: UserDTO) : Ior[NonEmptyChain[String], User] = for {
    email <- emailValidatorI.toIorNec
    name <- usernameValidatorI.toIorNec
    age <- userageValidatorI.toIorNec
  } yield User(email, name ,age)




  def main(args: Array[String]): Unit ={
//    println(validateUserDataE(UserDTO("","",50)))
  //  println(validateUserDataV2(UserDTO("","",50)))
    //println(validateUserDataI(UserDTO("","",50)))
    println(validateUserDataI2(UserDTO("","",50)))

  }

  // Kleisli

  val f1: Int => String = i => i.toString
  val f2: String => String = s => s + "dxfg"
  val f3: Int => String = f1 andThen f2

  val f4: String => Option[Int] = _.toIntOption
  val f5: Int => Option[Int] = i => Try(10/i).toOption

  val f6: Kleisli[Option, String, Int]  = Kleisli(f4) andThen Kleisli(f5)
  val _f6 = f6.run
}

//transformers
object transformers{
  val f1: Future[Int] = Future.successful(2)
  def f2(i:Int): Future[Option[Int]] = Future.successful(Try(10/i).toOption)
  def f3(i:Int): Future[Option[Int]] = Future.successful(Try(10/i).toOption)

  import  scala.concurrent.ExecutionContext.Implicits.global
  /*
  val r = for {
    i1 <- f1 //Int
    i2 <- f2(i1) // option[int]
    i3 <- f3(i1) // option[int]
  } yield i2+i3
  */

  val r: OptionT[Future,Int] = for {
    i1 <- OptionT.liftF(f1)
    i2 <- OptionT(f2(i1))
    i3 <- OptionT(f3(i1))
  } yield  i2 +i3

  val _: Future[Option[Int]] = r.value
}

// теория monoid
//1. моноид не монада
//2. моноид это аккумулятор

//semigroup

//Группы

//1.ассоциация 2х элементов
//2. группы есть нейтральный элемент 1
//2. группы есть обратный элемент

// semigroup - grupp with 1 property
// monoid - grupp 1, 2

//Option это функтор
import java.lang.Integer.toHexString
object  Demo extends  App {
  val k: Option[Int] = Option(100500)
  val s: Option[String] = k map toHexString
}


//Try

import scala.util.Try
object Demo1 extends App {
  val k: Try[Int] = Try(100500)
  val s: Try[String] = k map toHexString
}

// Future

import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
object Demo2 extends App {
  //val k: Try[Int] = Future(100500)
 // val s: Try[String] = k map toHexString
}


object  cats_type_classes{

  trait Semigroup[A] {

    def combine(a:A, y:A):A
  }

  object Semigroup{
    def apply[A](implicit  ev: Semigroup[A]) = ev

    implicit val intSemigroup: Semigroup[Int] = new Semigroup[Int] {
      override  def combine(x:Int, y:Int): Int = x+y
    }
  }


  // |+|
  val _ = (1::2::3::Nil).foldLeft(0)(Semigroup[Int].combine(_,_))

  val _ = (1::2::3::Nil).foldLeft(0)(_ |+| _)


  trait Functor[F[_]] {
    def map[A,B](fa: F[A])(f: A=>B): F[B]
  }


}






