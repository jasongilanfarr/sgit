package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import scala.util.Try
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.ptr.PointerByReference
import scala.util.Success

class GitObject private[sgit] (ptr: Pointer) extends PointerType with Freeable {
  def this() = this(Pointer.NULL)

  def id(): Oid = {
    new Oid(Git2.object_id[OidT](ptr))
  }

  def owner(): Repository = {
    new Repository(Git2.object_owner[Pointer](this))
  }

  def peel[T >: GitObject](`type`: OType): Try[T] = {
    val ptr = new PointerByReference
    Git2.object_peel[Int](ptr, this, `type`.id) match {
      case 0 => Success(GitObject.forOType(ptr.getValue, `type`))
      case x => Git2.exception(x)

    }
  }

  def `type`(): OType = {
    OType.forId(Git2.object_type[Int](this))
  }

  override def equals(other: Any): Boolean = other match {
    case x: GitObject =>
      id == x.id
    case _ => false
  }
  override def freeObject() {
    Git2.object_free[Void](ptr)
  }
}

object GitObject {
  def forOType[T >: GitObject](ptr: Pointer, `type`: OType): T = `type` match {
    case OType.Blob => new Blob(ptr)
    case OType.Commit => new Commit(ptr)
    case OType.Tag => new Commit(ptr)
    case OType.Tree => new Tree(ptr)
    case _ => new GitObject(ptr)
  }

  def lookup[T >: GitObject](repo: Repository, id: Oid, `type`: OType): Try[T] = {
    val ptr = new PointerByReference
    Git2.object_lookup[Int](ptr, repo, id, `type`.id) match {
      case 0 => Success(forOType(ptr.getValue, `type`))
      case x => Git2.exception(x)
    }
  }

  def typeToString(`type`: OType): String = {
    Git2.object_type2string[String](`type`.id)
  }

  def stringToType(str: String): OType = {
    OType.forId(Git2.object_string2type[Int](str))
  }

  private def id(ptr: Pointer): OType = {
    OType.forId(Git2.object_type[Int](this))
  }

  def revParse[T >: GitObject](repo: Repository, spec: String): Try[T] = {
    val ptr = new PointerByReference
    Git2.revparse_single[Int](ptr, repo, spec) match {
      case 0 =>
        Success(forOType(ptr.getValue, id(ptr.getValue)))
        Success(new GitObject(ptr.getValue))
      case x => Git2.exception(x)
    }
  }
}