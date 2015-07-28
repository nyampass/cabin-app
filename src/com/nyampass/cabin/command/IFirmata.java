package com.nyampass.cabin.command;

public interface IFirmata {
    public void digitalWrite(int pinNo, boolean value);
    public void on(String eventName, Object eventListener);
}
