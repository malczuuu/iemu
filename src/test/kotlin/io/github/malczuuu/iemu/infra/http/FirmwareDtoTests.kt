package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FirmwareDtoTests {

  private fun sample() =
      FirmwareDto(
          fileChecksum = "sha256:abc",
          packageUri = "http://x",
          state = FirmwareUpdateState.DOWNLOADED,
          result = FirmwareUpdateResult.NONE,
          pkgVersion = "1.0",
          deliveryMethod = FirmwareUpdateDeliveryMethod.BOTH,
          progress = 42,
      )

  @Test
  fun `derived value fields should expose the integer codes of each enum`() {
    val dto = sample()

    assertEquals(FirmwareUpdateState.DOWNLOADED.value, dto.stateValue)
    assertEquals(FirmwareUpdateResult.NONE.value, dto.resultValue)
    assertEquals(FirmwareUpdateDeliveryMethod.BOTH.value, dto.deliveryMethodValue)
  }

  @Test
  fun `derived value fields should be null when the underlying enums are null`() {
    val dto =
        FirmwareDto(
            fileChecksum = null,
            packageUri = null,
            state = null,
            result = null,
            pkgVersion = null,
            deliveryMethod = null,
            progress = null,
        )

    assertNull(dto.stateValue)
    assertNull(dto.resultValue)
    assertNull(dto.deliveryMethodValue)
  }

  @Test
  fun `two DTOs with identical content should be equal and share a hash code`() {
    val a = sample()
    val b = sample()

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun `two DTOs with different packageUri should not be equal`() {
    val a = sample()
    val b = a.copy(packageUri = "http://other")

    assertNotEquals(a, b)
  }

  @Test
  fun `equals should reject instances of a different type`() {
    assertNotEquals(sample() as Any, "not-a-dto" as Any)
  }

  @Test
  fun `equals should be reflexive for the same instance`() {
    val dto = sample()

    assertEquals(dto, dto)
  }
}
