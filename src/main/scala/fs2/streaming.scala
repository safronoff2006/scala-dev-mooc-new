package catsstreaming

import cats.effect.kernel.Async
import cats.effect.std.Queue
import cats.effect.{IO, IOApp, Resource, SyncIO}
import fs2.{Chunk, Pure, Stream}
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration._
import java.time.Instant

object  Streams extends IOApp.Simple {

  //1.
  val pureApply: Stream[Pure, Int] = Stream.apply(1,2,3) //.toList

  //2
  val ioApply: Stream[IO, Int] = pureApply.covary[IO]

  //3
  val list = List(1,2,3,4)
  val strem1: Stream[Pure, Int] = Stream.emits(list)

  //4
  val a: Seq[Int] = pureApply.toList

  val aa: IO[List[Int]] = ioApply.compile.toList

  //5
  val unfolded: Stream[IO, String] = Stream.unfoldEval(0){ s=>
    val next = s + 10
    if (s >= 50) IO.none
    else IO.println(next.toString).as(Some((next.toString, next)))
  }

  //6
  val s = Stream.eval(IO.readLine).evalMap(s=>IO.println(s">>$s")).repeatN(3)


  //7
  type Descriptor = String
  def openFile: IO[Descriptor] = IO.println("open file").as("file descriptor")
  def closeFile(descriptor: Descriptor): IO[Unit] =
    IO.println("closing file")
  def readFile(desciptor: Descriptor): Stream[IO, Byte] =
    Stream.emits(s"File content".map(_.toByte).toArray)

  val fileResource: Resource[IO, Descriptor] = Resource.make(openFile)(closeFile)
  val resourceStream: Stream[IO, Int] = Stream.resource(fileResource).flatMap(readFile).map(b=> b.toInt + 100)

  //9
  def writeToSocket[F[_] : Async](chunk: Chunk[String]): F[Unit] =
    Async[F].async_{ callback =>
      println(s"[thread: ${Thread.currentThread().getName}] :: Writing $chunk to socket ")
      callback(Right())
    }

  //10
  val fixedDelayStream = Stream.fixedDelay[IO](1.second)
    .evalMap(_ => IO.println(Instant.now))
  val fixedRateStream = Stream.fixedRate[IO](1.second)
    .evalMap(_ => IO.println(Instant.now))


  //11
  val queueIO = cats.effect.std.Queue.bounded[IO, Int](100)
  def putInQueue(queue: Queue[IO, Int], value: Int) =
    queue.offer(value)

  val queueStreamIO: IO[Stream[IO, Int]] = for {
    q <- queueIO
    _ <- (IO.sleep(500.millis) *> putInQueue(q, 5)).replicateA(10).start
  } yield Stream.fromQueueUnterminated(q)

  val queueStream = Stream.force(queueStreamIO)
  def increment(s: Stream[IO, Int]): Stream[IO, Int] = s.map(_ + 1)




  def run: IO[Unit] = {
//    s.compile.drain
    //8
/*    Stream((1 to 100) : _*)
      .chunkN(10)
      .map(println)
      .compile.drain
*/
    //s.compile.drain
   // resourceStream.evalMap(IO.println).compile.drain


/*    Stream((1 to 100).map(_.toString): _*)
      .chunkN(10)
      .covary[IO]
      .parEvalMapUnordered(10)(writeToSocket[IO])
      .compile
      .drain*/

//    fixedRateStream.compile.drain
    /*
    2024-01-18T17:55:16.582238Z
2024-01-18T17:55:17.562671500Z
2024-01-18T17:55:18.560450700Z
2024-01-18T17:55:19.561501100Z
2024-01-18T17:55:20.554525900Z
2024-01-18T17:55:21.560533600Z
2024-01-18T17:55:22.557236200Z
2024-01-18T17:55:23.554671600Z
2024-01-18T17:55:24.550876Z
2024-01-18T17:55:25.551432500Z
2024-01-18T17:55:26.559668600Z
    */
    //fixedDelayStream.compile.drain
    /*
    2024-01-18T17:56:21.555324700Z
2024-01-18T17:56:22.583871100Z
2024-01-18T17:56:23.606159200Z
2024-01-18T17:56:24.615180400Z
2024-01-18T17:56:25.625840500Z
2024-01-18T17:56:26.641397300Z
2024-01-18T17:56:27.658522100Z
2024-01-18T17:56:28.671567500Z
2024-01-18T17:56:29.685164400Z
2024-01-18T17:56:30.699579900Z
*/
//    queueStream.evalMap(IO.println).compile.drain

    queueStream.through(increment).through(increment).evalMap(IO.println).compile.drain

    (queueStream ++ queueStream).evalMap(IO.println).compile.drain

  }
}
