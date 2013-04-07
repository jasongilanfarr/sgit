package org.thisamericandream.sgit

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec

class BlobSpec extends WordSpec with ShouldMatchers with TestRepository {
  "Blobs" should withTestRepo() { repo =>
    "be able to read data" in {
      val blob = Blob.lookup(repo, "b02def2d0ff040b219b32ff5611e164f7252bc9f").get
      new String(blob.content.map(_.toChar)) should equal("test\ntest2\n")
      blob.`type` should equal(OType.Blob)
      blob.id.toString should equal("b02def2d0ff040b219b32ff5611e164f7252bc9f")
    }
    "be able to write data" in {
      Blob.create(repo, "new blob content".getBytes) should be('success)
    }
    "fetch blob content with nulls" in {
      val content = "\0hello\0there\0"
      val oid = repo.write(content.getBytes, OType.Blob).get
      val blob = repo.lookup[Blob](oid)
      new String(blob.get.content.map(_.toChar)) should equal(content)
    }
  }
}