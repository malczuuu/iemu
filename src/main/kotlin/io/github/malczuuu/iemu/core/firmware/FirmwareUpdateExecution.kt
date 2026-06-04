package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState

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
