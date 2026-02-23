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

import io.github.malczuuu.iemu.common.JacksonFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.yaml.YAMLMapper;

@Slf4j
public class ConfigReader {

  private final YAMLMapper mapper;

  public ConfigReader() {
    this(new JacksonFactory().getYamlMapper());
  }

  public ConfigReader(YAMLMapper mapper) {
    this.mapper = mapper;
  }

  public Config readConfig(String profile) {
    String filename = getConfigFilename(profile);
    try {
      Config config = mapper.readValue(Files.readAllBytes(Paths.get(filename)), Config.class);
      log.info("Loaded config from config {} file", filename);
      return config;
    } catch (JacksonException | IOException e) {
      log.error("Unable to read config from {} file", filename);
      System.exit(1);
      return new Config();
    }
  }

  @NotNull
  private String getConfigFilename(String profile) {
    String filename;
    if (!profile.isEmpty()) {
      filename = "data/config-" + profile + ".yml";
    } else {
      filename = "data/config.yml";
    }
    return filename;
  }
}
