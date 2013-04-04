package org.thisamericandream.sgit

import com.sun.jna.Pointer
import java.util.concurrent.atomic.AtomicBoolean
import java.io.Closeable
import com.sun.jna.PointerType
import scala.util.Try
import com.sun.jna.ptr.PointerByReference
import scala.util.Success

class Reference private[sgit](val ptr: Pointer) extends PointerType(ptr) with Freeable {
  def this() = this(Pointer.NULL)
  
  def name(): String = {
    Git2.reference_name[String](this)
  }

  def target(): Oid = {
      val oidPtr = Git2.reference_target[Pointer](this)
      Oid(oidPtr)
  }
  
  def hasLog(): Try[Boolean] = {
    Git2.boolValue(Git2.reference_has_log[Int](this))
  }
  
  def isBranch(): Try[Boolean] = {
    Git2.boolValue(Git2.reference_is_branch[Int](this))
  }
  
  def isRemote(): Try[Boolean] = {
    Git2.boolValue(Git2.reference_is_remote[Int](this))
  }
  
  def isValidName(): Try[Boolean] = {
    Git2.boolValue(Git2.reference_is_valid_name[Int](this))
  }
 
  def owner(): Repository = {
    val ptr = Git2.reference_owner[Pointer](this)
    new Repository(ptr)
  }
  
  def rename(newName: String, force: Boolean) : Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_rename[Int](ptr, this, newName, if (force) { 1 } else { 0 }) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }
  

  protected override def freeObject() {
      Git2.reference_free[Unit](ptr)
  }
}
