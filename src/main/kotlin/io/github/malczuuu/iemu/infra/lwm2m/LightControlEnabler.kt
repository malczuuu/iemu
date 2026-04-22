package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.domain.StateDto
import io.github.malczuuu.iemu.domain.StateService
import org.eclipse.leshan.client.resource.BaseInstanceEnabler
import org.eclipse.leshan.client.servers.ServerIdentity
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.node.LwM2mResource
import org.eclipse.leshan.core.response.ReadResponse
import org.eclipse.leshan.core.response.WriteResponse

class LightControlEnabler(private val state: StateService) : BaseInstanceEnabler() {

  init {
    state.subscribeOnStateChange { fireResourcesChange(ON_OFF) }
    state.subscribeOnDimmerChange { fireResourcesChange(DIMMER) }
    state.subscribeOnTimeCounterChange { fireResourcesChange(ON_TIME) }
  }

  override fun read(identity: ServerIdentity, resourceId: Int): ReadResponse =
      when (resourceId) {
        ON_OFF -> ReadResponse.success(resourceId, state.getState().on!!)
        DIMMER -> ReadResponse.success(resourceId, state.getState().dimmer!!.toLong())
        ON_TIME -> ReadResponse.success(resourceId, state.getState().onTime!!)
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
          state.changeState(StateDto(on = value.value as Boolean))
          WriteResponse.success()
        }
        DIMMER -> {
          state.changeState(StateDto(dimmer = (value.value as Long).toInt()))
          WriteResponse.success()
        }
        ON_TIME -> {
          state.changeState(StateDto(onTime = value.value as Long))
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

    fun create(stateService: StateService): LightControlEnabler {
      val lightControl = LightControlEnabler(stateService)
      stateService.subscribeOnStateChange { lightControl.fireResourcesChange(ON_OFF) }
      stateService.subscribeOnDimmerChange { lightControl.fireResourcesChange(DIMMER) }
      stateService.subscribeOnTimeCounterChange { lightControl.fireResourcesChange(ON_TIME) }
      return lightControl
    }
  }
}
