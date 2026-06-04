package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.core.DeviceError
import io.github.malczuuu.iemu.core.DeviceState
import io.github.malczuuu.iemu.core.FirmwareState
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateState
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RestModelMappingTests {

  @Test
  fun `State toDto() should map all fields to a StateDto with string currentTime`() {
    val state =
        DeviceState(
            deviceType = "lwm2m_emulator",
            currentTime = Instant.parse("2025-06-01T10:00:00Z"),
            timeZone = "Europe/Warsaw",
            utcOffset = "+02:00",
            errors = listOf(DeviceError(code = 1, message = "err")),
            on = true,
            onTime = 42L,
            dimmer = 80,
        )

    val dto = state.toDto()

    assertEquals("lwm2m_emulator", dto.deviceType)
    assertEquals("2025-06-01T10:00:00Z", dto.currentTime)
    assertEquals("Europe/Warsaw", dto.timeZone)
    assertEquals("+02:00", dto.utcOffset)
    assertEquals(1, dto.errors?.size)
    assertEquals(1, dto.errors?.first()?.code)
    assertEquals("err", dto.errors?.first()?.message)
    assertEquals(true, dto.on)
    assertEquals(42L, dto.onTime)
    assertEquals(80, dto.dimmer)
  }

  @Test
  fun `State toDto() should produce an empty errors list when state has no errors`() {
    val state =
        DeviceState(
            deviceType = "lwm2m_emulator",
            currentTime = Instant.parse("2025-01-01T00:00:00Z"),
            timeZone = "UTC",
            utcOffset = "+00:00",
            errors = emptyList(),
            on = false,
            onTime = 0L,
            dimmer = 0,
        )

    val dto = state.toDto()

    assertEquals(emptyList<ErrorDto>(), dto.errors)
  }

  @Test
  fun `StateDto toUpdate() should parse currentTime string into an Instant`() {
    val dto = DeviceStateDto(currentTime = "2024-03-15T08:30:00Z", on = true, dimmer = 50)

    val update = dto.toUpdate()

    assertEquals(Instant.parse("2024-03-15T08:30:00Z"), update.currentTime)
    assertEquals(true, update.on)
    assertEquals(50, update.dimmer)
  }

  @Test
  fun `StateDto toUpdate() should leave null fields as null in the update`() {
    val dto = DeviceStateDto()

    val update = dto.toUpdate()

    assertNull(update.currentTime)
    assertNull(update.timeZone)
    assertNull(update.utcOffset)
    assertNull(update.on)
    assertNull(update.onTime)
    assertNull(update.dimmer)
  }

  @Test
  fun `Firmware toDto() should map all fields including enum values and packageVersion to pkgVersion`() {
    val firmware =
        FirmwareState(
            file = "bytes".toByteArray(),
            fileChecksum = "sha256:abc",
            packageUri = "http://fw",
            state = FirmwareUpdateState.DOWNLOADED,
            result = FirmwareUpdateResult.NONE,
            packageVersion = "2.0.0",
            deliveryMethod = FirmwareUpdateDeliveryMethod.BOTH,
            progress = 0,
        )

    val dto = firmware.toDto()

    assertEquals("http://fw", dto.packageUri)
    assertEquals(FirmwareUpdateState.DOWNLOADED, dto.state)
    assertEquals(FirmwareUpdateResult.NONE, dto.result)
    assertEquals("2.0.0", dto.pkgVersion)
    assertEquals(FirmwareUpdateDeliveryMethod.BOTH, dto.deliveryMethod)
    assertEquals(0, dto.progress)
    assertEquals("sha256:abc", dto.fileChecksum)
  }
}
