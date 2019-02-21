package io.github.malczuuu.iemu.http;

public class WebSocketServiceFactory {

  public WebSocketService create() {
    return new WebSocketServiceImpl();
  }
}
