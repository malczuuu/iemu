package io.github.malczuuu.iemu.core.firmware

import io.github.malczuuu.iemu.core.FirmwareUpdate
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateDeliveryMethod
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateResult
import io.github.malczuuu.iemu.lwm2m.firmware.FirmwareUpdateState
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirmwareServiceTests {

  private val scheduler = mockk<ScheduledExecutorService>(relaxed = true)
  private val persistence = mockk<FirmwareProvider>(relaxed = true)

  @BeforeEach
  fun setUp() {
    every { persistence.load() } returns
        FirmwareSnapshot(FirmwareProvider.DEFAULT_COLOR, FirmwareService.DEFAULT_VERSION)
  }

  private fun service(
      digest: MessageDigest = MessageDigest.getInstance("SHA-256"),
      downloader: FirmwareDownloader = FirmwareDownloader { "#FF0000".toByteArray() },
      ticksPerStep: Int = 1,
  ) = FirmwareService(
      scheduler = scheduler,
      digest = digest,
      persistence = persistence,
      downloader = downloader,
      ticksPerStep = ticksPerStep,
  )

  @Test
  fun `start() should schedule the tick task at one second fixed rate`() {
    service().start()

    verify { scheduler.scheduleAtFixedRate(any(), 1L, 1L, TimeUnit.SECONDS) }
  }

  @Test
  fun `constructor should not schedule anything until start is called`() {
    service()

    verify(exactly = 0) { scheduler.scheduleAtFixedRate(any(), any(), any(), any()) }
  }

  @Test
  fun `getFirmware() should start in IDLE and expose loaded version`() {
    every { persistence.load() } returns FirmwareSnapshot("#CC0000", "9.9.9")

    val fw = service().getFirmware()

    assertEquals(FirmwareUpdateState.IDLE, fw.state)
    assertEquals("9.9.9", fw.packageVersion)
    assertEquals("#CC0000", fw.color)
    assertEquals(FirmwareUpdateDeliveryMethod.BOTH, fw.deliveryMethod)
  }

  @Test
  fun `getFirmware() should produce a sha256 checksum`() {
    val fw = service().getFirmware()

    assertTrue(fw.fileChecksum.startsWith("sha256:"))
    assertEquals(64, fw.fileChecksum.removePrefix("sha256:").length)
  }

  @Test
  fun `getFirmware() should use the injected MessageDigest`() {
    val mockDigest = mockk<MessageDigest>()
    every { mockDigest.digest(any()) } returns ByteArray(32) { 0x0A }

    val checksum = service(digest = mockDigest).getFirmware().fileChecksum

    assertNotNull(checksum)
    assertTrue(checksum.endsWith("0a".repeat(32)))
    verify { mockDigest.digest(any()) }
  }

  @Test
  fun `stageFile() with valid hex color should save to disk then transition to DOWNLOADED`() {
    val service = service()
    val states = mutableListOf<FirmwareUpdateState>()
    service.onStateChange(states::add)

    assertTrue(service.stageFile("#FF0000".toByteArray()))

    // Saved to disk before state change
    verify { persistence.save(match { it.stagedColor == "#FF0000" }) }
    assertEquals(FirmwareUpdateState.DOWNLOADED, service.getFirmware().state)
    assertEquals(FirmwareProvider.DEFAULT_COLOR, service.getFirmware().color) // installed unchanged
    assertEquals(listOf(FirmwareUpdateState.DOWNLOADED), states)
  }

  @Test
  fun `stageFile() with invalid content should stay IDLE with FIRMWARE_UPDATE_FAILED`() {
    val service = service()

    assertFalse(service.stageFile("not-a-color".toByteArray()))

    assertEquals(FirmwareUpdateState.IDLE, service.getFirmware().state)
    assertEquals(FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED, service.getFirmware().result)
  }

  @Test
  fun `changeFirmware() with file delegates to stageFile`() {
    val service = service()
    val states = mutableListOf<FirmwareUpdateState>()
    service.onStateChange(states::add)

    service.changeFirmware(FirmwareUpdate(file = "#AA33BB".toByteArray()))

    assertEquals(FirmwareUpdateState.DOWNLOADED, service.getFirmware().state)
    verify { persistence.save(match { it.stagedColor == "#AA33BB" }) }
  }

  @Test
  fun `changeFirmware() with packageUri transitions to DOWNLOADING`() {
    val service = service()
    val uris = mutableListOf<String?>()
    service.onPackageUriChange(uris::add)

    service.changeFirmware(FirmwareUpdate(packageUri = "http://example/fw"))

    assertEquals(FirmwareUpdateState.DOWNLOADING, service.getFirmware().state)
    assertEquals(listOf("http://example/fw"), uris)
  }

  @Test
  fun `executeFirmwareUpdate() returns false when not in DOWNLOADED state`() {
    assertFalse(service().executeFirmwareUpdate())
  }

  @Test
  fun `executeFirmwareUpdate() transitions from DOWNLOADED to UPDATING`() {
    val service = service()
    service.stageFile("#FF0000".toByteArray())
    val states = mutableListOf<FirmwareUpdateState>()
    service.onStateChange(states::add)

    assertTrue(service.executeFirmwareUpdate())

    assertEquals(FirmwareUpdateState.UPDATING, service.getFirmware().state)
    assertEquals(listOf(FirmwareUpdateState.UPDATING), states)
  }

  @Test
  fun `tick() does not advance IDLE state`() {
    val service = service()
    val states = mutableListOf<FirmwareUpdateState>()
    service.onStateChange(states::add)

    service.tick()

    assertTrue(states.isEmpty())
    assertEquals(FirmwareUpdateState.IDLE, service.getFirmware().state)
  }

  @Test
  fun `tick() advances UPDATING state and fires progress callbacks`() {
    val service = service()
    service.stageFile("#AA33BB".toByteArray())
    service.executeFirmwareUpdate()
    val progress = mutableListOf<Int>()
    service.onProgressChange(progress::add)

    service.tick()

    assertFalse(progress.isEmpty())
    assertTrue(progress.first() > 0)
  }

  @Test
  fun `subscribers should be registered successfully`() {
    val service = service()

    assertTrue(service.onFileChange {})
    assertTrue(service.onPackageUriChange {})
    assertTrue(service.onStateChange {})
    assertTrue(service.onResultChange {})
    assertTrue(service.onPackageVersionChange {})
    assertTrue(service.onProgressChange {})
  }

  // ── Flow tests ──────────────────────────────────────────────────────────────

  @Test
  fun `file write flow - IDLE → DOWNLOADED → UPDATING → IDLE with persistence`() {
    val service = service()
    val states = mutableListOf<FirmwareUpdateState>()
    service.onStateChange(states::add)

    service.stageFile("#FF0000".toByteArray())
    service.executeFirmwareUpdate()
    repeat(200) { service.tick() }

    assertEquals(FirmwareUpdateState.IDLE, service.getFirmware().state)
    assertEquals(FirmwareUpdateResult.SUCCESSFUL, service.getFirmware().result)
    assertEquals("#FF0000", service.getFirmware().color)
    verify { persistence.save(match { it.stagedColor == "#FF0000" }) }
    verify { persistence.save(match { it.color == "#FF0000" && it.stagedColor == null }) }
    assertEquals(listOf(FirmwareUpdateState.DOWNLOADED, FirmwareUpdateState.UPDATING, FirmwareUpdateState.IDLE), states)
  }

  @Test
  fun `file write flow - installed color unchanged until SUCCESSFUL`() {
    val service = service()

    service.stageFile("#FF0000".toByteArray())
    assertEquals(FirmwareProvider.DEFAULT_COLOR, service.getFirmware().color)

    service.executeFirmwareUpdate()
    service.tick()
    if (service.getFirmware().state == FirmwareUpdateState.UPDATING) {
      assertEquals(FirmwareProvider.DEFAULT_COLOR, service.getFirmware().color)
    }
  }

  @Test
  fun `uri download flow - IDLE → DOWNLOADING → DOWNLOADED → UPDATING → IDLE`() {
    val service = service(downloader = FirmwareDownloader { "#AA33BB".toByteArray() })
    val states = mutableListOf<FirmwareUpdateState>()
    service.onStateChange(states::add)

    service.changeFirmware(FirmwareUpdate(packageUri = "http://example/fw"))
    repeat(50) { service.tick() }
    assertEquals(FirmwareUpdateState.DOWNLOADED, service.getFirmware().state)
    verify { persistence.save(match { it.stagedColor == "#AA33BB" }) }

    service.executeFirmwareUpdate()
    repeat(200) { service.tick() }
    assertEquals(FirmwareUpdateState.IDLE, service.getFirmware().state)
    assertEquals("#AA33BB", service.getFirmware().color)
    verify { persistence.save(match { it.color == "#AA33BB" && it.stagedColor == null }) }

    assertTrue(states.containsAll(listOf(
        FirmwareUpdateState.DOWNLOADING,
        FirmwareUpdateState.DOWNLOADED,
        FirmwareUpdateState.UPDATING,
        FirmwareUpdateState.IDLE,
    )))
  }

  @Test
  fun `on startup with staged firmware, service starts in DOWNLOADED state`() {
    every { persistence.load() } returns FirmwareSnapshot("#000000", "0.1", stagedColor = "#FF0000")

    val service = service()

    assertEquals(FirmwareUpdateState.DOWNLOADED, service.getFirmware().state)
    assertEquals("#000000", service.getFirmware().color)
  }

  @Test
  fun `on startup without staged firmware, service starts in IDLE with loaded color`() {
    every { persistence.load() } returns FirmwareSnapshot("#CC0000", "1.0")

    val service = service()

    assertEquals(FirmwareUpdateState.IDLE, service.getFirmware().state)
    assertEquals("#CC0000", service.getFirmware().color)
  }
}
