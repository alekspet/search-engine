package search.server.storage

import akka.actor.{Actor, ActorLogging, Props}
import search.server.storage.FileService._


/**
  * File persistence api.
  **/
class FileService(fileStorage: FileStorage) extends Actor with ActorLogging {

  override def receive: Receive = {

    case Put(key, doc) =>
      log.info(s"Document persisted to storage with id $key")
      fileStorage.put(key, doc)
      sender ! SaveDone(key)

    case Get(key) =>
      fileStorage.get(key) match {
        case Some(doc) =>
          log.info(s"Document for $key found")
          sender ! Right(DocumentResult(key, doc))
        case None =>
          log.info(s"Document for $key not found")
          sender ! Left(EmptyDocument)
      }
  }
}

object FileService {

  case class Put(key: String, doc: String)

  case class Get(key: String)

  case class SaveDone(id: String)

  case class DocumentResult(id: String, doc: String)

  case class EmptyDocument()

  def props(fileStorage: FileStorage): Props = Props(classOf[FileService], fileStorage)
}
