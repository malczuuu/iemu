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
  fun `readSettings() should try yaml extension first for the default profile`() {
    var seenPath: Path? = null
    val reader =
        SettingsReader(
            fileReader = {
              seenPath = it
              yaml
            }
        )

    val settings = reader.readSettings("default")

    assertEquals(Path.of("data/config.yaml"), seenPath)
    assertEquals(9000, settings.http.port)
    assertEquals("my-client", settings.lwM2m.endpoint)
  }

  @Test
  fun `readSettings() should fall back to yml when yaml is not found for the default profile`() {
    var seenPath: Path? = null
    val reader =
        SettingsReader(
            fileReader = { path ->
              if (path.toString().endsWith(".yaml")) throw IOException("not found")
              seenPath = path
              yaml
            }
        )

    reader.readSettings("default")

    assertEquals(Path.of("data/config.yml"), seenPath)
  }

  @Test
  fun `readSettings() should try yaml extension first for a named profile`() {
    var seenPath: Path? = null
    val reader =
        SettingsReader(
            fileReader = {
              seenPath = it
              yaml
            }
        )

    reader.readSettings("dev")

    assertEquals(Path.of("data/config-dev.yaml"), seenPath)
  }

  @Test
  fun `readSettings() should throw SettingsLoadException when neither yaml nor yml can be read`() {
    val reader = SettingsReader(fileReader = { throw IOException("boom") })

    val ex = assertThrows(SettingsException::class.java) { reader.readSettings("default") }
    assertEquals("Unable to read settings from data/config.yml file", ex.message)
  }

  @Test
  fun `readSettings() should throw SettingsLoadException when the YAML is invalid`() {
    val reader = SettingsReader(fileReader = { "key: [unclosed" })

    assertThrows(SettingsException::class.java) { reader.readSettings("default") }
  }
}
