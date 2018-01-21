package search.shard

import akka.actor.{Actor, ActorLogging, Props}
import search.shard.IndexNode.{IdsForWords, IndexItem, WordResult}

import scala.collection.mutable

class IndexNode extends Actor with ActorLogging {

  private val invertedIndex: mutable.Map[String, Set[String]] = mutable.Map.empty

  override def receive: Receive = {
    case IndexItem(id, words) =>
      log.info(s"index successfully updated for document id $id with word $words")
      words.foreach { word =>
        invertedIndex += word -> (invertedIndex.getOrElse(word, Set()) ++ Set(id))
      }
    case IdsForWords(words) =>
      val onlySearchWords = filterIndexForSeachCriteria(words)
      val ids = intersectIdsForAllSearchedWords(onlySearchWords)
      log.info(s"found documents set $ids for word $words")
      sender ! WordResult(ids)

  }

  private def intersectIdsForAllSearchedWords(values: Iterable[Set[String]]) = {
    if (values.nonEmpty) values.reduce(_ & _) else Set.empty[String]
  }

  private def filterIndexForSeachCriteria(words: Set[String]) = invertedIndex.filter(
    { k =>
      words.contains(k._1)
    }).values

}

object IndexNode {

  case class IndexItem(id: String, words: Set[String])

  case class IdsForWords(words: Set[String])

  case class WordResult(ids: Set[String])

  def props(): Props = Props(classOf[IndexNode])
}
