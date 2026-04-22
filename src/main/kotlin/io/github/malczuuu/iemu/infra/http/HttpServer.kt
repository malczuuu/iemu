package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.common.Config
import io.github.malczuuu.iemu.domain.FirmwareService
import io.github.malczuuu.iemu.domain.StateDto
import io.github.malczuuu.iemu.domain.StateService
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemException
import io.javalin.Javalin
import io.javalin.http.HttpStatus
import io.javalin.http.staticfiles.Location
import org.slf4j.LoggerFactory
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

class HttpServer(
    private val config: Config.Http,
    private val webSocketService: WebSocketService,
    private val stateService: StateService,
    private val firmwareService: FirmwareService,
    private val mapper: JsonMapper,
) {

  fun start() {
    val app = Javalin.create { config ->
      config.http.generateEtags = true
      config.startup.showJavalinBanner = false
      config.staticFiles.add("/static", Location.CLASSPATH)

      config.requestLogger.http { ctx, executionTimeMs ->
        log.debug(
            "Handled HTTP request method={}, path={}, query={}, headers={} in {}ms",
            ctx.method(),
            ctx.method(),
            ctx.queryString(),
            ctx.headerMap(),
            executionTimeMs,
        )
      }

      config.routes.get("/api/state") { ctx ->
        val state = stateService.getState()
        ctx.status(HttpStatus.OK)
            .contentType("application/json")
            .result(mapper.writeValueAsString(state))
      }

      config.routes.patch("/api/state") { ctx ->
        val state = mapper.readValue(ctx.bodyAsBytes(), StateDto::class.java)
        stateService.changeState(state)
        ctx.status(HttpStatus.NO_CONTENT)
      }

      config.routes.get("/api/firmware") { ctx ->
        val firmware = firmwareService.getFirmware()
        ctx.status(HttpStatus.OK)
            .contentType("application/json")
            .result(mapper.writeValueAsString(firmware))
      }

      config.routes.ws("/api/websocket") { ws ->
        ws.onConnect(webSocketService::onConnect)
        ws.onMessage(webSocketService::onMessage)
        ws.onClose(webSocketService::onClose)
        ws.onError(webSocketService::onError)
      }

      config.routes.exception(JacksonException::class.java) { _, ctx ->
        ctx.status(HttpStatus.BAD_REQUEST)
            .contentType(Problem.CONTENT_TYPE)
            .result(
                mapper.writeValueAsString(
                    Problem.of(HttpStatus.BAD_REQUEST.code, "Failed to parse JSON object")
                )
            )
      }

      config.routes.exception(ProblemException::class.java) { ex, ctx ->
        ctx.status(ex.problem.status)
            .contentType(Problem.CONTENT_TYPE)
            .result(mapper.writeValueAsString(ex.problem))
      }

      config.routes.exception(Exception::class.java) { ex, ctx ->
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(Problem.CONTENT_TYPE)
            .result(
                mapper.writeValueAsString(
                    Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.code, ex.message)
                )
            )
      }

      config.routes.error(HttpStatus.NOT_FOUND) { ctx ->
        ctx.status(HttpStatus.NOT_FOUND)
            .contentType(Problem.CONTENT_TYPE)
            .result(mapper.writeValueAsString(Problem.of(HttpStatus.NOT_FOUND.code)))
      }

      config.routes.error(HttpStatus.INTERNAL_SERVER_ERROR) { ctx ->
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(Problem.CONTENT_TYPE)
            .result(mapper.writeValueAsString(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.code)))
      }
    }

    Runtime.getRuntime().addShutdownHook(Thread { app.stop() })

    app.start(config.port)
  }

  companion object {
    private val log = LoggerFactory.getLogger(HttpServer::class.java)
  }
}
