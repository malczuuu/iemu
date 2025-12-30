package io.github.malczuuu.iemu.infrastructure.lwm2m;

import io.github.malczuuu.iemu.domain.StateDTO;
import io.github.malczuuu.iemu.domain.StateService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

public class LightControlEnabler extends BaseInstanceEnabler {

  public static LightControlEnabler create(StateService stateService) {
    LightControlEnabler lightControl = new LightControlEnabler(stateService);

    stateService.subscribeOnStateChange(ignored -> lightControl.fireResourcesChange(ON_OFF));
    stateService.subscribeOnDimmerChange(ignored -> lightControl.fireResourcesChange(DIMMER));
    stateService.subscribeOnTimeCounterChange(ignored -> lightControl.fireResourcesChange(ON_TIME));

    return lightControl;
  }

  public static final int OBJECT_ID = 3311;

  private static final int ON_OFF = 5850;
  private static final int DIMMER = 5851;
  private static final int ON_TIME = 5852;
  private static final int CUMULATIVE_ACTIVE_POWER = 5805;
  private static final int POWER_FACTOR = 5820;
  private static final int COLOUR = 5706;
  private static final int SENSOR_UNITS = 5701;
  private static final int APPLICATION_TYPE = 5750;

  private static final Set<Integer> SUPPORTED_RESOURCES =
      new HashSet<>(Arrays.asList(ON_OFF, DIMMER, ON_TIME));

  private final StateService state;

  private LightControlEnabler(StateService state) {
    this.state = state;
  }

  @Override
  public ReadResponse read(ServerIdentity identity, int resourceId) {
    switch (resourceId) {
      case ON_OFF:
        return ReadResponse.success(resourceId, state.getState().getOn());
      case DIMMER:
        return ReadResponse.success(resourceId, state.getState().getDimmer());
      case ON_TIME:
        return ReadResponse.success(resourceId, state.getState().getOnTime());
      case CUMULATIVE_ACTIVE_POWER:
        return ReadResponse.notFound();
    }
    return super.read(identity, resourceId);
  }

  @Override
  public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
    switch (resourceId) {
      case ON_OFF:
        state.changeState(StateDTO.changeOn((Boolean) value.getValue()));
        break;
      case DIMMER:
        state.changeState(StateDTO.changeDimmer(((Long) value.getValue()).intValue()));
        break;
      case ON_TIME:
        state.changeState(StateDTO.changeOnTime(((Long) value.getValue()).intValue()));
        break;
      default:
        return super.write(identity, resourceId, value);
    }
    return WriteResponse.success();
  }

  @Override
  public List<Integer> getAvailableResourceIds(ObjectModel model) {
    return new ArrayList<>(SUPPORTED_RESOURCES);
  }
}
