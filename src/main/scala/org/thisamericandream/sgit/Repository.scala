package org.thisamericandream.sgit

import java.util.concurrent.atomic.AtomicBoolean
import scala.util.Success
import scala.util.Try
import com.sun.jna.PointerType
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import java.io.Closeable
import com.sun.jna.Memory
import com.sun.jna.ptr.IntByReference
import scala.collection.mutable.Buffer
import com.sun.jna.Callback
import scala.util.Failure
import com.sun.jna.ptr.NativeLongByReference
import scala.util.matching.Regex

class Repository(val ptr: Pointer) extends PointerType(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  def config: Try[Config] = {
    val ptr = new PointerByReference
    Git2.repository_config[Int](ptr, this) match {
      case 0 => Success(Config(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def config_=(config: Config) {
    Git2.repository_set_config[Unit](this, config)
  }

  def detachHead(): Try[Unit] = {
    Git2.unitValue(Git2.repository_detach_head[Int](this))
  }

  def headDetached_=(commitIsh: String): Try[Unit] = {
    Git2.unitValue(Git2.repository_set_head_detached[Int](this, commitIsh))
  }

  def isHeadDetached: Try[Boolean] = {
    Git2.boolValue(Git2.repository_head_detached[Int](this))
  }

  def isHeadOrphan: Try[Boolean] = {
    Git2.boolValue(Git2.repository_head_orphan[Int](this))
  }

  def isBare: Try[Boolean] = {
    Git2.boolValue(Git2.repository_is_bare[Int](this))
  }

  def isEmpty: Try[Boolean] = {
    Git2.boolValue(Git2.repository_is_empty[Int](this))
  }

  def mergeCleanup(): Try[Unit] = {
    Git2.unitValue(Git2.repository_merge_cleanup[Int](this))
  }

  def path: String = {
    Git2.repository_path[String](this)
  }

  def workDir: Option[String] = {
    Option(Git2.repository_workdir[String](this))
  }

  def workDir_=(path: String): Try[Unit] = {
    Git2.unitValue(Git2.repository_set_workdir[Int](this, workDir, 1 /* Update_GitLink */ ))
  }

  def head: Try[Reference] = {
    val refPtr = new PointerByReference
    Git2.repository_head[Int](refPtr, this) match {
      case 0 => Success(new Reference(refPtr.getValue))
      case x => Git2.exception(x)
    }
  }

  def head_=(refName: String): Try[Unit] = {
    Git2.unitValue(Git2.repository_set_head[Int](this, refName))
  }

  def lastCommit: Try[Commit] = {
    head.flatMap(h => lookup[Commit](h.target))
  }

  def lookup[T <: GitObject](id: Oid): Try[T] = {
    GitObject.lookup[T](this, id)
  }

  def revParse(spec: String): Try[GitObject] = {
    GitObject.revParse(this, spec)
  }

  def ref(refName: String): Try[Reference] = {
    Reference.lookup(this, refName)
  }

  def refs(pattern: Regex): Traversable[Reference] = {
    Reference.allNames(this).filter(pattern.findFirstIn(_).isDefined).map { name =>
      ref(name)
    }.filter(_.isSuccess).map(_.get)
  }

  def refNames: Traversable[String] = {
    Reference.allNames(this)
  }

  def refs: Traversable[Reference] = {
    Reference.allNames(this).map(ref(_)).filter(_.isSuccess).map(_.get)
  }

  def tagNames: Seq[String] = {
    Tag.allNames(this)
  }

  def tags: Seq[Tag] = {
    Tag.all(this)
  }

  def blobAt(oid: Oid, path: String): Try[Blob] = {
    for (
      commit <- Commit.lookup(this, oid);
      tree <- commit.tree;
      blobData <- tree.entryByPath(path);
      blob <- Blob.lookup(this, blobData.id)
    ) yield blob
  }

  def index: Try[Index] = {
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

  def find(refName: String): Try[Reference] = {
    val ptr = new PointerByReference
    Git2.reference_lookup[Int](ptr, this, refName) match {
      case 0 => Success(new Reference(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def status(path: String): Try[Seq[StatusCode]] = {
    val codes = new IntByReference
    Git2.status_file[Int](codes, this, path) match {
      case 0 => Success(mapStatusCodes(codes.getValue()))
      case x => Git2.exception(x)
    }
  }

  def status(f: (String, Seq[StatusCode]) => Boolean): Try[Unit] = {
    class StatusCb extends Callback {
      def callback(path: String, flags: Int, payload: Pointer): Boolean = {
        !f(path, mapStatusCodes(flags))
      }
    }

    val cb = new StatusCb
    Git2.unitValue(Git2.status_foreach[Int](this, cb, Pointer.NULL))
  }

  private def mapStatusCodes(codes: Int): Seq[StatusCode] = {
    val statuses = Buffer[StatusCode]()
    StatusCode.values().foreach { statusCode =>
      if ((codes & statusCode.id) != 0) {
        statuses += statusCode
      }
    }
    statuses.toSeq
  }

  def eachId[U](f: (Oid) => U): Try[Unit] = {
    class OdbEachIdCb extends Callback {
      def callback(id: Oid, pointer: Pointer): Boolean = {
        f(id)
        false
      }
    }

    val cb = new OdbEachIdCb()

    odb.flatMap { odbPtr =>
      Git2.odb_foreach[Int](odbPtr, cb, Pointer.NULL) match {
        case 0 => Success()
        case x => Git2.exception(x)
      }
    }
  }

  def ids: Try[Seq[Oid]] = {
    var buf = Buffer[Oid]()
    eachId(id => buf += id) match {
      case t: Success[_] => Success(buf.toSeq)
      case t: Failure[_] => Failure(t.exception)
    }
  }

  def contains(oid: Oid): Try[Boolean] = {
    odb.map { odbPtr =>
      Git2.odb_exists[Int](odbPtr, oid) match {
        case 1 => true
        case x => false
      }
    }
  }

  def mergeBase(oid1: Oid, oid2: Oid): Try[Oid] = {
    val oid = new Oid
    Git2.merge_base[Int](oid, this, oid1, oid2) match {
      case 0 => Success(oid)
      case x => Git2.exception(x)
    }
  }

  def read(oid: Oid): Try[OdbObject] = {
    odb.flatMap { odbPtr =>
      val odbObjPtr = new PointerByReference
      Git2.odb_read[Int](odbObjPtr, odbPtr, oid) match {
        case 0 => Success(new OdbObject(odbObjPtr.getValue))
        case x => Git2.exception(x)
      }
    }
  }

  def readHeader(oid: Oid): Try[(OType, Long)] = {
    val lenPtr = new NativeLongByReference
    val oTypePtr = new IntByReference

    odb.flatMap { odbPtr =>
      Git2.odb_read_header[Int](lenPtr, oTypePtr, odbPtr, oid) match {
        case 0 => Success((OType.forId(oTypePtr.getValue()), lenPtr.getValue().longValue()))
        case x => Git2.exception(x)
      }
    }
  }

  def write(buf: Array[Byte], `type`: OType): Try[Oid] = {
    odb.flatMap { odbPtr =>
      val nativeBuf = new Memory(buf.size)
      nativeBuf.write(0, buf, 0, buf.length)
      val oid = new Oid
      Git2.odb_write[Int](oid, odbPtr, nativeBuf, buf.length, `type`.id) match {
        case 0 => Success(oid)
        case x => Git2.exception(x)
      }
    }
  }

  lazy val odb: Try[Pointer] = {
    val odbPtr = new PointerByReference
    Git2.repository_odb[Int](odbPtr, this) match {
      case 0 => Success(odbPtr.getValue)
      case x => Git2.exception(x)
    }
  }

  protected override def freeObject() {
    odb.foreach(Git2.odb_free[Void](_))
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
    Git2.repository_init[Int](ptr, path, bare) match {
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

  /** TODO: Support Clone Options */
  def clone(url: String, path: String): Try[Repository] = {
    val ptr = new PointerByReference
    Git2.clone[Int](ptr, url, path, null) match {
      case 0 => Success(new Repository(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def hash(buffer: Array[Byte], `type`: OType): Try[Oid] = {
    val oid = new Oid
    val nativeBuffer = new Memory(buffer.length)
    nativeBuffer.write(0, buffer, 0, buffer.length)
    Git2.odb_hash[Int](oid, nativeBuffer, buffer.length, `type`.id) match {
      case 0 => Success(oid)
      case x => Git2.exception(x)
    }
  }

  def hashFile(path: String, `type`: OType): Try[Oid] = {
    val oid = new Oid
    Git2.odb_hashfile[Int](oid, path, `type`.id) match {
      case 0 => Success(oid)
      case x => Git2.exception(x)
    }
  }
}