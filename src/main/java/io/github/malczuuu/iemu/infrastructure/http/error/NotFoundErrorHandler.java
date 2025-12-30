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
package io.github.malczuuu.iemu.infrastructure.http.error;

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
                        .title("Not Found")
                        .status(404)
                        .detail("No handler found for requested path and method")
                        .build()));
      } catch (JsonProcessingException e) {
        ctx.status(404).contentType("text/plain").result("Not found");
      }
    }
  }
}
