package search.server.storage

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import search.protocol._
import search.shard.IndexNode.{IdsForWords, IndexItem}

class TextStorageServiceTest() extends TestKit(ActorSystem("TextStorageServiceTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "A TextStorageService actor" must {

    "search found when document in index" in {

      val singleNode = TestProbe()
      val shardNodes = Seq(system.actorSelection(singleNode.ref.path))
      val textStorageService = system.actorOf(TextStorageService.props(shardNodes))

      textStorageService ! Search(Set("hi", "there"))

      singleNode.expectMsgAllOf(IndexSearch(Set("hi", "there")))
    }

    "index correct amount of words" in {
      val singleNode = TestProbe()
      val shardNodes = Seq(system.actorSelection(singleNode.ref.path))
      val textStorageService = system.actorOf(TextStorageService.props(shardNodes))

      textStorageService ! Put("1", "hi there")

      singleNode.expectMsgAllOf(Text("1", "hi there"))
      expectMsg(SaveDone("1"))
    }
  }
}
