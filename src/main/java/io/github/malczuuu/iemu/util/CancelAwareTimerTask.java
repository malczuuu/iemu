package io.github.malczuuu.iemu.util;

import java.util.TimerTask;
import lombok.Getter;

public class CancelAwareTimerTask extends TimerTask {

  private final Runnable runnable;

  @Getter private boolean cancelled;

  public CancelAwareTimerTask(Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public boolean cancel() {
    cancelled = true;
    return super.cancel();
  }

  @Override
  public void run() {
    runnable.run();
  }
}
