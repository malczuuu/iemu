package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.domain.ConnectionService
import io.github.malczuuu.iemu.domain.FirmwareService
import io.github.malczuuu.iemu.domain.StateService
import io.github.malczuuu.iemu.settings.Settings
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemException
import io.javalin.Javalin
import io.javalin.http.HttpStatus
import io.javalin.http.staticfiles.Location
import org.slf4j.LoggerFactory
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

class HttpServer(
    private val httpConfig: Settings.Http,
    private val lwm2mConfig: Settings.LwM2m,
    private val webSocketService: WebSocketService,
    private val stateService: StateService,
    private val firmwareService: FirmwareService,
    private val connectionService: ConnectionService,
    private val mapper: JsonMapper,
) {

  fun start() {
    val state = StateEndpoint(stateService, mapper)
    val firmware = FirmwareEndpoint(firmwareService, mapper)
    val connection = ConnectionEndpoint(lwm2mConfig, connectionService, mapper)

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

      config.routes.get("/api/connection", connection::get)
      config.routes.post("/api/connection", connection::connect)
      config.routes.delete("/api/connection", connection::disconnect)
      config.routes.get("/api/state", state::get)
      config.routes.patch("/api/state", state::patch)
      config.routes.get("/api/firmware", firmware::get)

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
                    Problem.of(HttpStatus.BAD_REQUEST.code, "Failed to parse JSON object"),
                ),
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
                    Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.code, ex.message),
                ),
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

    app.start(httpConfig.port)
  }

  companion object {
    private val log = LoggerFactory.getLogger(HttpServer::class.java)
  }
}
