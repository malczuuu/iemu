package io.github.malczuuu.iemu.domain

import java.time.Instant
import java.util.Timer

class DefaultStateService(
    private val currentTime: CurrentTimeModule = DefaultCurrentTimeModule(Timer()),
    private val light: LightModule = DefaultLightModule(false, Timer()),
) : StateService {

  override fun getState(): State =
      State(
          deviceType = "lwm2m_emulator",
          currentTime = currentTime.currentTime,
          timeZone = currentTime.timeZone,
          utcOffset = currentTime.utcOffset,
          errors = emptyList(),
          on = light.isOn,
          onTime = light.onTime,
          dimmer = light.dimmer,
      )

  override fun changeState(update: StateUpdate) {
    update.currentTime?.let { currentTime.currentTime = it }
    update.timeZone?.let { currentTime.timeZone = it }
    update.utcOffset?.let { currentTime.utcOffset = it }
    update.on?.let { light.isOn = it }
    update.onTime?.let { light.onTime = it }
    update.dimmer?.let { light.dimmer = it }
  }

  override fun subscribeOnCurrentTimeChange(consumer: (Instant) -> Unit) =
      currentTime.subscribeOnCurrentTimeChange(consumer)

  override fun subscribeOnStateChange(consumer: (Boolean) -> Unit) =
      light.subscribeOnStateChange(consumer)

  override fun subscribeOnDimmerChange(consumer: (Int) -> Unit) =
      light.subscribeOnDimmerChange(consumer)

  override fun subscribeOnTimeCounterChange(consumer: (Long) -> Unit) =
      light.subscribeOnTimeCounterChange(consumer)

  override fun resetErrors() {}

  override fun shutdown() {
    light.shutdown()
    currentTime.shutdown()
  }
}
