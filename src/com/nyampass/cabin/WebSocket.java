package com.nyampass.cabin;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.ws.util.StringUtils;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@ClientEndpoint
public class WebSocket {
    Session session = null;
    private final WebSocketHandler handler;

    private final BlockingQueue<Response> queue = new LinkedBlockingQueue<Response>();

    //private static final URI ENDPOINT_URI = URI.create("ws://cabin.nyampass.com/ws");
    private static final URI ENDPOINT_URI = URI.create("ws://localhost:3000/ws");

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
                case Promote:
                case Demote:
                    // FIXME: check if status is ok
                    break;
                case Command:
                    Request request = mapper.readValue(message, Request.class);
                    handler.handleCommand(request);
                    break;
                case Result:
                    queue.add(response);
                    break;
                case Error:
                    queue.add(response);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        this.handler.appendLog("ERROR: " + t.getLocalizedMessage());
    }

    public void send(String message) {
        appendMessage("SEND: " + message);
        this.session.getAsyncRemote().sendText(message);
    }

    public void sendPromote(String from, String password) {
        Request request = new Request(Request.Type.Promote, from).password(password);
        String customName = Environ.instance().customName;
        String customNamePassword = Environ.instance().customNamePassword;
        if (customName != null) {
            request.customName(customName, customNamePassword);
        }
        send(request.toJson());
    }

    public void sendDemote(String from) {
        String request = new Request(Request.Type.Demote, from).toJson();
        send(request);
    }

    public void sendCommand(String id, String from, String to, String password, String klass, String command, List<Object> args) {
        String request = new Request(id)
                .from(from)
                .to(to)
                .password(password)
                .command(klass, command, args)
                .toJson();
        send(request);
    }

    public Response getNextResponse() throws InterruptedException {
        return queue.take();
    }

    public Response getNextResponse(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public void sendResult(String id, String from, String to, Object ret) {
        String request = new Request(id)
                .from(from)
                .to(to)
                .result(ret)
                .toJson();
        send(request);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        public enum Type {
            Promote, Demote, Connected, Command, Result, Error;

            @JsonCreator
            public static Type fromString(String value) {
                return valueOf(StringUtils.capitalize(value));
            }
        }

        enum ResultType {
            Int, String, Bool;
            @JsonCreator
            public static ResultType fromString(String value) {
                return valueOf(StringUtils.capitalize(value));
            }
        }

        public String id;
        public Type type;

        public String status;

        public Object value;

        public String cause;

        @JsonProperty(value = "peer-id")
        public String peerId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        enum Type {
            Promote, Demote, Command, Result;

            @SuppressWarnings("unused")
            @JsonValue
            public String jsonName() {
                return this.toString().toLowerCase();
            }
        }

        public String id;
        public Type type;
        public String from;
        public String to;
        public String password;
        public String klass;
        public String command;
        public List<Object> args;

        public Object value;

        @JsonProperty("custom-name")
        public String customName;
        @JsonProperty("custom-name-password")
        public String customNamePassword;

        Request() {
        }

        Request(String id) {
            this.id = id;
        }

        Request(Type type, String from) {
            this.type = type;
            this.from = from;
        }

        Request from(String from) {
            this.from = from;
            return this;
        }

        Request to(String to) {
            this.to = to;
            return this;
        }

        Request result(Object value) {
            this.type = Type.Result;
            this.value = value;
            return this;
        }

        Request password(String password) {
            this.password = password;
            return this;
        }

        Request customName(String customName, String customNamePassword) {
            this.customName = customName;
            this.customNamePassword = customNamePassword;
            return this;
        }

        Request command(String klass, String command, List<Object> args) {
            this.type = Request.Type.Command;
            this.klass = klass;
            this.command = command;
            this.args = args;
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
        void handleCommand(Request command);

        void appendLog(String log);

        void onSetPeerId(String peerId);
    }

    private void appendMessage(String log) {
        this.handler.appendLog(log);
    }
}
