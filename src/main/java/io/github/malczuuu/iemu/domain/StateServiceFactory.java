package io.github.malczuuu.iemu.domain;

public class StateServiceFactory {

  public StateService getStateService() {
    return new StateServiceImpl();
  }
}
