package com.nyampass.cabin.command;

import com.nyampass.cabin.Driver;
import com.nyampass.cabin.Environ;
import gnu.mapping.Procedure;
import jssc.SerialPortList;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;
import org.firmata4j.firmata.FirmataDevice;

import javax.jws.WebParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class FirmataDriver implements Driver.DriverImpl, IFirmata {
    private final FirmataDevice device;

    private static FirmataDriver instance;

    public static Driver.DriverImpl instance() {
        if (instance == null)
            instance = new FirmataDriver();
        return instance;
    }

    public void onDestroy() {
        if (this.device != null)
            try {
                this.device.stop();
            } catch (IOException e) {
                // do nothing
            }
        instance = null;
    }

    public FirmataDriver() {
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

    @Override
    public void onValueChange(int pinNo, Object eventListener) {
        onValueChangeEvent(pinNo, v -> {
            try {
                ((Procedure)eventListener).apply1(v);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void onValueChangeEvent(int pinNo, Consumer<Integer> eventListener) {
        try {
            this.device.getPin(pinNo).setMode(Pin.Mode.INPUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.device.getPin(pinNo).addEventListener(new PinEventListener() {
            @Override
            public void onModeChange(IOEvent ioEvent) {}

            @Override
            public void onValueChange(IOEvent ioEvent) {
                try {
                    eventListener.accept((int)ioEvent.getValue());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
