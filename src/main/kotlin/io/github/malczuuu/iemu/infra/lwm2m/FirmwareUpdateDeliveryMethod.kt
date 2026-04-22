package io.github.malczuuu.iemu.infra.lwm2m

enum class FirmwareUpdateDeliveryMethod(val value: Int) {
  PULL_ONLY(0),
  PUSH_ONLY(1),
  BOTH(2);

  companion object {
    fun initial(): FirmwareUpdateDeliveryMethod = BOTH

    fun fromValue(value: Int?): FirmwareUpdateDeliveryMethod =
        entries.firstOrNull { it.value == value } ?: PULL_ONLY
  }
}
