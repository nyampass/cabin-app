package com.nyampass.cabin.app;

import gnu.expr.ModuleBody;
import gnu.expr.ModuleMethod;
import gnu.expr.RunnableModule;
import gnu.lists.Pair;
import gnu.mapping.*;
import gnu.math.IntNum;
import gnu.lists.*;
import gnu.mapping.*;

@SuppressWarnings("unused")
public class SchemeBridge extends ModuleBody implements RunnableModule {
    @Override
    public void run(CallContext ctx) throws Throwable {
        super.run(ctx);
    }

    public static final Procedure0 hoge = new Procedure0("hoge") {
        @Override
        public Object apply0() throws Throwable {
            return IntNum.make(3);
        }
    };
}
