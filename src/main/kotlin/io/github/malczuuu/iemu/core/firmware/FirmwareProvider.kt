package io.github.malczuuu.iemu.core.firmware

interface FirmwareProvider {

  fun load(): FirmwareSnapshot

  fun save(snapshot: FirmwareSnapshot)

  companion object {
    const val DEFAULT_COLOR = "#000000"
    const val DEFAULT_VERSION = "0.1"
  }
}
