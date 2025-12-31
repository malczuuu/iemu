/*
 * Copyright (c) 2025 Damian Malczewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * SPDX-License-Identifier: MIT
 */
package io.github.malczuuu.iemu.infrastructure.lwm2m;

import io.github.malczuuu.iemu.domain.FirmwareDto;
import io.github.malczuuu.iemu.domain.FirmwareService;
import java.util.List;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
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
    service.subscribeOnPackageUriChange(ign -> firmware.fireResourcesChange(PACKAGE_URI));
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
      List.of(FILE, PACKAGE_URI, UPDATE_ACTION, STATE, UPDATE_RESULT, PACKAGE_VERSION, MODE);

  private final FirmwareService firmwareService;

  private FirmwareUpdateEnabler(FirmwareService firmwareService) {
    this.firmwareService = firmwareService;
  }

  @Override
  public ReadResponse read(ServerIdentity identity, int resourceId) {
    log.debug(
        "Received read request to Firmware instanceId={} to resourceId={}", getId(), resourceId);
    return switch (resourceId) {
      case PACKAGE_URI ->
          ReadResponse.success(resourceId, firmwareService.getFirmware().getPackageUri());
      case STATE ->
          ReadResponse.success(resourceId, firmwareService.getFirmware().getState().getValue());
      case UPDATE_RESULT ->
          ReadResponse.success(resourceId, firmwareService.getFirmware().getResult().getValue());
      case PACKAGE_VERSION ->
          ReadResponse.success(resourceId, firmwareService.getFirmware().getPkgVersion());
      case MODE ->
          ReadResponse.success(
              resourceId, firmwareService.getFirmware().getDeliveryMethod().getValue());
      default -> super.read(identity, resourceId);
    };
  }

  @Override
  public ReadResponse read(ServerIdentity identity) {
    log.debug("Received read request to Firmware instanceId={}", getId());
    FirmwareDto firmware = firmwareService.getFirmware();
    return ReadResponse.success(
        new LwM2mObjectInstance(
            getId(),
            LwM2mSingleResource.newStringResource(PACKAGE_URI, firmware.getPackageUri()),
            LwM2mSingleResource.newIntegerResource(STATE, firmware.getState().getValue()),
            LwM2mSingleResource.newIntegerResource(UPDATE_RESULT, firmware.getResult().getValue()),
            LwM2mSingleResource.newStringResource(PACKAGE_VERSION, firmware.getPkgVersion()),
            LwM2mSingleResource.newIntegerResource(MODE, firmware.getDeliveryMethod().getValue())));
  }

  @Override
  public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
    log.debug(
        "Received write request to Firmware instanceId={} to resourceId={}, value={}",
        getId(),
        resourceId,
        value);
    return switch (resourceId) {
      case FILE -> {
        firmwareService.changeFirmware(
            new FirmwareDto((byte[]) value.getValue(), null, null, null, null, null, null, null));
        yield WriteResponse.success();
      }
      case PACKAGE_URI -> {
        firmwareService.changeFirmware(
            new FirmwareDto(null, null, (String) value.getValue(), null, null, null, null, null));
        yield WriteResponse.success();
      }
      default -> super.write(identity, resourceId, value);
    };
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
