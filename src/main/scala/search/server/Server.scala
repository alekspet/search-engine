package search.server

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import search.server.storage.{FileService, FileStorage, IndexNodeFactory, IndexService}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Document server runner on Akka-HTTP.
  */
object Server {

  implicit val system: ActorSystem = ActorSystem("search-engine-server")
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("blocking-dispatcher")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Duration.fromNanos(system.settings.config.getDuration("service.timeout").toNanos)

  def main(args: Array[String]): Unit = {

    val nodesConfig: Config = system.settings.config
    val indexNodes: Seq[ActorSelection] = IndexNodeFactory.indexNodes(system)

    val fileService: ActorRef = system.actorOf(FileService.props(FileStorage()), "fileStorage")
    val indexService: ActorRef = system.actorOf(IndexService.props(indexNodes), "indexService")

    val searchService = new SearchService(fileService, indexService)

    val searchEndpoint: Future[ServerBinding] = Http().bindAndHandle(
      route2HandlerFlow(searchService.searchRoute),
      system.settings.config.getString("service.host"),
      system.settings.config.getInt("service.port"))

    scala.sys.addShutdownHook(
      shutdown(searchEndpoint)
    )
  }

  private def shutdown(searchEndpoint: Future[ServerBinding]): Unit = {
    Await.result(Http().shutdownAllConnectionPools(), 1.minute)
    Await.result(searchEndpoint.map(_.unbind()), 1.minute)
    system.terminate()
    Await.result(system.whenTerminated, 1.minute)
  }
}
