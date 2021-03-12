package io.github.malczuuu.iemu.domain;

import io.github.malczuuu.iemu.domain.firmware.Downloading;
import io.github.malczuuu.iemu.domain.firmware.FirmwareUpdateExecution;
import io.github.malczuuu.iemu.domain.firmware.Idle;
import io.github.malczuuu.iemu.domain.firmware.Updating;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateMode;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.eclipse.leshan.core.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmwareServiceImpl implements FirmwareService, Initializing {

  private static final Logger log = LoggerFactory.getLogger(FirmwareServiceImpl.class);

  private final ScheduledExecutorService scheduler;
  private final MessageDigest digest;

  private FirmwareUpdateExecution firmware =
      new Idle(
          "1.0.0-SNAPSHOT".getBytes(),
          "about:blank",
          FirmwareUpdateState.initial(),
          FirmwareUpdateResult.initial(),
          "1.0.0-SNAPSHOT");

  private final FirmwareUpdateMode mode = FirmwareUpdateMode.initial();

  private final List<Consumer<byte[]>> onFileChange = new ArrayList<>();
  private final List<Consumer<String>> onUrlChange = new ArrayList<>();
  private final List<Consumer<FirmwareUpdateState>> onStateChange = new ArrayList<>();
  private final List<Consumer<FirmwareUpdateResult>> onResultChange = new ArrayList<>();
  private final List<Consumer<String>> onPackageVersionChange = new ArrayList<>();

  public FirmwareServiceImpl(ScheduledExecutorService scheduler, MessageDigest digest) {
    this.scheduler = scheduler;
    this.digest = digest;
  }

  @Override
  public void initialize() {
    scheduler.scheduleAtFixedRate(this::update, 1L, 1L, TimeUnit.SECONDS);
    log.info("Scheduler for firmware update state machine initialized");
  }

  private void update() {
    if (firmware.hasNext()) {
      FirmwareUpdateExecution previous = firmware;

      firmware = firmware.execute();

      fireOnAnythingChanged(previous);
    }
  }

  private void fireOnAnythingChanged(FirmwareUpdateExecution previousFirmware) {
    if (firmware.equals(previousFirmware)) {
      return;
    }
    if (firmware.getFile() != previousFirmware.getFile()) {
      onFileChange.forEach(c -> c.accept(firmware.getFile()));
    }
    if (!firmware.getPackageUri().equals(previousFirmware.getPackageUri())) {
      onUrlChange.forEach(c -> c.accept(firmware.getPackageUri()));
    }
    if (firmware.getState() != previousFirmware.getState()) {
      onStateChange.forEach(c -> c.accept(firmware.getState()));
    }
    if (firmware.getResult() != previousFirmware.getResult()) {
      onResultChange.forEach(c -> c.accept(firmware.getResult()));
    }
    if (!firmware.getPackageVersion().equals(previousFirmware.getPackageVersion())) {
      onPackageVersionChange.forEach(c -> c.accept(firmware.getPackageVersion()));
    }
  }

  @Override
  public FirmwareDTO getFirmware() {
    return new FirmwareDTO(
        firmware.getFile(),
        firmware.getFile() != null ? Hex.encodeHexString(digest.digest(firmware.getFile())) : null,
        firmware.getPackageUri(),
        firmware.getPackageVersion(),
        mode,
        firmware.getState(),
        firmware.getResult());
  }

  private String stringifyFile(byte[] file) {
    file = cut(file, 20);
    String result = new String(cut(file, 10));
    return result.split("\n")[0];
  }

  private byte[] cut(byte[] file, int maxLength) {
    if (file.length <= maxLength) {
      return Arrays.copyOf(file, file.length);
    } else {
      return Arrays.copyOf(file, maxLength);
    }
  }

  @Override
  public void changeFirmware(FirmwareDTO firmware) {
    if (firmware.getFile() != null) {
      this.firmware =
          new Idle(
              firmware.getFile(),
              this.firmware.getPackageUri(),
              FirmwareUpdateState.DOWNLOADED,
              this.firmware.getResult(),
              this.firmware.getPackageVersion());
      onFileChange.forEach(c -> c.accept(this.firmware.getFile()));
      log.info("Updated firmware file to {}", stringifyFile(this.firmware.getFile()));
    }
    if (firmware.getUrl() != null) {
      this.firmware =
          new Downloading(
              this.firmware.getFile(),
              firmware.getUrl(),
              this.firmware.getState(),
              this.firmware.getResult(),
              this.firmware.getPackageVersion());
      onUrlChange.forEach(c -> c.accept(this.firmware.getPackageUri()));
      log.info("Updated firmware package URI to {}", this.firmware.getPackageUri());
    }
  }

  @Override
  public void executeFirmwareUpdate() {
    firmware = new Updating(firmware);
  }

  @Override
  public void subscribeOnFileChange(Consumer<byte[]> consumer) {
    onFileChange.add(consumer);
  }

  @Override
  public void subscribeOnUrlChange(Consumer<String> consumer) {
    onUrlChange.add(consumer);
  }

  @Override
  public void subscribeOnStateChange(Consumer<FirmwareUpdateState> consumer) {
    onStateChange.add(consumer);
  }

  @Override
  public void subscribeOnResultChange(Consumer<FirmwareUpdateResult> consumer) {
    onResultChange.add(consumer);
  }

  @Override
  public void subscribeOnPackageVersionChange(Consumer<String> consumer) {
    onPackageVersionChange.add(consumer);
  }
}
