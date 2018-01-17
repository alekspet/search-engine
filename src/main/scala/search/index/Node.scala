package search.index

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration._

object Node {

  val system: ActorSystem = ActorSystem("index-node")

  def main(args: Array[String]): Unit = {
    system.actorOf(IndexNode.props(), "index")
    scala.sys.addShutdownHook(
      shutdown()
    )
  }

  private def shutdown(): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, 1.minute)
  }
}
