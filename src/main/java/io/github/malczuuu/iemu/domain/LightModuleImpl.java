package io.github.malczuuu.iemu.domain;

import io.github.malczuuu.iemu.util.CancelAwareTimerTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

class LightModuleImpl implements LightModule {

  private List<Consumer<Boolean>> onStateChange = new ArrayList<>();
  private List<Consumer<Integer>> onDimmerChange = new ArrayList<>();
  private List<Consumer<Long>> onTimeChange = new ArrayList<>();

  private final Timer timer;

  private boolean on;
  private int dimmer;
  private long onTime;

  private TimerTask timerTask;

  LightModuleImpl(boolean on, Timer timer) {
    this.timer = timer;
    this.on = on;
    this.dimmer = 0;
    this.onTime = 0L;

    if (on) {
      scheduleIncOnTime();
    }
  }

  @Override
  public void setOn(boolean on) {
    if (this.on != on) {
      this.on = on;
      onStateChange.forEach(c -> c.accept(this.on));
      scheduleIncOnTime();
    }
  }

  @Override
  public void setDimmer(int dimmer) {
    if (dimmer < 0) {
      dimmer = 0;
    } else if (dimmer > 100) {
      dimmer = 100;
    }
    if (dimmer != this.dimmer) {
      this.dimmer = dimmer;
      onDimmerChange.forEach(c -> c.accept(this.dimmer));
    }
  }

  @Override
  public void setOnTime(long onTime) {
    if (onTime != this.onTime) {
      this.onTime = onTime;
      onTimeChange.forEach(c -> c.accept(onTime));
    }
  }

  private synchronized void scheduleIncOnTime() {
    if (on) {
      if (timerTask != null) {
        throw new IllegalStateException("timerTask must be null to schedule");
      }
      timerTask = new CancelAwareTimerTask(() -> incOnTime());
      timer.schedule(timerTask, 1000, 1000);
    } else {
      if (timerTask == null) {
        throw new IllegalStateException("timerTask must not be null to unschedule");
      }
      timerTask.cancel();
      timerTask = null;
    }
  }

  @Override
  public boolean isOn() {
    return on;
  }

  @Override
  public int getDimmer() {
    return dimmer;
  }

  @Override
  public long getOnTime() {
    return onTime;
  }

  private void incOnTime() {
    ++onTime;
    onTimeChange.forEach(c -> c.accept(onTime));
  }

  @Override
  public synchronized void shutdown() {
    if (timerTask != null) {
      timerTask.cancel();
    }
    timer.cancel();
    timer.purge();
    onStateChange.clear();
    onDimmerChange.clear();
    onTimeChange.clear();
  }

  @Override
  public void subscribeOnStateChange(Consumer<Boolean> consumer) {
    onStateChange.add(consumer);
  }

  @Override
  public void subscribeOnDimmerChange(Consumer<Integer> consumer) {
    onDimmerChange.add(consumer);
  }

  @Override
  public void subscribeOnTimeCounterChange(Consumer<Long> consumer) {
    onTimeChange.add(consumer);
  }
}
