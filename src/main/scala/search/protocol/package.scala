package search

package object protocol {

  val systemName = "index-node"
  val indexName = "index"

  case class IndexItem(word: String, id: String)

  case class IdsForWord(word: String)

  case class WordResult(word: String, ids: Set[String])

}
