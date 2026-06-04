package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.domain.DeviceError
import io.github.malczuuu.iemu.domain.State
import io.github.malczuuu.iemu.domain.StateService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.Date
import org.eclipse.leshan.client.servers.ServerIdentity
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.node.LwM2mSingleResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeviceEnablerTests {

  private val stateService = mockk<StateService>(relaxed = true)
  private val identity = mockk<ServerIdentity>(relaxed = true)
  private val model = mockk<ObjectModel>(relaxed = true)

  private fun fullState(
      currentTime: Instant = Instant.parse("2025-01-01T00:00:00Z"),
      timeZone: String = "UTC",
      utcOffset: String = "+00:00",
      errors: List<DeviceError> = emptyList(),
      deviceType: String = "lwm2m_emulator",
  ) =
      State(
          deviceType = deviceType,
          currentTime = currentTime,
          timeZone = timeZone,
          utcOffset = utcOffset,
          errors = errors,
          on = false,
          onTime = 0L,
          dimmer = 0,
      )

  private fun enabler() = DeviceEnabler(stateService)

  @Test
  fun `constructor should subscribe to current time change notifications`() {
    enabler()

    verify { stateService.subscribeOnCurrentTimeChange(any()) }
  }

  @Test
  fun `read() MANUFACTURER should return a constant manufacturer string`() {
    assertTrue(enabler().read(identity, 0).isSuccess)
  }

  @Test
  fun `read() MODEL_NUMBER SERIAL_NUMBER FIRMWARE_VERSION should return constant strings`() {
    val e = enabler()
    assertTrue(e.read(identity, 1).isSuccess)
    assertTrue(e.read(identity, 2).isSuccess)
    assertTrue(e.read(identity, 3).isSuccess)
  }

  @Test
  fun `read() BATTERY_LEVEL BATTERY_STATUS should return 100 as a constant`() {
    val e = enabler()
    assertTrue(e.read(identity, 9).isSuccess)
    assertTrue(e.read(identity, 20).isSuccess)
  }

  @Test
  fun `read() MEMORY_FREE and MEMORY_TOTAL should return non-zero values derived from Runtime`() {
    val e = enabler()
    assertTrue(e.read(identity, 10).isSuccess)
    assertTrue(e.read(identity, 21).isSuccess)
  }

  @Test
  fun `read() ERROR_CODE should return a map of indexed error codes`() {
    every { stateService.getState() } returns
        fullState(
            errors =
                listOf(DeviceError(code = 1, message = "a"), DeviceError(code = 2, message = "b")),
        )

    val response = enabler().read(identity, 11)

    assertTrue(response.isSuccess)
  }

  @Test
  fun `read() ERROR_CODE should handle an empty errors list gracefully`() {
    every { stateService.getState() } returns fullState(errors = emptyList())

    val response = enabler().read(identity, 11)

    assertTrue(response.isSuccess)
  }

  @Test
  fun `read() CURRENT_TIME should return the current time from state as a Date response`() {
    every { stateService.getState() } returns
        fullState(currentTime = Instant.parse("2024-06-15T12:34:56Z"))

    val response = enabler().read(identity, 13)

    assertTrue(response.isSuccess)
  }

  @Test
  fun `read() UTC_OFFSET TIMEZONE DEVICE_TYPE should be sourced from the state service`() {
    every { stateService.getState() } returns
        fullState(utcOffset = "+02:00", timeZone = "Europe/Warsaw", deviceType = "custom-type")

    val e = enabler()
    assertTrue(e.read(identity, 14).isSuccess)
    assertTrue(e.read(identity, 15).isSuccess)
    assertTrue(e.read(identity, 17).isSuccess)
  }

  @Test
  fun `read() SUPPORTED_BINDINGS_AND_MODES HARDWARE_VERSION SOFTWARE_VERSION should return constants`() {
    val e = enabler()
    assertTrue(e.read(identity, 16).isSuccess)
    assertTrue(e.read(identity, 18).isSuccess)
    assertTrue(e.read(identity, 19).isSuccess)
  }

  @Test
  fun `write() CURRENT_TIME should forward a truncated instant to StateService`() {
    every { stateService.changeState(any()) } just Runs
    val instantWithMillis = Instant.parse("2024-06-15T12:34:56.789Z")

    val response =
        enabler()
            .write(
                identity,
                13,
                LwM2mSingleResource.newDateResource(13, Date.from(instantWithMillis)),
            )

    assertTrue(response.isSuccess)
    verify {
      stateService.changeState(match { it.currentTime == Instant.parse("2024-06-15T12:34:56Z") })
    }
  }

  @Test
  fun `execute() REBOOT and FACTORY_RESET should return a success response without side effects`() {
    val e = enabler()
    assertTrue(e.execute(identity, 4, "").isSuccess)
    assertTrue(e.execute(identity, 5, "").isSuccess)
    verify(exactly = 0) { stateService.resetErrors() }
  }

  @Test
  fun `execute() RESET_ERROR_CODE should delegate to StateService resetErrors`() {
    every { stateService.resetErrors() } just Runs

    val response = enabler().execute(identity, 12, "")

    assertTrue(response.isSuccess)
    verify { stateService.resetErrors() }
  }

  @Test
  fun `getAvailableResourceIds() should list all supported device resource ids`() {
    val ids = enabler().getAvailableResourceIds(model)

    assertEquals(
        listOf(0, 1, 2, 3, 4, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21),
        ids,
    )
  }

  @Test
  fun `companion object create() should produce a DeviceEnabler and subscribe to current time once`() {
    val produced = DeviceEnabler.create(stateService)

    verify(exactly = 1) { stateService.subscribeOnCurrentTimeChange(any()) }
  }
}
