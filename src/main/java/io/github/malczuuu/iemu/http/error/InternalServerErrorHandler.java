package io.github.malczuuu.iemu.http.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.problem4j.core.Problem;
import io.javalin.Context;
import io.javalin.ErrorHandler;
import org.jetbrains.annotations.NotNull;

public class InternalServerErrorHandler implements ErrorHandler {

  private final ObjectMapper mapper;

  public InternalServerErrorHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull Context ctx) {
    String contentType = ctx.contentType();
    if (contentType != null && !contentType.equals(Problem.CONTENT_TYPE)) {
      try {
        ctx.status(500)
            .contentType(Problem.CONTENT_TYPE)
            .result(
                mapper.writeValueAsString(
                    Problem.builder().title("Internal server error").status(500).build()));
      } catch (JsonProcessingException e) {
        ctx.status(500).contentType("text/plain").result("Internal server error");
      }
    }
  }
}
