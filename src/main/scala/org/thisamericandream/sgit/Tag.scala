package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.Callback
import scala.collection.mutable.Buffer
import org.thisamericandream.sgit.struct.OidT
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class Tag private[sgit] (val ptr: Pointer) extends PointerType(ptr) with GitObject {
  def this() = this(Pointer.NULL)

  override def id: Oid = {
    new Oid(Git2.tag_id[OidT](this))
  }

  def message: String = {
    Git2.tag_message[String](this)
  }

  def name: String = {
    Git2.tag_name[String](this)
  }

  override def peel[T >: GitObject](`type`: OType = OType.Commit): Try[GitObject] = {
    val ptrRef = new PointerByReference
    Git2.tag_peel[Int](ptrRef, this) match {
      case 0 => GitObject.forPtr(ptrRef.getValue)
      case x => Git2.exception(x)
    }
  }

  def targetId: Oid = {
    new Oid(Git2.tag_target_id[OidT](this))
  }

  def target: Try[GitObject] = {
    val ptrRef = new PointerByReference
    Git2.tag_target[Int](ptrRef, this) match {
      case 0 =>
        GitObject.forPtr(ptrRef.getValue)
      case x => Git2.exception(x)
    }
  }

  def targetType: OType = {
    OType.forId(Git2.tag_target_type[Int](this))
  }

  override def toString: String = name

  override def freeObject() {
    Git2.tag_free(this)
  }
}

object Tag {
  def all(repo: Repository): Seq[Tag] = {
    allNames(repo).map(Tag(repo, _)).filter(_.isSuccess).map(_.get)
  }

  def allNames(repo: Repository): Seq[String] = {
    allNamesWithOid(repo).map(_._1)
  }

  def apply(repo: Repository, name: String): Try[Tag] = {
    Reference.fromName(repo, name).flatMap(oid =>
      GitObject.lookup[Tag](repo, oid))
  }

  def allNamesWithOid(repo: Repository): Seq[(String, Oid)] = {
    var tags = Buffer[(String, Oid)]()
    class ForeachCb extends Callback {
      def callback(name: String, oid: OidT, p: Pointer): Int = {
        tags += (name -> new Oid(oid))
        0
      }
    }

    Git2.tag_foreach[Int](repo, new ForeachCb, Pointer.NULL)
    tags.toSeq
  }
}