package search.server

import akka.actor.ActorRef
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import search.server.storage.{FileService, FileStorage, IndexService}

import scala.concurrent.duration._

class SearchServiceTest extends WordSpec with Matchers with ScalatestRouteTest {

  implicit val timeout: Timeout = 5.seconds
  implicit val dispatcher: MessageDispatcher = system.dispatchers.defaultGlobalDispatcher

  val fileService: ActorRef = system.actorOf(FileService.props(FileStorage()), "fileStorage")
  val indexService: ActorRef = system.actorOf(IndexService.props(), "indexService")
  val searchService = new SearchService(fileService, indexService)

  val route = searchService.searchRoute

  "The SearchService" should {

    "return a empty document for GET document which not exist" in {
      Get("/get") ~> route ~> check {
        responseAs[String] shouldEqual "{}"
      }
    }
  }
}
