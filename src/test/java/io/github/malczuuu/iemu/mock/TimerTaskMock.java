package io.github.malczuuu.iemu.mock;

import java.lang.reflect.Field;
import java.util.TimerTask;

public class TimerTaskMock {

  public static void run(TimerTask task) {
    if (!isCanceled(task)) {
      task.run();
    }
  }

  public static boolean isCanceled(TimerTask task) {
    try {
      Field state = TimerTask.class.getDeclaredField("state");
      Field cancelled = TimerTask.class.getDeclaredField("CANCELLED");
      state.setAccessible(true);
      cancelled.setAccessible(true);
      int stateValue = (int) state.get(task);
      int cancelledValue = (int) cancelled.get(task);
      return stateValue == cancelledValue;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
