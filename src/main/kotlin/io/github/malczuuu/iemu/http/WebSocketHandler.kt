package io.github.malczuuu.iemu.http

import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsErrorContext
import io.javalin.websocket.WsMessageContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.slf4j.LoggerFactory

class WebSocketHandler(private val executor: Executor = Executors.newSingleThreadExecutor()) {

  private val sessions: MutableMap<String, WsContext> = mutableMapOf()

  fun onConnect(session: WsConnectContext) {
    executor.execute {
      log.debug("Connected session={}", session.sessionId())
      sessions[session.sessionId()] = session
    }
  }

  fun onMessage(session: WsMessageContext) {
    executor.execute {
      log.debug("Received message={} from session={}", session.message(), session.sessionId())
    }
  }

  fun onClose(session: WsCloseContext) {
    executor.execute {
      log.debug(
          "Closed session={} with statusCode={}, reason={}",
          session.sessionId(),
          session.status(),
          session.reason(),
      )
      sessions.remove(session.sessionId())
    }
  }

  fun onError(session: WsErrorContext) {
    executor.execute {
      log.error("An error occurred in session={}", session.sessionId(), session.error())
      sessions.remove(session.sessionId())
      session.session.close()
    }
  }

  fun sendMessage(message: String) {
    executor.execute {
      sessions.forEach { (key, context) ->
        try {
          context.send(message)
        } catch (t: Throwable) {
          log.error("Failed to send message={} to session={}", message, key, t)
        }
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(WebSocketHandler::class.java)
  }
}
