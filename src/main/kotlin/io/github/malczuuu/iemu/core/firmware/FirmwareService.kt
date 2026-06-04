package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.core.FirmwareState
import io.github.malczuuu.iemu.core.FirmwareUpdate
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import org.eclipse.jetty.client.HttpClient
import org.slf4j.LoggerFactory

class FirmwareService(
    private val scheduler: ScheduledExecutorService,
    private val persistence: FirmwareProvider,
    private val downloader: FirmwareDownloader = FirmwareDownloader { uri ->
      val client = HttpClient().also { it.start() }
      try {
        client.GET(uri).content
      } finally {
        client.stop()
      }
    },
    private val ticksPerStep: Int = Updating.TICKS_PER_STEP,
) {

  private val snapshot = persistence.load()
  private var firmware: FirmwareUpdateExecution =
      if (snapshot.stagedColor != null)
          Downloaded(snapshot.packageUri, FirmwareUpdateResult.NONE, snapshot.version)
      else
          Idle(snapshot.packageUri, FirmwareUpdateResult.initial(), snapshot.version)
  private var installedColor: String = snapshot.color
  private var stagedColor: String? = snapshot.stagedColor

  private val deliveryMethod: FirmwareUpdateDeliveryMethod = FirmwareUpdateDeliveryMethod.initial()

  private val onUrlChange: MutableList<(String?) -> Unit> = mutableListOf()
  private val onStateChange: MutableList<(FirmwareUpdateState) -> Unit> = mutableListOf()
  private val onResultChange: MutableList<(FirmwareUpdateResult) -> Unit> = mutableListOf()
  private val onPackageVersionChange: MutableList<(String?) -> Unit> = mutableListOf()
  private val onProgressChange: MutableList<(Int) -> Unit> = mutableListOf()

  private var future: ScheduledFuture<*>? = null

  fun start() {
    future = scheduler.scheduleAtFixedRate(::tick, 1L, 1L, TimeUnit.SECONDS)
    log.info("Scheduler for firmware update state machine initialized")
  }

  fun shutdown() {
    future?.cancel(false)
    future = null
  }

  internal fun tick() {
    if (firmware.hasNext()) {
      val previous = firmware
      firmware = firmware.execute()
      fireOnAnythingChanged(previous)
    }
  }

  private fun fireOnAnythingChanged(previousFirmware: FirmwareUpdateExecution) {
    if (firmware == previousFirmware) return

    // URI download path: file was already saved by the onFetched callback in Downloading.fetch()
    // Here we just persist state transition DOWNLOADING → DOWNLOADED for non-restart awareness
    if (firmware.state == FirmwareUpdateState.DOWNLOADED
        && previousFirmware.state == FirmwareUpdateState.DOWNLOADING) {
      val staged = stagedColor
      if (staged != null) {
        persistence.save(FirmwareSnapshot(installedColor, firmware.packageVersion ?: DEFAULT_VERSION, staged))
      }
    }

    if (firmware.result == FirmwareUpdateResult.SUCCESSFUL
        && previousFirmware.result != FirmwareUpdateResult.SUCCESSFUL) {
      val color = stagedColor ?: installedColor
      installedColor = color
      stagedColor = null
      persistence.save(FirmwareSnapshot(color, firmware.packageVersion ?: DEFAULT_VERSION))
    }

    if (firmware.packageUri != previousFirmware.packageUri) onUrlChange.forEach { it(firmware.packageUri) }
    if (firmware.state != previousFirmware.state) onStateChange.forEach { it(firmware.state) }
    if (firmware.result != previousFirmware.result) onResultChange.forEach { it(firmware.result) }
    if (firmware.packageVersion != previousFirmware.packageVersion) onPackageVersionChange.forEach { it(firmware.packageVersion) }
    if (firmware.progress != previousFirmware.progress) onProgressChange.forEach { it(firmware.progress) }
  }

  fun getFirmware(): FirmwareState =
      FirmwareState(
          color = installedColor,
          packageUri = firmware.packageUri,
          state = firmware.state,
          result = firmware.result,
          packageVersion = firmware.packageVersion,
          deliveryMethod = deliveryMethod,
          progress = firmware.progress,
      )

  fun changeFirmware(update: FirmwareUpdate) {
    update.file?.let { stageFile(it) }
    update.packageUri?.let { uri ->
      val previous = firmware
      firmware = Downloading(
          packageUri = uri,
          result = FirmwareUpdateResult.NONE,
          packageVersion = firmware.packageVersion,
          ticksPerStep = ticksPerStep,
          downloader = downloader,
          onFetched = { color ->
            stagedColor = color
            persistence.save(FirmwareSnapshot(installedColor, firmware.packageVersion ?: DEFAULT_VERSION, color))
          },
      )
      fireOnAnythingChanged(previous)
      log.info("Updated firmware package URI to {}", uri)
    }
  }

  fun stageFile(bytes: ByteArray): Boolean {
    val color = String(bytes).trim()
    if (!color.matches(HEX_COLOR_REGEX)) {
      log.error("Firmware staging rejected: not a valid hex color")
      val previous = firmware
      firmware = Idle(firmware.packageUri, FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED, firmware.packageVersion)
      fireOnAnythingChanged(previous)
      return false
    }
    // Save to disk BEFORE transitioning to DOWNLOADED
    persistence.save(FirmwareSnapshot(installedColor, firmware.packageVersion ?: DEFAULT_VERSION, color))
    stagedColor = color
    val previous = firmware
    firmware = Downloaded(firmware.packageUri, FirmwareUpdateResult.NONE, firmware.packageVersion)
    fireOnAnythingChanged(previous)
    log.info("Staged firmware color={}", color)
    return true
  }

  fun executeFirmwareUpdate(): Boolean {
    if (firmware.state != FirmwareUpdateState.DOWNLOADED) {
      log.warn("Execute rejected: state is {} not DOWNLOADED", firmware.state)
      return false
    }
    val previous = firmware
    firmware = Updating(firmware, ticksPerStep = ticksPerStep)
    fireOnAnythingChanged(previous)
    return true
  }

  fun onPackageUriChange(consumer: (String?) -> Unit) = onUrlChange.add(consumer)

  fun onStateChange(consumer: (FirmwareUpdateState) -> Unit) = onStateChange.add(consumer)

  fun onResultChange(consumer: (FirmwareUpdateResult) -> Unit) = onResultChange.add(consumer)

  fun onPackageVersionChange(consumer: (String?) -> Unit) = onPackageVersionChange.add(consumer)

  fun onProgressChange(consumer: (Int) -> Unit) = onProgressChange.add(consumer)

  companion object {
    const val DEFAULT_VERSION = "0.1"
    private val log = LoggerFactory.getLogger(FirmwareService::class.java)
  }
}
