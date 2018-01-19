package search.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.settings.{ClientConnectionSettings, ConnectionPoolSettings, ParserSettings, ServerSettings}
import akka.stream.ActorMaterializer
import akka.testkit.{SocketUtil, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import search.server.storage.FileService.{DocumentResult, EmptyDocument}
import search.server.storage.IndexService.{IndexSearchResponse, IndexUpdateResponse}
import search.server.support.JsonSupport._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ClientTest() extends TestKit(ActorSystem("ClientTest"))
  with WordSpecLike with Matchers with BeforeAndAfterAll
  with Directives
  with RequestBuilding {

  import system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val (host, port) = SocketUtil.temporaryServerHostnameAndPort()

  private val parserSettings = ParserSettings(system)
  private val serverSettings = ServerSettings(system).withParserSettings(parserSettings)
  private val clientConSettings = ClientConnectionSettings(system).withParserSettings(parserSettings)
  private val clientSettings = ConnectionPoolSettings(system).withConnectionSettings(clientConSettings)
  private val binding: Future[Http.ServerBinding] = Http()
    .bindAndHandle(routes, host, port, settings = serverSettings)
  private val client = Client(host, port)

  def routes =
    path("put" / Segment) {
      _ =>
        put {
          complete(IndexUpdateResponse(2))
        }
    } ~ path("get" / "1") {
      get {
        complete(Left(EmptyDocument()))
      }
    } ~
      path("get" / Segment) {
        id =>
          get {
            complete(Right(DocumentResult(id, "hi there")))
          }
      } ~
      path("search") {
        parameters('searchQuery) {
          searchQuery =>
            get {
              complete {
                IndexSearchResponse(Set("1", "2"))
              }
            }
        }
      }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
    client.shutdown()
  }

  "Search client" must {

    "upload document correctly" in {
      Await.result(client.put("1", "some document"), 10.seconds) shouldBe IndexUpdateResponse(2)
    }

    "get empty document correctly" in {
      Await.result(client.get("1"), 10.seconds) shouldBe Left(EmptyDocument())
    }

    "get full document correctly" in {
      Await.result(client.get("2"), 10.seconds) shouldBe Right(DocumentResult("2", "hi there"))
    }

    "search document call works" in {
      Await.result(client.search("hii"), 10.seconds) shouldBe IndexSearchResponse(Set("1", "2"))
    }
  }
}
