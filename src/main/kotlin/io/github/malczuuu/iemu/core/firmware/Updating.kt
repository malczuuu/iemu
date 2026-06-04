package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
import java.util.Random
import kotlin.math.min
import org.slf4j.LoggerFactory

class Updating
private constructor(
    override val packageUri: String?,
    override val result: FirmwareUpdateResult,
    override val packageVersion: String?,
    private val random: Random,
    override val progress: Int,
    private val ticksPerStep: Int,
    private val ticksUntilNext: Int,
) : FirmwareUpdateExecution {

  constructor(
      firmware: FirmwareUpdateExecution,
      ticksPerStep: Int = TICKS_PER_STEP,
  ) : this(firmware.packageUri, FirmwareUpdateResult.NONE, firmware.packageVersion, Random(), 0, ticksPerStep, ticksPerStep)

  override val state: FirmwareUpdateState = FirmwareUpdateState.UPDATING

  override fun execute(): FirmwareUpdateExecution {
    if (ticksUntilNext > 1) {
      return Updating(packageUri, result, packageVersion, random, progress, ticksPerStep, ticksUntilNext - 1)
    }
    if (progress >= 100) {
      val finalResult = FirmwareUpdateResult.SUCCESSFUL
      log.info("Installation finished, move to 'Idle' state with {} result", finalResult)
      return Idle(packageUri, finalResult, packageVersion)
    }
    val next = min(100, progress + 5 + random.nextInt(16))
    log.info("Installation progress={}%, keep state={}", next, state)
    return Updating(packageUri, result, packageVersion, random, next, ticksPerStep, ticksPerStep)
  }

  override fun hasNext(): Boolean = true

  companion object {
    const val TICKS_PER_STEP = 5
    private val log = LoggerFactory.getLogger(Updating::class.java)
  }
}
