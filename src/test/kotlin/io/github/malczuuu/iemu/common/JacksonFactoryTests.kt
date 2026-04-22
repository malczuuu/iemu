package io.github.malczuuu.iemu.common

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

class JacksonFactoryTests {

  @Test
  fun `getJsonMapper() should return a fresh JsonMapper instance on every call`() {
    val first = JacksonFactory.getJsonMapper()
    val second = JacksonFactory.getJsonMapper()

    assertNotNull(first)
    assertNotSame(first, second)
  }

  @Test
  fun `getYamlMapper() should return a fresh YamlMapper instance on every call`() {
    val first = JacksonFactory.getYamlMapper()
    val second = JacksonFactory.getYamlMapper()

    assertNotNull(first)
    assertNotSame(first, second)
  }
}
