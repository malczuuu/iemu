package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.common.JacksonFactory
import io.github.malczuuu.iemu.domain.Firmware
import io.github.malczuuu.iemu.domain.FirmwareService
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirmwareEndpointTests {

  private val firmwareService = mockk<FirmwareService>()
  private val mapper = JacksonFactory.getJsonMapper()
  private val ctx = mockk<Context>(relaxed = true)
  private val endpoint = FirmwareEndpoint(firmwareService, mapper)

  @BeforeEach
  fun setUp() {
    every { ctx.status(any<HttpStatus>()) } returns ctx
    every { ctx.contentType(any<String>()) } returns ctx
    every { ctx.result(any<String>()) } returns ctx
  }

  private fun sampleFirmware() =
      Firmware(
          file = "bytes".toByteArray(),
          fileChecksum = "sha256:abc",
          packageUri = "http://fw-server/package",
          state = FirmwareUpdateState.IDLE,
          result = FirmwareUpdateResult.NONE,
          packageVersion = "1.0.0",
          deliveryMethod = FirmwareUpdateDeliveryMethod.BOTH,
          progress = 0,
      )

  @Test
  fun `get() should set status 200 and serialize the current firmware as JSON`() {
    every { firmwareService.getFirmware() } returns sampleFirmware()

    endpoint.get(ctx)

    verify { ctx.status(HttpStatus.OK) }
    verify { ctx.contentType("application/json") }
    verify { ctx.result(any<String>()) }
  }

  @Test
  fun `get() should include packageUri and pkgVersion in the JSON response`() {
    every { firmwareService.getFirmware() } returns sampleFirmware()

    endpoint.get(ctx)

    verify {
      ctx.result(match<String> { it.contains("http://fw-server/package") && it.contains("1.0.0") })
    }
  }
}
