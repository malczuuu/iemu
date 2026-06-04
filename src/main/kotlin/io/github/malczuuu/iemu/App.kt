package io.github.malczuuu.iemu

import io.github.malczuuu.iemu.common.JacksonFactory
import io.github.malczuuu.iemu.domain.DefaultFirmwareService
import io.github.malczuuu.iemu.domain.DefaultStateService
import io.github.malczuuu.iemu.infra.http.HttpServer
import io.github.malczuuu.iemu.infra.http.WebSocketEvent
import io.github.malczuuu.iemu.infra.http.WebSocketService
import io.github.malczuuu.iemu.infra.http.toDto
import io.github.malczuuu.iemu.infra.lwm2m.DefaultConnectionService
import io.github.malczuuu.iemu.settings.CommandLine
import io.github.malczuuu.iemu.settings.SettingsException
import io.github.malczuuu.iemu.settings.SettingsReader
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("io.github.malczuuu.iemu.App")

fun main(args: Array<String>) {
  val commandLine = CommandLine(args)
  try {
    commandLine.init()
  } catch (e: Exception) {
    log.error(e.message)
    exitProcess(-1)
  }

  val settings =
      try {
        SettingsReader().readSettings(commandLine.profile)
      } catch (e: SettingsException) {
        log.error(e.message, e.cause)
        exitProcess(1)
      }

  val mapper = JacksonFactory.jsonMapper

  val webSocketService = WebSocketService()
  val stateService = DefaultStateService()
  val firmwareService = DefaultFirmwareService().apply { start() }

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

  val connectionService = DefaultConnectionService(settings.lwM2m, stateService, firmwareService)
  HttpServer(
          settings.http,
          settings.lwM2m,
          webSocketService,
          stateService,
          firmwareService,
          connectionService,
          mapper,
      )
      .start()
}
