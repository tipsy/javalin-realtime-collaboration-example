package app

import io.javalin.Javalin
import io.javalin.websocket.WsSession
import java.util.concurrent.ConcurrentHashMap

data class Collaboration(var doc: String = "", val sessions: MutableSet<WsSession> = ConcurrentHashMap.newKeySet())

fun main(args: Array<String>) {

    val collaborations = ConcurrentHashMap<String, Collaboration>()

    Javalin.create().apply {
        enableStaticFiles("/public")
        ws("/docs/:doc-id") { ws ->
            ws.onConnect { session ->
                if (collaborations[session.docId] == null) {
                    collaborations[session.docId] = Collaboration()
                }
                collaborations[session.docId]!!.sessions.add(session)
                session.send(collaborations[session.docId]!!.doc)
            }
            ws.onMessage { session, message ->
                collaborations[session.docId]!!.doc = message
                collaborations[session.docId]!!.sessions.filter { it.isOpen }.forEach {
                    it.send(collaborations[session.docId]!!.doc)
                }
            }
            ws.onClose { session, _, _ ->
                collaborations[session.docId]!!.sessions.remove(session)
            }
        }
    }.start(7070)

}

val WsSession.docId: String get() = this.pathParam("doc-id")
