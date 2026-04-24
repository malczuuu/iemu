package io.github.malczuuu.iemu.domain

import java.time.Instant

interface CurrentTimeModule {

  var timeZone: String

  var utcOffset: String

  var currentTime: Instant

  fun subscribeOnCurrentTimeChange(consumer: (Instant) -> Unit): Boolean

  fun shutdown()
}
