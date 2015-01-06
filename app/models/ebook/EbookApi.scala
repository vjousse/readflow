package readflow.ebook

import java.io.{ ByteArrayInputStream, File, FileOutputStream, InputStream, PrintWriter, FileInputStream }

import nl.siegmann.epublib.domain.{ Author, Book, Metadata, Resource, TOCReference }
import nl.siegmann.epublib.epub.EpubWriter

import org.pegdown.{Extensions, PegDownProcessor}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{ Failure, Success, Try }

import readflow.user.User
import readflow.user.UserApi
import org.apache.commons.io.{FileUtils, FilenameUtils}

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

      def getResource(file: File, href: String): Resource = {
        println("Getting resource for file " + file.getAbsolutePath())
        val r = new Resource(new FileInputStream(file), href)
        println("Here is the resource : " + r)
        r
      }

      def mdFiles(file: File): Boolean =
        file.getAbsolutePath().toLowerCase().endsWith(".md")

    // Lot of java stuff here, let's be safe
    Try {
      // Create new Book
      var book = new Book()
      var metadata = book.getMetadata()

      // Set the title
      metadata.addTitle("Test ebook")

      // Add an Author
      metadata.addAuthor(new Author("Vincent", "Jousse"))

      // Add a section per file
      listDirectoryForUser(directory, user, mdFiles).map {
        _.map(file =>
          // Add Chapter
          markdownToHtmlFile(file, new File(userApi.htmlPathForFilePath(file.getAbsolutePath(), user))) match {
            case Success(f) => {
              book.addSection(
                file.getName(),
                getResource(f, f.getName() + ".html")
              )
            }

            case Failure(e) => println("Unable to create html file for " + file)
          }
        )
      }
      println(book)

      println(book.getTableOfContents().size())
      // Create EpubWriter
      var epubWriter = new EpubWriter()

      // Write the Book as Epub
      epubWriter.write(book, new FileOutputStream("test1_book1.epub"))

    }
  }

  def markdownToHtml(file: File, encoding: String = "utf-8"): String =
    (new PegDownProcessor(Extensions.ALL)).markdownToHtml(
      Source.fromFile(file, encoding).getLines.toList.mkString("\n")
    )

  def markdownToHtmlFile(sourceFile: File, destinationFile: File, encoding: String = "utf-8"): Try[File] =
    // Jeez, that's a lot of Java stuff
    Try {
      val html = markdownToHtml(sourceFile, encoding)
      val destinationDirectory = FilenameUtils.getFullPath(destinationFile.getAbsolutePath())
      FileUtils.forceMkdir(new File(destinationDirectory))

      val writer = new PrintWriter(destinationFile)
      writer.write(html)
      writer.close()

      destinationFile
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
