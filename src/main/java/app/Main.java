package app;

import io.javalin.Javalin;
import io.javalin.embeddedserver.jetty.websocket.WsSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static Map<String, Collab> collabs = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        Javalin.create()
            .port(7070)
            .enableStaticFiles("/public")
            .ws("/docs/:doc-id", ws -> {
                ws.onConnect(session -> {
                    if (getCollab(session) == null) {
                        createCollab(session);
                    }
                    getCollab(session).sessions.add(session);
                    session.send(getCollab(session).doc);
                });
                ws.onMessage((session, message) -> {
                    getCollab(session).doc = message;
                    getCollab(session).sessions.stream().filter(WsSession::isOpen).forEach(s -> {
                        s.send(getCollab(session).doc);
                    });
                });
                ws.onClose((session, status, message) -> {
                    getCollab(session).sessions.remove(session);
                });
            })
            .start();

    }

    private static Collab getCollab(WsSession session) {
        return collabs.get(session.param("doc-id"));
    }

    private static void createCollab(WsSession session) {
        collabs.put(session.param("doc-id"), new Collab());
    }

}
