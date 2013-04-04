package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType

class Index private[sgit] (ptr: Pointer) extends PointerType {
  def this() = this(Pointer.NULL)

}

object Index {
  private[sgit] def apply(ptr: Pointer) = new Index(ptr)
}