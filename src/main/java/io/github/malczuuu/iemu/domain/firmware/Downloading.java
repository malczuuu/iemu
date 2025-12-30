package io.github.malczuuu.iemu.domain.firmware;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downloading implements FirmwareUpdateExecution {

  private static final Logger log = LoggerFactory.getLogger(Downloading.class);

  private final byte[] file;
  private final String packageUri;
  private final FirmwareUpdateResult result;
  private final String packageVersion;

  private final boolean managedClient;
  private HttpClient client;

  public Downloading(
      byte[] file, String packageUri, FirmwareUpdateResult result, String packageVersion) {
    this(file, packageUri, result, packageVersion, null);
  }

  public Downloading(
      byte[] file,
      String packageUri,
      FirmwareUpdateResult result,
      String packageVersion,
      HttpClient client) {
    this.file = file;
    this.packageUri = packageUri;
    this.result = result;
    this.packageVersion = packageVersion;

    this.client = client;
    managedClient = client == null;
  }

  @Override
  public FirmwareUpdateExecution execute() {
    try {
      URI uri = new URI(this.packageUri);
      if (!uri.getScheme().matches("^(http|https)$")) {
        FirmwareUpdateResult result = FirmwareUpdateResult.UNSUPPORTED_PROTOCOL;
        log.error("Schema doesn't match http|https, move into idle state with result={}", result);
        return new Idle(file, packageUri, result, packageVersion);
      }

      byte[] file = downloadFile(uri);

      if (file.length == 0) {
        return fileEmptyFailure(file);
      }

      return successfulDownloading(file);

    } catch (URISyntaxException e) {
      return onURISyntaxException();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      return onDownloadingBroke(e);
    } catch (Exception e) {
      return onUnknownException(e);
    }
  }

  private byte[] downloadFile(URI uri) throws Exception {
    if (managedClient) {
      client = new HttpClient();
      client.start();
    }
    ContentResponse response = client.GET(uri);

    byte[] file = response.getContent();

    if (managedClient) {
      client.stop();
    }
    return file;
  }

  private FirmwareUpdateExecution fileEmptyFailure(byte[] file) {
    FirmwareUpdateResult result = FirmwareUpdateResult.PACKAGE_INTEGRITY_CHECK_FAILURE;
    log.error("Read file is empty, move into idle state with result={}", result);
    return new Idle(file, packageUri, result, packageVersion);
  }

  private Downloaded successfulDownloading(byte[] file) {
    FirmwareUpdateResult result = FirmwareUpdateResult.NONE;
    log.info("Download complete, move into downloaded state with result={}", result);
    return new Downloaded(file, packageUri, result, packageVersion);
  }

  private FirmwareUpdateExecution onURISyntaxException() {
    FirmwareUpdateResult result = FirmwareUpdateResult.INVALID_URI;
    log.error("PackageURI doesn't follow URI syntax, move into idle state with result={}", result);
    return new Idle(file, packageUri, result, packageVersion);
  }

  /** It could at least attempt to retry a broken downloading a few times. */
  private FirmwareUpdateExecution onDownloadingBroke(Exception e) {
    FirmwareUpdateResult result = FirmwareUpdateResult.NONE;
    if (log.isDebugEnabled()) {
      log.error("Downloading file broke, move into idle state with result={}", result, e);
    } else {
      log.error("Downloading file broke, move into idle state with result={}", result);
    }
    return new Idle(file, packageUri, result, packageVersion);
  }

  private FirmwareUpdateExecution onUnknownException(Exception e) {
    FirmwareUpdateResult result = FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED;
    if (log.isDebugEnabled()) {
      log.error("Downloading file broke, move into idle state with result={}", result, e);
    } else {
      log.error("Downloading file broke, move into idle state with result={}", result);
    }
    return new Idle(file, packageUri, result, packageVersion);
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
    return FirmwareUpdateState.DOWNLOADING;
  }

  @Override
  public FirmwareUpdateResult getResult() {
    return result;
  }

  @Override
  public int getProgress() {
    return 0;
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
