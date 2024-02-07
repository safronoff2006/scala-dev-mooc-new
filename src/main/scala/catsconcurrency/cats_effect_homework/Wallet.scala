package catsconcurrency.cats_effect_homework

import cats.effect.Sync
import cats.implicits._
import catsconcurrency.cats_effect_homework.Wallet._

import java.nio.file.Files._
import java.nio.file.{Path, Paths}



// DSL управления электронным кошельком
trait Wallet[F[_]] {
  // возвращает текущий баланс
  def balance: F[BigDecimal]
  // пополняет баланс на указанную сумму
  def topup(amount: BigDecimal): F[Unit]
  // списывает указанную сумму с баланса (ошибка если средств недостаточно)
  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]]
}

// Игрушечный кошелек который сохраняет свой баланс в файл
// todo: реализовать используя java.nio.file._
// Насчёт безопасного конкуррентного доступа и производительности не заморачиваемся, делаем максимально простую рабочую имплементацию. (Подсказка - можно читать и сохранять файл на каждую операцию).
// Важно аккуратно и правильно завернуть в IO все возможные побочные эффекты.
//
// функции которые пригодятся:
// - java.nio.file.Files.write
// - java.nio.file.Files.readString
// - java.nio.file.Files.exists
// - java.nio.file.Paths.get
final class FileWallet[F[_]: Sync](id: WalletId) extends Wallet[F] {
  def balance: F[BigDecimal] = readFromWallet(id)
  def topup(amount: BigDecimal): F[Unit] = balance
    .flatMap(b => writeToWallet(id, b + amount )) //F[Path]
    .map(_ => ()) //приводим F[Path] к F[Unit]

  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]] =
    for {
      currentBalance <- balance
      unit <- if (currentBalance < amount) topup(0) else topup(- amount)
      either <- if (currentBalance < amount)
        Sync[F].delay(Left(BalanceTooLow))
        else Sync[F].delay(Right(unit))
    } yield either

}

object Wallet {

  // todo: реализовать конструктор
  // внимание на сигнатуру результата - инициализация кошелька имеет сайд-эффекты
  // Здесь нужно использовать обобщенную версию уже пройденного вами метода IO.delay,
  // вызывается она так: Sync[F].delay(...)
  // Тайпкласс Sync из cats-effect описывает возможность заворачивания сайд-эффектов


  val pathCatalogWallets = "src/test/scala/home/"

  def fileWallet[F[_]: Sync](id: WalletId): F[Wallet[F]] = for {
    _ <- createWalletInFile(id)
    wallet <- Sync[F].delay(new FileWallet(id))
  } yield wallet

  def createWalletInFile[F[_]: Sync](id: WalletId): F[Unit] = {
    val pathFile =  Paths.get( pathCatalogWallets + id)
    if (exists(pathFile)) {
      Sync[F].unit
    } else {
      for {
        _ <- Sync[F].delay(createFile(pathFile))
        _ <- writeToWallet(id, 0)
      } yield ()
    }
  }
  def writeToWallet[F[_]: Sync](idWallet: WalletId, data: BigDecimal): F[Path] = Sync[F].delay{
      val pathFile = Paths.get(pathCatalogWallets + idWallet)
      val strdata: String = data.toString()
      writeString(pathFile, strdata)
    }


  def readFromWallet[F[_]: Sync](idWallet: WalletId): F[BigDecimal] = {
    val pathFile =  Paths.get( pathCatalogWallets + idWallet)
    Sync[F].delay(BigDecimal(readString(pathFile)))
  }

  type WalletId = String

  sealed trait WalletError
  case object BalanceTooLow extends WalletError
}
