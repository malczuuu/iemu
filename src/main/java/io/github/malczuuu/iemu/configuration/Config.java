package io.github.malczuuu.iemu.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Config {

  private final HttpConfig httpConfig;
  private final LwM2mConfig lwM2mConfig;
  private final MqttConfig mqttConfig;

  public Config() {
    this(new HttpConfig(), new LwM2mConfig(), new MqttConfig());
  }

  @JsonCreator
  public Config(
      @JsonProperty("http") HttpConfig http,
      @JsonProperty("lwm2m") LwM2mConfig lwm2m,
      @JsonProperty("mqtt") MqttConfig mqtt) {
    this.httpConfig = http != null ? http : new HttpConfig();
    this.lwM2mConfig = lwm2m != null ? lwm2m : new LwM2mConfig();
    this.mqttConfig = mqtt != null ? mqtt : new MqttConfig();
  }
}
