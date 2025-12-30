package io.github.malczuuu.iemu.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LwM2mConfig {

  private final boolean enabled;
  private final int localPort;
  private final String endpoint;
  private final boolean bootstrap;
  private final String upstream;
  private final LwM2mSecurityConfig security;

  public LwM2mConfig() {
    this(false, 0, "client", false, "localhost:5683", new LwM2mSecurityConfig());
  }

  @JsonCreator
  public LwM2mConfig(
      @JsonProperty("enabled") Boolean enabled,
      @JsonProperty("localPort") Integer localPort,
      @JsonProperty("endpoint") String endpoint,
      @JsonProperty("bootstrap") Boolean bootstrap,
      @JsonProperty("upstream") String upstream,
      @JsonProperty("security") LwM2mSecurityConfig security) {
    this.enabled = enabled != null ? enabled : false;
    this.localPort = localPort != null ? localPort : 0;
    this.endpoint = endpoint;
    this.bootstrap = bootstrap != null ? bootstrap : false;
    this.upstream = upstream;
    this.security = security != null ? security : new LwM2mSecurityConfig();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public int getLocalPort() {
    return localPort;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public boolean useBootstrap() {
    return bootstrap;
  }

  public String getUpstream() {
    return upstream;
  }

  public LwM2mSecurityConfig getSecurity() {
    return security;
  }

  public boolean useSecureMode() {
    return security.getIdentity() != null && security.getPsk() != null;
  }
}
