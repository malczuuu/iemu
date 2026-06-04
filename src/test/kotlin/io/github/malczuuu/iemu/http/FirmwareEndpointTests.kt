package io.github.malczuuu.iemu.http

import io.github.malczuuu.iemu.core.FirmwareState
import io.github.malczuuu.iemu.core.firmware.FirmwareService
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirmwareEndpointTests {

  private val firmwareService = mockk<FirmwareService>()
  private val ctx = mockk<Context>(relaxed = true)
  private val endpoint = FirmwareEndpoint(firmwareService)

  @BeforeEach
  fun setUp() {
    every { ctx.status(any<HttpStatus>()) } returns ctx
    every { ctx.json(any()) } returns ctx
  }

  private fun sampleFirmware() =
      FirmwareState(
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
  fun `get() should set status 200 and return firmware as DTO`() {
    every { firmwareService.getFirmware() } returns sampleFirmware()

    endpoint.get(ctx)

    verify { ctx.status(HttpStatus.OK) }
    verify { ctx.json(any<FirmwareDto>()) }
  }

  @Test
  fun `get() should include packageUri and pkgVersion in the DTO`() {
    every { firmwareService.getFirmware() } returns sampleFirmware()

    endpoint.get(ctx)

    verify {
      ctx.json(
          match<FirmwareDto> {
            it.packageUri == "http://fw-server/package" && it.pkgVersion == "1.0.0"
          },
      )
    }
  }
}
