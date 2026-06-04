package io.github.malczuuu.iemu.http

import io.github.malczuuu.iemu.core.DeviceStateService
import io.github.malczuuu.iemu.core.LwM2mLifecycle
import io.github.malczuuu.iemu.core.firmware.FirmwareService
import io.github.malczuuu.iemu.settings.Settings
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemException
import io.javalin.Javalin
import io.javalin.http.HttpStatus
import io.javalin.http.staticfiles.Location
import io.javalin.json.JavalinJackson3
import org.slf4j.LoggerFactory
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

class HttpServer(
    private val settings: Settings.Http,
    private val mapper: JsonMapper,
    private val webSocket: WebSocketHandler,
    private val deviceStateService: DeviceStateService,
    private val firmwareService: FirmwareService,
    private val lwM2mManager: LwM2mLifecycle,
    private val features: Settings.Features = Settings.Features(),
) {

  fun start() {
    val state = StateEndpoint(deviceStateService)
    val firmware = FirmwareEndpoint(firmwareService)
    val connection = LwM2mClientEndpoint(lwM2mManager)

    val app = Javalin.create { config ->
      config.http.generateEtags = true
      config.startup.showJavalinBanner = false
      config.staticFiles.add("/static", Location.CLASSPATH)
      config.jsonMapper(JavalinJackson3(jsonMapper = mapper, useVirtualThreads = true))

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

      config.routes.get("/api/lwm2m-client", connection::get)
      config.routes.post("/api/lwm2m-client", connection::connect)
      config.routes.delete("/api/lwm2m-client", connection::disconnect)
      config.routes.get("/api/state", state::get)
      config.routes.patch("/api/state", state::patch)
      config.routes.get("/api/features") { ctx ->
        ctx.status(HttpStatus.OK).json(FeaturesDto(features.firmwareUpdate.enabled))
      }
      if (features.firmwareUpdate.enabled) {
        config.routes.get("/api/firmware", firmware::get)
        config.routes.post("/api/firmware", firmware::stage)
        config.routes.post("/api/firmware/execute", firmware::execute)
      }

      config.routes.ws("/api/websocket") { ws ->
        ws.onConnect(webSocket::onConnect)
        ws.onMessage(webSocket::onMessage)
        ws.onClose(webSocket::onClose)
        ws.onError(webSocket::onError)
      }

      config.routes.exception(JacksonException::class.java) { _, ctx ->
        ctx.status(HttpStatus.BAD_REQUEST)
            .json(Problem.of(HttpStatus.BAD_REQUEST.code, "Failed to parse JSON object"))
            .contentType(Problem.CONTENT_TYPE)
      }

      config.routes.exception(ProblemException::class.java) { ex, ctx ->
        ctx.status(ex.problem.status).json(ex.problem).contentType(Problem.CONTENT_TYPE)
      }

      config.routes.exception(Exception::class.java) { ex, ctx ->
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .json(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.code, ex.message))
            .contentType(Problem.CONTENT_TYPE)
      }

      config.routes.error(HttpStatus.NOT_FOUND) { ctx ->
        ctx.status(HttpStatus.NOT_FOUND)
            .json(Problem.of(HttpStatus.NOT_FOUND.code))
            .contentType(Problem.CONTENT_TYPE)
      }

      config.routes.error(HttpStatus.INTERNAL_SERVER_ERROR) { ctx ->
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .json(Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.code))
            .contentType(Problem.CONTENT_TYPE)
      }
    }

    Runtime.getRuntime().addShutdownHook(Thread { app.stop() })

    app.start(settings.port)
  }

  companion object {
    private val log = LoggerFactory.getLogger(HttpServer::class.java)
  }
}
