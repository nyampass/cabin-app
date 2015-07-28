package com.nyampass.cabin.lang;

import com.nyampass.cabin.Canvas;
import com.nyampass.cabin.Driver;
import com.nyampass.cabin.Environ;
import com.nyampass.cabin.command.CommandRunner;
import com.nyampass.cabin.command.IFirmata;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.expr.RunnableModule;
import gnu.lists.EmptyList;
import gnu.mapping.*;
import gnu.math.DFloNum;
import gnu.math.IntNum;
import javafx.scene.paint.Color;
import kawa.standard.Scheme;

@SuppressWarnings("unused")
public class SchemeModuleBody extends ModuleBody implements RunnableModule {
    @Override
    public void run(CallContext ctx) throws Throwable {
        super.run(ctx);
    }

    public static final Procedure0 canvas = new Procedure0("canvas") {
        @Override
        public Object apply0() throws Throwable {
            return new Canvas(Environ.instance().graphicsContext);
        }
    };

    public static final Procedure1 delay = new Procedure1("delay") {
        @Override
        public Object apply1(Object second) throws Throwable {
            try {
                Thread.sleep(((DFloNum) second).longValue() * 1000);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }
    };

    private static double obj2double(Object obj) {
        if (obj instanceof IntNum) {
            return ((IntNum) obj).doubleValue();
        }
        throw new ClassCastException("can't convert double from " + obj.toString());
    }

    public static final ProcedureN color = new ProcedureN("color") {
        @Override
        public Object applyN(Object[] objects) throws Throwable {
            switch (objects.length) {
                case 1:
                    return Color.valueOf((String) objects[0]);

                case 3:
                    return Color.color(obj2double(objects[0]),
                            obj2double(objects[0]),
                            obj2double(objects[0]));
                default:
                    return Color.WHITE;
            }
        }
    };

    public static final ProcedureN firmata = new ProcedureN("firmata") {
        @Override
        public Object applyN(Object[] objects) throws Throwable {
            if (objects.length == 2)
                return new SchemeBridge.Firmata((String) objects[0], (String) objects[1]);
            return Driver.activate("Firmata");
        }
    };

    public static final Procedure1 display = new Procedure1("display") {
        @Override
        public Object apply1(Object o) throws Throwable {
            System.out.println(o.toString());
            return EmptyList.emptyList;
        }
    };
}
