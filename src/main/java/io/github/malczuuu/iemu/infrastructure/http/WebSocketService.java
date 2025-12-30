package io.github.malczuuu.iemu.infrastructure.http;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import java.util.function.Consumer;

public interface WebSocketService {

  void onConnect(WsConnectContext session);

  void onMessage(WsMessageContext session);

  void onClose(WsCloseContext session);

  void onError(WsErrorContext session);

  void subscribe(Consumer<String> consumer);

  void sendMessage(String message);
}
