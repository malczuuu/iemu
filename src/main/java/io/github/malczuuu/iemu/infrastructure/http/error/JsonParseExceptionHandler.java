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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.problem4j.core.Problem;
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
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
                      .title("Bad Request")
                      .status(400)
                      .detail("Failed to parse JSON object")
                      .build()));
    } catch (JsonProcessingException e) {
      ctx.status(400).result("Bad Request");
    }
  }
}
