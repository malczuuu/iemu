package io.github.malczuuu.iemu.lwm2m.firmware

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirmwareUpdateStateTests {

  @Test
  fun `fromValue() should resolve each known integer code to its matching state`() {
    assertEquals(FirmwareUpdateState.IDLE, FirmwareUpdateState.fromValue(0))
    assertEquals(FirmwareUpdateState.DOWNLOADING, FirmwareUpdateState.fromValue(1))
    assertEquals(FirmwareUpdateState.DOWNLOADED, FirmwareUpdateState.fromValue(2))
    assertEquals(FirmwareUpdateState.UPDATING, FirmwareUpdateState.fromValue(3))
  }

  @Test
  fun `fromValue() should fall back to IDLE for unknown or null codes`() {
    assertEquals(FirmwareUpdateState.IDLE, FirmwareUpdateState.fromValue(42))
    assertEquals(FirmwareUpdateState.IDLE, FirmwareUpdateState.fromValue(null))
  }
}
