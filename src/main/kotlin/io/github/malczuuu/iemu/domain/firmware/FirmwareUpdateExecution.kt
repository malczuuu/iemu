package io.github.malczuuu.iemu.domain.firmware

import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState

interface FirmwareUpdateExecution {

  val file: ByteArray

  val packageUri: String?

  val packageVersion: String?

  val state: FirmwareUpdateState

  val result: FirmwareUpdateResult

  val progress: Int

  fun execute(): FirmwareUpdateExecution

  fun hasNext(): Boolean
}
