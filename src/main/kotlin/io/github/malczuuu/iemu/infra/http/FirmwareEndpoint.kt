package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.domain.FirmwareService
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import tools.jackson.databind.json.JsonMapper

class FirmwareEndpoint(
    private val firmwareService: FirmwareService,
    private val mapper: JsonMapper,
) {

  fun get(ctx: Context) {
    ctx.status(HttpStatus.OK)
        .contentType("application/json")
        .result(mapper.writeValueAsString(firmwareService.getFirmware().toDto()))
  }
}
