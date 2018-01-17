package search.server.storage

import org.scalatest.{Matchers, WordSpecLike}

class FileStorageTest() extends WordSpecLike with Matchers {


  "FileStorage" must {
    "persist and retrive from in-memory map" in {
      val fileStorage = FileStorage()
      fileStorage.put("1", "doc")
      fileStorage.get("1") shouldEqual Some("doc")
      fileStorage.get("2") shouldEqual None
    }
  }
}
