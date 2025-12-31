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

public class StateDto {

  private final String deviceType;

  private final String currentTime;
  private final String timeZone;
  private final String utcOffset;

  private final List<ErrorDto> errors;

  private final Boolean on;
  private final Long onTime;
  private final Integer dimmer;

  @JsonCreator
  public StateDto(
      @JsonProperty("deviceType") String deviceType,
      @JsonProperty("currentTime") String currentTime,
      @JsonProperty("timeZone") String timeZone,
      @JsonProperty("utcOffset") String utcOffset,
      @JsonProperty("errors") List<ErrorDto> errors,
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

  public static StateDto changeCurrentTime(String currentTime) {
    return new StateDto(null, currentTime, null, null, null, null, null, null);
  }

  public static StateDto changeOn(Boolean on) {
    return new StateDto(null, null, null, null, null, on, null, null);
  }

  public static StateDto changeDimmer(int dimmer) {
    return new StateDto(null, null, null, null, null, null, null, dimmer);
  }

  public static StateDto changeOnTime(long onTime) {
    return new StateDto(null, null, null, null, null, null, onTime, null);
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
  public List<ErrorDto> getErrors() {
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
