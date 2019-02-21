package io.github.malczuuu.iemu.domain;

import java.util.Timer;

public class CurrentTimeModuleFactory {

  public CurrentTimeModule getCurrentTimeModule() {
    return new CurrentTimeModuleImpl(new Timer());
  }
}
