package io.github.malczuuu.iemu.domain;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateMode;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;

public class FirmwareDTO {

  private final byte[] file;
  private final String fileChecksum;
  private final String url;
  private final String packageVersion;
  private final FirmwareUpdateMode mode;
  private final FirmwareUpdateState state;
  private final FirmwareUpdateResult result;

  public FirmwareDTO(
      byte[] file,
      String fileChecksum,
      String url,
      String packageVersion,
      FirmwareUpdateMode mode,
      FirmwareUpdateState state,
      FirmwareUpdateResult result) {
    this.file = file;
    this.fileChecksum = fileChecksum;
    this.url = url;
    this.packageVersion = packageVersion;
    this.mode = mode;
    this.state = state;
    this.result = result;
  }

  public byte[] getFile() {
    return file;
  }

  public String getFileChecksum() {
    return fileChecksum;
  }

  public String getUrl() {
    return url;
  }

  public String getPackageVersion() {
    return packageVersion;
  }

  public FirmwareUpdateMode getMode() {
    return mode;
  }

  public FirmwareUpdateState getState() {
    return state;
  }

  public FirmwareUpdateResult getResult() {
    return result;
  }
}
