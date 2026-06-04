package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.domain.ConnectionService
import io.github.malczuuu.iemu.domain.FirmwareService
import io.github.malczuuu.iemu.domain.StateService
import io.github.malczuuu.iemu.settings.Settings

class DefaultConnectionService(
    private val config: Settings.LwM2m,
    private val stateService: StateService,
    private val firmwareService: FirmwareService,
) : ConnectionService {

  @Volatile private var client: LwM2mClient? = null

  override fun start() {
    synchronized(this) {
      if (client != null) return
      val c = LwM2mClient(config, stateService, firmwareService)
      c.start()
      client = c
    }
  }

  override fun stop() {
    synchronized(this) {
      client?.stop()
      client = null
    }
  }

  override fun isStarted(): Boolean = client != null
}
