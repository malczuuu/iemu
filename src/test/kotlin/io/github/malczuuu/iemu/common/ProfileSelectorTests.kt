package io.github.malczuuu.iemu.common

import org.junit.jupiter.api.Assertions.assertEquals
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
}
