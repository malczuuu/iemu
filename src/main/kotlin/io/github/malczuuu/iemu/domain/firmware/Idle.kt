package io.github.malczuuu.iemu.domain.firmware

import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import org.slf4j.LoggerFactory

class Idle(
    override val file: ByteArray,
    override val packageUri: String?,
    override val result: FirmwareUpdateResult,
    override val packageVersion: String?,
) : FirmwareUpdateExecution {

  override val state: FirmwareUpdateState = FirmwareUpdateState.IDLE

  override val progress: Int = 0

  override fun execute(): FirmwareUpdateExecution {
    log.error("Attempting to change state from idle state, returning idle as well")
    return this
  }

  override fun hasNext(): Boolean = false

  companion object {
    private val log = LoggerFactory.getLogger(Idle::class.java)
  }
}
