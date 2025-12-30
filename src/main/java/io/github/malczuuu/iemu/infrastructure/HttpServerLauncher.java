package io.github.malczuuu.iemu.infrastructure;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.iemu.configuration.HttpConfig;
import io.github.malczuuu.iemu.domain.FirmwareService;
import io.github.malczuuu.iemu.domain.StateService;
import io.github.malczuuu.iemu.infrastructure.http.FirmwareGetEndpointHandler;
import io.github.malczuuu.iemu.infrastructure.http.HttpRequestLogger;
import io.github.malczuuu.iemu.infrastructure.http.StateGetEndpointHandler;
import io.github.malczuuu.iemu.infrastructure.http.StatePatchEndpointHandler;
import io.github.malczuuu.iemu.infrastructure.http.WebSocketService;
import io.github.malczuuu.iemu.infrastructure.http.error.BaseExceptionHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.InternalServerErrorHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.JsonParseExceptionHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.JsonProcessingExceptionHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.NotFoundErrorHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.ProblemExceptionHandler;
import io.github.problem4j.core.ProblemException;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerLauncher implements Launcher {

  private final HttpConfig config;
  private final WebSocketService webSocketService;
  private final StateService stateService;
  private final FirmwareService firmwareService;
  private final ObjectMapper mapper;

  public HttpServerLauncher(
      HttpConfig config,
      WebSocketService webSocketService,
      StateService stateService,
      FirmwareService firmwareService,
      ObjectMapper mapper) {
    this.config = config;
    this.webSocketService = webSocketService;
    this.stateService = stateService;
    this.firmwareService = firmwareService;
    this.mapper = mapper;
  }

  @Override
  public void start() {
    Javalin app =
        Javalin.create(
            config -> {
              config.http.generateEtags = true;
              config.showJavalinBanner = false;
              config.staticFiles.add("/static", Location.CLASSPATH);
              config.requestLogger.http(new HttpRequestLogger());
            });

    app.get("/api/state", new StateGetEndpointHandler(stateService, mapper));
    app.patch("/api/state", new StatePatchEndpointHandler(stateService, mapper));
    app.get("/api/firmware", new FirmwareGetEndpointHandler(firmwareService, mapper));

    initExceptionHandling(app);

    initWebSocket(app);

    Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

    app.start(config.getPort());
  }

  private void initWebSocket(Javalin app) {
    app.ws(
        "/api/websocket",
        ws -> {
          ws.onConnect(webSocketService::onConnect);
          ws.onMessage(webSocketService::onMessage);
          ws.onClose(webSocketService::onClose);
          ws.onError(webSocketService::onError);
        });
  }

  private void initExceptionHandling(Javalin app) {
    app.exception(JsonParseException.class, new JsonParseExceptionHandler(mapper));
    app.exception(JsonProcessingException.class, new JsonProcessingExceptionHandler(mapper));
    app.exception(ProblemException.class, new ProblemExceptionHandler(mapper));
    app.exception(Exception.class, new BaseExceptionHandler(mapper));

    app.error(404, new NotFoundErrorHandler(mapper));
    app.error(500, new InternalServerErrorHandler(mapper));
  }
}
