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
