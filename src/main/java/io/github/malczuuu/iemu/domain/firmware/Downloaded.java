package io.github.malczuuu.iemu.domain.firmware;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downloaded implements FirmwareUpdateExecution {

  private static final Logger log = LoggerFactory.getLogger(Downloaded.class);

  private final byte[] file;
  private final String packageUri;
  private final FirmwareUpdateState state;
  private final FirmwareUpdateResult result;
  private final String packageVersion;

  public Downloaded(
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
    log.error("Attempting to change state from 'Downloaded', returning 'Downloaded' as well");
    return this;
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

  @Override
  public boolean hasNext() {
    return false;
  }
}
