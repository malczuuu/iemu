package io.github.malczuuu.iemu.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsTests {

  @Test
  fun `default Settings should expose documented fallback values`() {
    val settings = Settings()

    assertEquals(4500, settings.http.port)
    assertFalse(settings.lwM2m.isEnabled)
    assertEquals(0, settings.lwM2m.localPort)
    assertEquals("client", settings.lwM2m.endpoint)
    assertEquals("localhost:5683", settings.lwM2m.upstream)
    assertFalse(settings.lwM2m.bootstrap)
  }

  @Test
  fun `useBootstrap() should reflect the bootstrap flag`() {
    assertTrue(Settings.LwM2m(bootstrap = true).useBootstrap())
    assertFalse(Settings.LwM2m(bootstrap = false).useBootstrap())
  }

  @Test
  fun `useSecureMode() should require both identity and psk to be present`() {
    val bothSet = Settings.LwM2m(security = Settings.LwM2m.Security(identity = "id", psk = "aa"))
    val missingPsk = Settings.LwM2m(security = Settings.LwM2m.Security(identity = "id", psk = null))
    val missingIdentity =
        Settings.LwM2m(security = Settings.LwM2m.Security(identity = null, psk = "aa"))
    val none = Settings.LwM2m(security = Settings.LwM2m.Security())

    assertTrue(bothSet.useSecureMode())
    assertFalse(missingPsk.useSecureMode())
    assertFalse(missingIdentity.useSecureMode())
    assertFalse(none.useSecureMode())
  }
}
