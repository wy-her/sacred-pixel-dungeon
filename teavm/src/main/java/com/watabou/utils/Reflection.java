package com.watabou.utils;

import com.sacredpixel.sacredpixeldungeon.teavm.TeaVMClassRegistry;
import com.watabou.noosa.Game;

/**
 * TeaVM version of Reflection.
 * Uses TeaVMClassRegistry instead of Java reflection (not available in TeaVM).
 */
public class Reflection {

    public static boolean isMemberClass( Class cls ){
        return cls.getName().contains("$");
    }

    public static boolean isStatic( Class cls ){
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance( Class<T> cls ){
        try {
            Object obj = TeaVMClassRegistry.newInstanceFromClass(cls);
            if (obj != null) return (T) obj;
            Game.reportException(new RuntimeException("TeaVMClassRegistry: no factory for " + cls.getName()));
            return null;
        } catch (Exception e) {
            Game.reportException(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstanceUnhandled( Class<T> cls ) throws Exception {
        Object obj = TeaVMClassRegistry.newInstanceFromClass(cls);
        if (obj != null) return (T) obj;
        throw new RuntimeException("TeaVMClassRegistry: no factory for " + cls.getName());
    }

    public static Class forName( String name ){
        try {
            Class<?> cls = TeaVMClassRegistry.forName(name);
            if (cls != null) return cls;
            return null;
        } catch (Exception e) {
            Game.reportException(e);
            return null;
        }
    }

    public static Class forNameUnhandled( String name ) throws Exception {
        Class<?> cls = TeaVMClassRegistry.forName(name);
        if (cls != null) return cls;
        throw new ClassNotFoundException("TeaVMClassRegistry: " + name + " not registered");
    }
}
