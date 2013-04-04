package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType

class RefDb private[sgit] (ptr: Pointer) extends PointerType {
  def this() = this(Pointer.NULL)
}

object RefDb {
  private[sgit] def apply(ptr: Pointer) = new RefDb(ptr)
}