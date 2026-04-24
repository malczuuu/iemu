package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.domain.StateService
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import tools.jackson.databind.json.JsonMapper

class StateEndpoint(
    private val stateService: StateService,
    private val mapper: JsonMapper,
) {

  fun get(ctx: Context) {
    ctx.status(HttpStatus.OK)
        .contentType("application/json")
        .result(mapper.writeValueAsString(stateService.getState().toDto()))
  }

  fun patch(ctx: Context) {
    val dto = mapper.readValue(ctx.bodyAsBytes(), StateDto::class.java)
    stateService.changeState(dto.toUpdate())
    ctx.status(HttpStatus.NO_CONTENT)
  }
}
