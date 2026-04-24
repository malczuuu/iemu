package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.common.CancelAwareTimerTask
import java.util.Timer
import java.util.TimerTask

class DefaultLightModule(on: Boolean, private val timer: Timer) : LightModule {

  private val onStateChange: MutableList<(Boolean) -> Unit> = mutableListOf()
  private val onDimmerChange: MutableList<(Int) -> Unit> = mutableListOf()
  private val onTimeChange: MutableList<(Long) -> Unit> = mutableListOf()

  private var timerTask: TimerTask? = null

  override var isOn: Boolean = on
    set(value) {
      if (field != value) {
        field = value
        onStateChange.forEach { it(field) }
        scheduleIncOnTime()
      }
    }

  override var dimmer: Int = 0
    set(value) {
      val clamped = value.coerceIn(0, 100)
      if (clamped != field) {
        field = clamped
        onDimmerChange.forEach { it(field) }
      }
    }

  override var onTime: Long = 0L
    set(value) {
      if (value != field) {
        field = value
        onTimeChange.forEach { it(value) }
      }
    }

  init {
    if (on) {
      scheduleIncOnTime()
    }
  }

  @Synchronized
  private fun scheduleIncOnTime() {
    if (isOn) {
      check(timerTask == null) { "timerTask must be null to schedule" }
      timerTask = CancelAwareTimerTask { incOnTime() }
      timer.schedule(timerTask, 1000, 1000)
    } else {
      checkNotNull(timerTask) { "timerTask must not be null to unschedule" }
      timerTask!!.cancel()
      timerTask = null
    }
  }

  private fun incOnTime() = onTime++

  @Synchronized
  override fun shutdown() {
    timerTask?.cancel()
    timer.cancel()
    timer.purge()
    onStateChange.clear()
    onDimmerChange.clear()
    onTimeChange.clear()
  }

  override fun subscribeOnStateChange(consumer: (Boolean) -> Unit) = onStateChange.add(consumer)

  override fun subscribeOnDimmerChange(consumer: (Int) -> Unit) = onDimmerChange.add(consumer)

  override fun subscribeOnTimeCounterChange(consumer: (Long) -> Unit) = onTimeChange.add(consumer)
}
