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
package io.github.malczuuu.iemu.mock;

import io.github.malczuuu.iemu.common.CancelAwareTimerTask;
import java.util.TimerTask;

public class TimerTaskMock {

  public static void run(TimerTask task) {
    if (!isCanceled(task)) {
      task.run();
    }
  }

  public static boolean isCanceled(TimerTask task) {
    return task instanceof CancelAwareTimerTask c && c.isCancelled();
  }
}
