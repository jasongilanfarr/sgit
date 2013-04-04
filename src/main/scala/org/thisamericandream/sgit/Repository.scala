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

  def config(): Try[Config] = {
    val ptr = new PointerByReference
    Git2.repository_config[Int](ptr, this) match {
      case 0 => Success(Config(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def config_=(config: Config) {
    Git2.repository_set_config[Unit](this, config)
  }

  def detachHead: Try[Unit] = {
    Git2.unitValue(Git2.repository_detach_head[Int](this))
  }

  def headDetached_=(commitIsh: String): Try[Unit] = {
    Git2.repository_set_head_detached[Int](this, commitIsh) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
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

  def workDir(): Option[String] = {
    Option(Git2.repository_workdir[String](this))
  }

  def workDir_=(path: String): Try[Unit] = {
    Git2.repository_set_workdir[Int](this, workDir, 1 /* Update_GitLink */ ) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def head(): Try[Reference] = {
    val refPtr = new PointerByReference
    Git2.repository_head[Int](refPtr, this) match {
      case 0 => Success(new Reference(refPtr.getValue))
      case x => Git2.exception(x)
    }
  }

  def head_=(refName: String): Try[Unit] = {
    Git2.repository_set_head[Int](this, refName) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def index(): Try[Index] = {
    val indexPtr = new PointerByReference
    Git2.repository_index[Int](indexPtr, this) match {
      case 0 => Success(new Index(indexPtr.getValue))
      case x => Git2.exception(x)
    }
  }

  def index_=(idx: Index): Try[Unit] = {
    Git2.repository_set_index[Int](this, idx) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def refDb(): Try[RefDb] = {
    val refPtr = new PointerByReference
    Git2.repository_refdb[Int](refPtr, this) match {
      case 0 => Success(new RefDb(refPtr.getValue))
      case x => Git2.exception(x)
    }
  }

  def refDb_=(refDb: RefDb): Try[Unit] = {
    Git2.repository_set_refdb[Int](this, refDb) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def find(refName: String): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_lookup[Int](ptr, this, refName) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  protected override def freeObject() {
    Git2.repository_free[Unit](this)
  }

  override def equals(other: Any) = other match {
    case other: Repository => ptr == other.ptr
    case p: Pointer => ptr == p
    case _ => false
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