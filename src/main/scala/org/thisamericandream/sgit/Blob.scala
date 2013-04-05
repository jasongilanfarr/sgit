package org.thisamericandream.sgit

import com.sun.jna.Pointer

class Blob private[sgit] (val ptr: Pointer) extends GitObject(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  override def freeObject() {
    Git2.blob_free(this)
  }
}

object Blob {

}