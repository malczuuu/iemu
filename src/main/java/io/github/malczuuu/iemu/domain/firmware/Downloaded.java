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
package io.github.malczuuu.iemu.domain.firmware;

import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downloaded implements FirmwareUpdateExecution {

  private static final Logger log = LoggerFactory.getLogger(Downloaded.class);

  private final byte[] file;
  private final String packageUri;
  private final FirmwareUpdateResult result;
  private final String packageVersion;

  public Downloaded(
      byte[] file, String packageUri, FirmwareUpdateResult result, String packageVersion) {
    this.file = file;
    this.packageUri = packageUri;
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
    return FirmwareUpdateState.DOWNLOADED;
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
