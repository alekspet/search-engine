# Simple search engine.

### Components
- Http Client
- Http Server
- Distributed storage with search index

To run and execute project you need to have 'sbt' installed. The project structure include 2 server side component and client from root folder.
1. Start http server with config file 'src/main/resources/server.conf': 
```bash
sbt -Dconfig.file=src/main/resources/server.conf "runMain search.server.Server"
```
server has configured storage nodes to works with:

```js
storage {
  nodes = [
    {
      host = "127.0.0.1"
      port = "5150"
    },
    {
      host = "127.0.0.1"
      port = "5151"
    }
  ]
}
```
2. Default config has 2 storage nodes above to start them on specified ports perform:

```bash
sbt -Dconfig.file=src/main/resources/shard.conf -Dakka.remote.netty.tcp.port=5150 "runMain search.shard.Shard"
```
```bash
sbt -Dconfig.file=src/main/resources/shard.conf -Dakka.remote.netty.tcp.port=5151 "runMain search.shard.Shard"
```
as result you will have server with http endpoints and 2 storage nodes.

3. Server expose api for usage:

**GET /get/:documentId**<br />
**Response:** `{"id":"2","doc":"text of the document"}` | `{}`<br /><br />
**PUT /put/:documentId**<br />
**Request body:** text <br />
**Response:** `{"wordsIndexed":4}`<br /><br />
**GET /search?searchQuery=text to search**<br />
**Response:** `{"ids":["idOne","idTwo"]}`<br /><br />
To use this api you can take client:
```scala
trait Client {

  def get(key: String): Future[Either[EmptyDocument, DocumentResult]]

  def put(key: String, document: String): Future[SaveDone]

  def search(words: String): Future[SearchResponse]

  def shutdown(): Unit

}

// Open client
 val client = Client(host, port) 
 
 //Close client
 client.shutdown()
```

The ab search results on local pc 4 cores:

|conc req   |avg time|
|---------:|-----|
|1         | 2 ms |
|2         | 2 ms |
|4         | 3 ms |
|8         | 5 ms |
|16        | 11 ms |
|32        | 19 ms |
|64        | 38 ms |

