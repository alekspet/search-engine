package search.index

import akka.actor.{Actor, ActorLogging, Props}

import scala.collection.mutable

class IndexNode extends Actor with ActorLogging {

  private val invertedIndex: mutable.Map[String, Set[String]] = mutable.Map.empty

  override def receive: Receive = {
    case x =>
      log.info(s"Recived message $x")
      sender ! Set("no", "no")
  }
}

object IndexNode {

  case class IndexItem(word: String, id: String)

  def props(): Props = Props(classOf[IndexNode])
}
