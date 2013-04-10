package org.thisamericandream.sgit

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec

class ReferenceSpec extends WordSpec with ShouldMatchers with TestRepository {
  "Reference" should withTestRepo() { repo =>
    "list reference names" in {
      Reference.allNames(repo).map(_.replace("refs/", "")).sorted should equal(Seq("heads/master", "remotes/origin/master", "tags/test-tag"))
    }
    "list references" in {
      Reference.allNames(repo).size should equal(Reference.all(repo).size)
    }
    "lookup a non-existing reference should fail" in {
      Reference.lookup(repo, "lol/wat") should be('failure)
    }
    "lookup a reference" in {
      val ref = Reference.lookup(repo, "refs/heads/master").get
      ref.name should equal("refs/heads/master")
      ref.target should equal("536c169501d92f7abc1ebbad1b79ba63a2e40e67")
      ref should be('direct)
    }
    "should load the reflog" in {
      val ref = Reference.lookup(repo, "refs/heads/master").get
      ref.hasLog.get should be(false)
    }
  }

}