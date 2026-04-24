package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.domain.State
import io.github.malczuuu.iemu.domain.StateService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.eclipse.leshan.client.servers.ServerIdentity
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.node.LwM2mSingleResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LightControlEnablerTests {

  private val stateService = mockk<StateService>(relaxed = true)
  private val identity = mockk<ServerIdentity>(relaxed = true)
  private val model = mockk<ObjectModel>(relaxed = true)

  private fun state(on: Boolean = false, dimmer: Int = 0, onTime: Long = 0L) =
      State(
          deviceType = "lwm2m_emulator",
          currentTime = Instant.parse("2025-01-01T00:00:00Z"),
          timeZone = "UTC",
          utcOffset = "+00:00",
          errors = emptyList(),
          on = on,
          dimmer = dimmer,
          onTime = onTime,
      )

  private fun enabler() = LightControlEnabler(stateService)

  @Test
  fun `constructor should subscribe to state, dimmer and time counter changes`() {
    enabler()

    verify { stateService.subscribeOnStateChange(any()) }
    verify { stateService.subscribeOnDimmerChange(any()) }
    verify { stateService.subscribeOnTimeCounterChange(any()) }
  }

  @Test
  fun `read() for ON_OFF should return the current on flag from state`() {
    every { stateService.getState() } returns state(on = true)

    val response = enabler().read(identity, 5850)

    assertTrue(response.isSuccess)
  }

  @Test
  fun `read() for DIMMER should return the current dimmer value from state`() {
    every { stateService.getState() } returns state(dimmer = 42)

    val response = enabler().read(identity, 5851)

    assertTrue(response.isSuccess)
  }

  @Test
  fun `read() for ON_TIME should return the current on time counter from state`() {
    every { stateService.getState() } returns state(onTime = 123L)

    val response = enabler().read(identity, 5852)

    assertTrue(response.isSuccess)
  }

  @Test
  fun `read() for CUMULATIVE_ACTIVE_POWER should return a not-found response`() {
    val response = enabler().read(identity, 5805)

    assertEquals(org.eclipse.leshan.core.ResponseCode.NOT_FOUND, response.code)
  }

  @Test
  fun `write() for ON_OFF should forward the new on state to StateService`() {
    every { stateService.changeState(any()) } just Runs

    val response =
        enabler().write(identity, 5850, LwM2mSingleResource.newBooleanResource(5850, true))

    assertTrue(response.isSuccess)
    verify { stateService.changeState(match { it.on == true }) }
  }

  @Test
  fun `write() for DIMMER should forward the new dimmer value to StateService`() {
    every { stateService.changeState(any()) } just Runs

    val response =
        enabler().write(identity, 5851, LwM2mSingleResource.newIntegerResource(5851, 55L))

    assertTrue(response.isSuccess)
    verify { stateService.changeState(match { it.dimmer == 55 }) }
  }

  @Test
  fun `write() for ON_TIME should forward the new on time to StateService`() {
    every { stateService.changeState(any()) } just Runs

    val response =
        enabler().write(identity, 5852, LwM2mSingleResource.newIntegerResource(5852, 999L))

    assertTrue(response.isSuccess)
    verify { stateService.changeState(match { it.onTime == 999L }) }
  }

  @Test
  fun `getAvailableResourceIds() should list only the resources supported by the emulator`() {
    val ids = enabler().getAvailableResourceIds(model)

    assertEquals(listOf(5850, 5851, 5852), ids)
  }

  @Test
  fun `companion object create() should produce an enabler and wire up subscriptions once each`() {
    val produced = LightControlEnabler.create(stateService)

    assertTrue(produced is LightControlEnabler)
    verify(exactly = 1) { stateService.subscribeOnStateChange(any()) }
    verify(exactly = 1) { stateService.subscribeOnDimmerChange(any()) }
    verify(exactly = 1) { stateService.subscribeOnTimeCounterChange(any()) }
  }
}
