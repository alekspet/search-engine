package search.shard

import akka.actor.ActorSystem
import search.protocol._

import scala.concurrent.Await
import scala.concurrent.duration._

object Shard {

  val system: ActorSystem = ActorSystem(systemName)

  def main(args: Array[String]): Unit = {
    system.actorOf(ShardNode.props(), shardName)
    scala.sys.addShutdownHook(
      shutdown()
    )
  }

  private def shutdown(): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, 1.minute)
  }
}
