package com.nyampass.cabin;

import com.sun.tools.doclint.Env;

import java.util.HashMap;
import java.util.Map;

public class Environ {
    private String name;
    private WebSocket socket;

    private static int counter = 0;

    private Environ() {
        this.name = "env-" + (counter++);
    }

    public static Environ instance() {
        return new EnvironThreadLocal().get();
    }

    static class EnvironThreadLocal extends ThreadLocal<Environ> {
        @Override
        protected Environ initialValue() {
            return new Environ();
        }
    }

}
