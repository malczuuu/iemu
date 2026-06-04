package io.github.malczuuu.iemu.infra.http

import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsErrorContext
import io.javalin.websocket.WsMessageContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.Executor
import org.junit.jupiter.api.Test

class WebSocketHandlerTests {

  // Matches the production ExecutorService semantics: uncaught errors in a background task
  // don't propagate back to the caller. Running synchronously keeps assertions simple.
  private val synchronousExecutor = Executor { runnable ->
    try {
      runnable.run()
    } catch (_: Throwable) {}
  }

  private fun service() = WebSocketHandler(synchronousExecutor)

  private fun connectContext(id: String) =
      mockk<WsConnectContext>(relaxed = true).also { every { it.sessionId() } returns id }

  private fun messageContext(id: String, message: String) =
      mockk<WsMessageContext>(relaxed = true).also {
        every { it.sessionId() } returns id
        every { it.message() } returns message
      }

  private fun closeContext(id: String) =
      mockk<WsCloseContext>(relaxed = true).also {
        every { it.sessionId() } returns id
        every { it.status() } returns 1000
        every { it.reason() } returns "bye"
      }

  private fun errorContext(id: String): WsErrorContext {
    val ctx = mockk<WsErrorContext>(relaxed = true)
    every { ctx.sessionId() } returns id
    every { ctx.error() } returns RuntimeException("boom")
    return ctx
  }

  @Test
  fun `onConnect() should register the session so sendMessage reaches it`() {
    val svc = service()
    val ctx = connectContext("s1")

    svc.onConnect(ctx)
    svc.sendMessage("hello")

    verify { (ctx as WsContext).send("hello") }
  }

  @Test
  fun `onMessage() should not alter the registered sessions`() {
    val svc = service()
    val connect = connectContext("s1")
    svc.onConnect(connect)

    svc.onMessage(messageContext("s1", "ping"))
    svc.sendMessage("hi")

    verify { (connect as WsContext).send("hi") }
  }

  @Test
  fun `onClose() should remove the session so it no longer receives messages`() {
    val svc = service()
    val connect = connectContext("s1")
    svc.onConnect(connect)

    svc.onClose(closeContext("s1"))
    svc.sendMessage("after-close")

    verify(exactly = 0) { (connect as WsContext).send(any<String>()) }
  }

  @Test
  fun `onError() should remove the session so it no longer receives messages`() {
    val svc = service()
    val connect = connectContext("s1")
    svc.onConnect(connect)

    svc.onError(errorContext("s1"))
    svc.sendMessage("after-error")

    verify(exactly = 0) { (connect as WsContext).send(any<String>()) }
  }

  @Test
  fun `sendMessage() should fan out to every registered session and swallow per-session errors`() {
    val svc = service()
    val ok = connectContext("ok")
    val bad = connectContext("bad")
    every { (bad as WsContext).send(any<String>()) } throws RuntimeException("network dropped")
    svc.onConnect(ok)
    svc.onConnect(bad)

    svc.sendMessage("broadcast")

    verify { (ok as WsContext).send("broadcast") }
    verify { (bad as WsContext).send("broadcast") }
  }

  @Test
  fun `sendMessage() should reach zero sessions when none are registered`() {
    // A smoke test for the empty-fan-out branch. Should not throw.
    service().sendMessage("nobody's listening")
  }
}
