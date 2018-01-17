package search.client

import scala.concurrent.Future

trait Client {

  def get(key: String): Future[String]

  def put(key: String, document: String): Future[Unit]

  def search(words: List[String]): Future[Set[String]]

}

object Client {

  def apply(host: String, port: Int): Client = new SearchClient(host, port)

  private class SearchClient(host: String, port: Int) extends Client {

    override def get(key: String): Future[String] = ???

    override def put(key: String, document: String): Future[Unit] = ???

    override def search(words: List[String]): Future[Set[String]] = ???
  }

}
