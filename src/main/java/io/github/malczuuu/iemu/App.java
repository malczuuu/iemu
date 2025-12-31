/*
 * Copyright (c) 2025 Damian Malczewski
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
 * SPDX-License-Identifier: MIT
 */
package io.github.malczuuu.iemu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.iemu.common.ObjectMapperFactory;
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

  private final ObjectMapper mapper = new ObjectMapperFactory().getJsonObjectMapper();
  private final Config config;

  private final Runnable statePublish =
      () -> {
        try {
          webSocketService.sendMessage(
              mapper.writeValueAsString(new WebSocketEvent("state", stateService.getState())));
        } catch (JsonProcessingException ignored) {
          // ignored
        }
      };

  private final Runnable firmwarePublish =
      () -> {
        try {
          webSocketService.sendMessage(
              mapper.writeValueAsString(
                  new WebSocketEvent("firmware", firmwareService.getFirmware())));
        } catch (JsonProcessingException ignored) {
          // ignored
        }
      };

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
