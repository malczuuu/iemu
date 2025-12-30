package io.github.malczuuu.iemu.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateDeliveryMethod;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateState;

public class FirmwareDTO {

  private final byte[] file;
  private final String fileChecksum;
  private final String packageUri;
  private final FirmwareUpdateState state;
  private final FirmwareUpdateResult result;
  private final String pkgVersion;
  private final FirmwareUpdateDeliveryMethod deliveryMethod;
  private final Integer progress;

  public FirmwareDTO(
      byte[] file,
      String fileChecksum,
      String packageUri,
      FirmwareUpdateState state,
      FirmwareUpdateResult result,
      String pkgVersion,
      FirmwareUpdateDeliveryMethod deliveryMethod,
      Integer progress) {
    this.file = file;
    this.fileChecksum = fileChecksum;
    this.packageUri = packageUri;
    this.pkgVersion = pkgVersion;
    this.deliveryMethod = deliveryMethod;
    this.state = state;
    this.result = result;
    this.progress = progress;
  }

  @JsonIgnore
  public byte[] getFile() {
    return file;
  }

  public String getFileChecksum() {
    return fileChecksum;
  }

  public String getPackageUri() {
    return packageUri;
  }

  public FirmwareUpdateState getState() {
    return state;
  }

  public Integer getStateValue() {
    return state != null ? state.getValue() : null;
  }

  public FirmwareUpdateResult getResult() {
    return result;
  }

  public Integer getResultValue() {
    return result != null ? result.getValue() : null;
  }

  public String getPkgVersion() {
    return pkgVersion;
  }

  public FirmwareUpdateDeliveryMethod getDeliveryMethod() {
    return deliveryMethod;
  }

  public Integer getDeliveryMethodValue() {
    return deliveryMethod != null ? deliveryMethod.getValue() : null;
  }

  public Integer getProgress() {
    return progress;
  }
}
