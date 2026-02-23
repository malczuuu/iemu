/*
 * Copyright (c) 2025-2026 Damian Malczewski
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.malczuuu.iemu.infrastructure.http.error;

import io.github.problem4j.core.Problem;
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.json.JsonMapper;

public class FallbackExceptionHandler implements ExceptionHandler<Exception> {

  private final JsonMapper mapper;

  public FallbackExceptionHandler(JsonMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull Exception exception, @NotNull Context ctx) {
    Problem problem = Problem.of(500, exception.getMessage());
    ctx.status(problem.getStatus())
        .contentType(Problem.CONTENT_TYPE)
        .result(mapper.writeValueAsString(problem));
  }
}
