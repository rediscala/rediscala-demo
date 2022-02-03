import akka.util.ByteString
import akka.actor.ActorSystem
import redis.api.keys.Exists
import redis.ByteStringSerializer
import redis.RedisClient
import redis.ByteStringFormatter
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class DumbClass(s1: String, s2: String)

object DumbClass {
  implicit val byteStringFormatter: ByteStringFormatter[DumbClass] = new ByteStringFormatter[DumbClass] {
    def serialize(data: DumbClass): ByteString = {
      ByteString(data.s1 + "|" + data.s2)
    }

    def deserialize(bs: ByteString): DumbClass = {
      val r = bs.utf8String.split('|').toList
      DumbClass(r(0), r(1))
    }
  }
}

case class PrefixedKey[K: ByteStringSerializer](prefix: String, key: K)

object PrefixedKey {
  implicit def serializer[K](implicit redisKey: ByteStringSerializer[K]): ByteStringSerializer[PrefixedKey[K]] =
    new ByteStringSerializer[PrefixedKey[K]] {
      def serialize(data: PrefixedKey[K]): ByteString = {
        ByteString(data.prefix + redisKey.serialize(data.key))
      }
    }
}

object ExampleByteStringFormatter {
  def main(args: Array[String]): Unit = {
    implicit val akkaSystem: ActorSystem = ActorSystem()

    val redis = RedisClient()

    val dumb = DumbClass("s1", "s2")

    val r = for {
      set <- redis.set("dumbKey", dumb)
      getDumbOpt <- redis.get[DumbClass]("dumbKey")
    } yield {
      getDumbOpt.map(getDumb => {
        assert(getDumb == dumb)
        println(getDumb)
      })
    }

    Await.result(r, 5.seconds)

    val prefixedKey = PrefixedKey("prefix", ByteString("1"))

    val exists = redis.send(Exists(prefixedKey))

    val bool = Await.result(exists, 5.seconds)
    assert(!bool)

    Await.result(akkaSystem.terminate(), 20.seconds)
  }
}
