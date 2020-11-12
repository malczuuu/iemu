package io.github.malczuuu.iemu.lwm2m;

import io.github.malczuuu.iemu.domain.ErrorDTO;
import io.github.malczuuu.iemu.domain.StateDTO;
import io.github.malczuuu.iemu.domain.StateService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

@Getter
@Setter
public class DeviceEnabler extends BaseInstanceEnabler {

  public static DeviceEnabler create(StateService deviceService) {
    DeviceEnabler device = new DeviceEnabler(deviceService);
    deviceService.subscribeOnCurrentTimeChange(ignored -> device.fireResourcesChange(CURRENT_TIME));
    return device;
  }

  public static final int OBJECT_ID = 3;

  private static final int MANUFACTURER = 0;
  private static final int MODEL_NUMBER = 1;
  private static final int SERIAL_NUMBER = 2;
  private static final int FIRMWARE_VERSION = 3;
  private static final int REBOOT = 4;
  private static final int FACTORY_RESET = 5;
  private static final int AVAILABLE_POWER_SOURCES = 6;
  private static final int POWER_SOURCE_VOLTAGE = 7;
  private static final int POWER_SOURCE_CURRENT = 8;
  private static final int BATTERY_LEVEL = 9;
  private static final int MEMORY_FREE = 10;
  private static final int ERROR_CODE = 11;
  private static final int RESET_ERROR_CODE = 12;
  private static final int CURRENT_TIME = 13;
  private static final int UTC_OFFSET = 14;
  private static final int TIMEZONE = 15;
  private static final int SUPPORTED_BINDINGS_AND_MODES = 16;
  private static final int DEVICE_TYPE = 17;
  private static final int HARDWARE_VERSION = 18;
  private static final int SOFTWARE_VERSION = 19;
  private static final int BATTERY_STATUS = 20;
  private static final int MEMORY_TOTAL = 21;
  private static final int EXT_DEV_INFO = 22;

  private static final List<Integer> SUPPORTED_RESOURCES =
      Arrays.asList(
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
          MEMORY_TOTAL);

  private final StateService state;

  private DeviceEnabler(StateService state) {
    this.state = state;
  }

  @Override
  public ReadResponse read(ServerIdentity identity, int resourceId) {
    switch (resourceId) {
      case MANUFACTURER:
        return ReadResponse.success(resourceId, "Manufacturer");
      case MODEL_NUMBER:
        return ReadResponse.success(resourceId, "0");
      case SERIAL_NUMBER:
        return ReadResponse.success(resourceId, "0");
      case FIRMWARE_VERSION:
        return ReadResponse.success(resourceId, "0.0.0");
      case BATTERY_LEVEL:
        return ReadResponse.success(resourceId, 100);
      case MEMORY_FREE:
        return ReadResponse.success(resourceId, Runtime.getRuntime().freeMemory() / 1000);
      case ERROR_CODE:
        Map<Integer, Long> errorCodes = new HashMap<>();
        List<ErrorDTO> errors = state.getState().getErrors();
        for (int i = 0; i < errors.size(); ++i) {
          errorCodes.put(i, errors.get(i).getCode().longValue());
        }
        return ReadResponse.success(resourceId, errorCodes, Type.INTEGER);
      case CURRENT_TIME:
        return ReadResponse.success(
            resourceId, Date.from(Instant.parse(state.getState().getCurrentTime())));
      case UTC_OFFSET:
        return ReadResponse.success(resourceId, state.getState().getUTCOffset());
      case TIMEZONE:
        return ReadResponse.success(resourceId, state.getState().getTimeZone());
      case SUPPORTED_BINDINGS_AND_MODES:
        return ReadResponse.success(resourceId, "U");
      case DEVICE_TYPE:
        return ReadResponse.success(resourceId, state.getState().getDeviceType());
      case HARDWARE_VERSION:
        return ReadResponse.success(resourceId, "0.0.0");
      case SOFTWARE_VERSION:
        return ReadResponse.success(resourceId, "0.0.0");
      case BATTERY_STATUS:
        return ReadResponse.success(resourceId, 100);
      case MEMORY_TOTAL:
        return ReadResponse.success(resourceId, Runtime.getRuntime().totalMemory() / 1000);
    }
    return super.read(identity, resourceId);
  }

  @Override
  public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
    if (resourceId == CURRENT_TIME) {
      state.changeState(
          StateDTO.changeCurrentTime(
              ((Date) value.getValue()).toInstant().truncatedTo(ChronoUnit.SECONDS).toString()));
      return WriteResponse.success();
    }
    return super.write(identity, resourceId, value);
  }

  @Override
  public ExecuteResponse execute(ServerIdentity identity, int resourceId, String params) {
    switch (resourceId) {
      case REBOOT:
        return ExecuteResponse.success();
      case FACTORY_RESET:
        return ExecuteResponse.success();
      case RESET_ERROR_CODE:
        state.resetErrors();
        return ExecuteResponse.success();
    }
    return super.execute(identity, resourceId, params);
  }

  @Override
  public List<Integer> getAvailableResourceIds(ObjectModel model) {
    return SUPPORTED_RESOURCES;
  }
}
