package search.server

import akka.actor.ActorRef
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import search.protocol._
import search.server.support.JsonSupport._
import search.server.support.TextTokenizer._

/**
  * Rest service with endpoints to use search and text upload.
  **/
class SearchService(storageService: ActorRef)
                   (implicit val timeout: Timeout, val blockingExecutor: MessageDispatcher) {

  val searchRoute: Route = getDocument ~ putDocument ~ searchForAllWords


  private def getDocument: Route = path("get" / Segment) {
    id =>
      get {
        complete {
          (storageService ? Get(id))
            .mapTo[Option[Text]]
            .map({
              result =>
                Either.cond(result.isDefined,
                  DocumentResult(result.get.id, result.get.text),
                  EmptyDocument())
            })
        }
      }
  }

  private def putDocument: Route = path("put" / Segment) {
    id =>
      put {
        entity(as[String]) {
          doc =>
            complete {
              (storageService ? Put(id, doc)).mapTo[SaveDone]
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
            (storageService ? Search(wordSet)).mapTo[SearchResponse]
          }
        }
    }
  }
}
