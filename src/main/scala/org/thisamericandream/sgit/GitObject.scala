package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import scala.util.Try
import org.thisamericandream.sgit.struct.OidT
import com.sun.jna.ptr.PointerByReference
import scala.util.Success
import scala.util.Failure

class GitObject private[sgit] (ptr: Pointer) extends PointerType with Freeable {
  def this() = this(Pointer.NULL)

  def id(): Oid = {
    new Oid(Git2.object_id[OidT](ptr))
  }

  def owner(): Repository = {
    new Repository(Git2.object_owner[Pointer](ptr))
  }

  def peel[T >: GitObject](`type`: OType): Try[T] = {
    val ptrRef = new PointerByReference
    Git2.object_peel[Int](ptrRef, ptr, `type`.id) match {
      case 0 =>
        GitObject.forPtr(ptrRef.getValue)
      case x => Git2.exception(x)

    }
  }

  def `type`(): OType = {
    OType.forId(Git2.object_type[Int](ptr))
  }

  override def toString = id().toString

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
  private[sgit] def forPtr(ptr: Pointer): Try[GitObject] = id(ptr) match {
    case OType.Blob => Success(new Blob(ptr))
    case OType.Commit => Success(new Commit(ptr))
    case OType.Tag => Success(new Tag(ptr))
    case OType.Tree => Success(new Tree(ptr))
    // TODO: Use a proper exception
    case OType.Bad => Failure(new Exception("Bad Object"))
    case _ => Success(new GitObject(ptr))
  }

  def lookup[T <: GitObject](repo: Repository, oid: Oid): Try[T] = {
    val ptr = new PointerByReference
    Git2.object_lookup[Int](ptr, repo, oid, OType.Any.id) match {
      case 0 =>
        forPtr(ptr.getValue).map(_.asInstanceOf[T])
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
    OType.forId(Git2.object_type[Int](ptr))
  }

  def revParse[T <: GitObject](repo: Repository, spec: String): Try[T] = {
    val ptr = new PointerByReference
    Git2.revparse_single[Int](ptr, repo, spec) match {
      case 0 =>
        forPtr(ptr.getValue).map(_.asInstanceOf[T])
      case x => Git2.exception(x)
    }
  }
}