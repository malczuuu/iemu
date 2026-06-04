package io.github.malczuuu.iemu.infra.lwm2m.firmware

import io.github.malczuuu.iemu.core.FirmwareUpdate
import io.github.malczuuu.iemu.core.firmware.FirmwareService
import org.eclipse.leshan.client.resource.BaseInstanceEnabler
import org.eclipse.leshan.client.servers.ServerIdentity
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.node.LwM2mObjectInstance
import org.eclipse.leshan.core.node.LwM2mResource
import org.eclipse.leshan.core.node.LwM2mSingleResource
import org.eclipse.leshan.core.response.ExecuteResponse
import org.eclipse.leshan.core.response.ReadResponse
import org.eclipse.leshan.core.response.WriteResponse
import org.slf4j.LoggerFactory

class FirmwareUpdateEnabler(private val firmware: FirmwareService) : BaseInstanceEnabler() {

  init {
    firmware.onFileChange { fireResourcesChange(FILE) }
    firmware.onPackageUriChange { fireResourcesChange(PACKAGE_URI) }
    firmware.onStateChange { fireResourcesChange(STATE) }
    firmware.onResultChange { fireResourcesChange(UPDATE_RESULT) }
    firmware.onPackageVersionChange { fireResourcesChange(PACKAGE_VERSION) }
  }

  override fun read(identity: ServerIdentity, resourceId: Int): ReadResponse {
    log.debug(
        "Received read request to Firmware instanceId={} to resourceId={}",
        id,
        resourceId,
    )
    val fw = firmware.getFirmware()
    return when (resourceId) {
      PACKAGE_URI -> ReadResponse.success(resourceId, fw.packageUri)
      STATE -> ReadResponse.success(resourceId, fw.state.value.toLong())
      UPDATE_RESULT -> ReadResponse.success(resourceId, fw.result.value.toLong())
      PACKAGE_VERSION -> ReadResponse.success(resourceId, fw.packageVersion)
      MODE -> ReadResponse.success(resourceId, fw.deliveryMethod.value.toLong())
      else -> super.read(identity, resourceId)
    }
  }

  override fun read(identity: ServerIdentity): ReadResponse {
    log.debug("Received read request to Firmware instanceId={}", id)
    val fw = firmware.getFirmware()
    return ReadResponse.success(
        LwM2mObjectInstance(
            id,
            LwM2mSingleResource.newStringResource(PACKAGE_URI, fw.packageUri),
            LwM2mSingleResource.newIntegerResource(STATE, fw.state.value.toLong()),
            LwM2mSingleResource.newIntegerResource(UPDATE_RESULT, fw.result.value.toLong()),
            LwM2mSingleResource.newStringResource(PACKAGE_VERSION, fw.packageVersion),
            LwM2mSingleResource.newIntegerResource(MODE, fw.deliveryMethod.value.toLong()),
        ),
    )
  }

  override fun write(
      identity: ServerIdentity,
      resourceId: Int,
      value: LwM2mResource,
  ): WriteResponse {
    log.debug(
        "Received write request to Firmware instanceId={} to resourceId={}, value={}",
        id,
        resourceId,
        value,
    )
    return when (resourceId) {
      FILE -> {
        firmware.changeFirmware(FirmwareUpdate(file = value.value as ByteArray))
        WriteResponse.success()
      }

      PACKAGE_URI -> {
        firmware.changeFirmware(FirmwareUpdate(packageUri = value.value as String))
        WriteResponse.success()
      }

      else -> super.write(identity, resourceId, value)
    }
  }

  override fun execute(identity: ServerIdentity, resourceId: Int, params: String): ExecuteResponse =
      if (resourceId == UPDATE_ACTION) {
        firmware.executeFirmwareUpdate()
        ExecuteResponse.success()
      } else {
        super.execute(identity, resourceId, params)
      }

  override fun getAvailableResourceIds(model: ObjectModel): List<Int> = SUPPORTED_RESOURCES

  companion object {
    private val log = LoggerFactory.getLogger(FirmwareUpdateEnabler::class.java)

    const val OBJECT_ID: Int = 5

    private const val FILE = 0
    private const val PACKAGE_URI = 1
    private const val UPDATE_ACTION = 2
    private const val STATE = 3
    private const val UPDATE_RESULT = 5
    private const val PACKAGE_VERSION = 7
    private const val MODE = 9

    private val SUPPORTED_RESOURCES: List<Int> =
        listOf(FILE, PACKAGE_URI, UPDATE_ACTION, STATE, UPDATE_RESULT, PACKAGE_VERSION, MODE)
  }
}
