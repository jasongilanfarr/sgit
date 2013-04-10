package org.thisamericandream.sgit

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec

class ObjectSpec extends WordSpec with ShouldMatchers with TestRepository {
  "GitObject" should withTestRepo() { repo =>
    "fail to lookup an object that doesn't exist" in {
      GitObject.lookup[GitObject](repo, "a496071c1b46c854b31185ea97743be6a8774479") should be('failure)
    }
    "lookup an object" in {
      val obj = GitObject.lookup[GitObject](repo, "536c169501d92f7abc1ebbad1b79ba63a2e40e67").get
      obj.getClass should equal(classOf[Commit])
      obj.`type` should equal(OType.Commit)
    }
    "give the equal objects for the same lookup" in {
      val obj1 = GitObject.lookup[GitObject](repo, "536c169501d92f7abc1ebbad1b79ba63a2e40e67").get
      val obj2 = GitObject.lookup[GitObject](repo, "536c169501d92f7abc1ebbad1b79ba63a2e40e67").get
      obj1 should equal(obj2)
    }
    "lookup by revision" in {
      repo.revParse("HEAD") should be('success)
      repo.revParse("536c1695") should be('success)
    }
  }
}