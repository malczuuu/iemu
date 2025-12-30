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

public enum FirmwareUpdateState {
  IDLE(0),
  DOWNLOADING(1),
  DOWNLOADED(2),
  UPDATING(3);

  public static FirmwareUpdateState fromValue(Integer value) {
    if (value == null) {
      return IDLE;
    }
    return fromValue(value.intValue());
  }

  public static FirmwareUpdateState fromValue(int value) {
    for (FirmwareUpdateState state : values()) {
      if (state.getValue() == value) {
        return state;
      }
    }
    return IDLE;
  }

  private final int value;

  FirmwareUpdateState(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
