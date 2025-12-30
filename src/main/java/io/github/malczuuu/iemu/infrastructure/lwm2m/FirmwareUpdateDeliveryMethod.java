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

public enum FirmwareUpdateDeliveryMethod {
  PULL_ONLY(0),
  PUSH_ONLY(1),
  BOTH(2);

  public static FirmwareUpdateDeliveryMethod initial() {
    return BOTH;
  }

  public static FirmwareUpdateDeliveryMethod fromValue(Integer value) {
    if (value == null) {
      return PULL_ONLY;
    }
    return fromValue(value.intValue());
  }

  public static FirmwareUpdateDeliveryMethod fromValue(int value) {
    for (FirmwareUpdateDeliveryMethod mode : values()) {
      if (mode.getValue() == value) {
        return mode;
      }
    }
    return PULL_ONLY;
  }

  private final int value;

  FirmwareUpdateDeliveryMethod(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
