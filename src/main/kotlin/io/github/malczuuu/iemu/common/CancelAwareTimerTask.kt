package io.github.malczuuu.iemu.common

import java.util.TimerTask

class CancelAwareTimerTask(private val runnable: Runnable) : TimerTask() {

  var isCancelled: Boolean = false
    private set

  override fun cancel(): Boolean {
    isCancelled = true
    return super.cancel()
  }

  override fun run() = runnable.run()
}
