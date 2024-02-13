package catsconcurrency.cats_effect_homework

import cats.effect.{IO, IOApp, Spawn}
import cats.implicits._


import scala.concurrent.duration._
import scala.language.postfixOps

// Поиграемся с кошельками на файлах и файберами.

// Нужно написать программу где инициализируются три разных кошелька и для каждого из них работает фоновый процесс,
// который регулярно пополняет кошелек на 100 рублей раз в определенный промежуток времени. Промежуток надо сделать разный, чтобы легче было наблюдать разницу.
// Для определенности: первый кошелек пополняем раз в 100ms, второй каждые 500ms и третий каждые 2000ms.
// Помимо этих трёх фоновых процессов (подсказка - это файберы), нужен четвертый, который раз в одну секунду будет выводить балансы всех трех кошельков в консоль.
// Основной процесс программы должен просто ждать ввода пользователя (IO.readline) и завершить программу (включая все фоновые процессы) когда ввод будет получен.
// Итого у нас 5 процессов: 3 фоновых процесса регулярного пополнения кошельков, 1 фоновый процесс регулярного вывода балансов на экран и 1 основной процесс просто ждущий ввода пользователя.

// Можно делать всё на IO, tagless final тут не нужен.

// Подсказка: чтобы сделать бесконечный цикл на IO достаточно сделать рекурсивный вызов через flatMap:
// def loop(): IO[Unit] = IO.println("hello").flatMap(_ => loop())
object WalletFibersApp extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      _ <- IO.println("Press any key to stop...")
      wallet1 <- Wallet.fileWallet[IO]("1")
      wallet2 <- Wallet.fileWallet[IO]("2")
      wallet3 <- Wallet.fileWallet[IO]("3")
      fiber1 <- Spawn[IO].start(topupWithSleep(wallet1, 100).iterateWhile(_ => true))
      fiber2 <- Spawn[IO].start(topupWithSleep(wallet2, 500).iterateWhile(_ => true))
      fiber3 <- Spawn[IO].start(topupWithSleep(wallet3, 2000).iterateWhile(_ => true))
      fiberPrinter <- Spawn[IO].start(balancesPrint(wallet1, wallet2, wallet3))
      _ <- IO.readLine
      _ <- fiber1.cancel *> fiber2.cancel *> fiber3.cancel *> fiberPrinter.cancel
      // todo: запустить все файберы и ждать ввода от пользователя чтобы завершить работу
    } yield ()

  def topupWithSleep(w: Wallet[IO], duration: Int): IO[Unit] = for {
    _ <- w.topup(100)
    _ <- IO.sleep(duration milliseconds)
  } yield ()

  def balancesPrint(w1: Wallet[IO], w2: Wallet[IO], w3: Wallet[IO]): IO[Unit] = {
      val  effectPrint: IO[Unit] = for {
          b1 <- w1.balance
          b2 <- w2.balance
          b3 <- w3.balance
          _ <- IO.println(s"Balanses - Wallet 1: ${b1.toString()}   Wallet 2: ${b2.toString()}   Wallet 3: ${b3.toString()}")
          _ <- IO.sleep(5 seconds)
        } yield ()

      effectPrint.iterateWhile(_ => true)
  }
}
