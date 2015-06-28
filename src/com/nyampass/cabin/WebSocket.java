package com.nyampass.cabin;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.ws.util.StringUtils;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@SuppressWarnings("unused")
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
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
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
        this.handler.appendLog("RESPONSE: " + message);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Response response = mapper.readValue(message, Response.class);
            switch (response.type) {
                case Connected:
                    this.handler.onSetPeerId(response.peerId);
                    break;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        this.handler.appendLog("ERROR: " + t.getLocalizedMessage());
    }

    public void send(String message) {
        appendMessage(message);
        this.session.getAsyncRemote().sendText(message);
    }

    public void sendRequestPromote(String from, String password) {
        String request = new Request(Request.Type.Promote, from).password(password).toJson();
        send(request);
    }

    public void sendDemote(String from) {
        String request = new Request(Request.Type.Demote, from).toJson();
        send(request);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        enum Type {
            Promote, Demote, Connected;

            @JsonCreator
            public static Type fromString(String value) {
                return valueOf(StringUtils.capitalize(value));
            }
        }

        public Type type;

        public String status;

        @JsonProperty(value = "peer-id")
        public String peerId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Request {
        enum Type {
            Promote, Demote;

            @SuppressWarnings("unused")
            @JsonValue
            public String jsonName() {
                return this.toString().toLowerCase();
            }
        }

        public Type type;
        public String from;
        public String password;

        Request(Type type, String from) {
            this.type = type;
            this.from = from;
        }

        Request password(String password) {
            this.password = password;
            return this;
        }

        String toJson() {
            try {
                ObjectMapper mapper = new ObjectMapper();
                // mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                return mapper.writeValueAsString(this);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface WebSocketHandler {
        void handleMessage(String message);

        void appendLog(String log);

        void onSetPeerId(String peerId);
    }

    private void appendMessage(String log) {
        this.handler.appendLog(log);
    }
}
