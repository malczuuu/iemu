package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.firmware.FirmwareUpdateState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class DownloadedTests {

  private val downloaded =
      Downloaded(
          file = "payload".toByteArray(),
          packageUri = "http://example/fw.bin",
          result = FirmwareUpdateResult.NONE,
          packageVersion = "2.0.0",
      )

  @Test
  fun `state should be DOWNLOADED`() {
    assertEquals(FirmwareUpdateState.DOWNLOADED, downloaded.state)
  }

  @Test
  fun `progress should always be reported as zero`() {
    assertEquals(0, downloaded.progress)
  }

  @Test
  fun `hasNext() should be false because Downloaded is a terminal state`() {
    assertFalse(downloaded.hasNext())
  }

  @Test
  fun `execute() should return the same instance since Downloaded cannot advance`() {
    assertSame(downloaded, downloaded.execute())
  }
}
