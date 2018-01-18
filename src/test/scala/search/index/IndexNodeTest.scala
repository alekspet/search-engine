package search.index

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import search.protocol.{IdsForWord, IndexItem, WordResult}

class IndexNodeTest() extends TestKit(ActorSystem("IndexNodeTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A IndexNode actor" must {

    "search found when document in index" in {
      val indexNode = system.actorOf(IndexNode.props())
      indexNode ! IndexItem("hi", "1")
      indexNode ! IndexItem("there", "2")

      indexNode ! IdsForWord("hi")
      expectMsg(WordResult("hi", Set("1")))
    }
  }
}
