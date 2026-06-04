package io.github.malczuuu.iemu.common

import tools.jackson.databind.json.JsonMapper

object JacksonFactory {

  val jsonMapper: JsonMapper = JsonMapper.builder().findAndAddModules().build()
}
