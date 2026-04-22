package io.github.malczuuu.iemu.domain.firmware

import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.ContentResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

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
  fun `execute() should transition to Downloaded when the HTTP client returns non-empty content`() {
    val client = mock(HttpClient::class.java)
    val response = mock(ContentResponse::class.java)
    `when`(client.GET(any(java.net.URI::class.java))).thenReturn(response)
    `when`(response.content).thenReturn("firmware-bytes".toByteArray())

    val d =
        Downloading(
            file = "old".toByteArray(),
            packageUri = "http://example/fw",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0",
            client = client,
        )

    val next = d.execute()

    assertTrue(next is Downloaded)
    assertEquals(FirmwareUpdateResult.NONE, next.result)
    assertEquals("firmware-bytes", String(next.file))
  }

  @Test
  fun `execute() should fall back to Idle with PACKAGE_INTEGRITY_CHECK_FAILURE when response content is empty`() {
    val client = mock(HttpClient::class.java)
    val response = mock(ContentResponse::class.java)
    `when`(client.GET(any(java.net.URI::class.java))).thenReturn(response)
    `when`(response.content).thenReturn(ByteArray(0))

    val d =
        Downloading(
            file = "old".toByteArray(),
            packageUri = "http://example/fw",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0",
            client = client,
        )

    val next = d.execute()

    assertTrue(next is Idle)
    assertEquals(FirmwareUpdateResult.PACKAGE_INTEGRITY_CHECK_FAILURE, next.result)
  }
}
