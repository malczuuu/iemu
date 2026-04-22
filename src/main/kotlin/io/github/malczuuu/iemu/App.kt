package io.github.malczuuu.iemu

import io.github.malczuuu.iemu.common.ConfigReader
import io.github.malczuuu.iemu.common.JacksonFactory
import io.github.malczuuu.iemu.common.ProfileSelector
import io.github.malczuuu.iemu.domain.FirmwareService
import io.github.malczuuu.iemu.domain.StateService
import io.github.malczuuu.iemu.infra.http.HttpServer
import io.github.malczuuu.iemu.infra.http.WebSocketEvent
import io.github.malczuuu.iemu.infra.http.WebSocketService
import io.github.malczuuu.iemu.infra.lwm2m.LwM2mClient

fun main(args: Array<String>) {
  val profile = ProfileSelector(args).getProfileName()
  val config = ConfigReader().readConfig(profile)

  val mapper = JacksonFactory.getJsonMapper()

  val webSocketService = WebSocketService()
  val stateService = StateService()
  val firmwareService = FirmwareService()

  val statePublish = Runnable {
    webSocketService.sendMessage(
        mapper.writeValueAsString(WebSocketEvent("state", stateService.getState()))
    )
  }

  val firmwarePublish = Runnable {
    webSocketService.sendMessage(
        mapper.writeValueAsString(WebSocketEvent("firmware", firmwareService.getFirmware()))
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

  if (config.lwM2m.isEnabled) {
    LwM2mClient(config.lwM2m, stateService, firmwareService).start()
  }
  HttpServer(config.http, webSocketService, stateService, firmwareService, mapper).start()
}
