/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Sacred Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.sacredpixel.sacredpixeldungeon.teavm;

import org.teavm.jso.JSBody;

/**
 * TeaVM-specific leaderboard bridge for Appsintoss Game Center.
 * Communicates with parent window via window.parent calls.
 *
 * All calls are defensive - if the API is not available, they will
 * silently fail without throwing errors.
 */
public class TeaVMLeaderboard {

    /**
     * Check if running in Appsintoss environment.
     * Returns true if window.parent.__APPSINTOSS__ is set.
     */
    @JSBody(script =
        "try {" +
        "  return window.parent && window.parent.__APPSINTOSS__ === true;" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isAppsintoss();

    /**
     * Check if leaderboard API is available.
     * Returns true if the bridge functions exist in parent window.
     */
    @JSBody(script =
        "try {" +
        "  return window.parent && " +
        "         typeof window.parent.__openLeaderboard__ === 'function' && " +
        "         typeof window.parent.__submitScore__ === 'function';" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isLeaderboardAvailable();

    /**
     * Open the Game Center leaderboard UI.
     * Calls parent window's __openLeaderboard__ function.
     * Safe to call even if API is not available.
     */
    @JSBody(script =
        "try {" +
        "  if (window.parent && typeof window.parent.__openLeaderboard__ === 'function') {" +
        "    window.parent.__openLeaderboard__();" +
        "  } else {" +
        "    console.warn('TeaVMLeaderboard: __openLeaderboard__ not available');" +
        "  }" +
        "} catch(e) {" +
        "  console.warn('TeaVMLeaderboard: error opening leaderboard', e);" +
        "}")
    public static native void openLeaderboard();

    /**
     * Submit a score to the Game Center leaderboard.
     * Calls parent window's __submitScore__ function.
     * Safe to call even if API is not available.
     */
    @JSBody(params = {"score"}, script =
        "try {" +
        "  if (window.parent && typeof window.parent.__submitScore__ === 'function') {" +
        "    window.parent.__submitScore__(score);" +
        "  } else {" +
        "    console.warn('TeaVMLeaderboard: __submitScore__ not available');" +
        "  }" +
        "} catch(e) {" +
        "  console.warn('TeaVMLeaderboard: error submitting score', e);" +
        "}")
    public static native void submitScore(int score);

    /**
     * Check if user key API is available.
     * Returns true if the bridge function exists in parent window.
     */
    @JSBody(script =
        "try {" +
        "  return window.parent && typeof window.parent.__getUserKey__ === 'function';" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isUserKeyAvailable();

    /**
     * Get the user key for game identification.
     * This is an async operation - the callback will be called with the result.
     *
     * @param callback Called with the user hash string, or null if unavailable/error
     */
    public static void getUserKey(UserKeyCallback callback) {
        getUserKeyAsync(new JSUserKeyCallback() {
            @Override
            public void onResult(String hash) {
                callback.onUserKey(hash);
            }
        });
    }

    /**
     * Callback interface for getUserKey result.
     */
    public interface UserKeyCallback {
        void onUserKey(String hash);
    }

    /**
     * Internal JS callback interface.
     */
    private interface JSUserKeyCallback extends org.teavm.jso.JSObject {
        void onResult(String hash);
    }

    @JSBody(params = {"callback"}, script =
        "try {" +
        "  if (window.parent && typeof window.parent.__getUserKey__ === 'function') {" +
        "    window.parent.__getUserKey__().then(function(result) {" +
        "      if (result && result.type === 'HASH' && result.hash) {" +
        "        callback.onResult(result.hash);" +
        "      } else {" +
        "        callback.onResult(null);" +
        "      }" +
        "    }).catch(function(e) {" +
        "      console.warn('TeaVMLeaderboard: error getting user key', e);" +
        "      callback.onResult(null);" +
        "    });" +
        "  } else {" +
        "    console.warn('TeaVMLeaderboard: __getUserKey__ not available');" +
        "    callback.onResult(null);" +
        "  }" +
        "} catch(e) {" +
        "  console.warn('TeaVMLeaderboard: error in getUserKey', e);" +
        "  callback.onResult(null);" +
        "}")
    private static native void getUserKeyAsync(JSUserKeyCallback callback);
}
