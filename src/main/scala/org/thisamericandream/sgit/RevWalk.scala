package org.thisamericandream.sgit

import scala.util.Try
import scala.util.Success
import scala.collection.JavaConversions._
import com.sun.jna.PointerType
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import java.util.EnumSet
import scala.collection.mutable.Buffer

class RevWalk private[sgit] (ptr: Pointer) extends PointerType(ptr) with Freeable with Traversable[Commit] {
  def this() = this(Pointer.NULL)

  def sorting(sortMode: EnumSet[SortMode]) {
    val mode = sortMode.map(_.id).foldLeft(0)(_ | _)
    Git2.revwalk_sorting[Void](this, mode)
  }

  override protected def freeObject() {
    Git2.revwalk_free[Void](this)
  }

  def push(commit: Commit): Try[Unit] = {
    Git2.revwalk_push[Int](this, commit.id) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def hide(commit: Commit): Try[Unit] = {
    Git2.revwalk_hide[Int](this, commit.id) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def reset() {
    Git2.revwalk_reset[Void](this)
  }

  def foreach[U](f: Commit => U) {
    var x = 0
    while (x == 0) {
      val oid = new Oid
      x = Git2.revwalk_next[Int](oid, this)
      if (x == 0) {
        Commit.lookup(repository, oid).foreach(f(_))
      }
    }
  }

  def repository: Repository = {
    new Repository(Git2.revwalk_repository[Pointer](this))
  }
}

object RevWalk {
  def apply(repo: Repository): Try[RevWalk] = {
    val ptr = new PointerByReference
    Git2.revwalk_new[Int](ptr, repo) match {
      case 0 => Success(new RevWalk(ptr.getValue))
      case x => Git2.exception(x)
    }
  }
}