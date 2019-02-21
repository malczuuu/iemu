package io.github.malczuuu.iemu.starter;

import io.github.malczuuu.iemu.common.config.LwM2mConfig;
import io.github.malczuuu.iemu.domain.StateService;
import io.github.malczuuu.iemu.lwm2m.DeviceEnabler;
import io.github.malczuuu.iemu.lwm2m.LightControlEnabler;
import io.github.malczuuu.iemu.lwm2m.SingularObjectEnabler;
import java.util.List;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.leshan.LwM2mId;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.util.Hex;

public class LwM2mClientStarter implements Runnable {

  private static final String[] modelPaths = new String[] {"3311.xml"};

  private final LwM2mConfig config;
  private final StateService stateService;

  private final String serverURI;
  private final byte[] identity;
  private final byte[] psk;

  public LwM2mClientStarter(LwM2mConfig config, StateService stateService) {
    this.config = config;
    this.stateService = stateService;

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

    initializer.setInstancesForObject(DeviceEnabler.OBJECT_ID, DeviceEnabler.create(stateService));
    initializer.setInstancesForObject(
        LightControlEnabler.OBJECT_ID, LightControlEnabler.create(stateService));

    List<LwM2mObjectEnabler> objects =
        initializer.create(LwM2mId.SECURITY, LwM2mId.SERVER, DeviceEnabler.OBJECT_ID);
    objects.add(
        SingularObjectEnabler.create(
            LightControlEnabler.OBJECT_ID,
            models,
            ignored -> LightControlEnabler.create(stateService)));

    builder.setObjects(objects);
    builder.setCoapConfig(new NetworkConfig());

    LeshanClient client = builder.build();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> client.destroy(true)));

    client.start();
  }

  private LwM2mModel loadModels() {
    List<ObjectModel> models = ObjectLoader.loadDefault();
    models.addAll(ObjectLoader.loadDdfResources("/models", modelPaths));
    return new LwM2mModel(models);
  }

  private void setupSecurityAndServerWithBootstrap(ObjectsInitializer initializer) {
    if (config.useSecureMode()) {
      initializer.setInstancesForObject(
          LwM2mId.SECURITY, Security.pskBootstrap(serverURI, identity, psk));
      initializer.setClassForObject(LwM2mId.SERVER, Server.class);
    } else {
      initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSecBootstap(serverURI));
      initializer.setClassForObject(LwM2mId.SERVER, Server.class);
    }
  }

  private void setupSecurityAndServerWithoutBootstrap(ObjectsInitializer initializer) {
    if (config.useSecureMode()) {
      initializer.setInstancesForObject(
          LwM2mId.SECURITY, Security.psk(serverURI, 0, identity, psk));
      initializer.setInstancesForObject(LwM2mId.SERVER, new Server(0, 30, BindingMode.U, false));
    } else {
      initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(serverURI, 0));
      initializer.setInstancesForObject(LwM2mId.SERVER, new Server(0, 30, BindingMode.U, false));
    }
  }
}
