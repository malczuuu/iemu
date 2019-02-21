package io.github.malczuuu.iemu.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigReader {

  private final ObjectMapper mapper;

  public ConfigReader(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public Config readConfig() {
    try {
      return mapper.readValue(Files.readAllBytes(Paths.get("data/config.yml")), Config.class);
    } catch (IOException e) {
      return new Config();
    }
  }
}
