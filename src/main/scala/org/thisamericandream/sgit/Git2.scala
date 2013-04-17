package org.thisamericandream.sgit

import language.dynamics
import language.reflectiveCalls
import com.sun.jna.NativeLibrary
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import com.sun.jna.ptr.IntByReference
import com.sun.jna.Native
import org.thisamericandream.sgit.struct.ErrorT
import scala.collection.mutable.Buffer
import com.sun.jna.Memory

class GitException(val code: Int) extends Exception {
  val err = Git2.lastError()
  Git2.clearError()

  def errorCode = ErrorCode.forId(code)
  override def toString(): String = {
    s"${errorCode} - ${err.map(_.message)}"
  }
}

object Git2 extends Dynamic {
  Native.setProtected(true)
  private val lib = NativeLibrary.getInstance("git2")

  def version(): (Int, Int, Int) = {
    val major = new IntByReference
    val minor = new IntByReference
    val rev = new IntByReference

    Git2.libgit2_version[Unit](major, minor, rev)
    (major.getValue, minor.getValue, rev.getValue)
  }

  def capabilities(): Seq[Capability] = {
    val capabilities = Git2.libgit2_capabilities[Int]()

    if ((capabilities & Capability.Https.id) == 1 &&
      (capabilities & Capability.Threads.id) == 1) {
      Seq(Capability.Https, Capability.Threads)
    } else if ((capabilities & Capability.Https.id) == 1) {
      Seq(Capability.Https)
    } else if ((capabilities & Capability.Threads.id) == 1) {
      Seq(Capability.Threads)
    } else {
      Seq()
    }
  }

  def prettifyMessage(message: String, stripComments: Boolean = true): Try[String] = {
    val buf = new Memory(message.length)
    buf.clear
    Git2.message_prettify[Int](buf, message.length, message, stripComments) match {
      case x if x >= 0 => Success(buf.getString(0, false))
      case x if x < 0 => Git2.exception(x)
    }
  }

  def lastError(): Option[ErrorT] = {
    Option(lib.getFunction("giterr_last").invoke(classOf[ErrorT], Array[AnyRef]()).asInstanceOf[ErrorT])
  }
  def clearError() {
    lib.getFunction("giterr_clear").invokeVoid(Array[AnyRef]());
  }

  def applyDynamic[R: ClassTag](methodName: String)(args: Any*): R = {
    lib.getFunction(s"git_${methodName}").invoke(implicitly[ClassTag[R]].runtimeClass,
      args.map(_.asInstanceOf[AnyRef]).toArray[Object]).asInstanceOf[R]
  }

  /** TODO: Actually get a real error code */
  def exception[A](code: Int): Failure[A] = {
    Failure(new GitException(code))
  }

  def boolValue(code: Int): Try[Boolean] = code match {
    case 1 => Success(true)
    case 0 => Success(false)
    case _ => exception(code)
  }

  def unitValue(code: Int): Try[Unit] = code match {
    case 0 => Success()
    case _ => exception(code)
  }
}