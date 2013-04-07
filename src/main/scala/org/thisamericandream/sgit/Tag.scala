package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.Callback
import scala.collection.mutable.Buffer
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.ptr.PointerByReference
import scala.util.Try
import scala.util.Success
import scala.util.Failure

class Tag private[sgit] (val ptr: Pointer) extends GitObject(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  override def id(): Oid = {
    new Oid(Git2.tag_id[OidT](this))
  }

  def message(): String = {
    Git2.tag_message[String](this)
  }

  def name(): String = {
    Git2.tag_name[String](this)
  }

  override def peel[T >: GitObject](`type`: OType = OType.Commit): Try[GitObject] = {
    val ptr = new PointerByReference
    Git2.tag_peel[Int](ptr, this) match {
      case 0 =>
        GitObject.forPtr(ptr.getValue).map(Success(_)).getOrElse(Failure(new Exception("Bad Object")))
      case x => Git2.exception(x)
    }
  }

  def targetId(): Oid = {
    new Oid(Git2.tag_target_id[OidT](this))
  }

  def target(): Try[GitObject] = {
    val ptr = new PointerByReference
    Git2.tag_target[Int](ptr, this) match {
      case 0 =>
        GitObject.forPtr(ptr.getValue)
      case x => Git2.exception(x)
    }
  }

  def targetType(): OType = {
    OType.forId(Git2.taget_target_type[Int](this))
  }

  override def toString: String = name()

  override def freeObject() {
    Git2.tag_free(this)
  }
}

object Tag {
  def allNames(repo: Repository): Seq[String] = {
    allNamesWithOid(repo).map(_._1)
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