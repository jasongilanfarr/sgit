package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import scala.util.Try
import scala.util.Success
import com.sun.jna.ptr.PointerByReference
import scala.collection.mutable.Buffer
import com.sun.jna.Callback
import scala.remote
import com.sun.jna.Memory

/** TODO: Branch is a Reference */
class Branch private[sgit] (ptr: Pointer) extends PointerType(ptr) with GitObject {
  lazy val ref = new Reference(ptr)
  def this() = this(Pointer.NULL)

  override protected def freeObject() {
    Git2.branch_free[Void](this)
  }

  def tip: Try[Commit] = {
    ref.resolve.map(_.target).flatMap(owner.lookup[Commit](_))
  }

  def name: Try[String] = {
    val ptr = new PointerByReference
    Git2.branch_name[Int](ptr, this) match {
      case 0 => Success(ptr.getValue.getString(0))
      case x => Git2.exception(x)
    }
  }

  def canonicalName: String = {
    ref.name
  }

  def isRemote: Try[Boolean] = {
    Git2.boolValue(Git2.reference_is_remote[Int](this))
  }

  override def owner: Repository = {
    new Repository(Git2.reference_owner[Pointer](this))
  }

  def move(newName: String, force: Boolean = false): Try[Branch] = {
    val ptr = new PointerByReference
    Git2.branch_move[Int](ptr, this, newName, force) match {
      case 0 => Success(new Branch(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def rename(newName: String, force: Boolean = false) = move(newName, force)
  
  def trackingName: Try[Option[String]] = {
    val buf = new Memory(1024)
    Git2.branch_tracking_name[Int](buf, 1024, owner, canonicalName) match {
      case x if x >= 0 => Success(Some(buf.getString(0, false)))
      case x => Git2.exception(x)
    }
  }

  def tracking: Try[Option[Branch]] = {
    val ptr = new PointerByReference
    Git2.branch_tracking[Int](ptr, this) match {
      case 0 => Success(Some(new Branch(ptr.getValue)))
      case -3 /* NotFound */ => Success(None)
      case x => Git2.exception(x)
    }
  }

  def delete(): Try[Boolean] = {
    Git2.boolValue(Git2.branch_delete[Int](this))
  }

  def isHead: Try[Boolean] = {
    Git2.boolValue(Git2.branch_is_head[Int](this))
  }

  override def equals(other: Any): Boolean = other match {
    case x: Branch => canonicalName == x.canonicalName
    case _ => false
  }

  override def toString: String = name.get
}

object Branch {
  def create(repo: Repository, name: String, target: Commit, force: Boolean = false): Try[Branch] = {
    val ptr = new PointerByReference
    Git2.branch_create[Int](ptr, repo, name, target, force) match {
      case 0 => Success(new Branch(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def lookup(repo: Repository, name: String, branchType: BranchType = BranchType.Local): Try[Branch] = {
    val ptr = new PointerByReference
    Git2.branch_lookup[Int](ptr, repo, name, branchType.id) match {
      case 0 => Success(new Branch(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def allNames(repo: Repository, branchType: BranchType = BranchType.LocalOrRemote): Seq[String] = {
    var branches = Buffer[String]()
    class ForeachCb extends Callback {
      def callback(name: String, branchType: Int, p: Pointer): Int = {
        branches += name
        0
      }
    }

    Git2.branch_foreach[Int](repo, branchType.id, new ForeachCb, Pointer.NULL)
    branches.toSeq
  }

  def all(repo: Repository, branchType: BranchType = BranchType.LocalOrRemote): Seq[Branch] = {
    allNames(repo, branchType).map { name =>
      lookup(repo, name, branchType)
    }.filter(_.isSuccess).map(_.get)
  }
}