package io.github.malczuuu.iemu

import io.github.malczuuu.iemu.common.JacksonFactory
import io.github.malczuuu.iemu.core.DeviceStateService
import io.github.malczuuu.iemu.core.firmware.FirmwareService
import io.github.malczuuu.iemu.http.HttpServer
import io.github.malczuuu.iemu.http.WebSocketEvent
import io.github.malczuuu.iemu.http.WebSocketHandler
import io.github.malczuuu.iemu.http.toDto
import io.github.malczuuu.iemu.lwm2m.LwM2mManager
import io.github.malczuuu.iemu.settings.CommandLine
import io.github.malczuuu.iemu.settings.SettingsException
import io.github.malczuuu.iemu.settings.SettingsReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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

  val webSocketHandler = WebSocketHandler()

  val scheduler = Executors.newScheduledThreadPool(10)
  val deviceStateService = DeviceStateService(scheduler)
  val firmwareService = FirmwareService(scheduler).apply { start() }

  val statePublish = Runnable {
    webSocketHandler.sendMessage(
        mapper.writeValueAsString(WebSocketEvent("state", deviceStateService.deviceState.toDto())),
    )
  }

  val firmwarePublish = Runnable {
    webSocketHandler.sendMessage(
        mapper.writeValueAsString(
            WebSocketEvent("firmware", firmwareService.getFirmware().toDto())
        ),
    )
  }

  Runtime.getRuntime()
      .addShutdownHook(
          Thread {
            deviceStateService.shutdown()
            firmwareService.shutdown()
            scheduler.shutdown()
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
              val tasks = scheduler.shutdownNow()
              log.warn(
                  "Scheduler did not terminate in the specified time, a number of tasks did not terminate: ${tasks.size}"
              )
            }
          }
      )

  deviceStateService.onCurrentTimeChange { statePublish.run() }
  deviceStateService.onStateChange { statePublish.run() }
  deviceStateService.onTimeCounterChange { statePublish.run() }
  deviceStateService.onDimmerChange { statePublish.run() }

  firmwareService.onFileChange { firmwarePublish.run() }
  firmwareService.onPackageUriChange { firmwarePublish.run() }
  firmwareService.onStateChange { firmwarePublish.run() }
  firmwareService.onResultChange { firmwarePublish.run() }
  firmwareService.onPackageVersionChange { firmwarePublish.run() }
  firmwareService.onProgressChange { firmwarePublish.run() }

  val lwM2mManager = LwM2mManager(settings.lwM2m, deviceStateService, firmwareService)

  val httpServer =
      HttpServer(
          settings.http,
          mapper,
          webSocketHandler,
          deviceStateService,
          firmwareService,
          lwM2mManager,
      )
  httpServer.start()
}
