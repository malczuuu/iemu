package io.github.malczuuu.iemu.lwm2m;

import io.github.malczuuu.iemu.domain.FirmwareDTO;
import io.github.malczuuu.iemu.domain.FirmwareService;
import java.util.Arrays;
import java.util.List;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmwareUpdateEnabler extends BaseInstanceEnabler {

  private static final Logger log = LoggerFactory.getLogger(FirmwareUpdateEnabler.class);

  public static FirmwareUpdateEnabler create(FirmwareService service) {
    FirmwareUpdateEnabler firmware = new FirmwareUpdateEnabler(service);

    service.subscribeOnFileChange(ign -> firmware.fireResourcesChange(FILE));
    service.subscribeOnUrlChange(ign -> firmware.fireResourcesChange(PACKAGE_URI));
    service.subscribeOnStateChange(ign -> firmware.fireResourcesChange(STATE));
    service.subscribeOnResultChange(ign -> firmware.fireResourcesChange(UPDATE_RESULT));
    service.subscribeOnPackageVersionChange(ign -> firmware.fireResourcesChange(PACKAGE_VERSION));

    return firmware;
  }

  public static final int OBJECT_ID = 5;

  private static final int FILE = 0;
  private static final int PACKAGE_URI = 1;
  private static final int UPDATE_ACTION = 2;
  private static final int STATE = 3;
  private static final int UPDATE_RESULT = 5;
  private static final int PACKAGE_VERSION = 7;
  private static final int MODE = 9;

  private static final List<Integer> SUPPORTED_RESOURCES =
      Arrays.asList(FILE, PACKAGE_URI, UPDATE_ACTION, STATE, UPDATE_RESULT, PACKAGE_VERSION, MODE);

  private final FirmwareService firmwareService;

  private FirmwareUpdateEnabler(FirmwareService firmwareService) {
    this.firmwareService = firmwareService;
  }

  @Override
  public ReadResponse read(ServerIdentity identity, int resourceId) {
    log.debug(
        "Received read request to Firmware instanceId={} to resourceId={}", getId(), resourceId);
    switch (resourceId) {
      case FILE:
        return ReadResponse.success(resourceId, firmwareService.getFirmware().getFile());
      case PACKAGE_URI:
        return ReadResponse.success(resourceId, firmwareService.getFirmware().getUrl());
      case STATE:
        return ReadResponse.success(
            resourceId, firmwareService.getFirmware().getState().getValue());
      case UPDATE_RESULT:
        return ReadResponse.success(
            resourceId, firmwareService.getFirmware().getResult().getValue());
      case PACKAGE_VERSION:
        return ReadResponse.success(resourceId, firmwareService.getFirmware().getPackageVersion());
      case MODE:
        return ReadResponse.success(resourceId, firmwareService.getFirmware().getMode().getValue());
      default:
        return super.read(identity, resourceId);
    }
  }

  @Override
  public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
    log.debug(
        "Received write request to Firmware instanceId={} to resourceId={}, value={}",
        getId(),
        resourceId,
        value);
    switch (resourceId) {
      case FILE:
        firmwareService.changeFirmware(
            new FirmwareDTO((byte[]) value.getValue(), null, null, null, null, null, null));
        return WriteResponse.success();
      case PACKAGE_URI:
        firmwareService.changeFirmware(
            new FirmwareDTO(null, null, (String) value.getValue(), null, null, null, null));
        return WriteResponse.success();
      default:
        return super.write(identity, resourceId, value);
    }
  }

  @Override
  public ExecuteResponse execute(ServerIdentity identity, int resourceId, String params) {
    if (resourceId == UPDATE_ACTION) {
      firmwareService.executeFirmwareUpdate();
      return ExecuteResponse.success();
    }
    return super.execute(identity, resourceId, params);
  }

  @Override
  public List<Integer> getAvailableResourceIds(ObjectModel model) {
    return SUPPORTED_RESOURCES;
  }
}
