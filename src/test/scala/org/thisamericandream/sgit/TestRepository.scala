package org.thisamericandream.sgit

import org.scalatest.BeforeAndAfter
import org.scalatest.Suite
import java.nio.file.Files
import org.scalatest.BeforeAndAfterAll
import java.util.UUID

trait TestRepository extends Suite with BeforeAndAfterAll {
  private val tempDir = Files.createTempDirectory("sgit")
  private var reposToFree: List[Repository] = Nil

  def testRepo(bare: Boolean = false): Repository = {
    val dir = Files.createTempDirectory(UUID.randomUUID.toString)

    val repo = Repository.clone("test-repo", dir.toString).get
    reposToFree ::= repo
    repo
  }

  abstract override def afterAll {
    reposToFree.foreach(_.free)
    FileUtils.deleteRecursively(tempDir)
  }
}