package org.thisamericandream.sgit

import language.implicitConversions
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.Memory
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import com.sun.jna.Native
import org.thisamericandream.sgit.struct.OidT

class Oid(val oidT: OidT) extends PointerType(oidT.getPointer) with Ordered[Oid] {
  def this() = this(new OidT)

  def fmt(): String = {
    val buf = new Memory(40)
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
  def fromRaw(raw: String): Oid = {
    val oid = new Oid
    Git2.oid_fromraw[Unit](oid, raw)
    oid
  }

  def fromString(str: String): Try[Oid] = {
    val oid = new Oid
    Git2.oid_fromstr[Int](oid, str) match {
      case 0 => Success(oid)
      case x => Git2.exception(x)
    }
  }

  implicit def fromStr(str: String): Oid = fromString(str).get
}