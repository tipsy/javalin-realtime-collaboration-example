package app

import io.javalin.Javalin
import io.javalin.embeddedserver.jetty.websocket.WsSession
import java.util.concurrent.ConcurrentHashMap

data class Collaboration(var doc: String = "", val sessions: MutableSet<WsSession> = ConcurrentHashMap.newKeySet<WsSession>())

fun main(args: Array<String>) {

    val collaborations = ConcurrentHashMap<String, Collaboration>()

    val app = Javalin.create().apply {
        port(7070)
        enableStaticFiles("/public")
    }.start()

    app.ws("/docs/:doc-id") { ws ->
        ws.onConnect({ session ->
            if (collaborations[session.docId] == null) {
                collaborations[session.docId] = Collaboration()
            }
            collaborations[session.docId]!!.sessions.add(session)
            session.send(collaborations[session.docId]!!.doc)
        })
        ws.onMessage({ session, message ->
            collaborations[session.docId]!!.doc = message
            collaborations[session.docId]!!.sessions.filter { it.isOpen }.forEach {
                it.send(collaborations[session.docId]!!.doc)
            }
        })
        ws.onClose({ session, _, _ ->
            collaborations[session.docId]!!.sessions.remove(session)
        })
    }

}

val WsSession.docId: String get() = this.param("doc-id")!! // is always present, or route won't match
