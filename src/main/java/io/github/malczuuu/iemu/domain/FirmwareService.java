package io.github.malczuuu.iemu.domain;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;
import java.util.function.Consumer;

public interface FirmwareService {

  FirmwareDTO getFirmware();

  void changeFirmware(FirmwareDTO firmware);

  void executeFirmwareUpdate();

  void subscribeOnFileChange(Consumer<byte[]> consumer);

  void subscribeOnUrlChange(Consumer<String> consumer);

  void subscribeOnStateChange(Consumer<FirmwareUpdateState> consumer);

  void subscribeOnResultChange(Consumer<FirmwareUpdateResult> consumer);

  void subscribeOnPackageVersionChange(Consumer<String> consumer);
}
