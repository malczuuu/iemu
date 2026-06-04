package io.github.malczuuu.iemu.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebSocketEventTests {

  @Test
  fun `type and body should be exposed as given to the constructor`() {
    val event = WebSocketEvent("state", mapOf("on" to true))

    assertEquals("state", event.type)
    assertEquals(mapOf("on" to true), event.body)
  }

  @Test
  fun `two events with identical fields should be equal and share a hash code`() {
    val a = WebSocketEvent("firmware", 1)
    val b = WebSocketEvent("firmware", 1)

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }
}
