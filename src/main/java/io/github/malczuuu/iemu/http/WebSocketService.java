package io.github.malczuuu.iemu.http;

import io.javalin.websocket.WsSession;
import java.util.function.Consumer;

public interface WebSocketService {

  void onConnect(WsSession session);

  void onMessage(WsSession session, String message);

  void onClose(WsSession session, int statusCode, String reason);

  void onError(WsSession session, Throwable exception);

  void subscribe(Consumer<String> consumer);

  void sendMessage(String message);
}
