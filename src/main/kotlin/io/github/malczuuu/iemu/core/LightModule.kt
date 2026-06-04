package io.github.malczuuu.iemu.core

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LightModule(on: Boolean, private val scheduler: ScheduledExecutorService) {

  private val onStateChangeHooks: MutableList<(Boolean) -> Unit> = mutableListOf()
  private val onDimmerChangeHooks: MutableList<(Int) -> Unit> = mutableListOf()
  private val onTimeChangeHooks: MutableList<(Long) -> Unit> = mutableListOf()

  private var future: ScheduledFuture<*>? = null

  var isOn: Boolean = on
    set(value) {
      synchronized(this) {
        if (field != value) {
          field = value
          onStateChangeHooks.forEach { it(field) }
          scheduleIncOnTime()
        }
      }
    }

  var dimmer: Int = 0
    set(value) {
      synchronized(this) {
        val clamped = value.coerceIn(0, 100)
        if (clamped != field) {
          field = clamped
          onDimmerChangeHooks.forEach { it(field) }
        }
      }
    }

  var onTime: Long = 0L
    set(value) {
      synchronized(this) {
        if (value != field) {
          field = value
          onTimeChangeHooks.forEach { it(value) }
        }
      }
    }

  init {
    if (on) {
      synchronized(this) {
        scheduleIncOnTime()
      }
    }
  }

  private fun scheduleIncOnTime() {
    if (isOn) {
      check(future == null) { "future must be null to schedule" }
      future = scheduler.scheduleAtFixedRate(::incOnTime, 1000, 1000, TimeUnit.MILLISECONDS)
    } else {
      checkNotNull(future) { "future must not be null to unschedule" }
      future!!.cancel(false)
      future = null
    }
  }

  private fun incOnTime() {
    if (isOn) onTime++
  }

  fun shutdown() {
    synchronized(this) {
      onStateChangeHooks.clear()
      onDimmerChangeHooks.clear()
      onTimeChangeHooks.clear()
      future?.cancel(false)
      future = null
    }
  }

  fun onStateChange(consumer: (Boolean) -> Unit) = onStateChangeHooks.add(consumer)

  fun onDimmerChange(consumer: (Int) -> Unit) = onDimmerChangeHooks.add(consumer)

  fun onTimeCounterChange(consumer: (Long) -> Unit) = onTimeChangeHooks.add(consumer)
}
