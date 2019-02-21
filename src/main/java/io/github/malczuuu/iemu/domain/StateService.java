package io.github.malczuuu.iemu.domain;

import java.time.Instant;
import java.util.function.Consumer;

public interface StateService {

  StateDTO getState();

  void changeState(StateDTO state);

  void subscribeOnCurrentTimeChange(Consumer<Instant> consumer);

  void subscribeOnStateChange(Consumer<Boolean> consumer);

  void subscribeOnDimmerChange(Consumer<Integer> consumer);

  void subscribeOnTimeCounterChange(Consumer<Long> consumer);

  void resetErrors();

  void shutdown();
}
