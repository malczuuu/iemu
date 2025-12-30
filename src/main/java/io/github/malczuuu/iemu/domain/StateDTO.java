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
package io.github.malczuuu.iemu.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StateDTO {

  private final String deviceType;

  private final String currentTime;
  private final String timeZone;
  private final String utcOffset;

  private final List<ErrorDTO> errors;

  private final Boolean on;
  private final Long onTime;
  private final Integer dimmer;

  @JsonCreator
  public StateDTO(
      @JsonProperty("deviceType") String deviceType,
      @JsonProperty("currentTime") String currentTime,
      @JsonProperty("timeZone") String timeZone,
      @JsonProperty("utcOffset") String utcOffset,
      @JsonProperty("errors") List<ErrorDTO> errors,
      @JsonProperty("on") Boolean on,
      @JsonProperty("onTime") Long onTime,
      @JsonProperty("dimmer") Integer dimmer) {
    this.deviceType = deviceType;
    this.currentTime = currentTime;
    this.timeZone = timeZone;
    this.utcOffset = utcOffset;
    this.errors = errors;
    this.on = on;
    this.onTime = onTime;
    this.dimmer = dimmer;
  }

  public static StateDTO changeCurrentTime(String currentTime) {
    return new StateDTO(null, currentTime, null, null, null, null, null, null);
  }

  public static StateDTO changeOn(Boolean on) {
    return new StateDTO(null, null, null, null, null, on, null, null);
  }

  public static StateDTO changeDimmer(int dimmer) {
    return new StateDTO(null, null, null, null, null, null, null, dimmer);
  }

  public static StateDTO changeOnTime(long onTime) {
    return new StateDTO(null, null, null, null, null, null, onTime, null);
  }

  @JsonProperty("deviceType")
  public String getDeviceType() {
    return deviceType;
  }

  @JsonProperty("currentTime")
  public String getCurrentTime() {
    return currentTime;
  }

  @JsonProperty("timeZone")
  public String getTimeZone() {
    return timeZone;
  }

  @JsonProperty("utcOffset")
  public String getUTCOffset() {
    return utcOffset;
  }

  @JsonProperty("errors")
  public List<ErrorDTO> getErrors() {
    return errors;
  }

  @JsonProperty("on")
  public Boolean getOn() {
    return on;
  }

  @JsonProperty("onTime")
  public Long getOnTime() {
    return onTime;
  }

  @JsonProperty("dimmer")
  public Integer getDimmer() {
    return dimmer;
  }
}
