package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import org.eclipse.jetty.client.HttpClient
import org.slf4j.LoggerFactory

class Downloading(
    override val file: ByteArray,
    override val packageUri: String?,
    override val result: FirmwareUpdateResult,
    override val packageVersion: String?,
    private val downloader: FirmwareDownloader = FirmwareDownloader { uri ->
      val client = HttpClient().also { it.start() }
      try {
        client.GET(uri).content
      } finally {
        client.stop()
      }
    },
) : FirmwareUpdateExecution {

  override val state: FirmwareUpdateState = FirmwareUpdateState.DOWNLOADING

  override val progress: Int = 0

  override fun execute(): FirmwareUpdateExecution =
      try {
        val uri = URI(packageUri)
        if (uri.scheme?.matches(Regex("^(http|https)\$")) != true) {
          val unsupported = FirmwareUpdateResult.UNSUPPORTED_PROTOCOL
          log.error(
              "Schema doesn't match http|https, move into idle state with result={}",
              unsupported,
          )
          Idle(file, packageUri, unsupported, packageVersion)
        } else {
          val downloaded = downloader.download(uri)
          if (downloaded.isEmpty()) {
            fileEmptyFailure(downloaded)
          } else {
            successfulDownloading(downloaded)
          }
        }
      } catch (e: URISyntaxException) {
        onURISyntaxException()
      } catch (e: InterruptedException) {
        onDownloadingBroke(e)
      } catch (e: ExecutionException) {
        onDownloadingBroke(e)
      } catch (e: TimeoutException) {
        onDownloadingBroke(e)
      } catch (e: Exception) {
        onUnknownException(e)
      }

  private fun fileEmptyFailure(downloaded: ByteArray): FirmwareUpdateExecution {
    val failure = FirmwareUpdateResult.PACKAGE_INTEGRITY_CHECK_FAILURE
    log.error("Read file is empty, move into idle state with result={}", failure)
    return Idle(downloaded, packageUri, failure, packageVersion)
  }

  private fun successfulDownloading(downloaded: ByteArray): Downloaded {
    val ok = FirmwareUpdateResult.NONE
    log.info("Download complete, move into downloaded state with result={}", ok)
    return Downloaded(downloaded, packageUri, ok, packageVersion)
  }

  private fun onURISyntaxException(): FirmwareUpdateExecution {
    val invalid = FirmwareUpdateResult.INVALID_URI
    log.error("PackageURI doesn't follow URI syntax, move into idle state with result={}", invalid)
    return Idle(file, packageUri, invalid, packageVersion)
  }

  /** It could at least attempt to retry a broken downloading a few times. */
  private fun onDownloadingBroke(e: Exception): FirmwareUpdateExecution {
    val none = FirmwareUpdateResult.NONE
    if (log.isDebugEnabled) {
      log.error("Downloading file broke, move into idle state with result={}", none, e)
    } else {
      log.error("Downloading file broke, move into idle state with result={}", none)
    }
    return Idle(file, packageUri, none, packageVersion)
  }

  private fun onUnknownException(e: Exception): FirmwareUpdateExecution {
    val failed = FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED
    if (log.isDebugEnabled) {
      log.error("Downloading file broke, move into idle state with result={}", failed, e)
    } else {
      log.error("Downloading file broke, move into idle state with result={}", failed)
    }
    return Idle(file, packageUri, failed, packageVersion)
  }

  override fun hasNext(): Boolean = true

  companion object {
    private val log = LoggerFactory.getLogger(Downloading::class.java)
  }
}
