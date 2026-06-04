package io.github.malczuuu.iemu.domain

interface ConnectionService {

  fun start()

  fun stop()

  fun isStarted(): Boolean
}
