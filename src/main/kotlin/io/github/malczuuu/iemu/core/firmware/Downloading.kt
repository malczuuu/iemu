package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import java.net.URI
import java.net.URISyntaxException
import java.util.Random
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import kotlin.math.min
import org.slf4j.LoggerFactory

class Downloading
private constructor(
    override val packageUri: String?,
    override val result: FirmwareUpdateResult,
    override val packageVersion: String?,
    private val downloader: FirmwareDownloader,
    private val onFetched: (String) -> Unit,
    private val random: Random,
    override val progress: Int,
    private val ticksPerStep: Int,
    private val ticksUntilNext: Int,
    private val fetchDone: Boolean,
) : FirmwareUpdateExecution {

  constructor(
      packageUri: String?,
      result: FirmwareUpdateResult,
      packageVersion: String?,
      ticksPerStep: Int = TICKS_PER_STEP,
      downloader: FirmwareDownloader,
      onFetched: (String) -> Unit,
  ) : this(packageUri, result, packageVersion, downloader, onFetched, Random(), 0, ticksPerStep, ticksPerStep, false)

  override val state: FirmwareUpdateState = FirmwareUpdateState.DOWNLOADING

  override fun execute(): FirmwareUpdateExecution = if (!fetchDone) fetch() else animateProgress()

  private fun fetch(): FirmwareUpdateExecution =
      try {
        val uri = URI(packageUri)
        if (uri.scheme?.matches(Regex("^(http|https)\$")) != true) {
          log.error("Schema doesn't match http|https, result={}", FirmwareUpdateResult.UNSUPPORTED_PROTOCOL)
          Idle(packageUri, FirmwareUpdateResult.UNSUPPORTED_PROTOCOL, packageVersion)
        } else {
          val downloaded = downloader.download(uri)
          when {
            downloaded.isEmpty() -> {
              log.error("Downloaded file is empty, result={}", FirmwareUpdateResult.PACKAGE_INTEGRITY_CHECK_FAILURE)
              Idle(packageUri, FirmwareUpdateResult.PACKAGE_INTEGRITY_CHECK_FAILURE, packageVersion)
            }
            !String(downloaded).trim().matches(HEX_COLOR_REGEX) -> {
              log.error("Downloaded content is not a valid hex color, result={}", FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED)
              Idle(packageUri, FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED, packageVersion)
            }
            else -> {
              val color = String(downloaded).trim()
              log.info("Fetch complete (color={}), saving to disk and starting animation", color)
              onFetched(color)
              Downloading(packageUri, result, packageVersion, downloader, onFetched, random, 0, ticksPerStep, ticksPerStep, true)
            }
          }
        }
      } catch (e: URISyntaxException) {
        log.error("PackageURI invalid syntax, result={}", FirmwareUpdateResult.INVALID_URI)
        Idle(packageUri, FirmwareUpdateResult.INVALID_URI, packageVersion)
      } catch (e: InterruptedException) {
        onFetchBroke(e, FirmwareUpdateResult.NONE)
      } catch (e: ExecutionException) {
        onFetchBroke(e, FirmwareUpdateResult.NONE)
      } catch (e: TimeoutException) {
        onFetchBroke(e, FirmwareUpdateResult.NONE)
      } catch (e: Exception) {
        onFetchBroke(e, FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED)
      }

  private fun onFetchBroke(e: Exception, result: FirmwareUpdateResult): FirmwareUpdateExecution {
    if (log.isDebugEnabled) log.error("Downloading file broke, result={}", result, e)
    else log.error("Downloading file broke, result={}", result)
    return Idle(packageUri, result, packageVersion)
  }

  private fun animateProgress(): FirmwareUpdateExecution {
    if (ticksUntilNext > 1) {
      return Downloading(packageUri, result, packageVersion, downloader, onFetched, random, progress, ticksPerStep, ticksUntilNext - 1, true)
    }
    if (progress < 100) {
      val next = min(100, progress + 5 + random.nextInt(16))
      log.info("Download progress={}%", next)
      return Downloading(packageUri, result, packageVersion, downloader, onFetched, random, next, ticksPerStep, ticksPerStep, true)
    }
    log.info("Download animation complete, transitioning to Downloaded")
    return Downloaded(packageUri, FirmwareUpdateResult.NONE, packageVersion)
  }

  override fun hasNext(): Boolean = true

  companion object {
    const val TICKS_PER_STEP = 5
    private val log = LoggerFactory.getLogger(Downloading::class.java)
  }
}
