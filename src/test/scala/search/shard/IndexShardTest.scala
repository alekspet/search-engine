package search.shard

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import search.shard.IndexNode.{IdsForWords, IndexItem, WordResult}

class IndexShardTest() extends TestKit(ActorSystem("IndexNodeTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A IndexNode actor" must {

    "search found when document in index" in {
      val indexNode = system.actorOf(IndexNode.props())
      indexNode ! IndexItem("1", Set("hi"))
      indexNode ! IndexItem("2", Set("there"))

      indexNode ! IdsForWords(Set("hi"))
      expectMsg(WordResult(Set("1")))
    }
  }
}
