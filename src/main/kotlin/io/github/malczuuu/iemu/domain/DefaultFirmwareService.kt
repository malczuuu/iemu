package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.domain.firmware.Downloaded
import io.github.malczuuu.iemu.domain.firmware.Downloading
import io.github.malczuuu.iemu.domain.firmware.FirmwareUpdateExecution
import io.github.malczuuu.iemu.domain.firmware.Idle
import io.github.malczuuu.iemu.domain.firmware.Updating
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.eclipse.leshan.core.util.Hex
import org.slf4j.LoggerFactory

class DefaultFirmwareService(
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    private val digest: MessageDigest = MessageDigest.getInstance("SHA-256"),
    initial: FirmwareUpdateExecution =
        Idle(
            "1.0.0-SNAPSHOT".toByteArray(),
            "about:blank",
            FirmwareUpdateResult.initial(),
            "1.0.0-SNAPSHOT",
        ),
) : FirmwareService {

  private var firmware: FirmwareUpdateExecution = initial

  private val deliveryMethod: FirmwareUpdateDeliveryMethod = FirmwareUpdateDeliveryMethod.initial()

  private val onFileChange: MutableList<(ByteArray) -> Unit> = mutableListOf()
  private val onUrlChange: MutableList<(String?) -> Unit> = mutableListOf()
  private val onStateChange: MutableList<(FirmwareUpdateState) -> Unit> = mutableListOf()
  private val onResultChange: MutableList<(FirmwareUpdateResult) -> Unit> = mutableListOf()
  private val onPackageVersionChange: MutableList<(String?) -> Unit> = mutableListOf()
  private val onProgressChange: MutableList<(Int) -> Unit> = mutableListOf()

  override fun start() {
    scheduler.scheduleAtFixedRate(::tick, 1L, 1L, TimeUnit.SECONDS)
    log.info("Scheduler for firmware update state machine initialized")
  }

  internal fun tick() {
    if (firmware.hasNext()) {
      val previous = firmware
      firmware = firmware.execute()
      fireOnAnythingChanged(previous)
    }
  }

  private fun fireOnAnythingChanged(previousFirmware: FirmwareUpdateExecution) {
    if (firmware == previousFirmware) {
      return
    }
    if (firmware.file !== previousFirmware.file) {
      onFileChange.forEach { it(firmware.file) }
    }
    if (firmware.packageUri != previousFirmware.packageUri) {
      onUrlChange.forEach { it(firmware.packageUri) }
    }
    if (firmware.state != previousFirmware.state) {
      onStateChange.forEach { it(firmware.state) }
    }
    if (firmware.result != previousFirmware.result) {
      onResultChange.forEach { it(firmware.result) }
    }
    if (firmware.packageVersion != previousFirmware.packageVersion) {
      onPackageVersionChange.forEach { it(firmware.packageVersion) }
    }
    if (firmware.progress != previousFirmware.progress) {
      onProgressChange.forEach { it(firmware.progress) }
    }
  }

  override fun getFirmware(): Firmware =
      Firmware(
          file = firmware.file,
          fileChecksum = "sha256:" + Hex.encodeHexString(digest.digest(firmware.file)),
          packageUri = firmware.packageUri,
          state = firmware.state,
          result = firmware.result,
          packageVersion = firmware.packageVersion,
          deliveryMethod = deliveryMethod,
          progress = firmware.progress,
      )

  private fun stringifyFile(file: ByteArray): String {
    val trimmed = cut(cut(file, 20), 10)
    return String(trimmed).split("\n")[0]
  }

  private fun cut(file: ByteArray, maxLength: Int): ByteArray =
      if (file.size <= maxLength) file.copyOf() else file.copyOf(maxLength)

  override fun changeFirmware(update: FirmwareUpdate) {
    val previous = this.firmware
    update.file?.let { file ->
      this.firmware =
          Downloaded(
              file,
              this.firmware.packageUri,
              FirmwareUpdateResult.NONE,
              this.firmware.packageVersion,
          )
      fireOnAnythingChanged(previous)
      log.info("Updated firmware file to {}", stringifyFile(this.firmware.file))
    }
    update.packageUri?.let { uri ->
      this.firmware =
          Downloading(
              this.firmware.file,
              uri,
              FirmwareUpdateResult.NONE,
              this.firmware.packageVersion,
          )
      fireOnAnythingChanged(previous)
      log.info("Updated firmware package URI to {}", this.firmware.packageUri)
    }
  }

  override fun executeFirmwareUpdate() {
    val previous = firmware
    firmware = Updating(firmware)
    fireOnAnythingChanged(previous)
  }

  override fun subscribeOnFileChange(consumer: (ByteArray) -> Unit) = onFileChange.add(consumer)

  override fun subscribeOnPackageUriChange(consumer: (String?) -> Unit) = onUrlChange.add(consumer)

  override fun subscribeOnStateChange(consumer: (FirmwareUpdateState) -> Unit) =
      onStateChange.add(consumer)

  override fun subscribeOnResultChange(consumer: (FirmwareUpdateResult) -> Unit) =
      onResultChange.add(consumer)

  override fun subscribeOnPackageVersionChange(consumer: (String?) -> Unit) =
      onPackageVersionChange.add(consumer)

  override fun subscribeOnProgressChange(consumer: (Int) -> Unit) = onProgressChange.add(consumer)

  companion object {
    private val log = LoggerFactory.getLogger(DefaultFirmwareService::class.java)
  }
}
