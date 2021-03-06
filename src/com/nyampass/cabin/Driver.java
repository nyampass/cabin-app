package com.nyampass.cabin;

import com.nyampass.cabin.command.FirmataDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by sohta on 2015/06/29.
 */
public class Driver {
    public interface DriverImpl {
        static DriverImpl instance() {
            throw new UnsupportedOperationException();
        }
        public void onDestroy();
    }

    private static final Map<String, Class> classes = new HashMap<>();

    private static void registerClass(String name, Class klass) {
        classes.put(name, forceInit(klass));
    }

    synchronized public static DriverImpl activate(String klass) {
        Class<DriverImpl> c = classes.get(klass);
        DriverImpl instance;
        try {
            instance = (DriverImpl)c.getMethod("instance", null).invoke(null, null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Environ.instance().activeDrivers.add(instance);

        return instance;
    }

    static class Pair<T1,T2> {
        T1 first;
        T2 second;

        Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    private static Pair<DriverImpl,Method> findMatchingMethod(String klass, String command, List<Class<?>> argClasses) {
        Class<DriverImpl> c = classes.get(klass);
        Pair<DriverImpl,Method> ret;
        try {
            DriverImpl driver = activate(klass);
            Method method = c.getMethod(command, argClasses.stream().map(Driver::primitiveClass).toArray(Class[]::new));
            ret = new Pair<>(driver, method);
        } catch (Exception e) {
            ret = null;
        }
        return ret;
    }

    public static Object dispatch(String klass, String command, List<Object> args) {
        Pair<DriverImpl,Method> pair = findMatchingMethod(klass, command, args.stream().map(Object::getClass).collect(Collectors.toList()));
        if (pair != null) {
            try {
                return pair.second.invoke(pair.first, args.toArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("no matching method found: " + klass + "#" + command);
        }
    }

    public static void addEventListener(String klass, String command, List<Object> args, Consumer<Object> eventListener) {
        List<Class<?>> argClasses = args.stream().map(Object::getClass).collect(Collectors.toList());
        argClasses.add(Consumer.class);
        Pair<DriverImpl,Method> pair = findMatchingMethod(klass, command, argClasses);
        if (pair != null) {
            try {
                args.add(eventListener);
                pair.second.invoke(pair.first, args.toArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("no matching method found: " + klass + "#" + command);
        }
    }

    public static void destroy() {
        Set<DriverImpl> activeDrivers = Environ.instance().activeDrivers;
        for (DriverImpl driver : activeDrivers) {
            driver.onDestroy();
            activeDrivers.remove(driver);
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

    private static Class<?> primitiveClass(Class<?> klass) {
        if (wrapper2primitives.containsKey(klass)) {
            return wrapper2primitives.get(klass);
        } else {
            return klass;
        }
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
