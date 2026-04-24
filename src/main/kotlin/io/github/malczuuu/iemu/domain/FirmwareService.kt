package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState

interface FirmwareService {

  fun start()

  fun getFirmware(): Firmware

  fun changeFirmware(update: FirmwareUpdate)

  fun executeFirmwareUpdate()

  fun subscribeOnFileChange(consumer: (ByteArray) -> Unit): Boolean

  fun subscribeOnPackageUriChange(consumer: (String?) -> Unit): Boolean

  fun subscribeOnStateChange(consumer: (FirmwareUpdateState) -> Unit): Boolean

  fun subscribeOnResultChange(consumer: (FirmwareUpdateResult) -> Unit): Boolean

  fun subscribeOnPackageVersionChange(consumer: (String?) -> Unit): Boolean

  fun subscribeOnProgressChange(consumer: (Int) -> Unit): Boolean
}
