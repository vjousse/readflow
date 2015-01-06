package readflow.ebook

import java.io.{ ByteArrayInputStream, File, FileOutputStream, InputStream }

import nl.siegmann.epublib.domain.{ Author, Book, Metadata, Resource, TOCReference }
import nl.siegmann.epublib.epub.EpubWriter

import org.pegdown.{Extensions, PegDownProcessor}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{ Failure, Success, Try }

import readflow.user.User
import readflow.user.UserApi

case class Ebook(
  title: String,
  author: String)

final class EbookApi(
  userApi: UserApi) {

  def createEbook(): Either[String, String] = {
    // Create new Book
    val book = new Book()
    val metadata: Metadata = book.getMetadata();

    Left("Book not created")
  }

  def createEbookForDirectory(directory: String, user: User) = {

      def getResource(content: String, href: String ): Resource = {
        println("Getting resource for " + content)
        val r= new Resource(new ByteArrayInputStream(content.getBytes()), href)
        println(r)
        r
      }

      def mdFiles(file: File): Boolean =
        file.getAbsolutePath().toLowerCase().endsWith(".md")

    // Lot of java stuff here, let's be safe
    Try {
      // Create new Book
      var book = new Book()
      val metadata = book.getMetadata()

      // Set the title
      metadata.addTitle("Test ebook")

      // Add an Author
      metadata.addAuthor(new Author("Vincent", "Jousse"))

      // Add a section per file
      listDirectoryForUser(directory, user, mdFiles).map {
        _.map(file =>
          // Add Chapter 1
          book.addSection(file.getName(),
            getResource(markdownToHtml(file), file.getName() + ".html")
          )
        )
      }
      // Create EpubWriter
      val epubWriter = new EpubWriter()

      // Write the Book as Epub
      epubWriter.write(book, new FileOutputStream("/home/vjousse/usr/src/scala/readflow/test1_book1.epub"))

    }
  }

  def markdownToHtml(file: File, encoding: String = "utf-8"): String =
    (new PegDownProcessor(Extensions.ALL)).markdownToHtml(
      Source.fromFile(file, encoding).getLines.toList.mkString("\n")
    )

  def markdownToHtmlFile(file: File, encoding: String = "utf-8") = {
    val html = markdownToHtml(file, encoding)
    (new PegDownProcessor(Extensions.ALL)).markdownToHtml(
      Source.fromFile(file, encoding).getLines.toList.mkString("\n")
    )
  }

  def listDirectoryForUser(
    dir: String,
    user: User,
    f: (File => Boolean) = (a=> true)) : Future[List[File]]=
    Future(listFiles(new File(userApi.filesPathForUser(user) + dir)).filter(f).toList)

  def listRecursiveDirectoryForUser(dir: String, user: User) : Future[List[File]]= {
    Future(recursiveListFiles(new File(userApi.filesPathForUser(user) + dir)).toList)
  }

  def listFiles(f: File): Array[File] =
    Option(f.listFiles) getOrElse Array[File]()

  def recursiveListFiles(f: File): Array[File] = {
    val these: Option[Array[File]] = Option(f.listFiles)
    these.map { files =>
      files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
    } getOrElse Array[File]()
  }

}
