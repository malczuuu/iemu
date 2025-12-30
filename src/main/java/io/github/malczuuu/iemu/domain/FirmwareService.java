package io.github.malczuuu.iemu.domain;

import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.infrastructure.lwm2m.FirmwareUpdateState;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public interface FirmwareService {

  FirmwareDTO getFirmware();

  void changeFirmware(FirmwareDTO firmware);

  void executeFirmwareUpdate();

  void subscribeOnFileChange(Consumer<byte[]> consumer);

  void subscribeOnPackageUriChange(Consumer<String> consumer);

  void subscribeOnStateChange(Consumer<FirmwareUpdateState> consumer);

  void subscribeOnResultChange(Consumer<FirmwareUpdateResult> consumer);

  void subscribeOnPackageVersionChange(Consumer<String> consumer);

  void subscribeOnProgressChange(IntConsumer consumer);
}
