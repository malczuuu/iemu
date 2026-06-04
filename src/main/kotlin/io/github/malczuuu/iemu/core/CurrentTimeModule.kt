package io.github.malczuuu.iemu.core

import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class CurrentTimeModule(
    scheduler: ScheduledExecutorService,
    private val clock: Clock = Clock.systemUTC(),
) {

  private val onCurrentTimeChangeHooks: MutableList<(Instant) -> Unit> = mutableListOf()

  private var diff: Long = 0

  var timeZone: String = TimeZone.getDefault().id

  var utcOffset: String = SimpleDateFormat("X").format(Calendar.getInstance().time)

  var currentTime: Instant
    get() = clock.instant().minusMillis(diff).truncatedTo(ChronoUnit.SECONDS)
    set(value) {
      diff = clock.instant().toEpochMilli() - value.toEpochMilli()
      val now = currentTime
      onCurrentTimeChangeHooks.forEach { it(now) }
    }

  private var future: ScheduledFuture<*>? =
      scheduler.scheduleAtFixedRate(
          ::tick,
          1000 - clock.instant().toEpochMilli() % 1000,
          1000,
          TimeUnit.MILLISECONDS,
      )

  private fun tick() {
    val now = currentTime
    ArrayList(onCurrentTimeChangeHooks).forEach { it(now) }
  }

  fun shutdown() {
    onCurrentTimeChangeHooks.clear()
    future?.cancel(false)
    future = null
  }

  fun onCurrentTimeChange(consumer: (Instant) -> Unit) = onCurrentTimeChangeHooks.add(consumer)
}
