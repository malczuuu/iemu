package io.github.malczuuu.iemu.http;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class WebSocketServiceImpl implements WebSocketService {

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final Map<String, WsContext> sessions = new HashMap<>();
  private final List<Consumer<String>> consumers = new ArrayList<>();

  @Override
  public void onConnect(WsConnectContext session) {
    executorService.submit(
        () -> {
          log.debug("Connected session={}", session.getSessionId());
          sessions.put(session.getSessionId(), session);
        });
  }

  @Override
  public void onMessage(WsMessageContext session) {
    executorService.submit(
        () ->
            log.debug(
                "Received message={} from session={}", session.message(), session.getSessionId()));
  }

  @Override
  public void onClose(WsCloseContext session) {
    executorService.submit(
        () -> {
          log.debug(
              "Closed session={} with statusCode={}, reason={}",
              session.getSessionId(),
              session.status(),
              session.reason());
          sessions.remove(session.getSessionId());
        });
  }

  @Override
  public void onError(WsErrorContext session) {
    executorService.submit(
        () -> {
          log.error("An error occurred in session={}", session.getSessionId(), session.error());
          sessions.remove(session.getSessionId());
          session.session.close();
        });
  }

  @Override
  public void subscribe(Consumer<String> consumer) {
    executorService.submit(() -> consumers.add(consumer));
  }

  @Override
  public void sendMessage(String message) {
    executorService.submit(
        () ->
            sessions.forEach(
                (key, value) -> {
                  try {
                    value.send(message);
                  } catch (Throwable t) {
                    log.error("Failed to send message={} to session={}", message, key, t);
                  }
                }));
  }
}
