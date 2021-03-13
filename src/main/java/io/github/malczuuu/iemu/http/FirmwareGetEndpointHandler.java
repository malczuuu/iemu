package io.github.malczuuu.iemu.http;

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
