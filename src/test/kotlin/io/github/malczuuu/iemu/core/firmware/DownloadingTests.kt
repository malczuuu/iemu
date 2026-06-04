package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import java.util.concurrent.ExecutionException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DownloadingTests {

  private fun downloading(
      packageUri: String = "http://example/fw",
      downloader: FirmwareDownloader = FirmwareDownloader { "#FF0000".toByteArray() },
  ) =
      Downloading(
          file = "old".toByteArray(),
          packageUri = packageUri,
          result = FirmwareUpdateResult.NONE,
          packageVersion = "1.0",
          ticksPerStep = 1,
          downloader = downloader,
      )

  @Test
  fun `state should be DOWNLOADING and the execution should advance`() {
    val d = downloading()

    assertEquals(FirmwareUpdateState.DOWNLOADING, d.state)
    assertEquals(0, d.progress)
    assertTrue(d.hasNext())
  }

  @Test
  fun `execute() should fall back to Idle with UNSUPPORTED_PROTOCOL when scheme is not http or https`() {
    val d = downloading(packageUri = "ftp://x")

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.UNSUPPORTED_PROTOCOL, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with UNSUPPORTED_PROTOCOL when scheme is missing`() {
    val d = downloading(packageUri = "just-a-path")

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.UNSUPPORTED_PROTOCOL, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with INVALID_URI when packageUri is syntactically invalid`() {
    val d = downloading(packageUri = "http://bad uri")

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.INVALID_URI, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with PACKAGE_INTEGRITY_CHECK_FAILURE when downloader returns empty content`() {
    val d = downloading(downloader = { ByteArray(0) })

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.PACKAGE_INTEGRITY_CHECK_FAILURE, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with FIRMWARE_UPDATE_FAILED when downloaded content is not a hex color`() {
    val d = downloading(downloader = { "not-a-color".toByteArray() })

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with FIRMWARE_UPDATE_FAILED when the downloader throws a generic exception`() {
    val d = downloading(downloader = { throw IllegalStateException("unexpected") })

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with NONE when the downloader throws InterruptedException`() {
    val d = downloading(downloader = { throw InterruptedException("stopped") })

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.NONE, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with NONE when the downloader throws ExecutionException`() {
    val d = downloading(downloader = { throw ExecutionException("exec", RuntimeException()) })

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.NONE, next.result)
  }

  @Test
  fun `execute() should show increasing progress during animation phase after fetch`() {
    val d = downloading()

    val afterFetch = d.execute()

    assertTrue(afterFetch is Downloading)
    assertEquals(FirmwareUpdateState.DOWNLOADING, afterFetch.state)
    val afterProgress = afterFetch.execute()
    assertTrue(afterProgress is Downloading || afterProgress is Downloaded)
    if (afterProgress is Downloading) {
      assertTrue(afterProgress.progress > 0)
    }
  }

  @Test
  fun `execute() should eventually transition to Downloaded with correct file content`() {
    var current: FirmwareUpdateExecution = downloading()
    repeat(30) { current = current.execute() }

    assertTrue(current is Downloaded)
    assertEquals(FirmwareUpdateResult.NONE, current.result)
    assertEquals("#FF0000", String(current.file))
  }
}
