package io.github.malczuuu.iemu.common

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.LoggerFactory
import tools.jackson.core.JacksonException
import tools.jackson.dataformat.yaml.YAMLMapper

class ConfigReader(
    private val mapper: YAMLMapper = JacksonFactory.getYamlMapper(),
    private val fileReader: (Path) -> ByteArray = { Files.readAllBytes(it) },
) {

  fun readConfig(profile: String): Config {
    val path = configPath(profile)
    return try {
      val config = mapper.readValue(fileReader(path), Config::class.java)
      log.info("Loaded config from config {} file", path)
      config
    } catch (e: JacksonException) {
      throw ConfigLoadException("Unable to read config from $path file", e)
    } catch (e: IOException) {
      throw ConfigLoadException("Unable to read config from $path file", e)
    }
  }

  private fun configPath(profile: String): Path =
      Paths.get(if (profile.isNotEmpty()) "data/config-$profile.yml" else "data/config.yml")

  companion object {
    private val log = LoggerFactory.getLogger(ConfigReader::class.java)
  }
}
