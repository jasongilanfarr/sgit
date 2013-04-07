package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import scala.util.Try
import scala.util.Success
import com.sun.jna.ptr.PointerByReference

class Branch private[sgit] (ptr: Pointer) extends PointerType(ptr) with GitObject {
  def this() = this(Pointer.NULL)

  override protected def freeObject() {
    Git2.branch_free[Void](this)
  }
}

object Branch {
  def create(repo: Repository, name: String, target: Commit, force: Boolean = false): Try[Branch] = {
    val ptr = new PointerByReference
    Git2.branch_create[Int](ptr, repo, name, target, force) match {
      case 0 => Success(new Branch(ptr.getValue))
      case x => Git2.exception(x)
    }
  }
  def lookup() {}
  def allNames() {}
  def all() {}

}