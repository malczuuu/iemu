package io.github.malczuuu.iemu.http;

import lombok.Data;

@Data
public class WebSocketEvent {

  private final String type;
  private final Object body;

  public WebSocketEvent(String type, Object body) {
    this.type = type;
    this.body = body;
  }
}
