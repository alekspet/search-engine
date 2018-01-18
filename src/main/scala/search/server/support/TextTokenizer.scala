package search.server.support

sealed trait TextTokenizer {


  def textToWords(text: String): Set[String] = text
    .trim()
    .split("\\W+")
    .map(normalize)
    .toSet

  def normalize(word: String): String = word.toLowerCase
}


object TextTokenizer extends TextTokenizer