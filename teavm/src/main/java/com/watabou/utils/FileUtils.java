package com.watabou.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.util.ArrayList;

import org.teavm.jso.JSBody;

/**
 * TeaVM version: uses localStorage for save/load operations.
 * Asset reads (Internal) still use Gdx.files.internal().
 */
public class FileUtils {

    private static final String LS_PREFIX = "spd_";

    private static Files.FileType defaultFileType = null;
    private static String defaultPath = "";

    public static void setDefaultFileProperties( Files.FileType type, String path ){
        defaultFileType = type;
        defaultPath = path;
    }

    public static FileHandle getFileHandle( String name ){
        return getFileHandle( defaultFileType, defaultPath, name );
    }

    public static FileHandle getFileHandle( Files.FileType type, String name ){
        return getFileHandle( type, "", name );
    }

    public static FileHandle getFileHandle( Files.FileType type, String basePath, String name ){
        if (type == Files.FileType.Internal) {
            return Gdx.files.internal( basePath + name );
        }
        return Gdx.files.internal( basePath + name );
    }

    // Files

    public static boolean cleanTempFiles(){
        return cleanTempFiles("");
    }

    public static boolean cleanTempFiles( String dirName ){
        boolean foundTemp = false;
        ArrayList<String> keys = getAllKeys(dirName);
        for (String key : keys) {
            if (key.endsWith(".spdtmp")) {
                String originalKey = key.replace(".spdtmp", "");
                String tempData = lsGet(key);
                if (tempData != null && !tempData.isEmpty()) {
                    lsSet(originalKey, tempData);
                }
                lsRemove(key);
                foundTemp = true;
            }
        }
        return foundTemp;
    }

    public static boolean fileExists( String name ){
        return lsGet(name) != null;
    }

    public static long fileLength( String name ){
        String data = lsGet(name);
        if (data == null) return 0;
        return data.length();
    }

    public static boolean deleteFile( String name ){
        lsRemove(name);
        return true;
    }

    public static void overwriteFile( String name, int bytes ){
        // In localStorage, no need to actually write junk data - just remove the key
        lsRemove(name);
    }

    // Directories

    public static boolean dirExists( String name ){
        ArrayList<String> keys = getAllKeys(name);
        return !keys.isEmpty();
    }

    public static boolean deleteDir( String name ){
        ArrayList<String> keys = getAllKeys(name);
        for (String key : keys) {
            lsRemove(key);
        }
        return true;
    }

    public static ArrayList<String> filesInDir( String name ){
        ArrayList<String> result = new ArrayList<>();
        String prefix = name.endsWith("/") ? name : name + "/";
        int prefixLen = prefix.length();
        ArrayList<String> allKeys = getAllKeys(prefix);
        for (String key : allKeys) {
            String remainder = key.substring(prefixLen);
            if (!remainder.contains("/")) {
                result.add(remainder);
            }
        }
        return result;
    }

    // bundle reading

    public static Bundle bundleFromFile( String fileName ) throws IOException {
        try {
            String json = lsGet(fileName);
            if (json == null || json.isEmpty()) {
                throw new IOException("file does not exist!");
            }
            return Bundle.read(json);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    // bundle writing

    public static void bundleToFile( String fileName, Bundle bundle ) throws IOException {
        try {
            String json = bundle.toString();
            if (fileExists(fileName)) {
                String tempName = fileName + ".spdtmp";
                lsSet(tempName, json);
                try {
                    lsSet(fileName, json);
                } catch (Exception e) {
                    String tempData = lsGet(tempName);
                    if (tempData != null) {
                        lsSet(fileName, tempData);
                    }
                    throw e;
                }
                lsRemove(tempName);
            } else {
                lsSet(fileName, json);
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    // localStorage helpers

    private static String lsKey(String name) {
        return LS_PREFIX + name;
    }

    private static String lsGet(String name) {
        return localStorageGetItem(lsKey(name));
    }

    private static void lsSet(String name, String value) {
        localStorageSetItem(lsKey(name), value);
    }

    private static void lsRemove(String name) {
        localStorageRemoveItem(lsKey(name));
    }

    private static ArrayList<String> getAllKeys(String prefix) {
        String fullPrefix = lsKey(prefix);
        String[] keys = localStorageGetKeysWithPrefix(fullPrefix);
        ArrayList<String> result = new ArrayList<>(keys.length);
        for (String key : keys) {
            result.add(key.substring(LS_PREFIX.length()));
        }
        return result;
    }

    @JSBody(params = {"prefix"}, script =
        "var result = [];" +
        "for (var i = 0; i < window.localStorage.length; i++) {" +
        "  var key = window.localStorage.key(i);" +
        "  if (key && key.indexOf(prefix) === 0) result.push(key);" +
        "}" +
        "return result;")
    private static native String[] localStorageGetKeysWithPrefix(String prefix);

    // TeaVM @JSBody localStorage access (replaces GWT JSNI)

    @JSBody(params = {"key"}, script = "return window.localStorage.getItem(key);")
    private static native String localStorageGetItem(String key);

    @JSBody(params = {"key", "value"}, script =
        "try { window.localStorage.setItem(key, value); }" +
        "catch(e) { throw new Error('localStorage quota exceeded: ' + e.message); }")
    private static native void localStorageSetItem(String key, String value);

    @JSBody(params = {"key"}, script = "window.localStorage.removeItem(key);")
    private static native void localStorageRemoveItem(String key);

    @JSBody(script = "return window.localStorage.length;")
    private static native int localStorageLength();

    @JSBody(params = {"index"}, script = "return window.localStorage.key(index);")
    private static native String localStorageKey(int index);
}
