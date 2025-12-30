/*
 * Copyright (c) 2025 Damian Malczewski
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
 * SPDX-License-Identifier: MIT
 */
package io.github.malczuuu.iemu.infrastructure.http;

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
