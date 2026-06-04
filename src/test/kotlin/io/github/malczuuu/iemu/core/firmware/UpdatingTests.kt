package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdatingTests {

  private fun newUpdating() =
      Updating(
          firmware = Downloaded(
              packageUri = "http://example/fw.bin",
              result = FirmwareUpdateResult.NONE,
              packageVersion = "1.0.0",
          ),
          ticksPerStep = 1,
      )

  @Test
  fun `state should be UPDATING`() {
    assertEquals(FirmwareUpdateState.UPDATING, newUpdating().state)
  }

  @Test
  fun `progress should start at zero`() {
    assertEquals(0, newUpdating().progress)
  }

  @Test
  fun `hasNext() should be true because Updating must progress`() {
    assertTrue(newUpdating().hasNext())
  }

  @Test
  fun `execute() should advance progress while still below one hundred`() {
    val next = newUpdating().execute()

    assertTrue(next is Updating)
    assertTrue((next as Updating).progress > 0)
  }

  @Test
  fun `execute() should eventually transition to Idle with SUCCESSFUL result and unchanged version`() {
    var current: FirmwareUpdateExecution = newUpdating()
    repeat(20) { current = current.execute() }

    assertTrue(current is Idle)
    assertEquals(FirmwareUpdateResult.SUCCESSFUL, current.result)
    assertEquals("1.0.0", current.packageVersion)
    assertFalse(current.hasNext())
  }

  @Test
  fun `secondary constructor should copy fields from a previous FirmwareUpdateExecution`() {
    val downloaded = Downloaded(
        packageUri = "http://example",
        result = FirmwareUpdateResult.NONE,
        packageVersion = "0.9",
    )

    val updating = Updating(downloaded)

    assertEquals(FirmwareUpdateState.UPDATING, updating.state)
    assertEquals("http://example", updating.packageUri)
    assertEquals("0.9", updating.packageVersion)
    assertEquals(FirmwareUpdateResult.NONE, updating.result)
  }
}
