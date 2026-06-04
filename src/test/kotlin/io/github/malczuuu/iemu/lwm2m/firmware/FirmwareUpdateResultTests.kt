package io.github.malczuuu.iemu.lwm2m.firmware

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirmwareUpdateResultTests {

  @Test
  fun `initial() should return NONE as the default result`() {
    assertEquals(FirmwareUpdateResult.NONE, FirmwareUpdateResult.initial())
  }

  @Test
  fun `fromValue() should resolve each known integer code to its matching entry`() {
    assertEquals(FirmwareUpdateResult.NONE, FirmwareUpdateResult.fromValue(0))
    assertEquals(FirmwareUpdateResult.SUCCESSFUL, FirmwareUpdateResult.fromValue(1))
    assertEquals(FirmwareUpdateResult.INVALID_URI, FirmwareUpdateResult.fromValue(7))
    assertEquals(FirmwareUpdateResult.UNSUPPORTED_PROTOCOL, FirmwareUpdateResult.fromValue(9))
  }

  @Test
  fun `fromValue() should fall back to NONE for unknown or null codes`() {
    assertEquals(FirmwareUpdateResult.NONE, FirmwareUpdateResult.fromValue(999))
    assertEquals(FirmwareUpdateResult.NONE, FirmwareUpdateResult.fromValue(null))
  }

  @Test
  fun `enum entries should expose the integer codes defined by the LwM2m spec`() {
    assertEquals(0, FirmwareUpdateResult.NONE.value)
    assertEquals(8, FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED.value)
    assertEquals(9, FirmwareUpdateResult.UNSUPPORTED_PROTOCOL.value)
  }
}
