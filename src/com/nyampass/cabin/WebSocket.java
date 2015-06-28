package com.nyampass.cabin;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocket {
    Session session = null;
    private final WebSocketHandler handler;

    private static final URI ENDPOINT_URI = URI.create("ws://cabin.nyampass.com/ws");

    public WebSocket(WebSocketHandler handler) {
        this(ENDPOINT_URI, handler);
    }

    public WebSocket(URI endpointURI, WebSocketHandler handler) {
        this.handler = handler;
        try {
            WebSocketContainer container =  ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        appendMessage("CONNECTED");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
        appendMessage("DISCONNECTED");
    }

    @OnMessage
    public void onMessage(String message) {
        this.handler.appendLog("RESPONSE: "+ message);
        this.handler.handleMessage(message);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        this.handler.appendLog("ERROR: " + t.getLocalizedMessage());
    }

    public void send(String message) {
        this.session.getAsyncRemote().sendText(message);
    }

    public static interface WebSocketHandler {
        public void handleMessage(String message);

        void appendLog(String log);
    }

    private void appendMessage(String log) {
        this.handler.appendLog(log);
    }
}
