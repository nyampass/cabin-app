package com.nyampass.cabin;

import com.sun.tools.doclint.Env;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;
import java.util.Map;

public class Environ {
    private String name;
    public String peerId;
    public WebSocket socket;
    public GraphicsContext graphicsContext;

    private static int counter = 0;

    private static ThreadLocal<Environ> instance = new ThreadLocal<Environ>() {
        @Override
        protected Environ initialValue() {
            return new Environ();
        }
    };

    private Environ() {
        this.name = "env-" + (counter++);
    }

    public static Environ instance() {
        return instance.get();
    }
}
