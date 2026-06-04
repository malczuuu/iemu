package io.github.malczuuu.iemu.http

import io.github.malczuuu.iemu.core.DeviceStateService
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.http.bodyAsClass

class StateEndpoint(private val deviceStateService: DeviceStateService) {

  fun get(ctx: Context) {
    ctx.status(HttpStatus.OK).json(deviceStateService.deviceState.toDto())
  }

  fun patch(ctx: Context) {
    val dto = ctx.bodyAsClass<DeviceStateDto>()
    deviceStateService.changeDeviceState(dto.toUpdate())
    ctx.status(HttpStatus.NO_CONTENT)
  }
}
