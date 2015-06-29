package com.nyampass.cabin.command;

import com.nyampass.cabin.Driver;
import jssc.SerialPortList;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

@SuppressWarnings("unused")
public class FirmataCommand implements IFirmata {
    private final FirmataDevice device;

    static {
        Driver.registerClass("Firmata", FirmataCommand.class);
    }

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

    @Override
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
