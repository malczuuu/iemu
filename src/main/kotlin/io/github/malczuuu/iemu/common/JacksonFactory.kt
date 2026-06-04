package io.github.malczuuu.iemu.common

import tools.jackson.databind.json.JsonMapper

object JacksonFactory {

  fun getJsonMapper(): JsonMapper = JsonMapper.builder().findAndAddModules().build()
}
