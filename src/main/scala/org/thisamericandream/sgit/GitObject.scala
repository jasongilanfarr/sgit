package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType

class GitObject private[sgit] (ptr: Pointer) extends PointerType {
  def this() = this(Pointer.NULL)

}

object GitObject {
  private[sgit] def apply(ptr: Pointer) = new GitObject(ptr)
}