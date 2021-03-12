package io.github.malczuuu.iemu.domain.firmware;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;

public interface FirmwareUpdateExecution {

  FirmwareUpdateExecution execute();

  String getPackageVersion();

  boolean hasNext();

  byte[] getFile();

  String getPackageUri();

  FirmwareUpdateState getState();

  FirmwareUpdateResult getResult();
}
