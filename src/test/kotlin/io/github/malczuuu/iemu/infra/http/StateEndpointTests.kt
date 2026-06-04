package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.core.DeviceState
import io.github.malczuuu.iemu.core.DeviceStateService
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.http.bodyAsClass
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StateEndpointTests {

  private val deviceStateService = mockk<DeviceStateService>()
  private val ctx = mockk<Context>(relaxed = true)
  private val endpoint = StateEndpoint(deviceStateService)

  @BeforeEach
  fun setUp() {
    every { ctx.status(any<HttpStatus>()) } returns ctx
    every { ctx.json(any()) } returns ctx
  }

  private fun fullState() =
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

  private fun fullStateInWarsawTimeZone() =
      DeviceState(
          deviceType = "lwm2m_emulator",
          currentTime = Instant.parse("2024-06-15T10:00:00Z"),
          timeZone = "Europe/Warsaw",
          utcOffset = "+00:00",
          errors = emptyList(),
          on = false,
          onTime = 0L,
          dimmer = 0,
      )

  @Test
  fun `get() should set status 200 and return current state as DTO`() {
    every { deviceStateService.deviceState } returns fullState()

    endpoint.get(ctx)

    verify { ctx.status(HttpStatus.OK) }
    verify { ctx.json(match<DeviceStateDto> { it.deviceType == "lwm2m_emulator" }) }
  }

  @Test
  fun `get() should include currentTime and timezone fields in the DTO`() {
    every { deviceStateService.deviceState } returns fullStateInWarsawTimeZone()

    endpoint.get(ctx)

    verify {
      ctx.json(
          match<DeviceStateDto> {
            it.currentTime == "2024-06-15T10:00:00Z" && it.timeZone == "Europe/Warsaw"
          },
      )
    }
  }

  @Test
  fun `patch() should deserialize body, convert to DeviceStateUpdate, and call changeDeviceState`() {
    every { ctx.bodyAsClass<DeviceStateDto>() } returns DeviceStateDto(on = true, dimmer = 80)
    every { deviceStateService.changeDeviceState(any()) } just Runs

    endpoint.patch(ctx)

    verify { deviceStateService.changeDeviceState(match { it.on == true && it.dimmer == 80 }) }
    verify { ctx.status(HttpStatus.NO_CONTENT) }
  }

  @Test
  fun `patch() should pass only the non-null fields from the request body to changeDeviceState`() {
    every { ctx.bodyAsClass<DeviceStateDto>() } returns DeviceStateDto(timeZone = "Europe/Warsaw")
    every { deviceStateService.changeDeviceState(any()) } just Runs

    endpoint.patch(ctx)

    verify { deviceStateService.changeDeviceState(match { it.timeZone == "Europe/Warsaw" }) }
  }

  @Test
  fun `patch() should parse a currentTime string into an Instant in the DeviceStateUpdate`() {
    every { ctx.bodyAsClass<DeviceStateDto>() } returns
        DeviceStateDto(currentTime = "2025-03-01T12:00:00Z")
    every { ctx.body() } returns """{"currentTime":"2025-03-01T12:00:00Z"}"""
    every { deviceStateService.changeDeviceState(any()) } just Runs

    endpoint.patch(ctx)

    verify {
      deviceStateService.changeDeviceState(
          match { it.currentTime == Instant.parse("2025-03-01T12:00:00Z") }
      )
    }
  }
}
