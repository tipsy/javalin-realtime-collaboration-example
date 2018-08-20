package app;

import io.javalin.websocket.WsSession;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Collab {
    public String doc;
    public Set<WsSession> sessions;

    public Collab() {
        this.doc = "";
        this.sessions = ConcurrentHashMap.newKeySet();
    }
}
