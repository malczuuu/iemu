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
import io.github.malczuuu.iemu.domain.FirmwareDTO;
import io.github.malczuuu.iemu.domain.FirmwareService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class FirmwareGetEndpointHandler implements Handler {

  private final FirmwareService firmwareService;
  private final ObjectMapper mapper;

  public FirmwareGetEndpointHandler(FirmwareService firmwareService, ObjectMapper mapper) {
    this.firmwareService = firmwareService;
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    FirmwareDTO firmware = firmwareService.getFirmware();
    ctx.status(200).contentType("application/json").result(mapper.writeValueAsString(firmware));
  }
}
