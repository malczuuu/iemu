package io.github.malczuuu.iemu.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfigTests {

  @Test
  fun `default Config should expose documented fallback values`() {
    val config = Config()

    assertEquals(4500, config.http.port)
    assertFalse(config.lwM2m.isEnabled)
    assertEquals(0, config.lwM2m.localPort)
    assertEquals("client", config.lwM2m.endpoint)
    assertEquals("localhost:5683", config.lwM2m.upstream)
    assertFalse(config.lwM2m.bootstrap)
  }

  @Test
  fun `useBootstrap() should reflect the bootstrap flag`() {
    assertTrue(Config.LwM2m(bootstrap = true).useBootstrap())
    assertFalse(Config.LwM2m(bootstrap = false).useBootstrap())
  }

  @Test
  fun `useSecureMode() should require both identity and psk to be present`() {
    val bothSet = Config.LwM2m(security = Config.LwM2m.Security(identity = "id", psk = "aa"))
    val missingPsk = Config.LwM2m(security = Config.LwM2m.Security(identity = "id", psk = null))
    val missingIdentity =
        Config.LwM2m(security = Config.LwM2m.Security(identity = null, psk = "aa"))
    val none = Config.LwM2m(security = Config.LwM2m.Security())

    assertTrue(bothSet.useSecureMode())
    assertFalse(missingPsk.useSecureMode())
    assertFalse(missingIdentity.useSecureMode())
    assertFalse(none.useSecureMode())
  }
}
