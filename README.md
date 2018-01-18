# Simple search engine.

### Components
- Http Client
- Http Server
- Distributed search index

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

