package org.thisamericandream.sgit

import java.util.concurrent.atomic.AtomicBoolean
import scala.util.Success
import scala.util.Try
import com.sun.jna.PointerType
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import java.io.Closeable
import com.sun.jna.Memory

class Repository(val ptr: Pointer) extends PointerType(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  def detachHead: Try[Unit] = {
    Git2.unitValue(Git2.repository_detach_head[Int](this))
  }

  def isHeadDetached(): Try[Boolean] = {
    Git2.boolValue(Git2.repository_head_detached[Int](this))
  }

  def isHeadOrphan(): Try[Boolean] = {
    Git2.boolValue(Git2.repository_head_orphan[Int](this))
  }

  def isBare(): Try[Boolean] = {
    Git2.boolValue(Git2.repository_is_bare[Int](this))
  }

  def isEmpty(): Try[Boolean] = {
    Git2.boolValue(Git2.repository_is_empty[Int](this))
  }

  def mergeCleanup(): Try[Unit] = {
    Git2.unitValue(Git2.repository_merge_cleanup[Int](this))
  }

  def path(): String = {
    Git2.repository_path[String](this)
  }

  def workDir(): String = {
    Git2.repository_workdir[String](this)
  }

  def head(): Try[Reference] = {
    val refPtr = new PointerByReference
    val ret = Git2.repository_head[Int](refPtr, this)
    if (ret == 0) {
      Success(new Reference(refPtr.getValue))
    } else {
      Git2.exception(ret)
    }
  }

  protected override def freeObject() {
    Git2.repository_free[Unit](this)
  }
}

object Repository {
  def apply(path: String): Try[Repository] = {
    val p = new PointerByReference()
    Git2.repository_open[Int](p, path) match {
      case 0 => Success(new Repository(p.getValue))
      case x => Git2.exception(x)
    }
  }

  def init(path: String, bare: Boolean = false): Try[Repository] = {
    val ptr = new PointerByReference
    Git2.repository_init[Int](ptr, path, if (bare) { 1 } else { 0 }) match {
      case 0 => Success(new Repository(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def discover(path: String): Try[Repository] = {
    val pathPtr = new Memory(256)
    Git2.repository_discover[Int](pathPtr, 256, path, true, Pointer.NULL) match {
      case 0 => Repository(pathPtr.getString(0))
      case x => Git2.exception(x)
    }
  }
}