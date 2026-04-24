package io.github.malczuuu.iemu.domain

import io.github.malczuuu.iemu.domain.firmware.Downloaded
import io.github.malczuuu.iemu.domain.firmware.FirmwareUpdateExecution
import io.github.malczuuu.iemu.domain.firmware.Idle
import io.github.malczuuu.iemu.domain.firmware.Updating
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateResult
import io.github.malczuuu.iemu.infra.lwm2m.FirmwareUpdateState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.security.MessageDigest
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DefaultFirmwareServiceTests {

  private val scheduler = mockk<ScheduledExecutorService>(relaxed = true)
  private val digest = MessageDigest.getInstance("SHA-256")

  private fun idle(
      file: ByteArray = "1.0.0".toByteArray(),
      uri: String? = "about:blank",
      result: FirmwareUpdateResult = FirmwareUpdateResult.NONE,
      version: String? = "1.0.0",
  ): FirmwareUpdateExecution = Idle(file, uri, result, version)

  @Test
  fun `start() should schedule the tick task at one second fixed rate`() {
    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = idle())

    service.start()

    verify { scheduler.scheduleAtFixedRate(any(), 1L, 1L, TimeUnit.SECONDS) }
  }

  @Test
  fun `constructor should not schedule anything until start is called`() {
    DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = idle())

    verify(exactly = 0) { scheduler.scheduleAtFixedRate(any(), any(), any(), any()) }
  }

  @Test
  fun `getFirmware() should expose fields of the current execution and the sha256 checksum`() {
    val file = "firmware-payload".toByteArray()
    val service =
        DefaultFirmwareService(
            scheduler = scheduler,
            digest = digest,
            initial = idle(file = file, uri = "http://fw", version = "9.9.9"),
        )

    val fw = service.getFirmware()

    assertEquals("http://fw", fw.packageUri)
    assertEquals(FirmwareUpdateState.IDLE, fw.state)
    assertEquals(FirmwareUpdateResult.NONE, fw.result)
    assertEquals("9.9.9", fw.packageVersion)
    assertEquals(FirmwareUpdateDeliveryMethod.BOTH, fw.deliveryMethod)
    assertTrue(fw.fileChecksum.startsWith("sha256:"))
    assertEquals(64, fw.fileChecksum.removePrefix("sha256:").length)
  }

  @Test
  fun `changeFirmware() with a file should transition into Downloaded and notify subscribers`() {
    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = idle())
    val observedFiles = mutableListOf<ByteArray>()
    val observedStates = mutableListOf<FirmwareUpdateState>()
    service.subscribeOnFileChange(observedFiles::add)
    service.subscribeOnStateChange(observedStates::add)

    service.changeFirmware(FirmwareUpdate(file = "new-bytes".toByteArray()))

    assertEquals(FirmwareUpdateState.DOWNLOADED, service.getFirmware().state)
    assertEquals(1, observedFiles.size)
    assertEquals("new-bytes", String(observedFiles[0]))
    assertEquals(listOf(FirmwareUpdateState.DOWNLOADED), observedStates)
  }

  @Test
  fun `changeFirmware() with a packageUri should transition into Downloading and notify subscribers`() {
    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = idle())
    val observedUris = mutableListOf<String?>()
    service.subscribeOnPackageUriChange(observedUris::add)

    service.changeFirmware(FirmwareUpdate(packageUri = "http://example/fw"))

    assertEquals(FirmwareUpdateState.DOWNLOADING, service.getFirmware().state)
    assertEquals(listOf("http://example/fw"), observedUris)
  }

  @Test
  fun `executeFirmwareUpdate() should move the execution into Updating and notify state subscribers`() {
    val service =
        DefaultFirmwareService(
            scheduler = scheduler,
            digest = digest,
            initial =
                Downloaded(
                    file = "fw".toByteArray(),
                    packageUri = "http://x",
                    result = FirmwareUpdateResult.NONE,
                    packageVersion = "1.0.0",
                ),
        )
    val states = mutableListOf<FirmwareUpdateState>()
    service.subscribeOnStateChange(states::add)

    service.executeFirmwareUpdate()

    assertEquals(FirmwareUpdateState.UPDATING, service.getFirmware().state)
    assertEquals(listOf(FirmwareUpdateState.UPDATING), states)
  }

  @Test
  fun `tick() should not advance when the current execution is terminal`() {
    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = idle())
    val states = mutableListOf<FirmwareUpdateState>()
    service.subscribeOnStateChange(states::add)

    service.tick()

    assertTrue(states.isEmpty())
    assertEquals(FirmwareUpdateState.IDLE, service.getFirmware().state)
  }

  @Test
  fun `tick() should advance the execution when it has a next state`() {
    val next =
        Idle(
            "2.0.0".toByteArray(),
            "http://x",
            FirmwareUpdateResult.SUCCESSFUL,
            "2.0.0",
        )
    val stub =
        object : FirmwareUpdateExecution {
          override val file = "stub".toByteArray()
          override val packageUri = "http://x"
          override val packageVersion = "1.0.0"
          override val state = FirmwareUpdateState.UPDATING
          override val result = FirmwareUpdateResult.NONE
          override val progress = 99

          override fun execute(): FirmwareUpdateExecution = next

          override fun hasNext(): Boolean = true
        }

    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = stub)
    val states = mutableListOf<FirmwareUpdateState>()
    val versions = mutableListOf<String?>()
    val results = mutableListOf<FirmwareUpdateResult>()
    service.subscribeOnStateChange(states::add)
    service.subscribeOnPackageVersionChange(versions::add)
    service.subscribeOnResultChange(results::add)

    service.tick()

    assertEquals(listOf(FirmwareUpdateState.IDLE), states)
    assertEquals(listOf("2.0.0"), versions)
    assertEquals(listOf(FirmwareUpdateResult.SUCCESSFUL), results)
  }

  @Test
  fun `subscribers should be registered successfully and return true`() {
    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = idle())

    assertTrue(service.subscribeOnFileChange {})
    assertTrue(service.subscribeOnPackageUriChange {})
    assertTrue(service.subscribeOnStateChange {})
    assertTrue(service.subscribeOnResultChange {})
    assertTrue(service.subscribeOnPackageVersionChange {})
    assertTrue(service.subscribeOnProgressChange {})
  }

  @Test
  fun `getFirmware() should use the injected MessageDigest to produce the checksum`() {
    val digest = mockk<MessageDigest>()
    val stubbedHash = ByteArray(32) { 0x0A }
    every { digest.digest(any()) } returns stubbedHash

    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = idle())

    val checksum = service.getFirmware().fileChecksum

    assertNotNull(checksum)
    assertTrue(checksum.endsWith("0a".repeat(32)))
    verify { digest.digest(any()) }
  }

  @Test
  fun `applying an Updating execution repeatedly via tick should eventually produce progress updates`() {
    val updating =
        Updating(
            file = "2.0.0\nrest".toByteArray(),
            packageUri = "http://x",
            result = FirmwareUpdateResult.NONE,
            packageVersion = "1.0.0",
        )
    val service = DefaultFirmwareService(scheduler = scheduler, digest = digest, initial = updating)
    val progress = mutableListOf<Int>()
    service.subscribeOnProgressChange(progress::add)

    service.tick()

    assertFalse(progress.isEmpty())
    assertTrue(progress.first() > 0)
  }
}
