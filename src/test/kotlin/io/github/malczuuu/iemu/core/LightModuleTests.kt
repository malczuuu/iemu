package io.github.malczuuu.iemu.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LightModuleTests {

  private val runnables: MutableList<Runnable> = mutableListOf()
  private val futures: MutableList<ScheduledFuture<*>> = mutableListOf()
  private val scheduler: ScheduledExecutorService = mockk(relaxed = true)

  private val lightModule = LightModule(false, scheduler)

  @BeforeEach
  fun beforeEach() {
    val slot = slot<Runnable>()
    every {
      scheduler.scheduleAtFixedRate(capture(slot), any<Long>(), any<Long>(), any<TimeUnit>())
    } answers
        {
          val future = mockk<ScheduledFuture<*>>(relaxed = true)
          runnables.add(slot.captured)
          futures.add(future)
          future
        }
  }

  @Test
  fun `onTime should be incremented on each tick after isOn is set to true`() {
    lightModule.isOn = true
    runnables.forEach { it.run() }

    assertTrue(lightModule.isOn)
    assertEquals(1L, lightModule.onTime)
  }

  @Test
  fun `onTime should stop incrementing once isOn is set back to false`() {
    lightModule.isOn = true
    runnables.forEach { it.run() }
    runnables.forEach { it.run() }
    lightModule.isOn = false
    runnables.forEach { it.run() }
    runnables.forEach { it.run() }
    runnables.forEach { it.run() }

    assertFalse(lightModule.isOn)
    assertEquals(2L, lightModule.onTime)
  }

  @Test
  fun `state change subscribers should be notified when isOn is updated`() {
    var value = false
    lightModule.onStateChange { value = it }

    lightModule.isOn = true

    assertTrue(value)
  }

  @Test
  fun `onTime subscribers should be notified when the counter advances`() {
    var value = -1L
    lightModule.onTimeCounterChange { value = it }

    lightModule.isOn = true
    runnables.forEach { it.run() }

    assertEquals(1L, value)
  }

  @Test
  fun `dimmer subscribers should be notified when the dimmer is updated`() {
    var value = -1
    lightModule.onDimmerChange { value = it }

    lightModule.dimmer = 45

    assertEquals(45, value)
  }

  @Test
  fun `dimmer should be clamped to zero when a negative value is assigned`() {
    lightModule.dimmer = -1

    assertEquals(0, lightModule.dimmer)
  }

  @Test
  fun `dimmer should be clamped to one hundred when a value above one hundred is assigned`() {
    lightModule.dimmer = 101

    assertEquals(100, lightModule.dimmer)
  }

  @Test
  fun `shutdown() should cancel the active future`() {
    lightModule.isOn = true
    runnables.forEach { it.run() }

    lightModule.shutdown()

    verify { futures.first().cancel(false) }
  }
}
