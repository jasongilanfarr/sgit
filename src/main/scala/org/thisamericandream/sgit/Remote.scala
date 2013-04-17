package org.thisamericandream.sgit

import com.sun.jna.PointerType
import com.sun.jna.Pointer
import scala.util.Try
import com.sun.jna.ptr.PointerByReference
import scala.util.Success
import org.thisamericandream.sgit.struct.TransferProgressT
import com.sun.jna.Callback
import org.thisamericandream.sgit.struct.StrArrayT

class Remote(ptr: Pointer) extends PointerType(ptr) with Freeable {
  def this() = this(Pointer.NULL)

  def name: Option[String] = {
    Option(Git2.remote_name[String](this))
  }

  def pushUrl: Option[String] = {
    Option(Git2.remote_pushurl[String](this))
  }

  def stop() {
    Git2.remote_stop[Void](this)
  }

  def remoteUrl: String = {
    Git2.remote_url[String](this)
  }

  def connect(push: Boolean): Try[Unit] = {
    Git2.remote_connect[Int](this, push) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def isConnected: Boolean = {
    Git2.remote_connected[Boolean](this)
  }

  def disconnect() {
    Git2.remote_disconnect[Void](this)
  }

  def download[U](f: TransferProgressT => U): Try[Unit] = {
    class TransferProgressCb extends Callback {
      def callback(progress: TransferProgressT, p: Pointer): Int = {
        f(progress)
        0
      }
    }
    Git2.remote_download[Int](this, new TransferProgressCb, Pointer.NULL) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def ls[U](f: RemoteHead => U): Try[Unit] = {
    class RemoteHeadCb extends Callback {
      def callback(remoteHead: RemoteHead, p: Pointer): Int = {
        f(remoteHead)
        0
      }
    }
    Git2.remote_ls[Int](this, new RemoteHeadCb, Pointer.NULL) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  def save(): Try[Unit] = {
    Git2.remote_save[Int](this) match {
      case 0 => Success()
      case x => Git2.exception(x)
    }
  }

  override protected def freeObject() {
    Git2.remote_free[Void](this)
  }
}

object Remote {
  def create(repo: Repository, fetchSpec: String, url: String): Try[Remote] = {
    val ptr = new PointerByReference
    Git2.remote_create_inmemory[Int](ptr, repo, fetchSpec, url) match {
      case 0 => Success(new Remote(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def lookup(repo: Repository, name: String): Try[Remote] = {
    val ptr = new PointerByReference
    Git2.remote_load[Int](ptr, repo, name) match {
      case 0 => Success(new Remote(ptr.getValue))
      case x => Git2.exception(x)
    }
  }

  def all(repo: Repository): Try[Seq[String]] = {
    val r = new StrArrayT
    Git2.remote_list[Int](r, repo) match {
      case 0 => Success(r.strings.getStringArray(0, r.length.intValue))
      case x => Git2.exception(x)
    }
  }
}