package io.github.malczuuu.iemu.http

import io.github.malczuuu.iemu.core.firmware.FirmwareService
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemException
import io.javalin.http.Context
import io.javalin.http.HttpStatus

class FirmwareEndpoint(private val firmwareService: FirmwareService) {

  fun get(ctx: Context) {
    ctx.status(HttpStatus.OK).json(firmwareService.getFirmware().toDto())
  }

  fun stage(ctx: Context) {
    if (!firmwareService.stageFile(ctx.bodyAsBytes())) {
      throw ProblemException(
          Problem.of(422, "Firmware file must contain exactly one hex color in #RRGGBB format"),
      )
    }
    ctx.status(HttpStatus.ACCEPTED)
  }

  fun execute(ctx: Context) {
    firmwareService.executeFirmwareUpdate()
    ctx.status(HttpStatus.ACCEPTED)
  }
}
