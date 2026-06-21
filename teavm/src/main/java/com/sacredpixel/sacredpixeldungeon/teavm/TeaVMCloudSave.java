/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sacredpixel.sacredpixeldungeon.teavm;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * TeaVM-specific cloud save bridge for Appsintoss.
 * Communicates with parent window's Firebase integration via window.parent calls.
 *
 * Data format matches WebDataExporter/WebDataImporter for compatibility
 * with the "데이터 내보내기" feature in Cloudflare version.
 */
public class TeaVMCloudSave {

    /**
     * Check if running in Appsintoss environment with cloud save enabled.
     */
    @JSBody(script =
        "try {" +
        "  return window.parent && window.parent.__CLOUD_SAVE_AVAILABLE__ === true;" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isAvailable();

    /**
     * Callback interface for async cloud save operations.
     */
    public interface CloudSaveCallback {
        void onComplete(boolean success, String data);
    }

    /**
     * Internal JS callback interface.
     */
    private interface JSCloudCallback extends JSObject {
        void onResult(boolean success, String data);
    }

    /**
     * Load game data from cloud.
     * @param callback Called with (true, jsonData) on success, (false, null) on failure
     */
    public static void load(CloudSaveCallback callback) {
        loadAsync(new JSCloudCallback() {
            @Override
            public void onResult(boolean success, String data) {
                callback.onComplete(success, data);
            }
        });
    }

    @JSBody(params = {"callback"}, script =
        "try {" +
        "  if (window.parent && typeof window.parent.__loadCloudSave__ === 'function') {" +
        "    window.parent.__loadCloudSave__().then(function(data) {" +
        "      if (data) {" +
        "        callback.onResult(true, data);" +
        "      } else {" +
        "        callback.onResult(false, null);" +
        "      }" +
        "    }).catch(function(e) {" +
        "      console.warn('TeaVMCloudSave: load error', e);" +
        "      callback.onResult(false, null);" +
        "    });" +
        "  } else {" +
        "    console.warn('TeaVMCloudSave: __loadCloudSave__ not available');" +
        "    callback.onResult(false, null);" +
        "  }" +
        "} catch(e) {" +
        "  console.warn('TeaVMCloudSave: load exception', e);" +
        "  callback.onResult(false, null);" +
        "}")
    private static native void loadAsync(JSCloudCallback callback);

    /**
     * Save game data to cloud.
     * @param jsonData JSON string of CloudSaveData
     * @param callback Called with (true, null) on success, (false, null) on failure
     */
    public static void save(String jsonData, CloudSaveCallback callback) {
        saveAsync(jsonData, new JSCloudCallback() {
            @Override
            public void onResult(boolean success, String data) {
                callback.onComplete(success, data);
            }
        });
    }

    @JSBody(params = {"jsonData", "callback"}, script =
        "try {" +
        "  if (window.parent && typeof window.parent.__saveCloudSave__ === 'function') {" +
        "    window.parent.__saveCloudSave__(jsonData).then(function(success) {" +
        "      callback.onResult(success, null);" +
        "    }).catch(function(e) {" +
        "      console.warn('TeaVMCloudSave: save error', e);" +
        "      callback.onResult(false, null);" +
        "    });" +
        "  } else {" +
        "    console.warn('TeaVMCloudSave: __saveCloudSave__ not available');" +
        "    callback.onResult(false, null);" +
        "  }" +
        "} catch(e) {" +
        "  console.warn('TeaVMCloudSave: save exception', e);" +
        "  callback.onResult(false, null);" +
        "}")
    private static native void saveAsync(String jsonData, JSCloudCallback callback);
}
