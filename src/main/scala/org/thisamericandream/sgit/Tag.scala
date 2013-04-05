package org.thisamericandream.sgit

import com.sun.jna.Pointer

class Tag private[sgit] (val ptr: Pointer) extends GitObject(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  override def freeObject() {
    Git2.tag_free(this)
  }
}

object Tag {

}