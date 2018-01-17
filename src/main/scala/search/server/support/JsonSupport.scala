package search.server.support

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import search.server.storage.FileService._
import search.server.storage.IndexService.{IndexSearchResponse, IndexUpdateResponse}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

sealed trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val documentFormat: RootJsonFormat[DocumentResult] = jsonFormat2(DocumentResult)
  implicit val emptyDocumentFormat: RootJsonFormat[EmptyDocument] = jsonFormat0(EmptyDocument)

  implicit val indexSearchResponseFormat: RootJsonFormat[IndexSearchResponse] = jsonFormat1(IndexSearchResponse)
  implicit val indexUpdateResponseFormat: RootJsonFormat[IndexUpdateResponse] = jsonFormat1(IndexUpdateResponse)

}

object JsonSupport extends JsonSupport