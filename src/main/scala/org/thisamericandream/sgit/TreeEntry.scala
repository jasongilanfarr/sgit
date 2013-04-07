package org.thisamericandream.sgit

import com.sun.jna.Pointer
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import com.sun.jna.PointerType

class TreeEntry private[sgit] (val ptr: Pointer) extends PointerType(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  def name(): String = {
    Git2.tree_entry_name[String](this)
  }

  def id(): Oid = {
    new Oid(Git2.tree_entry_id[OidT](this))
  }

  def `type`(): OType = {
    OType.forId(Git2.tree_entry_type[Int](this))
  }

  def toObject(repo: Repository): Try[GitObject] = {
    val ptr = new PointerByReference
    Git2.tree_entry_to_object[Int](ptr, repo, this) match {
      case 0 => GitObject.forPtr(ptr.getValue)
      case x => Git2.exception(x)
    }
  }

  def fileMode(): FileMode = {
    FileMode.forId(Git2.tree_entry_filemode[Int](this))
  }

  override protected def freeObject() {
    Git2.tree_entry_free[Void](this)
  }
}

object TreeEntry {

}