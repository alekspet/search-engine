package search.client

import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpResponse
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait Client {

  def get(key: String): Future[String]

  def put(key: String, document: String): Future[Unit]

  def search(words: String): Future[Set[String]]

  def shutdown(): Unit

}

object Client {

  def apply(host: String, port: Int): Client = new SearchClient(host, port)

  private class SearchClient(host: String, port: Int) extends Client {

    private implicit val system = ActorSystem("client-system")
    private implicit val materializer = ActorMaterializer()

    import system.dispatcher

    private val poolClientFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http().outgoingConnection(host, port)

    override def get(key: String): Future[String] = {
      val request = HttpRequest(method = HttpMethods.GET, uri = s"/get/$key")
      dispatchRequest(request).map(_.entity().toString)
    }

    override def put(key: String, document: String): Future[Unit] = {
      val request = HttpRequest(method = HttpMethods.PUT, uri = s"/put/$key", entity = document)
      dispatchRequest(request).map(_ => ())
    }

    override def search(words: String): Future[Set[String]] = {
      val request = HttpRequest(method = HttpMethods.GET, uri = s"/search?searchQuery=$words")
      dispatchRequest(request).map(x => Set(x.entity().toString))
    }

    override def shutdown(): Unit = {
      Await.result(Http().shutdownAllConnectionPools(), 1.minute)
      system.terminate()
      Await.result(system.whenTerminated, 1.minute)
    }

    private def dispatchRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request)
      .via(poolClientFlow)
      .runWith(Sink.head)
  }

}
