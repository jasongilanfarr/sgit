package org.thisamericandream.sgit

import com.sun.jna.Pointer

class Commit private[sgit] (val ptr: Pointer) extends GitObject(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  override def freeObject() {
    Git2.commit_free(this)
  }
}

object Commit {

}