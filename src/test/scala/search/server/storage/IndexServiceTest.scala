package search.server.storage

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import search.protocol.{IdsForWord, IndexItem}
import search.server.storage.IndexService.{IndexUpdateResponse, Indexing, Search}

class IndexServiceTest() extends TestKit(ActorSystem("IndexServiceTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "A IndexService actor" must {

    "search found when document in index" in {

      val singleNode = TestProbe()
      val indexNodes = Seq(system.actorSelection(singleNode.ref.path))
      val indexService = system.actorOf(IndexService.props(indexNodes))

      indexService ! Search(Set("hi", "there"))

      singleNode.expectMsgAllOf(IdsForWord("hi"), IdsForWord("there"))
    }

    "index correct amount of words" in {
      val singleNode = TestProbe()
      val indexNodes = Seq(system.actorSelection(singleNode.ref.path))
      val indexService = system.actorOf(IndexService.props(indexNodes))

      indexService ! Indexing(Set("hi", "there"), "1")

      singleNode.expectMsgAllOf(IndexItem("hi", "1"), IndexItem("there", "1"))

      expectMsg(IndexUpdateResponse(2))
    }
  }
}
