package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.common.JacksonFactory
import io.github.malczuuu.iemu.domain.State
import io.github.malczuuu.iemu.domain.StateService
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StateEndpointTests {

  private val stateService = mockk<StateService>()
  private val mapper = JacksonFactory.getJsonMapper()
  private val ctx = mockk<Context>(relaxed = true)
  private val endpoint = StateEndpoint(stateService, mapper)

  @BeforeEach
  fun setUp() {
    every { ctx.status(any<HttpStatus>()) } returns ctx
    every { ctx.contentType(any<String>()) } returns ctx
    every { ctx.result(any<String>()) } returns ctx
  }

  private fun fullState() =
      State(
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
      State(
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
  fun `get() should set status 200 and serialize current state as JSON`() {
    every { stateService.getState() } returns fullState()

    endpoint.get(ctx)

    verify { ctx.status(HttpStatus.OK) }
    verify { ctx.contentType("application/json") }
    verify { ctx.result(match<String> { it.contains("lwm2m_emulator") }) }
  }

  @Test
  fun `get() should include currentTime and timezone fields in the JSON response`() {
    every { stateService.getState() } returns fullStateInWarsawTimeZone()

    endpoint.get(ctx)

    verify {
      ctx.result(
          match<String> { it.contains("2024-06-15T10:00:00Z") && it.contains("Europe/Warsaw") }
      )
    }
  }

  @Test
  fun `patch() should deserialize body, convert to StateUpdate, and call changeState`() {
    val body = """{"on":true,"dimmer":80}"""
    every { ctx.bodyAsBytes() } returns body.toByteArray()
    every { stateService.changeState(any()) } just Runs

    endpoint.patch(ctx)

    verify { stateService.changeState(match { it.on == true && it.dimmer == 80 }) }
    verify { ctx.status(HttpStatus.NO_CONTENT) }
  }

  @Test
  fun `patch() should pass only the non-null fields from the request body to changeState`() {
    val body = """{"timeZone":"Europe/Warsaw"}"""
    every { ctx.bodyAsBytes() } returns body.toByteArray()
    every { stateService.changeState(any()) } just Runs

    endpoint.patch(ctx)

    verify { stateService.changeState(match { it.timeZone == "Europe/Warsaw" }) }
  }

  @Test
  fun `patch() should parse a currentTime string into an Instant in the StateUpdate`() {
    val body = """{"currentTime":"2025-03-01T12:00:00Z"}"""
    every { ctx.bodyAsBytes() } returns body.toByteArray()
    every { stateService.changeState(any()) } just Runs

    endpoint.patch(ctx)

    verify {
      stateService.changeState(match { it.currentTime == Instant.parse("2025-03-01T12:00:00Z") })
    }
  }
}
