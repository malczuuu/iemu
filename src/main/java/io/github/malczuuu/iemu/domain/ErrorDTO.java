package io.github.malczuuu.iemu.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ErrorDTO {

  private final Integer code;
  private final String message;

  @JsonCreator
  public ErrorDTO(@JsonProperty("code") Integer code, @JsonProperty("message") String message) {
    this.code = code;
    this.message = message;
  }
}
