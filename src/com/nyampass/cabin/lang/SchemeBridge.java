package com.nyampass.cabin.lang;

import com.nyampass.cabin.app.Controller;
import com.nyampass.cabin.command.CommandRunner;
import com.nyampass.cabin.command.FirmataCommand;
import com.nyampass.cabin.command.IFirmata;
import gnu.expr.ModuleBody;
import gnu.expr.RunnableModule;
import gnu.mapping.*;
import gnu.math.DFloNum;

@SuppressWarnings("unused")
public class SchemeBridge extends ModuleBody implements RunnableModule {
    private CallContext context;

    @Override
    public void run(CallContext ctx) throws Throwable {
        this.context = ctx;
        super.run(ctx);
    }

    public static final Procedure0 canvas = new Procedure0("canvas") {
        @Override
        public Object apply0() throws Throwable {
            return Controller.instance().graphicsContext();
        }
    };

    public static final Procedure1 delay = new Procedure1("delay") {
        @Override
        public Object apply1(Object second) throws Throwable {
            try {
                Thread.sleep(((DFloNum) second).longValue() * 1000);
                return true;
            } catch (InterruptedException e) {
                Controller.instance().appendLog(e);
                return false;
            }
        }
    };

    public static final ProcedureN firmata = new ProcedureN("firmata") {
        @Override
        public Object applyN(Object[] objects) throws Throwable {
            if (objects.length == 2)
                return new Firmata((String)objects[0], (String)objects[1]);
            return new FirmataCommand();
        }
    };

    public static class Firmata extends CommandRunner implements IFirmata {
        public Firmata(String peerId, String password) {
            super("Firmata", peerId, password);
        }

        @Override
        public void digitalWrite(int pinNo, boolean value){
            run("digitalWrite", new Object[] {pinNo, value});
        }
    }
}
