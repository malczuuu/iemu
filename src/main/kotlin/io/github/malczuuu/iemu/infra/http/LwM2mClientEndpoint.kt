package io.github.malczuuu.iemu.infra.http

import io.github.malczuuu.iemu.core.LwM2mLifecycle
import io.javalin.http.Context
import io.javalin.http.HttpStatus

class LwM2mClientEndpoint(private val lwM2mManager: LwM2mLifecycle) {

  fun get(ctx: Context) {
    ctx.status(HttpStatus.OK).json(lwM2mManager.connectionState.toDto())
  }

  fun connect(ctx: Context) {
    ctx.status(HttpStatus.NO_CONTENT)
    if (!lwM2mManager.isStarted()) {
      lwM2mManager.start()
    }
  }

  fun disconnect(ctx: Context) {
    ctx.status(HttpStatus.NO_CONTENT)
    if (lwM2mManager.isStarted()) {
      lwM2mManager.stop()
    }
  }
}
