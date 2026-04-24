package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.domain.Firmware
import io.github.malczuuu.iemu.domain.FirmwareService
import io.github.malczuuu.iemu.domain.FirmwareUpdate
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.leshan.client.servers.ServerIdentity
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.node.LwM2mSingleResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FirmwareUpdateEnablerTests {

  private val firmware = mockk<FirmwareService>(relaxed = true)
  private val identity = mockk<ServerIdentity>(relaxed = true)
  private val model = mockk<ObjectModel>(relaxed = true)

  private val sampleFirmware =
      Firmware(
          file = "bytes".toByteArray(),
          fileChecksum = "sha256:abc",
          packageUri = "http://x",
          state = FirmwareUpdateState.DOWNLOADED,
          result = FirmwareUpdateResult.NONE,
          packageVersion = "1.2.3",
          deliveryMethod = FirmwareUpdateDeliveryMethod.BOTH,
          progress = 0,
      )

  private fun enabler() = FirmwareUpdateEnabler(firmware)

  @Test
  fun `constructor should subscribe to every change stream exposed by FirmwareService`() {
    enabler()

    verify { firmware.subscribeOnFileChange(any()) }
    verify { firmware.subscribeOnPackageUriChange(any()) }
    verify { firmware.subscribeOnStateChange(any()) }
    verify { firmware.subscribeOnResultChange(any()) }
    verify { firmware.subscribeOnPackageVersionChange(any()) }
  }

  @Test
  fun `read() PACKAGE_URI should return the current uri from the firmware service`() {
    every { firmware.getFirmware() } returns sampleFirmware

    assertTrue(enabler().read(identity, 1).isSuccess)
  }

  @Test
  fun `read() STATE UPDATE_RESULT PACKAGE_VERSION MODE should return values from firmware service`() {
    every { firmware.getFirmware() } returns sampleFirmware

    val e = enabler()
    assertTrue(e.read(identity, 3).isSuccess)
    assertTrue(e.read(identity, 5).isSuccess)
    assertTrue(e.read(identity, 7).isSuccess)
    assertTrue(e.read(identity, 9).isSuccess)
  }

  @Test
  fun `read() on the instance should produce a bundle containing every mandatory resource`() {
    every { firmware.getFirmware() } returns sampleFirmware
    val instance = enabler().also { it.id = 0 }

    val response = instance.read(identity)

    assertTrue(response.isSuccess)
  }

  @Test
  fun `write() FILE should invoke changeFirmware with the uploaded bytes`() {
    every { firmware.changeFirmware(any()) } just Runs
    val payload = "payload".toByteArray()

    val response = enabler().write(identity, 0, LwM2mSingleResource.newBinaryResource(0, payload))

    assertTrue(response.isSuccess)
    verify {
      firmware.changeFirmware(
          match<FirmwareUpdate> { it.file != null && String(it.file!!) == "payload" }
      )
    }
  }

  @Test
  fun `write() PACKAGE_URI should invoke changeFirmware with the supplied uri`() {
    every { firmware.changeFirmware(any()) } just Runs

    val response =
        enabler().write(identity, 1, LwM2mSingleResource.newStringResource(1, "http://host/fw"))

    assertTrue(response.isSuccess)
    verify { firmware.changeFirmware(match<FirmwareUpdate> { it.packageUri == "http://host/fw" }) }
  }

  @Test
  fun `execute() UPDATE_ACTION should trigger the firmware update`() {
    every { firmware.executeFirmwareUpdate() } just Runs

    val response = enabler().execute(identity, 2, "")

    assertTrue(response.isSuccess)
    verify { firmware.executeFirmwareUpdate() }
  }

  @Test
  fun `getAvailableResourceIds() should list every firmware resource the emulator supports`() {
    val ids = enabler().getAvailableResourceIds(model)

    assertEquals(listOf(0, 1, 2, 3, 5, 7, 9), ids)
  }
}
