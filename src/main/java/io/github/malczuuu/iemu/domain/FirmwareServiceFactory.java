package io.github.malczuuu.iemu.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FirmwareServiceFactory {

  private final ScheduledExecutorService scheduler;
  private final MessageDigest digest;

  public FirmwareServiceFactory() {
    this(Executors.newSingleThreadScheduledExecutor());
  }

  private FirmwareServiceFactory(ScheduledExecutorService scheduler) {
    this(scheduler, initMessageDigest());
  }

  public FirmwareServiceFactory(ScheduledExecutorService scheduler, MessageDigest digest) {
    this.scheduler = scheduler;
    this.digest = digest;
  }

  public FirmwareService getFirmwareService() {
    FirmwareServiceImpl firmwareService = new FirmwareServiceImpl(scheduler, digest);
    firmwareService.initialize();
    return firmwareService;
  }

  private static MessageDigest initMessageDigest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
