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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.iemu.common.ObjectMapperFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ConfigReader {

  private final ObjectMapper mapper;

  public ConfigReader() {
    this(new ObjectMapperFactory().getYamlObjectMapper());
  }

  public ConfigReader(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public Config readConfig(String profile) {
    String filename = getConfigFilename(profile);
    try {
      Config config = mapper.readValue(Files.readAllBytes(Paths.get(filename)), Config.class);
      log.info("Loaded config from config {} file", filename);
      return config;
    } catch (IOException e) {
      log.error("Unable to read config from {} file", filename);
      System.exit(1);
      return new Config();
    }
  }

  @NotNull
  private String getConfigFilename(String profile) {
    String filename;
    if (profile.length() > 0) {
      filename = "data/config-" + profile + ".yml";
    } else {
      filename = "data/config.yml";
    }
    return filename;
  }
}
