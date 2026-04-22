package io.github.malczuuu.iemu.common

import kotlin.system.exitProcess
import org.slf4j.LoggerFactory

class ProfileSelector(private val args: Array<String>) {

  fun getProfileName(): String {
    if (args.size == 1 && args[0].startsWith("--")) {
      val profile = args[0].substring(2)
      if (profile.isEmpty()) shutdown("Profile name cannot be empty")
      return profile
    } else if (args.size > 1) {
      shutdown("Too many program arguments; expecting either just one [ --{profile} ] or none")
    }
    return ""
  }

  private fun shutdown(error: String): Nothing {
    log.error(error)
    exitProcess(-1)
  }

  companion object {
    private val log = LoggerFactory.getLogger(ProfileSelector::class.java)
  }
}
