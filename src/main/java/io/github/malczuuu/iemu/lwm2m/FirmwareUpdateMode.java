package io.github.malczuuu.iemu.lwm2m;

public enum FirmwareUpdateMode {
  PULL_ONLY(0),
  PUSH_ONLY(1),
  BOTH(2);

  public static FirmwareUpdateMode initial() {
    return BOTH;
  }

  public static FirmwareUpdateMode fromValue(Integer value) {
    if (value == null) {
      return PULL_ONLY;
    }
    return fromValue(value.intValue());
  }

  public static FirmwareUpdateMode fromValue(int value) {
    for (FirmwareUpdateMode mode : values()) {
      if (mode.getValue() == value) {
        return mode;
      }
    }
    return PULL_ONLY;
  }

  private final int value;

  FirmwareUpdateMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
