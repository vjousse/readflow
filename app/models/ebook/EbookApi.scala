package readflow.ebook

import java.io.File

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

import org.pegdown.{Extensions, PegDownProcessor}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

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

    def mdFiles(file: File): Boolean =
      file.getAbsolutePath().toLowerCase().endsWith(".md")

    listDirectoryForUser(directory, user, mdFiles).map {
      _.map(file =>
          print(markdownToHtml(file)))
    }
  }

  def markdownToHtml(file: File, encoding: String = "utf-8"): String =
    (new PegDownProcessor(Extensions.ALL)).markdownToHtml(
      Source.fromFile(file, encoding).getLines.toList.mkString("\n")
    )

  def listDirectoryForUser(
    dir: String,
    user: User,
    f: (File => Boolean) = (a=> true)) : Future[List[File]]= {
    Future(listFiles(new File(userApi.pathForUser(user) + dir)).filter(f).toList)
  }

  def listRecursiveDirectoryForUser(dir: String, user: User) : Future[List[File]]= {
    Future(recursiveListFiles(new File(userApi.pathForUser(user) + dir)).toList)
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
