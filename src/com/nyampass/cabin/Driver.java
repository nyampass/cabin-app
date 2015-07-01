package com.nyampass.cabin;

import com.nyampass.cabin.command.FirmataDriver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sohta on 2015/06/29.
 */
public class Driver {
    public interface DriverImpl {
        static DriverImpl instance() {
            throw new UnsupportedOperationException();
        }
    }

    private static final Map<String, Class> classes = new HashMap<>();

    private static void registerClass(String name, Class klass) {
        classes.put(name, forceInit(klass));
    }

    public static Object dispatch(String klass, String command, List<Object> args) {
        Class<DriverImpl> c = classes.get(klass);
        try {
            Object instance = c.getMethod("instance", null).invoke(null, null);
            Method method = c.getMethod(command,
                    (Class<?>[]) args.stream().map(Driver::primitiveClass).toArray(Class[]::new));
            return method.invoke(instance, args.toArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final static Map<Class<?>, Class<?>> wrapper2primitives = new HashMap<>();
    static {
        wrapper2primitives.put(Boolean.class, boolean.class);
        wrapper2primitives.put(Byte.class, byte.class);
        wrapper2primitives.put(Short.class, short.class);
        wrapper2primitives.put(Character.class, char.class);
        wrapper2primitives.put(Integer.class, int.class);
        wrapper2primitives.put(Long.class, long.class);
        wrapper2primitives.put(Float.class, float.class);
        wrapper2primitives.put(Double.class, double.class);
    }

    private static Class<?> primitiveClass(Object object) {
        return wrapper2primitives.get(object.getClass());
    }

    public static void load() {
        registerClass("Firmata", FirmataDriver.class);
    }

    public static <T> Class<T> forceInit(Class<T> klass) {
        try {
            Class.forName(klass.getName(), true, klass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return klass;
    }
}
