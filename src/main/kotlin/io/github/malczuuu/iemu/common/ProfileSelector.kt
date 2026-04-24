package io.github.malczuuu.iemu.common

class ProfileSelector(private val args: Array<String>) {

  fun getProfileName(): String {
    if (args.size == 1 && args[0].startsWith("--")) {
      val profile = args[0].substring(2)
      if (profile.isEmpty()) {
        throw InvalidProfileException("Profile name cannot be empty")
      }
      return profile
    } else if (args.size > 1) {
      throw InvalidProfileException(
          "Too many program arguments; expecting either just one [ --{profile} ] or none"
      )
    }
    return ""
  }
}

class InvalidProfileException(message: String) : RuntimeException(message)
