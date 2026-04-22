package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.common.CancelAwareTimerTask
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.TimeZone
import java.util.Timer

class CurrentTimeModule(private val timer: Timer) {

  private val onCurrentTimeChange: MutableList<(Instant) -> Unit> = mutableListOf()

  private var diff: Long = 0

  var timeZone: String = TimeZone.getDefault().id

  var utcOffset: String = SimpleDateFormat("X").format(Calendar.getInstance().time)

  var currentTime: Instant
    get() = Instant.now().minusMillis(diff).truncatedTo(ChronoUnit.SECONDS)
    set(value) {
      diff = Instant.now().toEpochMilli() - value.toEpochMilli()
      val now = currentTime
      onCurrentTimeChange.forEach { it(now) }
    }

  init {
    timer.schedule(
        CancelAwareTimerTask {
          val now = currentTime
          ArrayList(onCurrentTimeChange).forEach { it(now) }
        },
        1000 - Instant.now().toEpochMilli() % 1000,
        1000,
    )
  }

  fun shutdown() {
    timer.cancel()
    timer.purge()
  }

  fun subscribeOnCurrentTimeChange(consumer: (Instant) -> Unit) = onCurrentTimeChange.add(consumer)
}
