package com.watabou.input;

import com.watabou.utils.PointF;

/**
 * TeaVM version for ControllerHandler.
 * Controllers are not supported in the browser, so this is a no-op stub.
 */
public class ControllerHandler {

    public enum ControllerType {
        XBOX,
        PLAYSTATION,
        NINTENDO,
        OTHER
    }

    public static ControllerType lastUsedType = ControllerType.OTHER;
    public static boolean controllerActive = false;
    public static final int CONTROLLER_POINTER_ID = 19;

    public static void setupControllerListener() {
    }

    public static PointF leftStickPosition = new PointF();
    public static PointF rightStickPosition = new PointF();

    public static int DPAD_KEY_OFFSET = 1000;

    public static boolean controllersSupported() {
        return false;
    }

    public static boolean vibrationSupported() {
        return false;
    }

    public static void vibrate(int millis) {
    }

    public static boolean isControllerConnected() {
        return false;
    }

    public static void setControllerPointer(boolean active) {
    }

    public static boolean controllerPointerActive() {
        return false;
    }

    public static PointF getControllerPointerPos() {
        return new PointF();
    }

    public static void updateControllerPointer(PointF pos, boolean sendEvent) {
    }

    public static int buttonToKey(Object controller, int btnCode) {
        return 0;
    }

    public static boolean icControllerKey(int keyCode) {
        return false;
    }

    public static String customButtonName(int keyCode) {
        return null;
    }
}
