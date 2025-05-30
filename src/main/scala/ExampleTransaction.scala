import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.util.ByteString
import redis.RedisClient
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

object ExampleTransaction {
  def main(args: Array[String]): Unit = {
    given actorSystem: ActorSystem = ActorSystem()

    val redis = RedisClient()

    val redisTransaction = redis.transaction()
    redisTransaction.watch("key")
    val set = redisTransaction.set("key", "abcValue")
    val decr = redisTransaction.decr("key")
    val get = redisTransaction.get("key")
    redisTransaction.exec()
    val r = for {
      s <- set
      g <- get
    } yield {
      assert(s)
      println("ok : set(\"key\", \"abcValue\")")
      assert(g == Some(ByteString("abcValue")))
      println("ok : get(\"key\") == \"abcValue\"")
    }
    decr.failed.foreach { error =>
      println(s"decr failed : $error")
    }
    Await.result(r, 10.seconds)

    Await.result(actorSystem.terminate(), 20.seconds)
  }
}
