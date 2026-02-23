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

package io.github.malczuuu.iemu.infrastructure;

import io.github.malczuuu.iemu.configuration.HttpConfig;
import io.github.malczuuu.iemu.domain.FirmwareService;
import io.github.malczuuu.iemu.domain.StateService;
import io.github.malczuuu.iemu.infrastructure.http.FirmwareGetEndpointHandler;
import io.github.malczuuu.iemu.infrastructure.http.HttpRequestLogger;
import io.github.malczuuu.iemu.infrastructure.http.StateGetEndpointHandler;
import io.github.malczuuu.iemu.infrastructure.http.StatePatchEndpointHandler;
import io.github.malczuuu.iemu.infrastructure.http.WebSocketService;
import io.github.malczuuu.iemu.infrastructure.http.error.FallbackExceptionHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.InternalServerErrorHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.JacksonExceptionHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.NotFoundErrorHandler;
import io.github.malczuuu.iemu.infrastructure.http.error.ProblemExceptionHandler;
import io.github.problem4j.core.ProblemException;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public class HttpServerLauncher implements Launcher {

  private final HttpConfig config;
  private final WebSocketService webSocketService;
  private final StateService stateService;
  private final FirmwareService firmwareService;
  private final JsonMapper mapper;

  public HttpServerLauncher(
      HttpConfig config,
      WebSocketService webSocketService,
      StateService stateService,
      FirmwareService firmwareService,
      JsonMapper mapper) {
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
              config.startup.showJavalinBanner = false;
              config.staticFiles.add("/static", Location.CLASSPATH);
              config.requestLogger.http(new HttpRequestLogger());

              config.routes.get("/api/state", new StateGetEndpointHandler(stateService, mapper));
              config.routes.patch(
                  "/api/state", new StatePatchEndpointHandler(stateService, mapper));
              config.routes.get(
                  "/api/firmware", new FirmwareGetEndpointHandler(firmwareService, mapper));

              config.routes.ws(
                  "/api/websocket",
                  ws -> {
                    ws.onConnect(webSocketService::onConnect);
                    ws.onMessage(webSocketService::onMessage);
                    ws.onClose(webSocketService::onClose);
                    ws.onError(webSocketService::onError);
                  });

              config.routes.exception(JacksonException.class, new JacksonExceptionHandler(mapper));
              config.routes.exception(ProblemException.class, new ProblemExceptionHandler(mapper));
              config.routes.exception(Exception.class, new FallbackExceptionHandler(mapper));

              config.routes.error(404, new NotFoundErrorHandler(mapper));
              config.routes.error(500, new InternalServerErrorHandler(mapper));
            });

    Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

    app.start(config.getPort());
  }
}
