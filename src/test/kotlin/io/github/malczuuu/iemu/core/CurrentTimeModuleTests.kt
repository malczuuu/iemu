package io.github.malczuuu.iemu.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CurrentTimeModuleTests {

  private val runnables: MutableList<Runnable> = mutableListOf()
  private val scheduler: ScheduledExecutorService = mockk(relaxed = true)
  private val mockFuture: ScheduledFuture<*> = mockk(relaxed = true)
  private val clock: Clock = Clock.systemUTC()

  @BeforeEach
  fun beforeEach() {
    val slot = slot<Runnable>()
    every {
      scheduler.scheduleAtFixedRate(capture(slot), any<Long>(), any<Long>(), any<TimeUnit>())
    } answers
        {
          runnables.add(slot.captured)
          mockFuture
        }
  }

  @Test
  fun `constructor should schedule a single ticking task on the scheduler`() {
    CurrentTimeModule(scheduler, clock)

    assertEquals(1, runnables.size)
  }

  @Test
  fun `timeZone and utcOffset should be populated from the host system by default`() {
    val module = CurrentTimeModule(scheduler, clock)

    assertNotNull(module.timeZone)
    assertNotNull(module.utcOffset)
  }

  @Test
  fun `currentTime should always be truncated to whole seconds`() {
    val module = CurrentTimeModule(scheduler, clock)

    val now = module.currentTime

    assertEquals(now, now.truncatedTo(ChronoUnit.SECONDS))
  }

  @Test
  fun `assigning currentTime should notify subscribers of the new time`() {
    val module = CurrentTimeModule(scheduler, clock)
    var received: Instant? = null
    module.onCurrentTimeChange { received = it }

    module.currentTime = Instant.parse("2020-01-01T00:00:00Z")

    assertNotNull(received)
  }

  @Test
  fun `scheduled tick should notify subscribers of the current time`() {
    val module = CurrentTimeModule(scheduler, clock)
    var received: Instant? = null
    module.onCurrentTimeChange { received = it }

    runnables.forEach { it.run() }

    assertNotNull(received)
  }

  @Test
  fun `assigning currentTime should shift subsequent reads to reflect the configured offset`() {
    val module = CurrentTimeModule(scheduler, clock)
    val past = Instant.parse("2000-01-01T00:00:00Z")

    module.currentTime = past

    val read = module.currentTime
    assertTrue(read.isBefore(Instant.now()))
    assertTrue(abs(read.epochSecond - past.epochSecond) < 5)
  }

  @Test
  fun `shutdown() should cancel the scheduled future`() {
    val module = CurrentTimeModule(scheduler, clock)

    module.shutdown()

    verify { mockFuture.cancel(false) }
  }
}
