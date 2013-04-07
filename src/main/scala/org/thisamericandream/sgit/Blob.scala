package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import com.sun.jna.NativeLong
import com.sun.jna.PointerType

class Blob private[sgit] (val ptr: Pointer) extends PointerType(ptr) with GitObject {
  def this() = this(Pointer.NULL)

  def content(): Array[Byte] = {
    val size = Git2.blob_rawsize[NativeLong](this)
    val raw = Git2.blob_rawcontent[Pointer](this)

    raw.getByteArray(0, size.intValue)
  }

  override def freeObject() {
    Git2.blob_free(this)
  }
}

object Blob {
  def lookup(repo: Repository, id: Oid): Try[Blob] = {
    GitObject.lookup[Blob](repo, id)
  }
}