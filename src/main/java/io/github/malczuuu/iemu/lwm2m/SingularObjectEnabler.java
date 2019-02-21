package io.github.malczuuu.iemu.lwm2m;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.leshan.client.resource.LwM2mInstanceEnabler;
import org.eclipse.leshan.client.resource.LwM2mInstanceEnablerFactory;
import org.eclipse.leshan.client.resource.ObjectEnabler;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.request.DeleteRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.core.response.DeleteResponse;

public class SingularObjectEnabler extends ObjectEnabler {

  public static SingularObjectEnabler create(
      int id, LwM2mModel model, LwM2mInstanceEnablerFactory instanceFactory) {
    Map<Integer, LwM2mInstanceEnabler> instances = new HashMap<>();
    ObjectModel objectModel = model.getObjectModel(id);
    instances.put(0, instanceFactory.create(objectModel));
    return new SingularObjectEnabler(
        id,
        objectModel,
        Collections.unmodifiableMap(instances),
        instanceFactory,
        ContentFormat.DEFAULT);
  }

  private SingularObjectEnabler(
      int id,
      ObjectModel objectModel,
      Map<Integer, LwM2mInstanceEnabler> instances,
      LwM2mInstanceEnablerFactory instanceFactory,
      ContentFormat defaultContentFormat) {
    super(id, objectModel, instances, instanceFactory, defaultContentFormat);
  }

  @Override
  protected CreateResponse doCreate(CreateRequest request) {
    return CreateResponse.methodNotAllowed();
  }

  @Override
  protected DeleteResponse doDelete(DeleteRequest request) {
    return DeleteResponse.methodNotAllowed();
  }
}
