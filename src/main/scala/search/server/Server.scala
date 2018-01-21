package search.server

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import akka.util.Timeout
import search.server.storage.{ShardNodeFactory, TextStorageService}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Document server runner on Akka-HTTP.
  */
object Server {

  implicit val system: ActorSystem = ActorSystem("search-engine-server")
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("blocking-dispatcher")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Duration.fromNanos(system.settings.config.getDuration("service.timeout").toNanos)

  def main(args: Array[String]): Unit = {

    val shardNodes: Seq[ActorSelection] = ShardNodeFactory.shardNodes(system)
    val textStorageService: ActorRef = system.actorOf(TextStorageService.props(shardNodes)
        .withRouter(RoundRobinPool(16)), "textStorageService")
    val searchService = new SearchService(textStorageService)

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
