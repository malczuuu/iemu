package io.github.malczuuu.iemu.common.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HttpConfig {

  private final int port;

  public HttpConfig() {
    this(4500);
  }

  @JsonCreator
  public HttpConfig(@JsonProperty("port") Integer port) {
    this.port = port != null ? port : 4500;
  }

  @JsonProperty("port")
  public Integer getPort() {
    return port;
  }
}
