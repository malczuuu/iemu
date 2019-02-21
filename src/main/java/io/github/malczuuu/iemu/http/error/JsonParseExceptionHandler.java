package io.github.malczuuu.iemu.http.error;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.problem4j.core.Problem;
import io.javalin.Context;
import io.javalin.ExceptionHandler;
import org.jetbrains.annotations.NotNull;

public class JsonParseExceptionHandler implements ExceptionHandler<JsonParseException> {

  private final ObjectMapper mapper;

  public JsonParseExceptionHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull JsonParseException exception, @NotNull Context ctx) {
    try {
      ctx.status(400)
          .contentType(Problem.CONTENT_TYPE)
          .result(
              mapper.writeValueAsString(
                  Problem.builder()
                      .title("Bad request")
                      .status(400)
                      .detail("Failed to parse JSON object")
                      .build()));
    } catch (JsonProcessingException e) {
      ctx.status(400).result("Bad request");
    }
  }
}
