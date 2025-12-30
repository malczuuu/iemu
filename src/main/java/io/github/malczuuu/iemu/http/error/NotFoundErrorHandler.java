package io.github.malczuuu.iemu.http.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.problem4j.core.Problem;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class NotFoundErrorHandler implements Handler {

  private final ObjectMapper mapper;

  public NotFoundErrorHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull Context ctx) {
    String contentType = ctx.contentType();
    if (contentType != null && !contentType.equals(Problem.CONTENT_TYPE)) {
      try {
        ctx.status(404)
            .contentType(Problem.CONTENT_TYPE)
            .result(
                mapper.writeValueAsString(
                    Problem.builder()
                        .title("Not found")
                        .status(404)
                        .detail("No handler found for requested path and method")
                        .build()));
      } catch (JsonProcessingException e) {
        ctx.status(404).contentType("text/plain").result("Not found");
      }
    }
  }
}
