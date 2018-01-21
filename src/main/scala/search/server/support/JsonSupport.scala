package search.server.support

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import search.protocol.{DocumentResult, EmptyDocument, SaveDone, SearchResponse}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

sealed trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val documentFormat: RootJsonFormat[DocumentResult] = jsonFormat2(DocumentResult)
  implicit val emptyDocumentFormat: RootJsonFormat[EmptyDocument] = jsonFormat0(EmptyDocument)

  implicit val searchResponseFormat: RootJsonFormat[SearchResponse] = jsonFormat1(SearchResponse)
  implicit val saveDoneResponseFormat: RootJsonFormat[SaveDone] = jsonFormat1(SaveDone)

}

object JsonSupport extends JsonSupport