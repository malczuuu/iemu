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

import io.github.malczuuu.iemu.common.CancelAwareTimerTask;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.function.Consumer;

class CurrentTimeModuleImpl implements CurrentTimeModule {

  private final List<Consumer<Instant>> onCurrentTimeChange = new ArrayList<>();

  private long diff = 0;
  private String timeZone = TimeZone.getDefault().getID();
  private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());

  private final Timer timer;

  CurrentTimeModuleImpl(Timer timer) {
    this.timer = timer;
    timer.schedule(
        new CancelAwareTimerTask(
            () -> {
              Instant now = getCurrentTime();
              new ArrayList<>(onCurrentTimeChange).forEach(c -> c.accept(now));
            }),
        1000 - Instant.now().toEpochMilli() % 1000,
        1000);
  }

  @Override
  public void setCurrentTime(Instant currentTime) {
    diff = Instant.now().toEpochMilli() - currentTime.toEpochMilli();
    Instant now = getCurrentTime();
    onCurrentTimeChange.forEach(c -> c.accept(now));
  }

  @Override
  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public void setUTCOffset(String utcOffset) {
    this.utcOffset = utcOffset;
  }

  @Override
  public Instant getCurrentTime() {
    return Instant.now().minusMillis(diff).truncatedTo(ChronoUnit.SECONDS);
  }

  @Override
  public String getTimeZone() {
    return timeZone;
  }

  @Override
  public String getUTCOffset() {
    return utcOffset;
  }

  @Override
  public void shutdown() {
    timer.cancel();
    timer.purge();
  }

  @Override
  public void subscribeOnCurrentTimeChange(Consumer<Instant> consumer) {
    onCurrentTimeChange.add(consumer);
  }
}
