package readflow.test

import org.specs2.mutable._

import readflow.ebook.EbookApi

class EbookApiSpec extends Specification {

  "EbookApi" should {

    "convert .md paths to .html paths" in new ReadflowApplication {

      ebookApi.createEbooksForUser(user)

    }
  }

}
