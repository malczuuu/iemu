package io.github.malczuuu.iemu.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CommandLineTests {

  @Test
  fun `profile should default to empty string before init is called`() {
    assertEquals("", CommandLine(emptyArray()).profile)
  }

  @Test
  fun `init() should set profile to empty string when no arguments are provided`() {
    val cmd = CommandLine(emptyArray())
    cmd.init()
    assertEquals("", cmd.profile)
  }

  @Test
  fun `init() should set profile from --profile option`() {
    val cmd = CommandLine(arrayOf("--profile", "dev"))
    cmd.init()
    assertEquals("dev", cmd.profile)
  }

  @Test
  fun `init() should set profile from -p short option`() {
    val cmd = CommandLine(arrayOf("-p", "dev"))
    cmd.init()
    assertEquals("dev", cmd.profile)
  }

  @Test
  fun `init() should preserve inner dashes of the profile name`() {
    val cmd = CommandLine(arrayOf("--profile", "prod-west"))
    cmd.init()
    assertEquals("prod-west", cmd.profile)
  }

  @Test
  fun `init() should throw when an unknown option is provided`() {
    assertThrows(IllegalArgumentException::class.java) {
      CommandLine(arrayOf("--unknown")).init()
    }
  }

  @Test
  fun `init() should throw when --profile is provided without a value`() {
    assertThrows(IllegalArgumentException::class.java) {
      CommandLine(arrayOf("--profile")).init()
    }
  }
}
