package io.github.malczuuu.iemu.common

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory
import tools.jackson.core.JacksonException
import tools.jackson.dataformat.yaml.YAMLMapper

class ConfigReader(private val mapper: YAMLMapper = JacksonFactory.getYamlMapper()) {

  fun readConfig(profile: String): Config {
    val filename = configFilename(profile)
    return try {
      val config = mapper.readValue(Files.readAllBytes(Paths.get(filename)), Config::class.java)
      log.info("Loaded config from config {} file", filename)
      config
    } catch (e: JacksonException) {
      log.error("Unable to read config from {} file", filename, e)
      exitProcess(1)
    } catch (e: IOException) {
      log.error("Unable to read config from {} file", filename, e)
      exitProcess(1)
    }
  }

  private fun configFilename(profile: String): String =
      if (profile.isNotEmpty()) "data/config-$profile.yml" else "data/config.yml"

  companion object {
    private val log = LoggerFactory.getLogger(ConfigReader::class.java)
  }
}
