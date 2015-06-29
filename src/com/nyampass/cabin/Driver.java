package com.nyampass.cabin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sohta on 2015/06/29.
 */
public class Driver {
    private static final Map<String, Class> classes = new HashMap<>();

    public static void registerClass(String name, Class klass) {
        classes.put(name, klass);
    }

    public static Object dispatch(String klass, String command, List<Object> args) {
        Class c = classes.get(klass);
        try {
            Object instance = c.newInstance();
            Method method = c.getMethod(command, (Class<?>[]) args.stream().map(Object::getClass).toArray());
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
