package io.github.malczuuu.iemu.configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProfileSelector {

  private final String[] args;

  public ProfileSelector(String[] args) {
    this.args = args;
  }

  public String getProfileName() {
    if (args.length == 1 && args[0].startsWith("--")) {
      String profile = readProfileName();
      validateProfile(profile);
      return profile;
    } else if (args.length > 1) {
      shutdown("Too many program arguments; expecting either just one [ --{profile} ] or none");
    }
    return "";
  }

  private String readProfileName() {
    return args[0].substring(2);
  }

  private void validateProfile(String profile) {
    if (profile.isEmpty()) {
      shutdown("Profile name cannot be empty");
    }
  }

  private void shutdown(String error) {
    log.error(error);
    System.exit(-1);
  }
}
