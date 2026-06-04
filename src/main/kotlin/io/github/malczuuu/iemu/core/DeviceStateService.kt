package io.github.malczuuu.iemu.core

import java.time.Instant
import java.util.concurrent.ScheduledExecutorService

class DeviceStateService(
    scheduler: ScheduledExecutorService,
    private val currentTime: CurrentTimeModule = CurrentTimeModule(scheduler),
    private val light: LightModule = LightModule(false, scheduler),
) {

  val deviceState: DeviceState
    get() =
        DeviceState(
            deviceType = "lwm2m_emulator",
            currentTime = currentTime.currentTime,
            timeZone = currentTime.timeZone,
            utcOffset = currentTime.utcOffset,
            errors = emptyList(),
            on = light.isOn,
            onTime = light.onTime,
            dimmer = light.dimmer,
        )

  fun changeDeviceState(update: DeviceStateUpdate) {
    update.currentTime?.let { currentTime.currentTime = it }
    update.timeZone?.let { currentTime.timeZone = it }
    update.utcOffset?.let { currentTime.utcOffset = it }
    update.on?.let { light.isOn = it }
    update.onTime?.let { light.onTime = it }
    update.dimmer?.let { light.dimmer = it }
  }

  fun onCurrentTimeChange(consumer: (Instant) -> Unit) = currentTime.onCurrentTimeChange(consumer)

  fun onStateChange(consumer: (Boolean) -> Unit) = light.onStateChange(consumer)

  fun onDimmerChange(consumer: (Int) -> Unit) = light.onDimmerChange(consumer)

  fun onTimeCounterChange(consumer: (Long) -> Unit) = light.onTimeCounterChange(consumer)

  fun resetErrors() {}

  fun shutdown() {
    light.shutdown()
    currentTime.shutdown()
  }
}
