package io.github.malczuuu.iemu.lwm2m;

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
