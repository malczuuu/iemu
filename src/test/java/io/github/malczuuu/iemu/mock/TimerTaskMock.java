package io.github.malczuuu.iemu.mock;

import io.github.malczuuu.iemu.common.CancelAwareTimerTask;
import java.util.TimerTask;

public class TimerTaskMock {

  public static void run(TimerTask task) {
    if (!isCanceled(task)) {
      task.run();
    }
  }

  public static boolean isCanceled(TimerTask task) {
    return task instanceof CancelAwareTimerTask c && c.isCancelled();
  }
}
