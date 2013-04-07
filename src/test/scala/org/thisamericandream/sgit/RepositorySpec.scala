package org.thisamericandream.sgit

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileVisitor
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException
import java.nio.file.FileVisitResult
import scala.util.Failure
import org.scalatest.BeforeAndAfterAll

class RepositorySpec extends WordSpec with ShouldMatchers with BeforeAndAfterAll with TestRepository {
  val tempDir = Files.createTempDirectory("sgit")
  var toFree: List[Freeable] = Nil

  override def afterAll {
    FileUtils.deleteRecursively(tempDir)
    toFree.foreach(_.free)
  }

  def withRepo(name: String, bare: Boolean = false)(testCode: Repository => Any) {
    require(tempDir != null)
    val subDir = Files.createTempDirectory(tempDir, "newRepos").toRealPath()
    val repo = Repository.init(subDir.toString, bare).get
    toFree ::= repo
    testCode(repo)
  }

  def withTestRepo(bare: Boolean = false)(testCode: Repository => Any) {
    testCode(testRepo(bare))
  }

  "Repository" should {
    "be creatable" in {
      val subDir = Files.createTempDirectory(tempDir, "init").toRealPath()
      val r = Repository.init(subDir.toString)
      toFree ::= r.get
      r should be('success)
      r.get.isBare.get should be(false)
      r.get.path should equal(subDir.toString + "/.git/")
      r.get.workDir should be('defined)
      new File(r.get.workDir.get).toPath should equal(subDir)
    }
    "be createable as a bare repo" in {
      val subDir = Files.createTempDirectory(tempDir, "init").toRealPath()
      val r = Repository.init(subDir.toString, bare = true)
      toFree ::= r.get
      r should be('success)
      r.get.isBare.get should be(true)
      new File(r.get.path).toPath.toRealPath() should equal(subDir)
      r.get.workDir should be(None)
    }
    "be able to open an existing repo" in withRepo("existing") { repo =>
      val r = Repository(repo.workDir.get)
      r should be('success)
      toFree ::= r.get
    }
    "be freeable" in withRepo("freeable") { repo =>
      Repository(repo.workDir.get).get.free
    }
    "fail to open non-existing repos" in {
      val subDir = Files.createTempDirectory(tempDir, "fail")
      Repository(subDir.toString) should be('failure)
    }
    "when working with new repositories" should withRepo("newRepos") { repo =>
      "have an orphaned head" in {
        repo.isHeadOrphan.get should be(true)
      }
      "be empty" in {
        repo.isEmpty.get should be(true)
      }
      "not be bare" in {
        repo.isBare.get should not be (true)
      }
      "not have a detached head" in {
        repo.isHeadDetached.get should be(false)
      }
      "have an index" in {
        repo.index should be('success)
      }
    }
    "when using a non-bare test repository" should withTestRepo() { repo =>
      "have the correct head" in {
        val head = repo.head
        head should be('success)
        head.get.target should be('defined)
        head.get.target.get should equal(Oid.fromString("536c169501d92f7abc1ebbad1b79ba63a2e40e67").get)
      }
      "be able to check if objects exist" in {
        repo.contains("207ef088ee067e000e6e0047bce198411b804f41").get should be(true)
        repo.contains("8496071c1c46c854b31185ea97743be6a8774479").get should be(false)
      }
      "be able to read a raw object" in {
        val raw = repo.read("b02def2d0ff040b219b32ff5611e164f7252bc9f").get
        raw.`type` should equal(OType.Blob)
        raw.len should equal(11)
        new String(raw.data.map(_.toChar)) should equal("test\ntest2\n")
      }
      "be able to read object headers" in {
        val header = repo.readHeader("b02def2d0ff040b219b32ff5611e164f7252bc9f").get
        header._1 should equal(OType.Blob)
        header._2 should equal(11L)
      }
      "be able to lookup an object" in {
        val obj = repo.lookup("b02def2d0ff040b219b32ff5611e164f7252bc9f").get
        obj.getClass should equal(classOf[Blob])
      }
      "be able to find a reference" in {
        val ref = repo.ref("refs/heads/master")
        ref.recover { case e => println(e.toString) }
        ref.get.name should equal("refs/heads/master")
      }
      "be able to match refs" in {
        repo.refs("refs".r).size should equal(3)
      }
      "list all refs" in {
        repo.refs.size should equal(3)
      }
      "list all tagNames" in {
        val tags = repo.tagNames
        tags.size should equal(1)
        tags.head should equal("refs/tags/test-tag")
      }
    }
  }

}