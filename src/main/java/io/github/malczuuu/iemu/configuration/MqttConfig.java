package io.github.malczuuu.iemu.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MqttConfig {

  private final boolean enabled;
  private final String uri;
  private final String username;
  private final String password;

  public MqttConfig() {
    this(false, "tcp://localhost:1883", "rabbitmq", "rabbitmq");
  }

  @JsonCreator
  public MqttConfig(
      @JsonProperty("enabled") Boolean enabled,
      @JsonProperty("uri") String uri,
      @JsonProperty("username") String username,
      @JsonProperty("password") String password) {
    this.enabled = enabled != null ? enabled : false;
    this.uri = uri;
    this.username = username;
    this.password = password;
  }

  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }

  @JsonProperty("uri")
  public String getURI() {
    return uri;
  }

  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  @JsonProperty("password")
  public String getPassword() {
    return password;
  }
}
