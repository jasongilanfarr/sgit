package org.thisamericandream.sgit

import com.sun.jna.Pointer
import com.sun.jna.PointerType
import org.thisamericandream.sgit.struct.ConfigEntryT
import com.sun.jna.Callback
import scala.util.Try
import scala.util.Success
import com.sun.jna.Memory

class Config private[sgit] (ptr: Pointer) extends PointerType with Freeable with Traversable[ConfigEntryT] {
  def this() = this(Pointer.NULL)

  private class ForeachCb[U](f: (ConfigEntryT) => U) extends Callback {
    def callback(entry: ConfigEntryT, ptr: Pointer): Int = {
      f(entry)
      0
    }
  }

  def deleteEntry(name: String) {
    Git2.config_delete_entry[Int](this, name)
  }

  def -=(name: String) = deleteEntry(name)

  override def foreach[U](f: (ConfigEntryT) => U) {
    Git2.config_foreach[Int](this, new ForeachCb(f), Pointer.NULL)
  }

  protected override def freeObject() {
    Git2.config_free[Unit](this)
  }

  override def toString(): String = {
    "Config"
  }
}

object Config {
  private[sgit] def apply(ptr: Pointer) = new Config(ptr)

  def global = findGlobal
  
  def findGlobal(): Try[String] = {
    val out = new Memory(1024)
    Git2.config_find_global[Int](out, out.size()) match {
      case 0 => Success(out.getString(0))
      case x => Git2.exception(x)
    }
  }
  
  def system = findSystem
  
  def findSystem() : Try[String] = {
    val out = new Memory(1024)
    Git2.config_find_system[Int](out, out.size()) match {
      case 0 => Success(out.getString(0))
      case x => Git2.exception(x)
    }
  }
  
  def xdg = findXdg
  
  def findXdg(): Try[String] = {
    val out = new Memory(1024)
    Git2.config_find_xdg[Int](out, out.size()) match {
      case 0 => Success(out.getString(0))
      case x => Git2.exception(x)
    }
  }
  
  
}