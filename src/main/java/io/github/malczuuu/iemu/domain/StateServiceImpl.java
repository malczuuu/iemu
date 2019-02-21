package io.github.malczuuu.iemu.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.function.Consumer;

public class StateServiceImpl implements StateService {

  private final CurrentTimeModule currentTime =
      new CurrentTimeModuleFactory().getCurrentTimeModule();
  private final LightModule light = new LightModuleFactory().getLightModule();

  @Override
  public StateDTO getState() {
    return new StateDTO(
        "lwm2m_emulator",
        currentTime.getCurrentTime().toString(),
        currentTime.getTimeZone(),
        currentTime.getUTCOffset(),
        Collections.emptyList(),
        light.isOn(),
        light.getOnTime(),
        light.getDimmer());
  }

  @Override
  public void changeState(StateDTO state) {
    if (state.getCurrentTime() != null) {
      currentTime.setCurrentTime(Instant.parse(state.getCurrentTime()));
    }
    if (state.getTimeZone() != null) {
      currentTime.setTimeZone(state.getTimeZone());
    }
    if (state.getUTCOffset() != null) {
      currentTime.setUTCOffset(state.getUTCOffset());
    }
    if (state.getOn() != null) {
      light.setOn(state.getOn());
    }
    if (state.getOnTime() != null) {
      light.setOnTime(state.getOnTime());
    }
    if (state.getDimmer() != null) {
      light.setDimmer(state.getDimmer());
    }
  }

  @Override
  public void subscribeOnCurrentTimeChange(Consumer<Instant> consumer) {
    currentTime.subscribeOnCurrentTimeChange(consumer);
  }

  @Override
  public void subscribeOnStateChange(Consumer<Boolean> consumer) {
    light.subscribeOnStateChange(consumer);
  }

  @Override
  public void subscribeOnDimmerChange(Consumer<Integer> consumer) {
    light.subscribeOnDimmerChange(consumer);
  }

  @Override
  public void subscribeOnTimeCounterChange(Consumer<Long> consumer) {
    light.subscribeOnTimeCounterChange(consumer);
  }

  @Override
  public void resetErrors() {}

  @Override
  public void shutdown() {
    light.shutdown();
    currentTime.shutdown();
  }
}
