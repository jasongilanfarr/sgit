package org.thisamericandream.sgit

import language.implicitConversions
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.Memory
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import com.sun.jna.Native

class Oid(val ptr: Pointer) extends PointerType(ptr) with Ordered[Oid] {
  def this() = this(Pointer.NULL)

  def fmt(): String = {
    val buf = new Memory(41)
    Git2.oid_fmt[Unit](buf, this)
    buf.getString(0, false)
  }

  def isZero: Boolean = {
    Git2.oid_iszero[Int](this) == 1
  }

  override def equals(that: Any) = that match {
    case t: Oid =>
      Git2.oid_equal[Int](this, that) == 1
    case t: String =>
      Git2.oid_streq[Int](this, t) == 1
    case _ => false
  }

  override def compare(that: Oid) = Git2.oid_cmp[Int](this, that)

  implicit override def toString: String = fmt
}

object Oid {
  private[sgit] def apply(ptr: Pointer): Oid = {
    new Oid(ptr)
  }

  def fromRaw(raw: String): Oid = {
    val ptr = new PointerByReference
    Git2.oid_fromraw[Void](ptr, raw)
    Oid(ptr.getValue)
  }

  def fromString(str: String): Try[Oid] = {
    val ptr = new PointerByReference
    Git2.oid_fromstr[Int](ptr, str) match {
      case 0 => Success(Oid(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  implicit def fromStr(str: String): Oid = fromString(str).get
}