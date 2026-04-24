package io.github.malczuuu.iemu.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProfileSelectorTests {

  @Test
  fun `getProfileName() should return an empty string when no arguments are provided`() {
    assertEquals("", ProfileSelector(emptyArray()).getProfileName())
  }

  @Test
  fun `getProfileName() should strip the leading double dash from a single argument`() {
    assertEquals("dev", ProfileSelector(arrayOf("--dev")).getProfileName())
  }

  @Test
  fun `getProfileName() should preserve inner dashes of the profile name`() {
    assertEquals("prod-west", ProfileSelector(arrayOf("--prod-west")).getProfileName())
  }

  @Test
  fun `getProfileName() should throw when the profile name after the dashes is empty`() {
    val selector = ProfileSelector(arrayOf("--"))

    val ex = assertThrows(InvalidProfileException::class.java) { selector.getProfileName() }
    assertEquals("Profile name cannot be empty", ex.message)
  }

  @Test
  fun `getProfileName() should throw when more than one argument is provided`() {
    val selector = ProfileSelector(arrayOf("--dev", "--prod"))

    assertThrows(InvalidProfileException::class.java) { selector.getProfileName() }
  }

  @Test
  fun `getProfileName() should fall through to empty when a single argument has no dash prefix`() {
    assertEquals("", ProfileSelector(arrayOf("dev")).getProfileName())
  }
}
