package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.Memory
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success

class Oid(val ptr: Pointer) extends PointerType(ptr) with Freeable with Ordered[Oid] {
  def this() = this(Pointer.NULL)

  def fmt(): String = {
    val strPtr = Git2.oid_allocfmt[Pointer](this)
    val ret = strPtr.getString(0)
    //Git2._free(strPtr) - git__free doesn't exist?
    ret
  }

  
  protected override def freeObject() {
    // TODO: Free the ptr
  }
  
  override def equals(that: Any) = that match {
    case t if t.isInstanceOf[Oid] =>
      Git2.oid_equal[Int](this, that) == 1
    case _ => false
  }

  override def compare(that: Oid) = Git2.oid_cmp[Int](this, that)
  
  implicit override def toString: String = fmt
}

object Oid {
  private[sgit] def apply(ptr: Pointer): Oid = {
    new Oid(ptr)
  }
  
  def fromRaw(raw: String) : Oid = {
    val ptr = new PointerByReference
    Git2.oid_fromraw[Void](ptr, raw)
    Oid(ptr.getValue)
  }
  
  def fromString(str: String) : Try[Oid] = {
    val ptr = new PointerByReference
    Git2.oid_fromstr[Int](ptr, str) match {
      case 0 => Success(Oid(ptr.getValue))
      case x => Git2.exception(x)
    } 
  }
}