package io.github.malczuuu.iemu.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ConfigReader {

  private final ObjectMapper mapper;

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
