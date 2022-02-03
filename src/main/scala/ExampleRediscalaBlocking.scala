import akka.actor.ActorSystem
import redis.RedisBlockingClient
import redis.RedisClient
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ExampleRediscalaBlocking {
  def main(args: Array[String]): Unit = {
    implicit val akkaSystem: ActorSystem = ActorSystem()

    val redis = RedisClient()

    val redisBlocking = RedisBlockingClient()

    def publisher() = {
      redis.lpush("workList", "doSomeWork")
      Thread.sleep(2000)
      redis.rpush("otherKeyWithWork", "doSomeWork1", "doSomeWork2")
    }

    def consumer() = Future {
      val waitWork = 3
      val sequenceFuture = for { i <- 0 to waitWork } yield {
        redisBlocking
          .blpop(Seq("workList", "otherKeyWithWork"), 5.seconds)
          .map(result => {
            result.map { case (key, work) =>
              println(s"list $key has work : ${work.utf8String}")
            }
          })
      }

      Await.result(Future.sequence(sequenceFuture), 10.seconds)
    }

    val r = redis
      .del("workList")
      .flatMap(_ => {
        consumer()
        publisher()
      })

    Await.result(r, 15.seconds)
    Await.result(akkaSystem.terminate(), 20.seconds)

  }
}
