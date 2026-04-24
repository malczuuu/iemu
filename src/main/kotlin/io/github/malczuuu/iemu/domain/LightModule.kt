package io.github.malczuuu.iemu.domain

interface LightModule {

  var isOn: Boolean

  var dimmer: Int

  var onTime: Long

  fun subscribeOnStateChange(consumer: (Boolean) -> Unit): Boolean

  fun subscribeOnDimmerChange(consumer: (Int) -> Unit): Boolean

  fun subscribeOnTimeCounterChange(consumer: (Long) -> Unit): Boolean

  fun shutdown()
}
