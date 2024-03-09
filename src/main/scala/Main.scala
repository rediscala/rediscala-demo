import org.apache.pekko.actor.ActorSystem
import redis.RedisClient
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem()

    val redis = RedisClient()

    val futurePong = redis.ping()
    println("Ping sent!")
    futurePong.map(pong => {
      println(s"Redis replied with a $pong")
    })
    Await.result(futurePong, 5.seconds)

    def doSomething(redis: RedisClient): Future[Boolean] = {
      // launch command set and del in parallel
      val s = redis.set("redis", "is awesome")
      val d = redis.del("i")
      for {
        set <- s
        del <- d
        incr <- redis.incr("i")
        iBefore <- redis.get("i")
        incrBy20 <- redis.incrby("i", 20)
        iAfter <- redis.get("i")
      } yield {
        println("SET redis \"is awesome\"")
        println("DEL i")
        println("INCR i")
        println("INCRBY i 20")
        val ibefore = iBefore.map(_.utf8String)
        val iafter = iAfter.map(_.utf8String)
        println(s"i was $ibefore, now is $iafter")
        iafter == Option("20")
      }
    }

    val futureResult = doSomething(redis)

    Await.result(futureResult, 5.seconds)

    Await.result(actorSystem.terminate(), 20.seconds)

  }
}
