package io.github.malczuuu.iemu.domain.firmware;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Updating implements FirmwareUpdateExecution {

  private static final Logger log = LoggerFactory.getLogger(Updating.class);

  private final byte[] file;
  private final String packageUri;
  private final FirmwareUpdateState state;
  private final FirmwareUpdateResult result;
  private final String packageVersion;

  private final Random random;
  private final int progress;

  public Updating(
      byte[] file,
      String packageUri,
      FirmwareUpdateState state,
      FirmwareUpdateResult result,
      String packageVersion) {
    this(file, packageUri, state, result, packageVersion, new Random(), 0);
  }

  private Updating(
      byte[] file,
      String packageUri,
      FirmwareUpdateState state,
      FirmwareUpdateResult result,
      String packageVersion,
      Random random,
      int progress) {
    this.file = file;
    this.packageUri = packageUri;
    this.state = state;
    this.result = result;
    this.packageVersion = packageVersion;
    this.random = random;
    this.progress = progress;
  }

  public Updating(FirmwareUpdateExecution firmware) {
    this(
        firmware.getFile(),
        firmware.getPackageUri(),
        FirmwareUpdateState.UPDATING,
        FirmwareUpdateResult.NONE,
        firmware.getPackageVersion());
  }

  @Override
  public FirmwareUpdateExecution execute() {
    if (progress < 100) {
      int progress = this.progress + 10 + random.nextInt(20);
      progress = Math.min(100, progress);
      log.info("Installation progress at {}%, keep 'Installing' state", progress);
      return new Updating(file, packageUri, state, result, packageVersion, random, progress);
    }

    String packageVersion = new String(file).split("\n")[0];
    FirmwareUpdateResult result = FirmwareUpdateResult.SUCCESSFUL;
    log.info(
        "Installation of package {} finished, move to 'Idle' state with {} result",
        packageVersion,
        result);
    return new Idle(file, packageUri, FirmwareUpdateState.IDLE, result, packageVersion);
  }

  @Override
  public byte[] getFile() {
    return file;
  }

  @Override
  public String getPackageUri() {
    return packageUri;
  }

  @Override
  public FirmwareUpdateState getState() {
    return state;
  }

  @Override
  public FirmwareUpdateResult getResult() {
    return result;
  }

  @Override
  public int getProgress() {
    return progress;
  }

  @Override
  public String getPackageVersion() {
    return packageVersion;
  }

  @Override
  public boolean hasNext() {
    return true;
  }
}
