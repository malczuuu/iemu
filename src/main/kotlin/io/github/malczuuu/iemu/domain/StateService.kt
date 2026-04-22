package io.github.malczuuu.iemu.domain

import java.time.Instant
import java.util.Timer

class StateService {

  private val currentTime: CurrentTimeModule = CurrentTimeModule(Timer())
  private val light: LightModule = LightModule(false, Timer())

  fun getState(): StateDto =
      StateDto(
          deviceType = "lwm2m_emulator",
          currentTime = currentTime.currentTime.toString(),
          timeZone = currentTime.timeZone,
          utcOffset = currentTime.utcOffset,
          errors = emptyList(),
          on = light.isOn,
          onTime = light.onTime,
          dimmer = light.dimmer,
      )

  fun changeState(state: StateDto) {
    state.currentTime?.let { currentTime.currentTime = Instant.parse(it) }
    state.timeZone?.let { currentTime.timeZone = it }
    state.utcOffset?.let { currentTime.utcOffset = it }
    state.on?.let { light.isOn = it }
    state.onTime?.let { light.onTime = it }
    state.dimmer?.let { light.dimmer = it }
  }

  fun subscribeOnCurrentTimeChange(consumer: (Instant) -> Unit) =
      currentTime.subscribeOnCurrentTimeChange(consumer)

  fun subscribeOnStateChange(consumer: (Boolean) -> Unit) = light.subscribeOnStateChange(consumer)

  fun subscribeOnDimmerChange(consumer: (Int) -> Unit) = light.subscribeOnDimmerChange(consumer)

  fun subscribeOnTimeCounterChange(consumer: (Long) -> Unit) =
      light.subscribeOnTimeCounterChange(consumer)

  fun resetErrors() {}

  fun shutdown() {
    light.shutdown()
    currentTime.shutdown()
  }
}
