package com.watabou.utils;

/**
 * TeaVM version: implements isInstance/isAssignableFrom by walking
 * the superclass chain using Class identity (==) comparison.
 *
 * TeaVM guarantees a single Class object per type, so == is equivalent to
 * getName().equals() but without String allocation overhead.
 *
 * LIMITATION: This does NOT support interfaces. All call sites in this codebase
 * pass concrete class types (Char.class, Buff.class, etc.), not interfaces.
 * If interface support is ever needed, this must be extended.
 */
public class Compat {

    public static boolean isInstance(Class<?> cls, Object obj) {
        if (obj == null) return false;
        return isAssignableFrom(cls, obj.getClass());
    }

    public static boolean isAssignableFrom(Class<?> cls, Class<?> other) {
        if (cls == other) return true;
        Class<?> c = other.getSuperclass();
        while (c != null) {
            if (c == cls) return true;
            c = c.getSuperclass();
        }
        return false;
    }

    public static float[] cloneArray(float[] array) {
        float[] copy = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i];
        }
        return copy;
    }
}
