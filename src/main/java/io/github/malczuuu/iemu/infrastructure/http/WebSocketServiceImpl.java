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
package io.github.malczuuu.iemu.infrastructure.http;

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
          log.debug("Connected session={}", session.sessionId());
          sessions.put(session.sessionId(), session);
        });
  }

  @Override
  public void onMessage(WsMessageContext session) {
    executorService.submit(
        () ->
            log.debug(
                "Received message={} from session={}", session.message(), session.sessionId()));
  }

  @Override
  public void onClose(WsCloseContext session) {
    executorService.submit(
        () -> {
          log.debug(
              "Closed session={} with statusCode={}, reason={}",
              session.sessionId(),
              session.status(),
              session.reason());
          sessions.remove(session.sessionId());
        });
  }

  @Override
  public void onError(WsErrorContext session) {
    executorService.submit(
        () -> {
          log.error("An error occurred in session={}", session.sessionId(), session.error());
          sessions.remove(session.sessionId());
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
