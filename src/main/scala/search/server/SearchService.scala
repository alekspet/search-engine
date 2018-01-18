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
import search.server.support.TextTokenizer._

/**
  * Rest service with endpoints to use search and text upload.
  **/
class SearchService(fileService: ActorRef, indexService: ActorRef)
                   (implicit val timeout: Timeout, val blockingExecutor: MessageDispatcher) {

  val searchRoute: Route = getDocument ~ putDocument ~ searchForAllWords


  private def getDocument: Route = path("get" / Segment) {
    id =>
      get {
        complete {
          (fileService ? Get(id)).mapTo[Either[EmptyDocument, DocumentResult]]
        }
      }
  }

  private def putDocument: Route = path("put" / Segment) {
    id =>
      put {
        entity(as[String]) {
          doc =>
            complete {
              val words = textToWords(doc)
              (fileService ? Put(id, doc))
                .flatMap(res =>
                  indexService ? Indexing(words, id))
                .mapTo[IndexUpdateResponse]
            }
        }
      }
  }

  private def searchForAllWords: Route = path("search") {
    parameters('searchQuery) {
      searchQuery =>
        get {
          complete {
            val wordSet = textToWords(searchQuery)
            (indexService ? Search(wordSet)).mapTo[IndexSearchResponse]
          }
        }
    }
  }
}
