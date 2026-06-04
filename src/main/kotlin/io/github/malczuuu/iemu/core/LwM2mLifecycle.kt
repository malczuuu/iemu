package io.github.malczuuu.iemu.core

interface LwM2mLifecycle {

  val connectionState: ConnectionState

  fun start()

  fun stop()

  fun isStarted(): Boolean
}
