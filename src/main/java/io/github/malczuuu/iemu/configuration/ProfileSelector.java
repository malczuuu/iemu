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
