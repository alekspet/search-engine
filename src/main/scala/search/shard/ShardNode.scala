package search.shard

import akka.actor.{Actor, ActorLogging, Props}
import search.protocol.{IndexSearch, Select, Text}
import search.server.support.TextTokenizer._
import search.shard.IndexNode.{IdsForWords, IndexItem}

import scala.collection.mutable

class ShardNode extends Actor with ActorLogging {

  private val texts: mutable.Map[String, String] = mutable.Map.empty
  private val index = context.actorOf(IndexNode.props(), "index")

  override def receive: Receive = {
    case Text(id, text) =>
      log.info(s"Persist text by id $id")
      texts += (id -> text)
      index ! IndexItem(id, textToWords(text))
    case Select(id) =>
      log.info(s"Select text by id $id")
      val text = texts.get(id).map(Text(id, _))
      sender ! text
    case IndexSearch(words) =>
      log.info(s"Search for words $words in shard index")
      index forward IdsForWords(words)

  }
}

object ShardNode {
  def props(): Props = Props(classOf[ShardNode])
}
