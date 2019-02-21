package io.github.malczuuu.iemu.domain;

import java.util.function.Consumer;

public interface LightModule {

  void setOn(boolean on);

  void setDimmer(int dimmer);

  void setOnTime(long onTime);

  boolean isOn();

  int getDimmer();

  long getOnTime();

  void shutdown();

  void subscribeOnStateChange(Consumer<Boolean> consumer);

  void subscribeOnDimmerChange(Consumer<Integer> consumer);

  void subscribeOnTimeCounterChange(Consumer<Long> consumer);
}
