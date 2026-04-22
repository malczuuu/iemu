package io.github.malczuuu.iemu.domain.firmware

import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class IdleTests {

  private val idle =
      Idle(
          file = "1.0.0".toByteArray(),
          packageUri = "about:blank",
          result = FirmwareUpdateResult.NONE,
          packageVersion = "1.0.0",
      )

  @Test
  fun `state should be IDLE`() {
    assertEquals(FirmwareUpdateState.IDLE, idle.state)
  }

  @Test
  fun `progress should always be reported as zero`() {
    assertEquals(0, idle.progress)
  }

  @Test
  fun `hasNext() should be false because Idle is a terminal state`() {
    assertFalse(idle.hasNext())
  }

  @Test
  fun `execute() should return the same instance since Idle cannot advance`() {
    assertSame(idle, idle.execute())
  }
}
