package search

package object protocol {

  val systemName = "shard-node"
  val shardName = "shard"

  // Rest API protocol requests
  case class Put(id: String, doc: String)
  case class Get(id: String)
  case class Search(allContains: Set[String])
  // Rest API protocol responses
  case class SaveDone(id: String)
  case class EmptyDocument()
  case class DocumentResult(id: String, doc: String)
  case class SearchResponse(ids: Set[String])


  // Storage usage protocol
  case class Select(id: String)
  case class Text(id: String, text: String)
  case class IndexSearch(allContains: Set[String])

}
