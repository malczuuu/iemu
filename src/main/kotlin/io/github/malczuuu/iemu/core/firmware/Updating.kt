package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import java.util.Random
import kotlin.math.min
import org.slf4j.LoggerFactory

class Updating
private constructor(
    override val file: ByteArray,
    override val packageUri: String?,
    override val result: FirmwareUpdateResult,
    override val packageVersion: String?,
    private val random: Random,
    override val progress: Int,
) : FirmwareUpdateExecution {

  constructor(
      file: ByteArray,
      packageUri: String?,
      result: FirmwareUpdateResult,
      packageVersion: String?,
  ) : this(file, packageUri, result, packageVersion, Random(), 0)

  constructor(
      firmware: FirmwareUpdateExecution,
  ) : this(firmware.file, firmware.packageUri, FirmwareUpdateResult.NONE, firmware.packageVersion)

  override val state: FirmwareUpdateState = FirmwareUpdateState.UPDATING

  override fun execute(): FirmwareUpdateExecution {
    if (progress < 100) {
      val next = min(100, progress + 10 + random.nextInt(20))
      log.info("Installation progress={}%, keep state={}", next, state)
      return Updating(file, packageUri, result, packageVersion, random, next)
    }

    val extractedVersion = String(file).split("\n")[0]
    val finalResult = FirmwareUpdateResult.SUCCESSFUL
    log.info(
        "Installation of package {} finished, move to 'Idle' state with {} result",
        extractedVersion,
        finalResult,
    )
    return Idle(file, packageUri, finalResult, extractedVersion)
  }

  override fun hasNext(): Boolean = true

  companion object {
    private val log = LoggerFactory.getLogger(Updating::class.java)
  }
}
