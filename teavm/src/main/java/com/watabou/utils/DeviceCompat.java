package com.watabou.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.watabou.noosa.Game;

/**
 * TeaVM version for DeviceCompat.
 */
public class DeviceCompat {

    public static int getPlatformVersion(){
        return Gdx.app.getVersion();
    }

    public static boolean isAndroid(){
        return false;
    }

    public static boolean isiOS(){
        return false;
    }

    public static boolean isDesktop(){
        return false;
    }

    public static boolean isHTML5(){
        return true;
    }

    public static boolean hasHardKeyboard(){
        return Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard);
    }

    public static boolean isDebug(){
        return Game.version.contains("INDEV");
    }

    public static void log( String tag, String message ){
        Gdx.app.log( tag, message );
    }

    public static float getRealPixelScaleX(){
        return (Gdx.graphics.getBackBufferWidth() / (float)Game.width );
    }

    public static float getRealPixelScaleY(){
        return (Gdx.graphics.getBackBufferHeight() / (float)Game.height );
    }
}
