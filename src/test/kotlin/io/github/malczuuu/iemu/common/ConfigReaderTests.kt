package io.github.malczuuu.iemu.common

import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import tools.jackson.core.JacksonException
import tools.jackson.dataformat.yaml.YAMLMapper

class ConfigReaderTests {

  private val yaml =
      """
      http:
        port: 9000
      lwm2m:
        enabled: true
        endpoint: my-client
      """
          .trimIndent()
          .toByteArray()

  @Test
  fun `readConfig() should parse the default config path when no profile is given`() {
    var seenPath: Path? = null
    val reader =
        ConfigReader(
            fileReader = {
              seenPath = it
              yaml
            },
        )

    val config = reader.readConfig("")

    assertEquals(Path.of("data/config.yml"), seenPath)
    assertEquals(9000, config.http.port)
    assertEquals("my-client", config.lwM2m.endpoint)
  }

  @Test
  fun `readConfig() should parse a profile-specific config path when a profile is given`() {
    var seenPath: Path? = null
    val reader =
        ConfigReader(
            fileReader = {
              seenPath = it
              yaml
            },
        )

    reader.readConfig("dev")

    assertEquals(Path.of("data/config-dev.yml"), seenPath)
  }

  @Test
  fun `readConfig() should throw ConfigLoadException when the file cannot be read`() {
    val reader = ConfigReader(fileReader = { throw IOException("boom") })

    val ex = assertThrows(ConfigLoadException::class.java) { reader.readConfig("") }
    assertEquals("Unable to read config from data/config.yml file", ex.message)
  }

  @Test
  fun `readConfig() should throw ConfigLoadException when the mapper fails to parse the bytes`() {
    val mapper = mockk<YAMLMapper>()
    every { mapper.readValue(any<ByteArray>(), Config::class.java) } throws
        object : JacksonException("bad yaml") {}
    val reader = ConfigReader(mapper = mapper, fileReader = { ByteArray(0) })

    assertThrows(ConfigLoadException::class.java) { reader.readConfig("prod") }
  }
}
