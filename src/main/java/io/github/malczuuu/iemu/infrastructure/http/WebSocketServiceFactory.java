package io.github.malczuuu.iemu.infrastructure.http;

public class WebSocketServiceFactory {

  public WebSocketService create() {
    return new WebSocketServiceImpl();
  }
}
