package com.nyampass.cabin.command;

import com.nyampass.cabin.Environ;
import com.nyampass.cabin.WebSocket;

import java.util.*;

public class CommandRunner {
    private final String klass;
    private final String password;
    private final String to;

    public CommandRunner(String klass, String to, String password) {
        this.klass = klass;
        this.to = to;
        this.password = password;
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
            if (id.equals(response.id)) {
                return response.value;
            } else {
                // throw new IllegalStateException();
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
