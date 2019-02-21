package io.github.malczuuu.iemu.domain;

import java.util.Timer;

public class LightModuleFactory {

  public LightModule getLightModule() {
    return new LightModuleImpl(false, new Timer());
  }
}
