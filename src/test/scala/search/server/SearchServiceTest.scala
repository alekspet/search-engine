package search.server

import java.net.URLEncoder

import akka.actor.ActorRef
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import search.server.storage.TextStorageService
import search.shard.ShardNode

import scala.concurrent.duration._

class SearchServiceTest extends WordSpec with Matchers with ScalatestRouteTest {

  private implicit val timeout: Timeout = 5.seconds
  private implicit val dispatcher: MessageDispatcher = system.dispatchers.defaultGlobalDispatcher

  private val storageNodes = Seq(system.actorSelection(system.actorOf(ShardNode.props(), "storage").path))
  private val storageService: ActorRef = system.actorOf(TextStorageService.props(storageNodes), "storageService")
  private val searchService = new SearchService(storageService)

  private val route = searchService.searchRoute

  "The SearchService" should {

    "return a empty document for GET document which not exist" in {
      Get("/get/1") ~> route ~> check {
        responseAs[String] shouldEqual "{}"
      }
    }

    "upload new document for id successfully" in {
      Put("/put/1", HttpEntity("""Databases are used to store and retrieve data""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"id":"1"}"""
      }
    }

    "upload new document for id and get back by id" in {
      Put("/put/2", HttpEntity("""Databases are used to store and retrieve data""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"id":"2"}"""
      }
      Get("/get/2") ~> route ~> check {
        responseAs[String] shouldEqual """{"id":"2","doc":"Databases are used to store and retrieve data"}"""
      }
    }

    "upload 3 documents and search by words works correctly" in {
      Put("/put/3", HttpEntity("""The Actor Model provides""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"id":"3"}"""
      }
      Put("/put/4", HttpEntity("""higher level of abstraction for writing concurrent and distributed systems""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"id":"4"}"""
      }
      Put("/put/5", HttpEntity("""concurrent and distributed systems""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"id":"5"}"""
      }

      val queryTwoDocs = URLEncoder.encode("concurrent distributed", "UTF-8")
      Get(s"/search?searchQuery=$queryTwoDocs") ~> route ~> check {
        responseAs[String] shouldEqual """{"ids":["4","5"]}"""
      }

      val queryOneDoc = URLEncoder.encode("Actor Model", "UTF-8")
      Get(s"/search?searchQuery=$queryOneDoc") ~> route ~> check {
        responseAs[String] shouldEqual """{"ids":["3"]}"""
      }
      val queryNoDocs = URLEncoder.encode("no such words", "UTF-8")
      Get(s"/search?searchQuery=$queryNoDocs") ~> route ~> check {
        responseAs[String] shouldEqual """{"ids":[]}"""
      }

    }
  }
}
