package com.nyampass.cabin.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRunner {
    private static final Map<String, Class> classes = new HashMap<>();

    private final String klass;
    private final String password;
    private final String to;

    public CommandRunner(String klass, String to, String password) {
        this.klass = klass;
        this.to = to;
        this.password = password;
    }

    public static void registerClass(String name, Class klass) {
        classes.put(name, klass);
    }

    public Response run(String name, Object[] args) {
        return run(command(name, Arrays.asList(args)));
    }

    public Response run(String name, List<Object> args) {
        return run(command(name, args));
    }

    private Response run(Command command) {
        // do something
        return null;
    }

    private Command command(String name, List<Object> args) {
        return new Command(this.klass, name, this.peerId, this.password);
    }

    public static Command command(String klass, String commmad, String peerId, String password) {
        return new Command(klass, commmad, peerId, password);
    }

    public enum ResponseType { Int, String, Bool }

    public static class Response {
        public ResponseType type;
        public Object value;

    }

    private static class Command {
        private final String klass;
        private final String command;
        private final String peerId;
        private final String password;

        public Command(String klass, String command, String peerId, String password) {
            this.klass = klass;
            this.command = command;
            this.peerId = peerId;
            this.password = password;
        }
    }
}