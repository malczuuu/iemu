package io.github.malczuuu.iemu.infra.http

import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsErrorContext
import io.javalin.websocket.WsMessageContext
import java.util.concurrent.Executors
import org.slf4j.LoggerFactory

class WebSocketService {

  private val executorService = Executors.newSingleThreadExecutor()

  private val sessions: MutableMap<String, WsContext> = mutableMapOf()

  fun onConnect(session: WsConnectContext) {
    executorService.submit {
      log.debug("Connected session={}", session.sessionId())
      sessions[session.sessionId()] = session
    }
  }

  fun onMessage(session: WsMessageContext) {
    executorService.submit {
      log.debug("Received message={} from session={}", session.message(), session.sessionId())
    }
  }

  fun onClose(session: WsCloseContext) {
    executorService.submit {
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
    executorService.submit {
      log.error("An error occurred in session={}", session.sessionId(), session.error())
      sessions.remove(session.sessionId())
      session.session.close()
    }
  }

  fun sendMessage(message: String) {
    executorService.submit {
      sessions.forEach { (key, value) ->
        try {
          value.send(message)
        } catch (t: Throwable) {
          log.error("Failed to send message={} to session={}", message, key, t)
        }
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(WebSocketService::class.java)
  }
}
