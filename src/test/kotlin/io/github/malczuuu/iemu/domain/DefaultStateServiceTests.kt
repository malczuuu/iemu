package io.github.malczuuu.iemu.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultStateServiceTests {

  private val currentTime = mockk<CurrentTimeModule>(relaxed = true)
  private val light = mockk<LightModule>(relaxed = true)
  private val service = DefaultStateService(currentTime, light)

  @Test
  fun `getState() should aggregate values from CurrentTimeModule and LightModule`() {
    every { currentTime.currentTime } returns Instant.parse("2025-01-01T12:00:00Z")
    every { currentTime.timeZone } returns "Europe/Warsaw"
    every { currentTime.utcOffset } returns "+01:00"
    every { light.isOn } returns true
    every { light.onTime } returns 42L
    every { light.dimmer } returns 75

    val state = service.getState()

    assertEquals("lwm2m_emulator", state.deviceType)
    assertEquals(Instant.parse("2025-01-01T12:00:00Z"), state.currentTime)
    assertEquals("Europe/Warsaw", state.timeZone)
    assertEquals("+01:00", state.utcOffset)
    assertEquals(emptyList<DeviceError>(), state.errors)
    assertEquals(true, state.on)
    assertEquals(42L, state.onTime)
    assertEquals(75, state.dimmer)
  }

  @Test
  fun `changeState() should forward non-null values to the underlying modules`() {
    service.changeState(
        StateUpdate(
            currentTime = Instant.parse("2020-05-05T00:00:00Z"),
            timeZone = "UTC",
            utcOffset = "+00:00",
            on = true,
            onTime = 5L,
            dimmer = 60,
        )
    )

    verify { currentTime.currentTime = Instant.parse("2020-05-05T00:00:00Z") }
    verify { currentTime.timeZone = "UTC" }
    verify { currentTime.utcOffset = "+00:00" }
    verify { light.isOn = true }
    verify { light.onTime = 5L }
    verify { light.dimmer = 60 }
  }

  @Test
  fun `changeState() should skip null fields and leave the modules untouched`() {
    service.changeState(StateUpdate())

    verify(exactly = 0) { currentTime.currentTime = any() }
    verify(exactly = 0) { currentTime.timeZone = any() }
    verify(exactly = 0) { currentTime.utcOffset = any() }
    verify(exactly = 0) { light.isOn = any() }
    verify(exactly = 0) { light.onTime = any() }
    verify(exactly = 0) { light.dimmer = any() }
  }

  @Test
  fun `subscribe methods should forward subscribers to the matching module`() {
    val onCurrentTime: (Instant) -> Unit = {}
    val onState: (Boolean) -> Unit = {}
    val onDimmer: (Int) -> Unit = {}
    val onTime: (Long) -> Unit = {}

    service.subscribeOnCurrentTimeChange(onCurrentTime)
    service.subscribeOnStateChange(onState)
    service.subscribeOnDimmerChange(onDimmer)
    service.subscribeOnTimeCounterChange(onTime)

    verify { currentTime.subscribeOnCurrentTimeChange(onCurrentTime) }
    verify { light.subscribeOnStateChange(onState) }
    verify { light.subscribeOnDimmerChange(onDimmer) }
    verify { light.subscribeOnTimeCounterChange(onTime) }
  }

  @Test
  fun `shutdown() should shut down the light module and the current time module`() {
    service.shutdown()

    verify { light.shutdown() }
    verify { currentTime.shutdown() }
  }

  @Test
  fun `resetErrors() should be a no-op`() {
    service.resetErrors()
    // Intentionally no behaviour yet; the method exists as a hook for future error tracking.
  }
}
