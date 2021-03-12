package io.github.malczuuu.iemu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.iemu.common.ObjectMapperFactory;
import io.github.malczuuu.iemu.common.config.Config;
import io.github.malczuuu.iemu.common.config.ConfigReader;
import io.github.malczuuu.iemu.common.config.ProfileSelector;
import io.github.malczuuu.iemu.domain.FirmwareService;
import io.github.malczuuu.iemu.domain.FirmwareServiceFactory;
import io.github.malczuuu.iemu.domain.StateService;
import io.github.malczuuu.iemu.domain.StateServiceFactory;
import io.github.malczuuu.iemu.http.WebSocketEvent;
import io.github.malczuuu.iemu.http.WebSocketService;
import io.github.malczuuu.iemu.http.WebSocketServiceFactory;
import io.github.malczuuu.iemu.starter.HttpServerStarter;
import io.github.malczuuu.iemu.starter.LwM2mClientStarter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

  public static void main(String[] args) {
    String profile = new ProfileSelector(args).getProfileName();
    ConfigReader reader = new ConfigReader(new ObjectMapperFactory().getYamlObjectMapper());
    Config config = reader.readConfig(profile);
    new App(config).run();
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
        } catch (JsonProcessingException e) {
          // Ignored
        }
      };

  private final Runnable firmwarePublish =
      () -> {
        try {
          webSocketService.sendMessage(
              mapper.writeValueAsString(
                  new WebSocketEvent("firmware", firmwareService.getFirmware())));
        } catch (JsonProcessingException e) {
          // Ignored
        }
      };

  private App(Config config) {
    this.config = config;
  }

  private void run() {
    Runtime.getRuntime().addShutdownHook(new Thread(stateService::shutdown));

    stateService.subscribeOnCurrentTimeChange(ignored -> statePublish.run());
    stateService.subscribeOnStateChange(ignored -> statePublish.run());
    stateService.subscribeOnTimeCounterChange(ignored -> statePublish.run());
    stateService.subscribeOnDimmerChange(ignored -> statePublish.run());

    firmwareService.subscribeOnFileChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnPackageUriChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnStateChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnResultChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnPackageVersionChange(ignored -> firmwarePublish.run());
    firmwareService.subscribeOnProgressChange(ign -> firmwarePublish.run());

    if (config.getLwM2mConfig().isEnabled()) {
      new LwM2mClientStarter(config.getLwM2mConfig(), stateService, firmwareService).run();
    }
    new HttpServerStarter(
            config.getHttpConfig(), webSocketService, stateService, firmwareService, mapper)
        .run();
  }
}
