package search.server

import akka.actor.ActorRef
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import search.server.storage.FileService.{DocumentResult, EmptyDocument, Get, Put}
import search.server.storage.IndexService.{IndexSearchResponse, IndexUpdateResponse, Indexing, Search}
import search.server.support.JsonSupport._

class SearchService(fileService: ActorRef, indexService: ActorRef)
                   (implicit val timeout: Timeout, val blockingExecutor: MessageDispatcher) {

  val searchRoute: Route = getDocument ~ putDocument ~ searchForAllWords


  private def getDocument: Route = path("get") {
    get {
      complete {
        (fileService ? Get("1")).mapTo[Either[EmptyDocument, DocumentResult]]
      }
    }
  }

  private def putDocument: Route = path("put") {
    get {
      complete {
        val id = "1"
        val value = "Databases are used to store and retrieve data"
        val words = value.split(" ")
        (fileService ? Put(id, value))
          .flatMap(res =>
            indexService ? Indexing(words.toSet, id))
          .mapTo[IndexUpdateResponse]
      }
    }
  }

  private def searchForAllWords: Route = path("search") {
    get {
      complete {
        (indexService ? Search(Set("store", "and"))).mapTo[IndexSearchResponse]
      }
    }
  }
}
