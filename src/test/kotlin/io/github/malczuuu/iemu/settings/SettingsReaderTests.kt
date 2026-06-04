package io.github.malczuuu.iemu.settings

import java.io.IOException
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SettingsReaderTests {

  private val yaml =
      """
      http:
        port: 9000
      lwm2m:
        enabled: true
        endpoint: my-client
      """
          .trimIndent()

  @Test
  fun `readSettings() should parse the default config path when no profile is given`() {
    var seenPath: Path? = null
    val reader =
        SettingsReader(
            fileReader = {
              seenPath = it
              yaml
            }
        )

    val settings = reader.readSettings("")

    assertEquals(Path.of("data/config.yml"), seenPath)
    assertEquals(9000, settings.http.port)
    assertEquals("my-client", settings.lwM2m.endpoint)
  }

  @Test
  fun `readSettings() should parse a profile-specific config path when a profile is given`() {
    var seenPath: Path? = null
    val reader =
        SettingsReader(
            fileReader = {
              seenPath = it
              yaml
            }
        )

    reader.readSettings("dev")

    assertEquals(Path.of("data/config-dev.yml"), seenPath)
  }

  @Test
  fun `readSettings() should throw SettingsLoadException when the file cannot be read`() {
    val reader = SettingsReader(fileReader = { throw IOException("boom") })

    val ex = assertThrows(SettingsException::class.java) { reader.readSettings("") }
    assertEquals("Unable to read settings from data/config.yml file", ex.message)
  }

  @Test
  fun `readSettings() should throw SettingsLoadException when the YAML is invalid`() {
    val reader = SettingsReader(fileReader = { "key: [unclosed" })

    assertThrows(SettingsException::class.java) { reader.readSettings("") }
  }
}
