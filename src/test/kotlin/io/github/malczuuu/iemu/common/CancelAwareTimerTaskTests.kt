package io.github.malczuuu.iemu.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CancelAwareTimerTaskTests {

  @Test
  fun `run() should delegate to the wrapped runnable on each invocation`() {
    var count = 0
    val task = CancelAwareTimerTask { count++ }

    task.run()
    task.run()

    assertEquals(2, count)
  }

  @Test
  fun `isCancelled should be false before cancel is invoked`() {
    val task = CancelAwareTimerTask {}

    assertFalse(task.isCancelled)
  }

  @Test
  fun `cancel() should flip isCancelled to true`() {
    val task = CancelAwareTimerTask {}

    task.cancel()

    assertTrue(task.isCancelled)
  }
}
