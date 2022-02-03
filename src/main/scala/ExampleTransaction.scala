import akka.actor.ActorSystem
import akka.util.ByteString
import redis.RedisClient
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ExampleTransaction extends App {
  implicit val akkaSystem: ActorSystem = ActorSystem()

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

  Await.result(akkaSystem.terminate(), 20.seconds)
}
