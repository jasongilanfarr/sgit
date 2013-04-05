package org.thisamericandream.sgit

import com.sun.jna.Pointer

class Tree private[sgit] (val ptr: Pointer) extends GitObject(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  override def freeObject() {
    Git2.tree_free(this)
  }
}

object Tree {

}