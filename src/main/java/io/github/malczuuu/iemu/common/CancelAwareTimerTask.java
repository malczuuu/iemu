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
package io.github.malczuuu.iemu.common;

import java.util.TimerTask;
import lombok.Getter;

public class CancelAwareTimerTask extends TimerTask {

  private final Runnable runnable;

  @Getter private boolean cancelled;

  public CancelAwareTimerTask(Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public boolean cancel() {
    cancelled = true;
    return super.cancel();
  }

  @Override
  public void run() {
    runnable.run();
  }
}
