package com.nyampass.cabin.app;

import com.nyampass.cabin.command.FirmataCommand;
import gnu.expr.ModuleBody;
import gnu.expr.ModuleMethod;
import gnu.expr.RunnableModule;
import gnu.lists.Pair;
import gnu.mapping.*;
import gnu.math.DFloNum;
import gnu.math.IntNum;
import gnu.lists.*;
import gnu.mapping.*;

@SuppressWarnings("unused")
public class SchemeBridge extends ModuleBody implements RunnableModule {
    @Override
    public void run(CallContext ctx) throws Throwable {
        super.run(ctx);
    }

    public static final Procedure1 delay = new Procedure1("delay") {
        @Override
        public Object apply1(Object second) throws Throwable {
            try {
                Thread.sleep(((DFloNum)second).longValue() * 1000);
            } catch (InterruptedException e) {
                Controller.instance().appendLog(e);
            }
            return IntNum.make(3);
        }
    };

    public static final ProcedureN firmata = new ProcedureN("firmata") {
        @Override
        public Object applyN(Object[] objects) throws Throwable {
            return new FirmataCommand();
        }
    };
}
