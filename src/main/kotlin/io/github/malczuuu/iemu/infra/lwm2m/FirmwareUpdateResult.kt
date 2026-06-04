package io.github.malczuuu.iemu.infra.lwm2m

enum class FirmwareUpdateResult(val value: Int) {
  NONE(0),
  SUCCESSFUL(1),
  FLASH_MEMORY_ERROR(2),
  OUT_OF_RAM(3),
  DOWNLOADING_CONNECTION_LOST(4),
  PACKAGE_INTEGRITY_CHECK_FAILURE(5),
  UNSUPPORTED_PACKAGE_TYPE(6),
  INVALID_URI(7),
  FIRMWARE_UPDATE_FAILED(8),
  UNSUPPORTED_PROTOCOL(9),
  ;

  companion object {
    fun initial(): FirmwareUpdateResult = NONE

    fun fromValue(value: Int?): FirmwareUpdateResult =
        entries.firstOrNull { it.value == value } ?: NONE
  }
}
