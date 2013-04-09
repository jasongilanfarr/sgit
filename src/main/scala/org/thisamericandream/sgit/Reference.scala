package org.thisamericandream.sgit

import scala.collection.mutable.Buffer
import scala.util.Success
import scala.util.Try

import org.thisamericandream.sgit.struct.OidT

import com.sun.jna.Callback
import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class Reference private[sgit] (val ptr: Pointer) extends PointerType(ptr) with Freeable with Ordered[Reference] {
  def this() = this(Pointer.NULL)

  def name: String = {
    Git2.reference_name[String](this)
  }

  def normalizeName: Try[String] = {
    val buf = new Memory(1024)
    Git2.reference_normalize_name[Int](buf, buf.size, name, 0) match {
      case 0 => Success(buf.getString(0))
      case x => Git2.exception(x)
    }
  }

  def peel(`type`: OType): Try[GitObject] = {
    val ptr = new PointerByReference
    Git2.reference_peel[Int](ptr, this, `type`.id) match {
      case 0 =>
        GitObject.forPtr(ptr.getValue)
      case x => Git2.exception(x)
    }
  }

  def hasLog: Try[Boolean] = {
    Git2.boolValue(Git2.reference_has_log[Int](this))
  }

  def isBranch: Try[Boolean] = {
    Git2.boolValue(Git2.reference_is_branch[Int](this))
  }

  def isRemote: Try[Boolean] = {
    Git2.boolValue(Git2.reference_is_remote[Int](this))
  }

  def isSymbolic: Boolean = {
    Git2.reference_type[Int](this) == 2
  }

  def isDirect = !isSymbolic

  def owner: Repository = {
    val ptr = Git2.reference_owner[Pointer](this)
    new Repository(ptr)
  }

  def rename(newName: String, force: Boolean): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_rename[Int](ptr, this, newName, force) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def resolve: Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_resolve[Int](ptr, this) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def target: Oid = {
    new Oid(Git2.reference_target[OidT](this))
  }

  def target_=(id: Oid): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_set_target[Int](ptr, this, id) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def symbolicTarget: Option[String] = {
    Option(Git2.reference_symbolic_target[String](this))
  }

  def symbolicTarget_=(target: String): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_symbolic_set_target[Int](ptr, this, target) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def delete(): Try[Unit] = {
    Git2.reference_delete[Int](this) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  override def compare(that: Reference): Int = {
    Git2.reference_cmp[Int](this, that)
  }

  override def equals(other: Any) = other match {
    case x: Reference => ptr == x.ptr
    case x: Pointer => ptr == x
    case _ => false
  }

  protected override def freeObject() {
    Git2.reference_free[Unit](ptr)
  }
}

object Reference {
  def create(repo: Repository, name: String, id: Oid, force: Boolean): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_create[Int](ptr, repo, name, id, force) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def createSymbolic(repo: Repository, name: String, target: String, force: Boolean): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_symbolic_create[Int](ptr, repo, name, target, force) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def lookup(repo: Repository, name: String): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_lookup[Int](ptr, repo, name) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def allNames(repo: Repository): Seq[String] = {
    val seq = Buffer[String]()
    class ForeachCb extends Callback {
      def callback(refName: String): Int = {
        seq += refName
        0
      }
    }

    Git2.reference_foreach[Int](repo, 1 | 2 /* all */ , new ForeachCb)
    seq.toSeq
  }

  def fromName(repo: Repository, name: String): Try[Oid] = {
    val oid = new OidT
    Git2.reference_name_to_id[Int](oid, repo, name) match {
      case 0 => Success(new Oid(oid))
      case x => Git2.exception(x)
    }
  }

  def isValidName(name: String): Try[Boolean] = {
    Git2.boolValue(Git2.reference_is_valid_name[Int](name))
  }
}