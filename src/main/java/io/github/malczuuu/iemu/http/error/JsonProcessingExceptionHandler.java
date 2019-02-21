package io.github.malczuuu.iemu.http.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.problem4j.core.Problem;
import io.javalin.Context;
import io.javalin.ExceptionHandler;
import org.jetbrains.annotations.NotNull;

public class JsonProcessingExceptionHandler implements ExceptionHandler<JsonProcessingException> {

  private final ObjectMapper mapper;

  public JsonProcessingExceptionHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull JsonProcessingException exception, @NotNull Context ctx) {
    try {
      ctx.status(500)
          .contentType(Problem.CONTENT_TYPE)
          .result(
              mapper.writeValueAsString(
                  Problem.builder()
                      .title("Internal server error")
                      .status(500)
                      .detail("Failed to read or write JSON object")
                      .build()));
    } catch (JsonProcessingException e) {
      ctx.status(500).contentType("text/plain").result("Internal server error");
    }
  }
}
