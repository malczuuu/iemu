package io.github.malczuuu.iemu.mock

import io.github.malczuuu.iemu.common.CancelAwareTimerTask
import java.util.TimerTask

object TimerTaskMock {

  fun run(task: TimerTask) {
    if (!isCanceled(task)) {
      task.run()
    }
  }

  fun isCanceled(task: TimerTask): Boolean = task is CancelAwareTimerTask && task.isCancelled
}
