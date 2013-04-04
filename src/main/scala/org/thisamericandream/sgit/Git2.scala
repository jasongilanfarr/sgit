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

class GitException(val code: Int) extends Exception


object Git2 extends Dynamic {
  Native.setProtected(true)
  private val lib = NativeLibrary.getInstance("git2")
  
  def version() : (Int, Int, Int) = {
    val major = new IntByReference
    val minor = new IntByReference
    val rev = new IntByReference
    
    Git2.libgit2_version[Unit](major, minor, rev)
    (major.getValue, minor.getValue, rev.getValue)
  }
  
  def applyDynamic[R : ClassTag](methodName: String)(args: Any*) : R = {
    lib.getFunction(s"git_${methodName}").invoke(implicitly[ClassTag[R]].runtimeClass, 
        args.map(_.asInstanceOf[AnyRef]).toArray[Object]).asInstanceOf[R]
  }
  
  /** TODO: Actually get a real error code */
  def exception[A](code: Int) : Failure[A] = {
    Failure(new GitException(code))
  }
  
  def boolValue(code: Int) : Try[Boolean] = code match {
    case 1 => Success(true)
    case 0 => Success(false)
    case _ => exception(code)
  }
  
  def unitValue(code: Int) : Try[Unit] = code match {
    case 0 => Success()
    case _ => exception(code)
  }
}