package org.thisamericandream.sgit

import com.sun.jna.Pointer
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import org.thisamericandream.sgit.struct.SignatureT
import org.thisamericandream.sgit.struct.TimeT
import scala.collection.mutable.Buffer
import com.sun.jna.PointerType

class Commit private[sgit] (val ptr: Pointer) extends PointerType(ptr) with GitObject with Freeable {
  def this() = this(Pointer.NULL)

  def message(): String = {
    Git2.commit_message[String](this)
  }

  def messageEncoding(): String = {
    Git2.commit_message_encoding[String](this)
  }

  def committer(): SignatureT = {
    Git2.commit_committer[SignatureT](this)
  }

  def commitAuthor(): SignatureT = {
    Git2.commit_author[SignatureT](this)
  }

  def epochTime(): TimeT = {
    Git2.commit_time[TimeT](this)
  }

  def tree(): Try[Tree] = {
    val ptrRef = new PointerByReference
    Git2.commit_tree[Int](ptrRef, this) match {
      case 0 => Success(new Tree(ptrRef.getValue))
      case x => Git2.exception(x)
    }
  }

  def treeId(): Oid = {
    val oid = new OidT
    new Oid(Git2.commit_tree_id[OidT](this))
  }

  def parents(): Seq[Commit] = {
    val parentCount = Git2.commit_parent_count[Int](this)
    val seq = Buffer[Commit]()
    var i = 0
    while (i < parentCount) {
      val ptrRef = new PointerByReference
      Git2.commit_parent[Int](ptrRef, this, i) match {
        case 0 => seq += new Commit(ptrRef.getValue)
        case x => i = parentCount
      }
      i += 1
    }
    seq.toSeq
  }

  def parentIds: Seq[Oid] = {
    val parentCount = Git2.commit_parent_count[Int](this)
    val seq = Buffer[Oid]()
    var i = 0
    while (i < parentCount) {
      val ptrRef = new PointerByReference
      new Oid(Git2.commit_parent_id[OidT](ptrRef, ptr, i))
      i += 1
    }
    seq.toSeq
  }

  override def freeObject() {
    Git2.commit_free(this)
  }
}

object Commit {
  def lookup(repo: Repository, id: Oid): Try[Commit] = {
    GitObject.lookup[Commit](repo, id)
  }
}