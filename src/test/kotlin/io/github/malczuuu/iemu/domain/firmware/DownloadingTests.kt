package io.github.malczuuu.iemu.domain.firmware

import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import java.util.concurrent.ExecutionException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DownloadingTests {

  @Test
  fun `state should be DOWNLOADING and the execution should advance`() {
    val d = Downloading("f".toByteArray(), "http://x", FirmwareUpdateResult.NONE, "1.0")

    assertEquals(FirmwareUpdateState.DOWNLOADING, d.state)
    assertEquals(0, d.progress)
    assertTrue(d.hasNext())
  }

  @Test
  fun `execute() should fall back to Idle with UNSUPPORTED_PROTOCOL when scheme is not http or https`() {
    val d = Downloading("f".toByteArray(), "ftp://x", FirmwareUpdateResult.NONE, "1.0")

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.UNSUPPORTED_PROTOCOL, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with UNSUPPORTED_PROTOCOL when scheme is missing`() {
    val d = Downloading("f".toByteArray(), "just-a-path", FirmwareUpdateResult.NONE, "1.0")

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.UNSUPPORTED_PROTOCOL, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with INVALID_URI when packageUri is syntactically invalid`() {
    val d = Downloading("f".toByteArray(), "http://bad uri", FirmwareUpdateResult.NONE, "1.0")

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.INVALID_URI, next.result)
  }

  @Test
  fun `execute() should transition to Downloaded when the downloader returns non-empty content`() {
    val d =
        Downloading(
            file = "old".toByteArray(),
            packageUri = "http://example/fw",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0",
            downloader = { "firmware-bytes".toByteArray() },
        )

    val next = d.execute()

    assertTrue(next is Downloaded)
    assertEquals(FirmwareUpdateResult.NONE, next.result)
    assertEquals("firmware-bytes", String(next.file))
  }

  @Test
  fun `execute() should fall back to Idle with PACKAGE_INTEGRITY_CHECK_FAILURE when downloader returns empty content`() {
    val d =
        Downloading(
            file = "old".toByteArray(),
            packageUri = "http://example/fw",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0",
            downloader = { ByteArray(0) },
        )

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.PACKAGE_INTEGRITY_CHECK_FAILURE, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with FIRMWARE_UPDATE_FAILED when the downloader throws a generic exception`() {
    val d =
        Downloading(
            file = "old".toByteArray(),
            packageUri = "http://example/fw",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0",
            downloader = { throw IllegalStateException("unexpected") },
        )

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with NONE when the downloader throws InterruptedException`() {
    val d =
        Downloading(
            file = "old".toByteArray(),
            packageUri = "http://example/fw",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0",
            downloader = { throw InterruptedException("stopped") },
        )

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.NONE, next.result)
  }

  @Test
  fun `execute() should fall back to Idle with NONE when the downloader throws ExecutionException`() {
    val d =
        Downloading(
            file = "old".toByteArray(),
            packageUri = "http://example/fw",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0",
            downloader = { throw ExecutionException("exec", RuntimeException()) },
        )

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.NONE, next.result)
  }
}
