package org.thisamericandream.sgit

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec

class BranchSpec extends WordSpec with ShouldMatchers with TestRepository {
  "Branch" should withTestRepo() { repo =>
    "list all names" in {
      Branch.allNames(repo).sorted should equal(Seq("master", "origin/master"))
    }
    "list all local names" in {
      Branch.allNames(repo, BranchType.Local).sorted should equal(Seq("master"))
    }
    "list all remote names" in {
      Branch.allNames(repo, BranchType.Remote).sorted should equal(Seq("origin/master"))
    }
    "lists the correct repository" in {
      Branch.lookup(repo, "master").get.owner should equal(repo)
    }
    "the lastest commit is correct" in {
      Branch.lookup(repo, "master").get.tip.get.id should equal("536c169501d92f7abc1ebbad1b79ba63a2e40e67")
    }
    "have the correct tracking name" in {
      Branch.lookup(repo, "master").get.trackingName.get should equal(Some("refs/remotes/origin/master"))
    }
    "have a tracking branch" in {
      Branch.lookup(repo, "master").get.tracking.get should be ('defined)
    }
    "lookup local branch" in {
      val branch = Branch.lookup(repo, "master").get
      branch.name.get should equal("master")
      branch.canonicalName should equal("refs/heads/master")
    }
    "lookup remote branch" in {
      val branch = Branch.lookup(repo, "origin/master", BranchType.Remote).get
      branch.name.get should equal("origin/master")
      branch.canonicalName should equal("refs/remotes/origin/master")
    }
    "be createable" in {
      val newBranch = Branch.create(repo, "newBranch", repo.lookup[Commit]("536c169501d92f7abc1ebbad1b79ba63a2e40e67").get).get
      newBranch.name.get should equal("newBranch")
      newBranch.canonicalName should equal("refs/heads/newBranch")
    }
    "be deleteable" in {
      val newBranch = Branch.create(repo, "newBranch2", repo.lookup[Commit]("536c169501d92f7abc1ebbad1b79ba63a2e40e67").get).get
      newBranch.delete
      Branch.lookup(repo, "newBranch2") should be('failure)
    }
    "be renameable" in {
      val newBranch = Branch.create(repo, "newBranch3", repo.lookup[Commit]("536c169501d92f7abc1ebbad1b79ba63a2e40e67").get).get
      newBranch.rename("newBranch-renamed")
      Branch.lookup(repo, "newBranch3") should be ('failure)
      Branch.lookup(repo, "newBranch-renamed") should be ('success)
    }
  }
}