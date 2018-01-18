package search.index

import akka.actor.{Actor, ActorLogging, Props}
import search.protocol.{IdsForWord, IndexItem, WordResult}

import scala.collection.mutable

class IndexNode extends Actor with ActorLogging {

  private val invertedIndex: mutable.Map[String, Set[String]] = mutable.Map.empty

  override def receive: Receive = {
    case IndexItem(word, id) =>
      log.info(s"index successfully updated for document id $id with word $word")
      invertedIndex += word -> (invertedIndex.getOrElse(word, Set()) ++ Set(id))
    case IdsForWord(word) =>
      val ids = invertedIndex.getOrElse(word, Set())
      log.info(s"found documents set $ids for word $word")
      sender ! WordResult(word, ids)

  }
}

object IndexNode {

  def props(): Props = Props(classOf[IndexNode])
}
