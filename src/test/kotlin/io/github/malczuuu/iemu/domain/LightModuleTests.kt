package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.mock.TimerTaskMock
import java.util.Timer
import java.util.TimerTask
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class LightModuleTests {

  private val tasks: MutableList<TimerTask> = mutableListOf()
  private val timer: Timer = mock(Timer::class.java)

  private val lightModule = LightModule(false, timer)

  @BeforeEach
  fun beforeEach() {
    doAnswer { tasks.add(it.getArgument(0, TimerTask::class.java)) }
        .`when`(timer)
        .schedule(any(TimerTask::class.java), anyLong(), anyLong())
  }

  @Test
  fun `onTime should be incremented on each tick after isOn is set to true`() {
    lightModule.isOn = true
    tasks.forEach(TimerTaskMock::run)

    assertTrue(lightModule.isOn)
    assertEquals(1L, lightModule.onTime)
  }

  @Test
  fun `onTime should stop incrementing once isOn is set back to false`() {
    lightModule.isOn = true
    tasks.forEach(TimerTaskMock::run)
    tasks.forEach(TimerTaskMock::run)
    lightModule.isOn = false
    tasks.forEach(TimerTaskMock::run)
    tasks.forEach(TimerTaskMock::run)
    tasks.forEach(TimerTaskMock::run)

    assertFalse(lightModule.isOn)
    assertEquals(2L, lightModule.onTime)
  }

  @Test
  fun `state change subscribers should be notified when isOn is updated`() {
    var value = false
    lightModule.subscribeOnStateChange { value = it }

    lightModule.isOn = true

    assertTrue(value)
  }

  @Test
  fun `onTime subscribers should be notified when the counter advances`() {
    var value = -1L
    lightModule.subscribeOnTimeCounterChange { value = it }

    lightModule.isOn = true
    tasks.forEach(TimerTaskMock::run)

    assertEquals(1L, value)
  }

  @Test
  fun `dimmer subscribers should be notified when the dimmer is updated`() {
    var value = -1
    lightModule.subscribeOnDimmerChange { value = it }

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
  fun `shutdown() should cancel and purge the underlying timer`() {
    var canceled = false
    var purged = false
    doAnswer {
          canceled = true
          null
        }
        .`when`(timer)
        .cancel()
    `when`(timer.purge()).thenAnswer {
      purged = true
      tasks.size
    }
    lightModule.isOn = true
    tasks.forEach(TimerTaskMock::run)
    tasks.forEach(TimerTaskMock::run)
    tasks.forEach(TimerTaskMock::run)
    lightModule.isOn = false
    tasks.forEach(TimerTaskMock::run)
    lightModule.isOn = true

    lightModule.shutdown()

    assertTrue(canceled)
    assertTrue(purged)
    assertTrue(tasks.isNotEmpty())
    tasks.forEach(TimerTaskMock::isCanceled)
  }
}
