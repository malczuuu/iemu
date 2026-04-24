package io.github.malczuuu.iemu.domain

import java.time.Instant

interface StateService {

  fun getState(): State

  fun changeState(update: StateUpdate)

  fun subscribeOnCurrentTimeChange(consumer: (Instant) -> Unit): Boolean

  fun subscribeOnStateChange(consumer: (Boolean) -> Unit): Boolean

  fun subscribeOnDimmerChange(consumer: (Int) -> Unit): Boolean

  fun subscribeOnTimeCounterChange(consumer: (Long) -> Unit): Boolean

  fun resetErrors()

  fun shutdown()
}
