package io.github.malczuuu.iemu.http.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.problem4j.core.Problem;
import io.javalin.Context;
import io.javalin.ExceptionHandler;
import org.jetbrains.annotations.NotNull;

public class BaseExceptionHandler implements ExceptionHandler<Exception> {

  private final ObjectMapper mapper;

  public BaseExceptionHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull Exception exception, @NotNull Context ctx) {
    Problem problem =
        Problem.builder()
            .title("Internal server error")
            .status(500)
            .detail(exception.getMessage())
            .build();
    try {
      ctx.status(problem.getStatus())
          .contentType(Problem.CONTENT_TYPE)
          .result(mapper.writeValueAsString(problem));
    } catch (JsonProcessingException e) {
      ctx.status(500).contentType("text/plain").result("Internal server error");
    }
  }
}
