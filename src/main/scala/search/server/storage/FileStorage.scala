package search.server.storage

import java.util.concurrent._

/**
  * File storage simple operations.
  **/
trait FileStorage {

  def put(key: String, doc: String): Unit

  def get(key: String): Option[String]
}

object FileStorage {

  def apply(): FileStorage = new InMemoryFileStorage()

  /**
    * In memory file storage implementation.
    **/
  private class InMemoryFileStorage() extends FileStorage {

    private val storage: ConcurrentMap[String, String] = new ConcurrentHashMap[String, String]()

    override def put(key: String, doc: String): Unit = storage.putIfAbsent(key, doc)

    override def get(key: String): Option[String] = Option(storage.get(key))
  }

}
