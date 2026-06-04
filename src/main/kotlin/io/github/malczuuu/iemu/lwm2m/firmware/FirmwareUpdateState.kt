package io.github.malczuuu.iemu.lwm2m.firmware

enum class FirmwareUpdateState(val value: Int) {
  IDLE(0),
  DOWNLOADING(1),
  DOWNLOADED(2),
  UPDATING(3),
  ;

  companion object {

    fun fromValue(value: Int?): FirmwareUpdateState =
        entries.firstOrNull { it.value == value } ?: IDLE
  }
}
