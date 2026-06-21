package com.watabou.utils;

/**
 * TeaVM version: all threading operations are no-ops
 * since JavaScript is single-threaded.
 */
public class ThreadCompat {

    public static void waitOnObject(Object obj) throws InterruptedException {
    }

    public static void waitOnObject(Object obj, long timeout) throws InterruptedException {
    }

    public static void notifyAllOnObject(Object obj) {
    }

    public static Object currentThread() {
        return null; // No threads in TeaVM/JS
    }

    public static boolean interrupted() {
        return false;
    }

    public static void waitOnCurrentThread() throws InterruptedException {
    }

    public static void notifyCurrentThread() {
    }

    public static int availableProcessors() {
        return 1;
    }
}
