package io.github.malczuuu.iemu.http;

import io.javalin.http.Context;
import io.javalin.http.RequestLogger;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class HttpRequestLogger implements RequestLogger {

  @Override
  public void handle(@NotNull Context ctx, @NotNull Float executionTimeMs) {
    log.debug(
        "Handled HTTP request method={}, path={}, query={}, headers={} in {}ms",
        ctx.method(),
        ctx.method(),
        ctx.queryString(),
        ctx.headerMap(),
        executionTimeMs);
  }
}
