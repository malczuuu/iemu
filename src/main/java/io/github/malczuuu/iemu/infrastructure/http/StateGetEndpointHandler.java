package io.github.malczuuu.iemu.infrastructure.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.malczuuu.iemu.domain.StateDTO;
import io.github.malczuuu.iemu.domain.StateService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class StateGetEndpointHandler implements Handler {

  private final StateService stateService;
  private final ObjectMapper mapper;

  public StateGetEndpointHandler(StateService stateService, ObjectMapper mapper) {
    this.stateService = stateService;
    this.mapper = mapper;
  }

  @Override
  public void handle(@NotNull Context ctx) throws Exception {
    StateDTO state = stateService.getState();
    ctx.status(200).contentType("application/json").result(mapper.writeValueAsString(state));
  }
}
