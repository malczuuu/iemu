package io.github.malczuuu.iemu.infra.lwm2m

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirmwareUpdateDeliveryMethodTests {

  @Test
  fun `initial() should return BOTH as the default delivery method`() {
    assertEquals(FirmwareUpdateDeliveryMethod.BOTH, FirmwareUpdateDeliveryMethod.initial())
  }

  @Test
  fun `fromValue() should resolve each known integer code to its matching entry`() {
    assertEquals(FirmwareUpdateDeliveryMethod.PULL_ONLY, FirmwareUpdateDeliveryMethod.fromValue(0))
    assertEquals(FirmwareUpdateDeliveryMethod.PUSH_ONLY, FirmwareUpdateDeliveryMethod.fromValue(1))
    assertEquals(FirmwareUpdateDeliveryMethod.BOTH, FirmwareUpdateDeliveryMethod.fromValue(2))
  }

  @Test
  fun `fromValue() should fall back to PULL_ONLY for unknown or null codes`() {
    assertEquals(FirmwareUpdateDeliveryMethod.PULL_ONLY, FirmwareUpdateDeliveryMethod.fromValue(42))
    assertEquals(
        FirmwareUpdateDeliveryMethod.PULL_ONLY,
        FirmwareUpdateDeliveryMethod.fromValue(null),
    )
  }
}
