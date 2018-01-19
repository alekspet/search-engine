package search.client

import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpResponse
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import search.server.storage.FileService.{DocumentResult, EmptyDocument}
import search.server.storage.IndexService.{IndexSearchResponse, IndexUpdateResponse}
import search.server.support.JsonSupport._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Client based on search server API entities.
  **/
trait Client {

  def get(key: String): Future[Either[EmptyDocument, DocumentResult]]

  def put(key: String, document: String): Future[IndexUpdateResponse]

  def search(words: String): Future[IndexSearchResponse]

  def shutdown(): Unit

}

object Client {

  def apply(host: String, port: Int): Client = new SearchClient(host, port)

  /**
    * Default Akka-Http based client.
    **/
  private class SearchClient(host: String, port: Int) extends Client {

    private implicit val system: ActorSystem = ActorSystem("client-system")
    private implicit val materializer: ActorMaterializer = ActorMaterializer()
    private val clientFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = Http().outgoingConnection(host, port)

    import system.dispatcher

    override def get(key: String): Future[Either[EmptyDocument, DocumentResult]] = {
      val request = HttpRequest(method = HttpMethods.GET, uri = s"/get/$key")
      dispatchRequest(request).flatMap({ response =>
        Unmarshal(response).to[Either[EmptyDocument, DocumentResult]]
      })
    }

    override def put(key: String, document: String): Future[IndexUpdateResponse] = {
      val request = HttpRequest(method = HttpMethods.PUT, uri = s"/put/$key", entity = document)
      dispatchRequest(request).flatMap({ response =>
        Unmarshal(response).to[IndexUpdateResponse]
      })
    }

    override def search(words: String): Future[IndexSearchResponse] = {
      val request = HttpRequest(method = HttpMethods.GET, uri = s"/search?searchQuery=$words")
      dispatchRequest(request).flatMap({ response =>
        Unmarshal(response).to[IndexSearchResponse]
      })
    }

    override def shutdown(): Unit = {
      Await.result(Http().shutdownAllConnectionPools(), 1.minute)
      system.terminate()
      Await.result(system.whenTerminated, 1.minute)
    }

    private def dispatchRequest(request: HttpRequest): Future[HttpEntity] = Source
      .single(request)
      .via(clientFlow)
      .runWith(Sink.head)
      .map(_.entity())
      .mapTo[HttpEntity]
  }

}
