package com.nyampass.cabin.command;

import com.nyampass.cabin.Environ;
import com.nyampass.cabin.WebSocket;

import java.util.*;
import java.util.function.Consumer;

public class CommandRunner implements WebSocket.WebSocketHandler {
    private final String klass;
    private final String password;
    private final String to;
    private final Map<String,Consumer<Object>> listeners;

    public CommandRunner(String klass, String to, String password) {
        this.klass = klass;
        this.to = to;
        this.password = password;
        this.listeners = new HashMap<>();
    }

    public Object run(String name, Object[] args) {
        return run(command(name, Arrays.asList(args)));
    }

    public Object run(String name, List<Object> args) {
        return run(command(name, args));
    }

    private Object run(Command command) {
        Environ environ = Environ.instance();
        String from = environ.peerId;
        WebSocket socket = environ.socket;
        String id = UUID.randomUUID().toString();

        socket.sendCommand(id, from, to, command.password, command.klass, command.command, command.args);

        WebSocket.Response response;
        try {
            response = socket.getNextResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (response.type == WebSocket.Response.Type.Error) {
            throw new RuntimeException(response.cause);
        } else if (id.equals(response.id)) {
            return response.value;
        } else { // if response id doesn't match the corresponding request id
            throw new IllegalStateException();
        }
    }

    public void setEventListener(String name, Object[] args, Consumer<Object> listener) {
        setEventListener(name, Arrays.asList(args), listener);
    }

    public void setEventListener(String name, List<Object> args, Consumer<Object> listener) {
        setEventListener(command(name, args), listener);
    }

    public void setEventListener(Command command, Consumer<Object> listener) {
        Environ environ = Environ.instance();
        String from = environ.peerId;
        WebSocket socket = environ.socket;
        String id = UUID.randomUUID().toString();

        socket.addHandler(this);
        listeners.put(id, listener);

        socket.sendCommand(id, from, to, command.password, command.klass, command.command, command.args);
    }

    @Override
    public void handleEvent(WebSocket.Response event) {
        String id = event.id;
        if (listeners.containsKey(id)) {
            Consumer<Object> listener = listeners.get(id);
            listener.accept(event.value);
        }
    }

    @Override
    public void handleCommand(WebSocket.Request command) {}

    @Override
    public void appendLog(String log) {}

    @Override
    public void onSetPeerId(String peerId) {}

    private Command command(String name, List<Object> args) {
        return new Command(this.klass, name, this.to, this.password, args);
    }

    public static Command command(String klass, String commmad, String peerId, String password, List<Object> args) {
        return new Command(klass, commmad, peerId, password, args);
    }

    private static class Command {
        private final String klass;
        private final String command;
        private final String peerId;
        private final String password;
        private final List<Object> args;

        public Command(String klass, String command, String peerId, String password, List<Object> args) {
            this.klass = klass;
            this.command = command;
            this.peerId = peerId;
            this.password = password;
            this.args = args;
        }
    }
}
