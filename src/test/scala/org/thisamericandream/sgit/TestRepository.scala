package org.thisamericandream.sgit

import org.scalatest.BeforeAndAfter
import org.scalatest.Suite
import java.nio.file.Files
import org.scalatest.BeforeAndAfterAll
import java.util.UUID
import java.net.URLClassLoader
import java.io.File

trait TestRepository extends Suite with BeforeAndAfterAll {
  private val tempDir = Files.createTempDirectory("sgit")
  private var reposToFree: List[Repository] = Nil

  def testRepo(bare: Boolean = false): Repository = {
    val dir = Files.createTempDirectory(UUID.randomUUID.toString)
    val testRepoDir = getClass.getClassLoader.asInstanceOf[URLClassLoader].getURLs.find(_.toString.contains("test-classes")).get
    val repo = Repository.clone(testRepoDir.getFile + "test-repo", dir.toString).get
    reposToFree ::= repo
    repo
  }

  abstract override def afterAll {
    reposToFree.foreach(_.free)
    FileUtils.deleteRecursively(tempDir)
  }
}