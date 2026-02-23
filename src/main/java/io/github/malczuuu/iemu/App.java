/*
 * Copyright (c) 2025-2026 Damian Malczewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.malczuuu.iemu;

import io.github.malczuuu.iemu.common.JacksonFactory;
import io.github.malczuuu.iemu.configuration.Config;
import io.github.malczuuu.iemu.configuration.ConfigReader;
import io.github.malczuuu.iemu.configuration.ProfileSelector;
import io.github.malczuuu.iemu.domain.FirmwareService;
import io.github.malczuuu.iemu.domain.FirmwareServiceFactory;
import io.github.malczuuu.iemu.domain.StateService;
import io.github.malczuuu.iemu.domain.StateServiceFactory;
import io.github.malczuuu.iemu.infrastructure.HttpServerLauncher;
import io.github.malczuuu.iemu.infrastructure.LwM2mClientLauncher;
import io.github.malczuuu.iemu.infrastructure.http.WebSocketEvent;
import io.github.malczuuu.iemu.infrastructure.http.WebSocketService;
import io.github.malczuuu.iemu.infrastructure.http.WebSocketServiceFactory;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public class App {

  public static void main(String[] args) {
    String profile = new ProfileSelector(args).getProfileName();
    ConfigReader reader = new ConfigReader();
    Config config = reader.readConfig(profile);
    new App(config).start();
  }

  private final WebSocketService webSocketService = new WebSocketServiceFactory().create();
  private final StateService stateService = new StateServiceFactory().getStateService();
  private final FirmwareService firmwareService = new FirmwareServiceFactory().getFirmwareService();

  private final JsonMapper mapper = new JacksonFactory().getJsonMapper();
  private final Config config;

  private final Runnable statePublish =
      () ->
          webSocketService.sendMessage(
              mapper.writeValueAsString(new WebSocketEvent("state", stateService.getState())));

  private final Runnable firmwarePublish =
      () ->
          webSocketService.sendMessage(
              mapper.writeValueAsString(
                  new WebSocketEvent("firmware", firmwareService.getFirmware())));

  private App(Config config) {
    this.config = config;
  }

  private void start() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

    stateService.subscribeOnCurrentTimeChange(ignored -> statePublish.run());
    stateService.subscribeOnStateChange(ignored -> statePublish.run());
    stateService.subscribeOnTimeCounterChange(ignored -> statePublish.run());
    stateService.subscribeOnDimmerChange(ignored -> statePublish.run());

    firmwareService.subscribeOnFileChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnPackageUriChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnStateChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnResultChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnPackageVersionChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnProgressChange(ignored -> firmwarePublish.run());

    if (config.getLwM2mConfig().isEnabled()) {
      new LwM2mClientLauncher(config.getLwM2mConfig(), stateService, firmwareService).start();
    }
    new HttpServerLauncher(
            config.getHttpConfig(), webSocketService, stateService, firmwareService, mapper)
        .start();
  }

  private void shutdown() {
    stateService.shutdown();
  }
}
