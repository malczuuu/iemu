package io.github.malczuuu.iemu.http

import io.github.malczuuu.iemu.core.firmware.FirmwareService
import io.javalin.http.Context
import io.javalin.http.HttpStatus

class FirmwareEndpoint(private val firmwareService: FirmwareService) {

  fun get(ctx: Context) {
    ctx.status(HttpStatus.OK).json(firmwareService.getFirmware().toDto())
  }
}
