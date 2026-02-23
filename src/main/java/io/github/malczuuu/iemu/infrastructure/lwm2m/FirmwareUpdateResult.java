/*
 * Copyright (c) 2025-2026 Damian Malczewski
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.malczuuu.iemu.infrastructure.lwm2m;

public enum FirmwareUpdateResult {
  NONE(0),
  SUCCESSFUL(1),
  FLASH_MEMORY_ERROR(2),
  OUT_OF_RAM(3),
  DOWNLOADING_CONNECTION_LOST(4),
  PACKAGE_INTEGRITY_CHECK_FAILURE(5),
  UNSUPPORTED_PACKAGE_TYPE(6),
  INVALID_URI(7),
  FIRMWARE_UPDATE_FAILED(8),
  UNSUPPORTED_PROTOCOL(9);

  public static FirmwareUpdateResult initial() {
    return NONE;
  }

  public static FirmwareUpdateResult fromValue(Integer value) {
    if (value == null) {
      return NONE;
    }
    return fromValue(value.intValue());
  }

  public static FirmwareUpdateResult fromValue(int value) {
    for (FirmwareUpdateResult mode : values()) {
      if (mode.getValue() == value) {
        return mode;
      }
    }
    return NONE;
  }

  private final int value;

  FirmwareUpdateResult(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
