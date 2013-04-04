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

class RepositorySpec extends WordSpec with ShouldMatchers with BeforeAndAfter {
  var tempDir: Path = null
  var baseRepoDir: String = null
  var toFree: List[Freeable] = Nil

  before {
    tempDir = Files.createTempDirectory("sgit")
    baseRepoDir = Files.createTempDirectory(tempDir, "baseRepo").toString
    Repository.init(baseRepoDir.toString) should be('success)
  }

  after {
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

  def withRepo(name: String)(testCode: Repository => Any) {
    val subDir = Files.createTempDirectory(tempDir, "newRepos").toRealPath()
    val repo = Repository.init(subDir.toString).get
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
    "be able to open an existing repo" in {
      val r = Repository(baseRepoDir)
      r should be('success)
      toFree ::= r.get
    }
    "be freeable" in {
      Repository(baseRepoDir).get.free
    }
    "New Repositories" should {
      "have an orphaned head" in withRepo("orphanedHead") { repo =>
        repo.isHeadOrphan.get should be(true)
      }
      "be empty" in withRepo("empty") { repo =>
        repo.isEmpty.get should be(true)
      }
      "not be bare" in withRepo("notBare") { repo =>
        repo.isBare.get should not be (true)
      }
      "not have a detached head" in withRepo("detachedHead") { repo =>
        repo.isHeadDetached.get should be(false)
      }
      "have an index" in withRepo("newIndex") { repo =>
        repo.index should be('success)
      }
    }
  }

}