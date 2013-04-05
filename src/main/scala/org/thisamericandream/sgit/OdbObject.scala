package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType

class OdbObject private[sgit] (ptr: Pointer) extends PointerType {
  def this() = this(Pointer.NULL)

}

object OdbObject {
  private[sgit] def apply(ptr: Pointer) = new OdbObject(ptr)
}