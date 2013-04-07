package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import com.sun.jna.NativeLong
import com.sun.jna.PointerType
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.Memory

class Blob private[sgit] (val ptr: Pointer) extends PointerType(ptr) with GitObject {
  def this() = this(Pointer.NULL)

  def content(): Array[Byte] = {
    val size = Git2.blob_rawsize[NativeLong](this)
    val raw = Git2.blob_rawcontent[Pointer](this)

    raw.getByteArray(0, size.intValue)
  }

  def isBinary(): Boolean = {
    Git2.blob_is_binary[Int](this) == 1
  }

  override def freeObject() {
    Git2.blob_free(this)
  }
}

object Blob {
  def lookup(repo: Repository, id: Oid): Try[Blob] = {
    GitObject.lookup[Blob](repo, id)
  }

  def fromWorkDir(repo: Repository, file: String): Try[Oid] = {
    val oid = new OidT
    Git2.blob_create_fromworkdir[Int](oid, repo, this) match {
      case 0 => Success(new Oid(oid))
      case x => Git2.exception(x)
    }
  }

  def create(repo: Repository, bytes: Array[Byte]): Try[Oid] = {
    val oid = new OidT
    val buf = new Memory(bytes.length)
    buf.write(0, bytes, 0, bytes.length)
    Git2.blob_create_frombuffer[Int](oid, repo, buf, bytes.length) match {
      case 0 => Success(new Oid(oid))
      case x => Git2.exception(x)
    }
  }
}