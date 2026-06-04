package io.github.malczuuu.iemu.settings

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException

class SettingsReader(
    private val fileReader: (Path) -> String = { Files.readString(it) },
) {

  fun readSettings(profile: String): Settings {
    var lastException: IOException? = null
    for (ext in listOf("yaml", "yml")) {
      val path = settingsPath(profile, ext)
      try {
        val content = fileReader(path)
        return try {
          val map: Map<String, Any?> = Yaml().load(content) ?: emptyMap()
          log.info("Loaded settings from {}", path)
          parseSettings(map)
        } catch (e: YAMLException) {
          throw SettingsException("Unable to read settings from $path file", e)
        }
      } catch (e: IOException) {
        lastException = e
      }
    }
    val path = settingsPath(profile, "yml")
    throw SettingsException("Unable to read settings from $path file", lastException!!)
  }

  private fun settingsBaseName(profile: String): String =
      if (profile == "default") "config" else "config-$profile"

  private fun settingsPath(profile: String, ext: String): Path =
      Paths.get("data/${settingsBaseName(profile)}.$ext")

  private fun parseSettings(map: Map<String, Any?>): Settings {
    val httpMap = map["http"] as? Map<*, *> ?: emptyMap<Any, Any>()
    val lwm2mMap = map["lwm2m"] as? Map<*, *> ?: emptyMap<Any, Any>()
    val securityMap = lwm2mMap["security"] as? Map<*, *> ?: emptyMap<Any, Any>()

    return Settings(
        http = Settings.Http(port = (httpMap["port"] as? Int) ?: 4500),
        lwM2m =
            Settings.LwM2m(
                isEnabled = (lwm2mMap["enabled"] as? Boolean) ?: false,
                localPort = (lwm2mMap["localPort"] as? Int) ?: 0,
                endpoint = lwm2mMap["endpoint"] as? String ?: "client",
                bootstrap = (lwm2mMap["bootstrap"] as? Boolean) ?: false,
                upstream = lwm2mMap["upstream"] as? String ?: "localhost:5683",
                security =
                    Settings.LwM2m.Security(
                        identity = securityMap["identity"] as? String,
                        psk = securityMap["psk"] as? String,
                    ),
            ),
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(SettingsReader::class.java)
  }
}
