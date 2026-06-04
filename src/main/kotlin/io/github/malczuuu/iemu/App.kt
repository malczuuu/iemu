package io.github.malczuuu.iemu

import io.github.malczuuu.iemu.common.ConfigLoadException
import io.github.malczuuu.iemu.common.ConfigReader
import io.github.malczuuu.iemu.common.InvalidProfileException
import io.github.malczuuu.iemu.common.JacksonFactory
import io.github.malczuuu.iemu.common.ProfileSelector
import io.github.malczuuu.iemu.domain.DefaultFirmwareService
import io.github.malczuuu.iemu.domain.DefaultStateService
import io.github.malczuuu.iemu.infra.http.HttpServer
import io.github.malczuuu.iemu.infra.http.WebSocketEvent
import io.github.malczuuu.iemu.infra.http.WebSocketService
import io.github.malczuuu.iemu.infra.http.toDto
import io.github.malczuuu.iemu.infra.lwm2m.DefaultConnectionService
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("io.github.malczuuu.iemu.App")

fun main(args: Array<String>) {
  val profile =
      try {
        ProfileSelector(args).getProfileName()
      } catch (e: InvalidProfileException) {
        log.error(e.message)
        exitProcess(-1)
      }

  val config =
      try {
        ConfigReader().readConfig(profile)
      } catch (e: ConfigLoadException) {
        log.error(e.message, e.cause)
        exitProcess(1)
      }

  val mapper = JacksonFactory.getJsonMapper()

  val webSocketService = WebSocketService()
  val stateService = DefaultStateService()
  val firmwareService = DefaultFirmwareService().also { it.start() }

  val statePublish = Runnable {
    webSocketService.sendMessage(
        mapper.writeValueAsString(WebSocketEvent("state", stateService.getState().toDto())),
    )
  }

  val firmwarePublish = Runnable {
    webSocketService.sendMessage(
        mapper.writeValueAsString(
            WebSocketEvent("firmware", firmwareService.getFirmware().toDto())
        ),
    )
  }

  Runtime.getRuntime().addShutdownHook(Thread { stateService.shutdown() })

  stateService.subscribeOnCurrentTimeChange { statePublish.run() }
  stateService.subscribeOnStateChange { statePublish.run() }
  stateService.subscribeOnTimeCounterChange { statePublish.run() }
  stateService.subscribeOnDimmerChange { statePublish.run() }

  firmwareService.subscribeOnFileChange { firmwarePublish.run() }
  firmwareService.subscribeOnPackageUriChange { firmwarePublish.run() }
  firmwareService.subscribeOnStateChange { firmwarePublish.run() }
  firmwareService.subscribeOnResultChange { firmwarePublish.run() }
  firmwareService.subscribeOnPackageVersionChange { firmwarePublish.run() }
  firmwareService.subscribeOnProgressChange { firmwarePublish.run() }

  val connectionService = DefaultConnectionService(config.lwM2m, stateService, firmwareService)
  HttpServer(
          config.http,
          config.lwM2m,
          webSocketService,
          stateService,
          firmwareService,
          connectionService,
          mapper,
      )
      .start()
}
