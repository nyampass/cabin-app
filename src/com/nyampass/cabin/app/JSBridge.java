package com.nyampass.cabin.app;

@SuppressWarnings("unused")
public class JSBridge {
    public void log(String text) {
        Controller.instance().appendLog(text);
    }

    public void setPeerId(String id) {
        Controller.instance().setPeerId(id);
    }

    public static void delay(float second) {
        try {
            Thread.sleep((long) (second * 1000));
        } catch (InterruptedException e) {
            Controller.instance().appendLog(e);
        }
    }
}
