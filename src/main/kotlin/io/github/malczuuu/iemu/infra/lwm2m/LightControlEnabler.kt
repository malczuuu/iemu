package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.core.DeviceStateService
import io.github.malczuuu.iemu.core.DeviceStateUpdate
import org.eclipse.leshan.client.resource.BaseInstanceEnabler
import org.eclipse.leshan.client.servers.ServerIdentity
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.node.LwM2mResource
import org.eclipse.leshan.core.response.ReadResponse
import org.eclipse.leshan.core.response.WriteResponse

class LightControlEnabler(private val state: DeviceStateService) : BaseInstanceEnabler() {

  init {
    state.onStateChange { fireResourcesChange(ON_OFF) }
    state.onDimmerChange { fireResourcesChange(DIMMER) }
    state.onTimeCounterChange { fireResourcesChange(ON_TIME) }
  }

  override fun read(identity: ServerIdentity, resourceId: Int): ReadResponse =
      when (resourceId) {
        ON_OFF -> ReadResponse.success(resourceId, state.deviceState.on)
        DIMMER -> ReadResponse.success(resourceId, state.deviceState.dimmer.toLong())
        ON_TIME -> ReadResponse.success(resourceId, state.deviceState.onTime)
        CUMULATIVE_ACTIVE_POWER -> ReadResponse.notFound()
        else -> super.read(identity, resourceId)
      }

  override fun write(
      identity: ServerIdentity,
      resourceId: Int,
      value: LwM2mResource,
  ): WriteResponse =
      when (resourceId) {
        ON_OFF -> {
          state.changeDeviceState(DeviceStateUpdate(on = value.value as Boolean))
          WriteResponse.success()
        }

        DIMMER -> {
          state.changeDeviceState(DeviceStateUpdate(dimmer = (value.value as Long).toInt()))
          WriteResponse.success()
        }

        ON_TIME -> {
          state.changeDeviceState(DeviceStateUpdate(onTime = value.value as Long))
          WriteResponse.success()
        }

        else -> super.write(identity, resourceId, value)
      }

  override fun getAvailableResourceIds(model: ObjectModel): List<Int> = SUPPORTED_RESOURCES.toList()

  companion object {
    const val OBJECT_ID: Int = 3311

    private const val ON_OFF = 5850
    private const val DIMMER = 5851
    private const val ON_TIME = 5852
    private const val CUMULATIVE_ACTIVE_POWER = 5805

    private val SUPPORTED_RESOURCES: Set<Int> = setOf(ON_OFF, DIMMER, ON_TIME)

    fun create(deviceStateService: DeviceStateService): LightControlEnabler =
        LightControlEnabler(deviceStateService)
  }
}
