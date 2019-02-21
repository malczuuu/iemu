package io.github.malczuuu.iemu.http;

import io.javalin.websocket.WsSession;
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

  private final Map<String, WsSession> sessions = new HashMap<>();
  private final List<Consumer<String>> consumers = new ArrayList<>();

  @Override
  public void onConnect(WsSession session) {
    executorService.submit(
        () -> {
          log.debug("Connected session={}", session.getId());
          sessions.put(session.getId(), session);
        });
  }

  @Override
  public void onMessage(WsSession session, String message) {
    executorService.submit(
        () -> log.debug("Received message={} from session={}", message, session.getId()));
  }

  @Override
  public void onClose(WsSession session, int statusCode, String reason) {
    executorService.submit(
        () -> {
          log.debug(
              "Closed session={} with statusCode={}, reason={}",
              session.getId(), statusCode, reason);
          sessions.remove(session.getId());
        });
  }

  @Override
  public void onError(WsSession session, Throwable exception) {
    executorService.submit(
        () -> {
          log.error("An error occurred in session={}", session.getId(), exception);
          sessions.remove(session.getId());
          session.close();
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
