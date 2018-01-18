package search.server.storage

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import akka.dispatch.MessageDispatcher
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import search.protocol.{IdsForWord, IndexItem, WordResult}
import search.server.storage.IndexService.{IndexSearchResponse, IndexUpdateResponse, Indexing, Search}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * The service for control access to index nodes and act with them.
  **/
class IndexService(nodes: Seq[ActorSelection]) extends Actor with ActorLogging {

  private def selectForWord: String => ActorSelection = IndexService.selectShard(nodes)

  private implicit val timeout: Timeout = Timeout(5.seconds)
  private implicit val blockingExecutor: MessageDispatcher = context.system.dispatchers.lookup("blocking-dispatcher")

  override def receive: Receive = {

    case Search(allContains) =>
      log.info("Index search request for words")
      val indexNodesResult: Set[Future[WordResult]] = gatherIndexedResults(allContains)
      computeDocIdsIntersection(indexNodesResult)
        .map(IndexSearchResponse)
        .pipeTo(sender)

    case Indexing(words, id) =>
      log.info(s"Index update request for document $id")
      sendIndexUpdate(words, id)
      sender ! IndexUpdateResponse(words.size)

  }

  private def computeDocIdsIntersection(indexNodesResult: Set[Future[WordResult]]): Future[Set[String]] = {
    Future.sequence(indexNodesResult
      .map(_.map(_.ids)))
      .map(_.reduceLeft(_ & _))
  }

  private def gatherIndexedResults(allContains: Set[String]): Set[Future[WordResult]] = {
    val indexNodesResult = allContains.map {
      word =>
        val indexNode = selectForWord(word)
        (indexNode ? IdsForWord(word)).mapTo[WordResult]
    }
    indexNodesResult
  }

  private def sendIndexUpdate(words: Set[String], id: String): Unit = {
    words.map(IndexItem(_, id)).foreach(
      item => {
        val indexNode = selectForWord(item.word)
        indexNode ! item
      }
    )
  }
}


object IndexService {

  case class Search(allContains: Set[String])

  case class Indexing(words: Set[String], id: String)

  case class IndexSearchResponse(ids: Set[String])

  case class IndexUpdateResponse(wordsIndexed: Int)

  def props(nodes: Seq[ActorSelection]): Props = Props(classOf[IndexService], nodes)

  private val TURN_OFF_SIGN_BIT_MASK = 0x7FFFFFFF

  private def selectShard(nodes: Seq[ActorSelection])
                         (word: String): ActorSelection = nodes((TURN_OFF_SIGN_BIT_MASK & word.hashCode) % nodes.size)

}
