package io.github.malczuuu.iemu.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.github.problem4j.jackson2.ProblemModule;

public class ObjectMapperFactory {

  public ObjectMapper getJsonObjectMapper() {
    return new ObjectMapper()
        .registerModule(new ProblemModule())
        .registerModule(new ParameterNamesModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
  }

  public ObjectMapper getYamlObjectMapper() {
    return new ObjectMapper(new YAMLFactory())
        .registerModule(new ProblemModule())
        .registerModule(new ParameterNamesModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
  }
}
