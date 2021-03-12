package io.github.malczuuu.iemu.lwm2m;

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
