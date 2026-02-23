/*
 * Copyright (c) 2025-2026 Damian Malczewski
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
