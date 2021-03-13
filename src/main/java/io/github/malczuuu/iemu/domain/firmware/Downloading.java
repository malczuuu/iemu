package io.github.malczuuu.iemu.domain.firmware;

import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateResult;
import io.github.malczuuu.iemu.lwm2m.FirmwareUpdateState;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downloading implements FirmwareUpdateExecution {

  private static final Logger log = LoggerFactory.getLogger(Downloading.class);

  private final byte[] file;
  private final String packageUri;
  private final FirmwareUpdateState state;
  private final FirmwareUpdateResult result;
  private final String packageVersion;

  private final boolean managedClient;
  private HttpClient client;

  public Downloading(
      byte[] file,
      String packageUri,
      FirmwareUpdateState state,
      FirmwareUpdateResult result,
      String packageVersion) {
    this(file, packageUri, state, result, packageVersion, null);
  }

  public Downloading(
      byte[] file,
      String packageUri,
      FirmwareUpdateState state,
      FirmwareUpdateResult result,
      String packageVersion,
      HttpClient client) {
    this.file = file;
    this.packageUri = packageUri;
    this.state = state;
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
        FirmwareUpdateState state = FirmwareUpdateState.IDLE;
        FirmwareUpdateResult result = FirmwareUpdateResult.INVALID_URI;
        log.error(
            "Schema doesn't match http|https, move into state={} with result={}", state, result);
        return new Idle(file, packageUri, FirmwareUpdateState.IDLE, result, packageVersion);
      }

      byte[] file = downloadFile(uri);

      if (file.length == 0) {
        return fileEmptyFailure(file);
      }

      return successfulDownloading(file);

    } catch (URISyntaxException e) {
      return onURISyntaxException();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      return onDownloadingBroke();
    } catch (Exception e) {
      return onUnknownException();
    }
  }

  private byte[] downloadFile(URI uri) throws Exception {
    if (managedClient) {
      client = new HttpClient(new SslContextFactory.Client());
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
    FirmwareUpdateState state = FirmwareUpdateState.IDLE;
    FirmwareUpdateResult result = FirmwareUpdateResult.UNSUPPORTED_PACKAGE_TYPE;
    log.error("Read file is empty, move into state={} with result={}", state, result);
    return new Idle(file, packageUri, state, result, packageVersion);
  }

  private Downloaded successfulDownloading(byte[] file) {
    FirmwareUpdateState state = FirmwareUpdateState.DOWNLOADED;
    FirmwareUpdateResult result = FirmwareUpdateResult.NONE;
    log.info("Download complete, move into state={} with result={}", state, result);
    return new Downloaded(file, packageUri, FirmwareUpdateState.DOWNLOADED, result, packageVersion);
  }

  private FirmwareUpdateExecution onURISyntaxException() {
    FirmwareUpdateState state = FirmwareUpdateState.IDLE;
    FirmwareUpdateResult result = FirmwareUpdateResult.INVALID_URI;
    log.error(
        "PackageURI doesn't follow URI syntax, move into state={} with result={}", state, result);
    return new Idle(file, packageUri, state, result, packageVersion);
  }

  /** It could at least attempt to retry a broken downloading a few times. */
  private FirmwareUpdateExecution onDownloadingBroke() {
    FirmwareUpdateState state = FirmwareUpdateState.DOWNLOADED;
    FirmwareUpdateResult result = FirmwareUpdateResult.NONE;
    log.error("Downloading file broke, move into state={} with result={}", state, result);
    return new Idle(file, packageUri, state, result, packageVersion);
  }

  private FirmwareUpdateExecution onUnknownException() {
    FirmwareUpdateState state = FirmwareUpdateState.IDLE;
    FirmwareUpdateResult result = FirmwareUpdateResult.FIRMWARE_UPDATE_FAILED;
    log.error("Downloading file broke, move into state={} with result={}", state, result);
    return new Idle(file, packageUri, state, result, packageVersion);
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
