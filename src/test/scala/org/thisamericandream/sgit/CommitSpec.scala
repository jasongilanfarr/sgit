package org.thisamericandream.sgit

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec

class CommitSpec extends WordSpec with ShouldMatchers with TestRepository {
  "Commits" should withTestRepo() { repo =>
    "be readable" in {
      val commit = repo.lookup[Commit]("536c169501d92f7abc1ebbad1b79ba63a2e40e67").get
      commit.id should equal("536c169501d92f7abc1ebbad1b79ba63a2e40e67")
      commit.`type` should equal(OType.Commit)
      commit.message should equal("Add a second line\n")
      //commit.epochTime should equal(1112312312)
      // TODO: add time once it doesn't crash
      val c = commit.committer
      c.name should equal("Jason Gilanfarr")
      c.email should equal("jason.gilanfarr@gmail.com")
      
      val a = commit.author
      a.name should equal("Jason Gilanfarr")
      a.email should equal("jason.gilanfarr@gmail.com")
      
      commit.tree.get.id should equal("207ef088ee067e000e6e0047bce198411b804f41")
      commit.parentIds should equal (Seq("a569a912e0d165942e4bcf7f08331f0f9fbd8e00"))
      commit.parents.map(_.id) should equal (Seq("a569a912e0d165942e4bcf7f08331f0f9fbd8e00"))
    }
  }

}