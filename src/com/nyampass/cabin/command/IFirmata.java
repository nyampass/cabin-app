package com.nyampass.cabin.command;

public interface IFirmata {
    public void digitalWrite(int pinNo, boolean value);
    public void onValueChange(int pinNo, Object eventListener);
}
