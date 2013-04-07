package org.thisamericandream.sgit

import com.sun.jna.Pointer
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import org.thisamericandream.sgit.struct.SignatureT
import org.thisamericandream.sgit.struct.TimeT
import scala.collection.mutable.Buffer

class Commit private[sgit] (val ptr: Pointer) extends GitObject(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  def message(): String = {
    Git2.commit_message[String](ptr)
  }

  def messageEncoding(): String = {
    Git2.commit_message_encoding[String](ptr)
  }

  def committer(): SignatureT = {
    Git2.commit_committer[SignatureT](ptr)
  }

  def commitAuthor(): SignatureT = {
    Git2.commit_author[SignatureT](ptr)
  }

  def epochTime(): TimeT = {
    Git2.commit_time[TimeT](ptr)
  }

  def tree(): Try[Tree] = {
    val ptrRef = new PointerByReference
    Git2.commit_tree[Int](ptrRef, ptr) match {
      case 0 => Success(new Tree(ptrRef.getValue))
      case x => Git2.exception(x)
    }
  }

  def treeId(): Oid = {
    val oid = new OidT
    new Oid(Git2.commit_tree_id[OidT](ptr))
  }

  def parents(): Seq[Commit] = {
    val parentCount = Git2.commit_parent_count[Int](ptr)
    val seq = Buffer[Commit]()
    var i = 0
    while (i < parentCount) {
      val ptrRef = new PointerByReference
      Git2.commit_parent[Int](ptrRef, ptr, i) match {
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