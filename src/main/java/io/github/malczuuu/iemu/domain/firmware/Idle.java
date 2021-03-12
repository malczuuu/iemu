package io.github.malczuuu.iemu.domain.firmware;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Idle implements FirmwareUpdateExecution {

  private static final Logger log = LoggerFactory.getLogger(Idle.class);

  private final byte[] file;
  private final String packageUri;
  private final FirmwareUpdateState state;
  private final FirmwareUpdateResult result;
  private final String packageVersion;

  public Idle(
      byte[] file,
      String packageUri,
      FirmwareUpdateState state,
      FirmwareUpdateResult result,
      String packageVersion) {
    this.file = file;
    this.packageUri = packageUri;
    this.state = state;
    this.result = result;
    this.packageVersion = packageVersion;
  }

  @Override
  public FirmwareUpdateExecution execute() {
    log.error("Attempting to change state from 'Idle', returning 'Idle' as well");
    return this;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public byte[] getFile() {
    return file;
  }

  @Override
  public String getPackageUri() {
    return packageUri;
  }

  @Override
  public FirmwareUpdateState getState() {
    return state;
  }

  @Override
  public FirmwareUpdateResult getResult() {
    return result;
  }

  @Override
  public int getProgress() {
    return 0;
  }

  @Override
  public String getPackageVersion() {
    return packageVersion;
  }
}
