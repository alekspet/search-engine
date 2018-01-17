package search.server.storage

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import search.server.storage.FileService._

class FileServiceTest() extends TestKit(ActorSystem("FileServiceTest")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A FileService actor" must {

    "persist document by id" in {
      val fileService = system.actorOf(FileService.props(FileStorage()))
      val id = "1"
      val doc = "hello world"
      fileService ! Put(id, doc)
      expectMsg(SaveDone(id))
    }

    "return persisted document by id correctly" in {
      val fileService = system.actorOf(FileService.props(FileStorage()))
      val id = "2"
      val doc = "my text to search"
      fileService ! Put(id, doc)
      expectMsg(SaveDone(id))
      fileService ! Get(id)
      expectMsg(Right(DocumentResult(id, doc)))
    }

    "return empty document if id not exists" in {
      val fileService = system.actorOf(FileService.props(FileStorage()))
      val id = "3"
      fileService ! Get(id)
      expectMsg(Left(EmptyDocument))
    }
  }
}
