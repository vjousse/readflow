package readflow.ebook

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

case class Ebook(
  title: String,
  author: String)

final class EbookApi {

  def createBook(): Either[String, String] = {
    // Create new Book
    val book = new Book()
    val metadata: Metadata = book.getMetadata();

    Left("Book not created")
  }

}
