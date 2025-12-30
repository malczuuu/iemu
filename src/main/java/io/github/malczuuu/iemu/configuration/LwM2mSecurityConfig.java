package io.github.malczuuu.iemu.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LwM2mSecurityConfig {

  private final String identity;
  private final String psk;

  public LwM2mSecurityConfig() {
    this(null, null);
  }

  @JsonCreator
  public LwM2mSecurityConfig(
      @JsonProperty("identity") String identity, @JsonProperty("psk") String psk) {
    this.identity = identity;
    this.psk = psk;
  }

  @JsonProperty("identity")
  public String getIdentity() {
    return identity;
  }

  @JsonProperty("psk")
  public String getPsk() {
    return psk;
  }
}
