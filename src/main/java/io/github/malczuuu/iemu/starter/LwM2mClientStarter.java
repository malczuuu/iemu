package io.github.malczuuu.iemu.starter;

import io.github.malczuuu.iemu.common.config.LwM2mConfig;
import io.github.malczuuu.iemu.domain.FirmwareService;
import io.github.malczuuu.iemu.domain.StateService;
import io.github.malczuuu.iemu.lwm2m.DeviceEnabler;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateEnabler;
import io.github.malczuuu.iemu.lwm2m.LightControlEnabler;
import java.util.List;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.LwM2mInstanceEnabler;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.util.Hex;

public class LwM2mClientStarter implements Runnable {

  private static final String[] modelPaths = new String[] {"3311.xml"};

  private final LwM2mConfig config;
  private final StateService stateService;
  private final FirmwareService firmwareService;

  private final String serverURI;
  private final byte[] identity;
  private final byte[] psk;

  public LwM2mClientStarter(
      LwM2mConfig config, StateService stateService, FirmwareService firmwareService) {
    this.config = config;
    this.stateService = stateService;
    this.firmwareService = firmwareService;

    serverURI = (config.useSecureMode() ? "coaps://" : "coap://") + config.getUpstream();
    identity =
        config.getSecurity().getIdentity() != null
            ? config.getSecurity().getIdentity().getBytes()
            : new byte[0];
    psk =
        config.getSecurity().getPsk() != null
            ? Hex.decodeHex(config.getSecurity().getPsk().toCharArray())
            : new byte[0];
  }

  @Override
  public void run() {
    LwM2mModel models = loadModels();
    ObjectsInitializer initializer = new ObjectsInitializer(models);
    LeshanClientBuilder builder = new LeshanClientBuilder(config.getEndpoint());
    builder.setLocalAddress("0.0.0.0", config.getLocalPort());
    if (config.useBootstrap()) {
      setupSecurityAndServerWithBootstrap(initializer);
    } else {
      setupSecurityAndServerWithoutBootstrap(initializer);
    }

    initializer.setFactoryForObject(
        DeviceEnabler.OBJECT_ID,
        (model, id, alreadyUsedIdentifier) -> createDeviceEnabler(model, id));
    initializer.setFactoryForObject(
        FirmwareUpdateEnabler.OBJECT_ID,
        (model, id, alreadyUsedIdentifier) -> createFirmwareEnabler(model, id));
    initializer.setFactoryForObject(
        LightControlEnabler.OBJECT_ID,
        (model, id, alreadyUsedIdentifier) -> createLightControlEnabler(model, id));

    initializer.setInstancesForObject(DeviceEnabler.OBJECT_ID, DeviceEnabler.create(stateService));
    initializer.setInstancesForObject(
        FirmwareUpdateEnabler.OBJECT_ID, FirmwareUpdateEnabler.create(firmwareService));
    initializer.setInstancesForObject(
        LightControlEnabler.OBJECT_ID, LightControlEnabler.create(stateService));

    List<LwM2mObjectEnabler> objects =
        initializer.create(
            LwM2mId.SECURITY,
            LwM2mId.SERVER,
            DeviceEnabler.OBJECT_ID,
            FirmwareUpdateEnabler.OBJECT_ID,
            LightControlEnabler.OBJECT_ID);

    builder.setObjects(objects);
    builder.setCoapConfig(new NetworkConfig().set(NetworkConfig.Keys.EXCHANGE_LIFETIME, 15000));

    LeshanClient client = builder.build();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> client.destroy(true)));

    client.start();
  }

  private LwM2mInstanceEnabler createDeviceEnabler(ObjectModel model, Integer id) {
    DeviceEnabler enabler = DeviceEnabler.create(stateService);
    enabler.setId(id);
    enabler.setModel(model);
    return enabler;
  }

  private LwM2mInstanceEnabler createFirmwareEnabler(ObjectModel model, Integer id) {
    FirmwareUpdateEnabler enabler = FirmwareUpdateEnabler.create(firmwareService);
    enabler.setId(id);
    enabler.setModel(model);
    return enabler;
  }

  private LwM2mInstanceEnabler createLightControlEnabler(ObjectModel model, Integer id) {
    LightControlEnabler enabler = LightControlEnabler.create(stateService);
    enabler.setId(id);
    enabler.setModel(model);
    return enabler;
  }

  private LwM2mModel loadModels() {
    List<ObjectModel> models = ObjectLoader.loadDefault();
    models.addAll(ObjectLoader.loadDdfResources("/models", modelPaths));
    return new StaticModel(models);
  }

  private void setupSecurityAndServerWithBootstrap(ObjectsInitializer initializer) {
    if (config.useSecureMode()) {
      initializer.setInstancesForObject(
          LwM2mId.SECURITY, Security.pskBootstrap(serverURI, identity, psk));
    } else {
      initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSecBootstap(serverURI));
    }
    initializer.setClassForObject(LwM2mId.SERVER, Server.class);
  }

  private void setupSecurityAndServerWithoutBootstrap(ObjectsInitializer initializer) {
    if (config.useSecureMode()) {
      initializer.setInstancesForObject(
          LwM2mId.SECURITY, Security.psk(serverURI, 0, identity, psk));
    } else {
      initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(serverURI, 0));
    }
    initializer.setInstancesForObject(LwM2mId.SERVER, new Server(0, 60, BindingMode.U, false));
  }
}
