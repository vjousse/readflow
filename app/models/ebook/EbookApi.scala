package readflow.ebook

import java.io.File

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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

  def createEbookForDirectory(directory: String) = {
    val localDir = new File(directory)
    if(localDir.isDirectory()) {

    }
  }

  def listDirectoryForUser(dir: String, user: User) : Future[List[File]]= {
    Future(recursiveListFiles(new File(userApi.pathForUser(user) + dir)).toList)
  }

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

}
