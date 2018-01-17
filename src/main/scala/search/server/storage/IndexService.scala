package search.server.storage

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import com.typesafe.config.Config
import search.index.IndexNode.IndexItem
import search.server.storage.IndexService.{IndexSearchResponse, IndexUpdateResponse, Indexing, Search}

class IndexService(nodeNames: Seq[String]) extends Actor with ActorLogging {

  private val nodes: Seq[ActorSelection] = nodeNames.map(context.actorSelection)

  private def selectForWord: String => ActorSelection = IndexService.selectShard(nodes)

  override def receive: Receive = {

    case Search(allContains) =>
      log.info("Index search request for words")
      sender ! IndexSearchResponse(Set("found"))

    case Indexing(words, id) =>
      log.info(s"Index update request for document $id")
      words.map(IndexItem(_, id)).foreach(
        item => selectForWord(item.word) ! item
      )
      sender ! IndexUpdateResponse(words.size)

  }
}


object IndexService {

  case class Search(allContains: Set[String])

  case class Indexing(words: Set[String], id: String)

  case class IndexSearchResponse(ids: Set[String])

  case class IndexUpdateResponse(count: Int)


  def props(nodes: Seq[Config]): Props = Props(classOf[IndexService], nodes.map(toActorUri))

  def props(): Props = Props(classOf[IndexService], Seq())

  private def selectShard(nodes: Seq[ActorSelection])
                         (word: String): ActorSelection = nodes(word.hashCode % nodes.size)


  private def toActorUri(config: Config): String =
    s"akka.tcp://index-node@${config.getString("host")}:${config.getString("port")}/user/index"

}
