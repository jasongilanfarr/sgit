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

class RepositorySpec extends WordSpec with ShouldMatchers with BeforeAndAfterAll {
  val tempDir = Files.createTempDirectory("sgit")
  var toFree: List[Freeable] = Nil

  override def afterAll {
    Files.walkFileTree(tempDir, new SimpleFileVisitor[Path]() {
      override def visitFile(file: Path, attr: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
      override def postVisitDirectory(dir: Path, ex: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
    toFree.foreach(_.free)
  }

  def withRepo(name: String, bare: Boolean = false)(testCode: Repository => Any) {
    require(tempDir != null)
    val subDir = Files.createTempDirectory(tempDir, "newRepos").toRealPath()
    val repo = Repository.init(subDir.toString, bare).get
    toFree ::= repo
    testCode(repo)
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
    "New Repositories" should withRepo("newRepos") { repo =>
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
  }

}