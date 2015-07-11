package com.nyampass.cabin.lang;

import com.nyampass.cabin.Utils;
import com.nyampass.cabin.command.CommandRunner;
import com.nyampass.cabin.command.IFirmata;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.mapping.*;
import kawa.standard.Scheme;
import org.omg.SendingContext.RunTime;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class SchemeBridge {
    private Scheme scheme;
    private static Pattern hyphenJavaMethodPattern = Pattern.compile("\\((\\w+):([\\w-]+)( .+)?\\)");
    private static Pattern underScoreMethodPattern = Pattern.compile("-(.)");

    static {
        ModuleBody.setMainPrintValues(true);
    }

    public SchemeBridge() {
        this.scheme = Scheme.getInstance();

        Environment env = Scheme.builtin();
        Language.setDefaults(this.scheme);
        Environment.setGlobal(env);
    }

    private String fixCode(String code) {
        return Utils.regexTransform(hyphenJavaMethodPattern, code, methodMatch -> {
            // canvas:get-width -> canvas:getWidth
            return "(" + methodMatch.group(1) + ":" + camelCaseMethodName(methodMatch.group(2) + (methodMatch.group(3) != null ? methodMatch.group(3) : "") + ")");
        });
    }

    private String camelCaseMethodName(String str) {
        return Utils.regexTransform(underScoreMethodPattern, str, matcher -> matcher.group(1).toUpperCase());
    }

    public Object eval(String code) {
        try {
            this.scheme.loadClass("com.nyampass.cabin.lang.SchemeModuleBody");

            code = fixCode(code);
            System.out.println(code);

            return this.scheme.eval(code);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Throwable throwable) {
            if (throwable instanceof RuntimeException)
                throw (RuntimeException)throwable;
            throw new RuntimeException(throwable);

        }
    }

    public static class Firmata extends CommandRunner implements IFirmata {
        public Firmata(String peerId, String password) {
            super("Firmata", peerId, password);
        }

        @Override
        public void digitalWrite(int pinNo, boolean value) {
            run("digitalWrite", new Object[]{pinNo, value});
        }
    }
}
