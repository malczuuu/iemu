package io.github.malczuuu.iemu.infra.lwm2m

import io.github.malczuuu.iemu.common.Config
import io.github.malczuuu.iemu.domain.FirmwareService
import io.github.malczuuu.iemu.domain.StateService
import org.eclipse.californium.core.network.config.NetworkConfig
import org.eclipse.leshan.client.californium.LeshanClientBuilder
import org.eclipse.leshan.client.`object`.Security
import org.eclipse.leshan.client.`object`.Server
import org.eclipse.leshan.client.resource.LwM2mInstanceEnabler
import org.eclipse.leshan.client.resource.ObjectsInitializer
import org.eclipse.leshan.core.LwM2mId
import org.eclipse.leshan.core.model.LwM2mModel
import org.eclipse.leshan.core.model.ObjectLoader
import org.eclipse.leshan.core.model.ObjectModel
import org.eclipse.leshan.core.model.StaticModel
import org.eclipse.leshan.core.request.BindingMode
import org.eclipse.leshan.core.util.Hex

class LwM2mClient(
    private val config: Config.LwM2m,
    private val stateService: StateService,
    private val firmwareService: FirmwareService,
) {

  private val serverURI: String =
      (if (config.useSecureMode()) "coaps://" else "coap://") + config.upstream
  private val identity: ByteArray = config.security.identity?.toByteArray() ?: ByteArray(0)
  private val psk: ByteArray =
      config.security.psk?.let { Hex.decodeHex(it.toCharArray()) } ?: ByteArray(0)

  fun start() {
    val models = loadModels()
    val initializer = ObjectsInitializer(models)
    val builder = LeshanClientBuilder(config.endpoint)
    builder.setLocalAddress("0.0.0.0", config.localPort)
    if (config.useBootstrap()) {
      setupSecurityAndServerWithBootstrap(initializer)
    } else {
      setupSecurityAndServerWithoutBootstrap(initializer)
    }

    initializer.setFactoryForObject(DeviceEnabler.OBJECT_ID) { model, id, _ ->
      createDeviceEnabler(model, id)
    }

    initializer.setFactoryForObject(FirmwareUpdateEnabler.OBJECT_ID) { model, id, _ ->
      createFirmwareEnabler(model, id)
    }

    initializer.setFactoryForObject(LightControlEnabler.OBJECT_ID) { model, id, _ ->
      createLightControlEnabler(model, id)
    }

    initializer.setInstancesForObject(DeviceEnabler.OBJECT_ID, DeviceEnabler(stateService))

    initializer.setInstancesForObject(
        FirmwareUpdateEnabler.OBJECT_ID,
        FirmwareUpdateEnabler(firmwareService),
    )

    initializer.setInstancesForObject(
        LightControlEnabler.OBJECT_ID,
        LightControlEnabler(stateService),
    )

    val objects =
        initializer.create(
            LwM2mId.SECURITY,
            LwM2mId.SERVER,
            DeviceEnabler.OBJECT_ID,
            FirmwareUpdateEnabler.OBJECT_ID,
            LightControlEnabler.OBJECT_ID,
        )

    builder.setObjects(objects)
    builder.setCoapConfig(NetworkConfig().set(NetworkConfig.Keys.EXCHANGE_LIFETIME, 15000))

    val client = builder.build()

    Runtime.getRuntime().addShutdownHook(Thread { client.destroy(true) })

    client.start()
  }

  private fun createDeviceEnabler(model: ObjectModel, id: Int): LwM2mInstanceEnabler =
      DeviceEnabler(stateService).also {
        it.id = id
        it.model = model
      }

  private fun createFirmwareEnabler(model: ObjectModel, id: Int): LwM2mInstanceEnabler =
      FirmwareUpdateEnabler(firmwareService).also {
        it.id = id
        it.model = model
      }

  private fun createLightControlEnabler(model: ObjectModel, id: Int): LwM2mInstanceEnabler =
      LightControlEnabler(stateService).also {
        it.id = id
        it.model = model
      }

  private fun loadModels(): LwM2mModel {
    val models = ObjectLoader.loadDefault()
    models.addAll(ObjectLoader.loadDdfResources("/models", MODEL_PATHS))
    return StaticModel(models)
  }

  private fun setupSecurityAndServerWithBootstrap(initializer: ObjectsInitializer) {
    if (config.useSecureMode()) {
      initializer.setInstancesForObject(
          LwM2mId.SECURITY,
          Security.pskBootstrap(serverURI, identity, psk),
      )
    } else {
      initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSecBootstap(serverURI))
    }
    initializer.setClassForObject(LwM2mId.SERVER, Server::class.java)
  }

  private fun setupSecurityAndServerWithoutBootstrap(initializer: ObjectsInitializer) {
    if (config.useSecureMode()) {
      initializer.setInstancesForObject(
          LwM2mId.SECURITY,
          Security.psk(serverURI, 0, identity, psk),
      )
    } else {
      initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(serverURI, 0))
    }
    initializer.setInstancesForObject(LwM2mId.SERVER, Server(0, 60, BindingMode.U, false))
  }

  companion object {
    private val MODEL_PATHS: Array<String> = arrayOf("3311.xml")
  }
}
