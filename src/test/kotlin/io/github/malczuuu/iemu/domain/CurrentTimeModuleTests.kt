package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.mock.TimerTaskMock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Timer
import java.util.TimerTask
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CurrentTimeModuleTests {

  private val tasks: MutableList<TimerTask> = mutableListOf()
  private val timer: Timer = mock(Timer::class.java)

  @BeforeEach
  fun beforeEach() {
    doAnswer { tasks.add(it.getArgument(0, TimerTask::class.java)) }
        .`when`(timer)
        .schedule(any(TimerTask::class.java), anyLong(), anyLong())
  }

  @Test
  fun `constructor should schedule a single ticking task on the timer`() {
    CurrentTimeModule(timer)

    assertEquals(1, tasks.size)
  }

  @Test
  fun `timeZone and utcOffset should be populated from the host system by default`() {
    val module = CurrentTimeModule(timer)

    assertNotNull(module.timeZone)
    assertNotNull(module.utcOffset)
  }

  @Test
  fun `currentTime should always be truncated to whole seconds`() {
    val module = CurrentTimeModule(timer)

    val now = module.currentTime

    assertEquals(now, now.truncatedTo(ChronoUnit.SECONDS))
  }

  @Test
  fun `assigning currentTime should notify subscribers of the new time`() {
    val module = CurrentTimeModule(timer)
    var received: Instant? = null
    module.subscribeOnCurrentTimeChange { received = it }

    module.currentTime = Instant.parse("2020-01-01T00:00:00Z")

    assertNotNull(received)
  }

  @Test
  fun `scheduled timer tick should notify subscribers of the current time`() {
    val module = CurrentTimeModule(timer)
    var received: Instant? = null
    module.subscribeOnCurrentTimeChange { received = it }

    tasks.forEach(TimerTaskMock::run)

    assertNotNull(received)
  }

  @Test
  fun `assigning currentTime should shift subsequent reads to reflect the configured offset`() {
    val module = CurrentTimeModule(timer)
    val past = Instant.parse("2000-01-01T00:00:00Z")

    module.currentTime = past

    val read = module.currentTime
    assertTrue(read.isBefore(Instant.now()))
    assertTrue(Math.abs(read.epochSecond - past.epochSecond) < 5)
  }

  @Test
  fun `shutdown() should cancel and purge the underlying timer`() {
    val module = CurrentTimeModule(timer)

    module.shutdown()

    verify(timer).cancel()
    verify(timer).purge()
  }
}
