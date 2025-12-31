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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.iemu.domain.StateDto;
import io.github.malczuuu.iemu.domain.StateService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class StatePatchEndpointHandler implements Handler {

  private final StateService stateService;
  private final ObjectMapper mapper;

  public StatePatchEndpointHandler(StateService stateService, ObjectMapper mapper) {
    this.stateService = stateService;
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    StateDto state = mapper.readValue(ctx.bodyAsBytes(), StateDto.class);
    stateService.changeState(state);
    ctx.status(204);
  }
}
