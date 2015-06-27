package com.nyampass.cabin.command;

import jssc.SerialPort;
import jssc.SerialPortList;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

/**
 * Created by tokusei on 15/06/27.
 */
@SuppressWarnings("unused")
public class FirmataCommand {
    private final FirmataDevice device;

    public FirmataCommand() {
        if (SerialPortList.getPortNames().length <= 0) {
            throw new RuntimeException("not found serial port");
        }
        String portName = SerialPortList.getPortNames()[0];
        this.device = new FirmataDevice(portName);
        try {
            this.device.start();
            this.device.ensureInitializationIsDone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void digitalWrite(int pinNo, boolean value) {
        Pin pin = this.device.getPin(pinNo);
        try {
            pin.setMode(Pin.Mode.OUTPUT);
            pin.setValue(value ? 1 : 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
