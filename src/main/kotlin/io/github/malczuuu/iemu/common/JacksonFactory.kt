package io.github.malczuuu.iemu.common

import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper

object JacksonFactory {

  fun getJsonMapper(): JsonMapper = JsonMapper.builder().findAndAddModules().build()

  fun getYamlMapper(): YAMLMapper = YAMLMapper.builder().findAndAddModules().build()
}
