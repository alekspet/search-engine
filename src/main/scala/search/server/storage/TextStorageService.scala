package search.server.storage

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props, Stash}
import akka.dispatch.MessageDispatcher
import akka.util.Timeout
import search.protocol._
import search.shard.IndexNode.WordResult

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

/**
  * The service for control access to storage nodes and act with them.
  **/
class TextStorageService(shardNodes: Seq[ActorSelection]) extends Actor with ActorLogging with Stash {

  private implicit val timeout: Timeout = Timeout(5.seconds)
  private implicit val blockingExecutor: MessageDispatcher = context.system.dispatchers.lookup("blocking-dispatcher")

  private val selectForId: String => ActorSelection = TextStorageService.selectShard(shardNodes)
  private var searches = ListBuffer[Set[String]]()

  override def receive: Receive = doService()

  def doService(): Receive = {
    case Put(id, doc) =>
      log.info(s"Document persisted to storage with id : $id")
      val selection = selectForId(id)
      selection ! Text(id, doc)
      sender ! SaveDone(id)
    case Get(id) =>
      log.info(s"Get document by id $id")
      val selection = selectForId(id)
      selection forward Select(id)
    case Search(allContains) =>
      log.info("Search in index for words")
      val respondTo = sender
      context.become(gatherSearchResults(respondTo))
      shardNodes.foreach(_ ! IndexSearch(allContains))

  }

  def gatherSearchResults(respondTo: ActorRef): Receive = {
    case WordResult(ids) if (searches.size + 1) == shardNodes.size =>
      searches :+= ids
      log.info(s"Search done $searches reply to $respondTo")
      val searchResult = searches.flatten.toSet
      respondTo ! SearchResponse(searchResult)
      searches = ListBuffer.empty
      context.unbecome()
      unstashAll()
    case WordResult(ids) =>
      log.info(s"Search response from shard")
      searches :+= ids
    case _ =>
      stash()
  }
}


object TextStorageService {

  private val TURN_OFF_SIGN_BIT_MASK = 0x7FFFFFFF

  def props(nodes: Seq[ActorSelection]): Props = Props(classOf[TextStorageService], nodes)

  private def selectShard(nodes: Seq[ActorSelection])
                         (partitionKey: String): ActorSelection = nodes(
    (TURN_OFF_SIGN_BIT_MASK & partitionKey.hashCode) % nodes.size
  )

}
