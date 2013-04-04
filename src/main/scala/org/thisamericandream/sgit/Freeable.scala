package org.thisamericandream.sgit

import java.util.concurrent.atomic.AtomicBoolean
import java.io.Closeable

trait Frees {
  protected def freeObject()
}

trait Freeable extends Frees with Closeable {
  protected var freed = new AtomicBoolean(false)
  override def finalize = free
  override def close = free

  def free() {
    if (freed.compareAndSet(false, true)) {
      freeObject()
    }
  }
}