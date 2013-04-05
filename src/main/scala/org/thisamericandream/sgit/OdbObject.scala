package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import scala.util.Try
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.NativeLong

class OdbObject private[sgit] (ptr: Pointer) extends PointerType with Freeable {
  def this() = this(Pointer.NULL)

  def id(): Oid = {
    new Oid(Git2.odb_object_id[OidT](ptr))
  }

  def data(): Array[Byte] = {
    val size = Git2.odb_object_size[NativeLong](ptr)
    val data = Git2.odb_object_data[Pointer](ptr)

    data.getByteArray(0, size.intValue);
  }

  def `type`(): OType = {
    OType.forId(Git2.odb_odbject_type[Int](ptr))
  }

  protected def freeObject() {
    Git2.odb_odbject_free(ptr)
  }
}

object OdbObject {
  private[sgit] def apply(ptr: Pointer) = new OdbObject(ptr)
}