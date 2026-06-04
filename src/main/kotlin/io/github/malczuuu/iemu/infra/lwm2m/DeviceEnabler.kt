package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.domain.StateService
import io.github.malczuuu.iemu.domain.StateUpdate
import java.time.temporal.ChronoUnit
import java.util.Date
import org.eclipse.leshan.client.resource.BaseInstanceEnabler
import org.eclipse.leshan.client.servers.ServerIdentity
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.model.ResourceModel
import org.eclipse.leshan.core.node.LwM2mResource
import org.eclipse.leshan.core.response.ExecuteResponse
import org.eclipse.leshan.core.response.ReadResponse
import org.eclipse.leshan.core.response.WriteResponse

class DeviceEnabler(val state: StateService) : BaseInstanceEnabler() {

  init {
    state.subscribeOnCurrentTimeChange { fireResourcesChange(CURRENT_TIME) }
  }

  override fun read(identity: ServerIdentity, resourceId: Int): ReadResponse =
      when (resourceId) {
        MANUFACTURER -> ReadResponse.success(resourceId, "Manufacturer")
        MODEL_NUMBER -> ReadResponse.success(resourceId, "0")
        SERIAL_NUMBER -> ReadResponse.success(resourceId, "0")
        FIRMWARE_VERSION -> ReadResponse.success(resourceId, "0.0.0")
        BATTERY_LEVEL -> ReadResponse.success(resourceId, 100L)
        MEMORY_FREE -> ReadResponse.success(resourceId, Runtime.getRuntime().freeMemory() / 1000)
        ERROR_CODE -> {
          val errors = state.getState().errors
          val errorCodes = errors.withIndex().associate { (i, e) -> i to e.code.toLong() }
          ReadResponse.success(resourceId, errorCodes, ResourceModel.Type.INTEGER)
        }
        CURRENT_TIME -> ReadResponse.success(resourceId, Date.from(state.getState().currentTime))
        UTC_OFFSET -> ReadResponse.success(resourceId, state.getState().utcOffset)
        TIMEZONE -> ReadResponse.success(resourceId, state.getState().timeZone)
        SUPPORTED_BINDINGS_AND_MODES -> ReadResponse.success(resourceId, "U")
        DEVICE_TYPE -> ReadResponse.success(resourceId, state.getState().deviceType)
        HARDWARE_VERSION -> ReadResponse.success(resourceId, "0.0.0")
        SOFTWARE_VERSION -> ReadResponse.success(resourceId, "0.0.0")
        BATTERY_STATUS -> ReadResponse.success(resourceId, 100L)
        MEMORY_TOTAL -> ReadResponse.success(resourceId, Runtime.getRuntime().totalMemory() / 1000)
        else -> super.read(identity, resourceId)
      }

  override fun write(
      identity: ServerIdentity,
      resourceId: Int,
      value: LwM2mResource,
  ): WriteResponse =
      if (resourceId == CURRENT_TIME) {
        state.changeState(
            StateUpdate(
                currentTime = (value.value as Date).toInstant().truncatedTo(ChronoUnit.SECONDS),
            ),
        )
        WriteResponse.success()
      } else {
        super.write(identity, resourceId, value)
      }

  override fun execute(identity: ServerIdentity, resourceId: Int, params: String): ExecuteResponse =
      when (resourceId) {
        REBOOT -> ExecuteResponse.success()

        FACTORY_RESET -> ExecuteResponse.success()

        RESET_ERROR_CODE -> {
          state.resetErrors()
          ExecuteResponse.success()
        }

        else -> super.execute(identity, resourceId, params)
      }

  override fun getAvailableResourceIds(model: ObjectModel): List<Int> = SUPPORTED_RESOURCES

  companion object {
    const val OBJECT_ID: Int = 3

    private const val MANUFACTURER = 0
    private const val MODEL_NUMBER = 1
    private const val SERIAL_NUMBER = 2
    private const val FIRMWARE_VERSION = 3
    private const val REBOOT = 4
    private const val FACTORY_RESET = 5
    private const val BATTERY_LEVEL = 9
    private const val MEMORY_FREE = 10
    private const val ERROR_CODE = 11
    private const val RESET_ERROR_CODE = 12
    private const val CURRENT_TIME = 13
    private const val UTC_OFFSET = 14
    private const val TIMEZONE = 15
    private const val SUPPORTED_BINDINGS_AND_MODES = 16
    private const val DEVICE_TYPE = 17
    private const val HARDWARE_VERSION = 18
    private const val SOFTWARE_VERSION = 19
    private const val BATTERY_STATUS = 20
    private const val MEMORY_TOTAL = 21

    private val SUPPORTED_RESOURCES: List<Int> =
        listOf(
            MANUFACTURER,
            MODEL_NUMBER,
            SERIAL_NUMBER,
            FIRMWARE_VERSION,
            REBOOT,
            FACTORY_RESET,
            BATTERY_LEVEL,
            MEMORY_FREE,
            ERROR_CODE,
            RESET_ERROR_CODE,
            CURRENT_TIME,
            UTC_OFFSET,
            TIMEZONE,
            SUPPORTED_BINDINGS_AND_MODES,
            DEVICE_TYPE,
            HARDWARE_VERSION,
            SOFTWARE_VERSION,
            BATTERY_STATUS,
            MEMORY_TOTAL,
        )

    fun create(deviceService: StateService): DeviceEnabler = DeviceEnabler(deviceService)
  }
}
