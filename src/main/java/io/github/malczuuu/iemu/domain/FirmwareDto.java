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
package io.github.malczuuu.iemu.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateDeliveryMethod;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateState;

public class FirmwareDto {

  private final byte[] file;
  private final String fileChecksum;
  private final String packageUri;
  private final FirmwareUpdateState state;
  private final FirmwareUpdateResult result;
  private final String pkgVersion;
  private final FirmwareUpdateDeliveryMethod deliveryMethod;
  private final Integer progress;

  public FirmwareDto(
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
