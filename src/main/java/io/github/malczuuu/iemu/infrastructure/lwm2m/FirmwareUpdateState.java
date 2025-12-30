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
