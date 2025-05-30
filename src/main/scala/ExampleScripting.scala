import org.apache.pekko.actor.ActorSystem
import redis.api.scripting.RedisScript
import redis.protocol.Bulk
import redis.protocol.MultiBulk
import redis.RedisClient
import scala.concurrent.duration.*
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

object ExampleScripting {
  def main(args: Array[String]): Unit = {
    given actorSystem: ActorSystem = ActorSystem()

    val redis = RedisClient()

    val redisScript = RedisScript("return 'rediscala'")
    val redisScriptKeysArgs = RedisScript("return {redis.call('get', KEYS[1]),ARGV[1]}")

    val set = redis.set("key", "scripting")

    val script = redis.evalshaOrEval(redisScriptKeysArgs, Seq("key"), Seq("arg"))

    val scriptString = redis.evalshaOrEval(redisScript)

    val r = for {
      _ <- set
      _scriptString <- scriptString
      _script <- script
    } yield {
      println(s"script ${redisScript.script} :")
      _scriptString match {
        case b: Bulk => println(b.toString())
        case _ => println("Bulk reply expected!")
      }

      println(s"script ${redisScriptKeysArgs.script} :")
      _script match {
        case mb: MultiBulk =>
          mb.responses.map(_.map(reply => {
            println(reply.toByteString.utf8String)
          }))
        case _ => println("MultiBulk reply expected!")
      }
    }
    Await.result(r, 5.seconds)

    Await.result(actorSystem.terminate(), 20.seconds)
  }
}
