package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.domain.ConnectionService
import io.github.malczuuu.iemu.settings.Settings
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemException
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import tools.jackson.databind.json.JsonMapper

class ConnectionEndpoint(
    private val config: Settings.LwM2m,
    private val connectionService: ConnectionService,
    private val mapper: JsonMapper,
) {

  fun get(ctx: Context) {
    ctx.status(HttpStatus.OK)
        .contentType("application/json")
        .result(mapper.writeValueAsString(connectionService.toDto(config)))
  }

  fun connect(ctx: Context) {
    if (connectionService.isStarted()) {
      throw ProblemException(Problem.of(HttpStatus.CONFLICT.code, "Already connected"))
    }
    connectionService.start()
    ctx.status(HttpStatus.NO_CONTENT)
  }

  fun disconnect(ctx: Context) {
    if (!connectionService.isStarted()) {
      throw ProblemException(Problem.of(HttpStatus.CONFLICT.code, "Not connected"))
    }
    connectionService.stop()
    ctx.status(HttpStatus.NO_CONTENT)
  }
}
