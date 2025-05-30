import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.Props
import java.net.InetSocketAddress
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.PMessage
import redis.api.pubsub.Message
import redis.RedisClient
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

object ExamplePubSub {
  def main(args: Array[String]): Unit = {
    given actorSystem: ActorSystem = ActorSystem()

    val redis = RedisClient()

    actorSystem.scheduler.schedule(2.seconds, 2.seconds)(redis.publish("time", System.currentTimeMillis()))
    actorSystem.scheduler.schedule(2.seconds, 5.seconds)(redis.publish("pattern.match", "pattern value"))
    actorSystem.scheduler.scheduleOnce(20.seconds)(actorSystem.terminate())

    val channels = Seq("time")
    val patterns = Seq("pattern.*")
    actorSystem.actorOf(Props(classOf[SubscribeActor], channels, patterns).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))

  }
}

class SubscribeActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
    extends RedisSubscriberActor(
      new InetSocketAddress("localhost", 6379),
      channels,
      patterns,
      onConnectStatus = connected => { println(s"connected: $connected") }
    ) {

  def onMessage(message: Message) = {
    println(s" message received: $message")
  }

  def onPMessage(pmessage: PMessage) = {
    println(s"pattern message received: $pmessage")
  }
}
