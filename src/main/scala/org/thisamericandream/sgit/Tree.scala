package org.thisamericandream.sgit

import com.sun.jna.Pointer
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.NativeLong
import scala.collection.mutable.Buffer
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import com.sun.jna.PointerType

class Tree private[sgit] (val ptr: Pointer) extends PointerType(ptr) with GitObject with Freeable {
  def this() = this(Pointer.NULL)

  override def id: Oid = {
    new Oid(Git2.tree_id[OidT](this))
  }

  def entryCount: Long = {
    Git2.tree_entrycount[NativeLong](this).longValue
  }

  def entries: Seq[TreeEntry] = {
    val count = entryCount
    var i = 0
    val seq = Buffer[TreeEntry]()
    while (i < count) {
      seq += Git2.tree_entry_byindex[TreeEntry](this, i)
      i += 1
    }
    seq.toSeq
  }

  def entryByPath(path: String): Try[TreeEntry] = {
    val ptrRef = new PointerByReference
    Git2.tree_entry_bypath[Int](ptrRef, this, path) match {
      case 0 => Success(new TreeEntry(ptrRef.getValue))
      case x => Git2.exception(x)
    }
  }

  override def freeObject() {
    Git2.tree_free(this)
  }
}

object Tree {

}