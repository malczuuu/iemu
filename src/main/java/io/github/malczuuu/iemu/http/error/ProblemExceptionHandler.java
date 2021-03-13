package io.github.malczuuu.iemu.http.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.core.ProblemException;
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import org.jetbrains.annotations.NotNull;

public class ProblemExceptionHandler implements ExceptionHandler<ProblemException> {

  private final ObjectMapper mapper;

  public ProblemExceptionHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull ProblemException exception, @NotNull Context ctx) {
    Problem problem = exception.getProblem();
    try {
      ctx.status(problem.getStatus())
          .contentType(Problem.CONTENT_TYPE)
          .result(mapper.writeValueAsString(problem));
    } catch (JsonProcessingException e) {
      ctx.status(500).contentType("text/plain").result("Internal server error");
    }
  }
}
