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
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * TeaVM-specific review bridge for Appsintoss.
 * Requests app review via parent window's requestReview API.
 */
public class TeaVMReview {

    /**
     * Callback interface for async review result.
     */
    @JSFunctor
    public interface ReviewCallback extends JSObject {
        void onComplete(boolean success);
    }

    /**
     * Check if review API is available.
     * Returns true if running in Appsintoss and the bridge function exists.
     */
    @JSBody(script =
        "try {" +
        "  return !!(window.parent && " +
        "         window.parent.__APPSINTOSS__ === true && " +
        "         typeof window.parent.__requestReview__ === 'function');" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isAvailable();

    /**
     * Request app review asynchronously.
     * The callback is called when the review request completes,
     * regardless of whether the review UI was actually shown.
     *
     * @param callback Called with true on success, false on failure
     */
    @JSBody(params = {"callback"},
        script =
        "var finished = false;" +
        "function finish(success) {" +
        "  if (finished) return;" +
        "  finished = true;" +
        "  try { callback(!!success); }" +
        "  catch (e) { console.error('TeaVMReview callback error', e); }" +
        "}" +

        "try {" +
        "  var api = window.parent && window.parent.__requestReview__;" +
        "  if (typeof api !== 'function') {" +
        "    finish(false);" +
        "    return;" +
        "  }" +

        "  var result = api();" +
        "  if (result && typeof result.then === 'function') {" +
        "    result.then(function(value) {" +
        "      finish(!!value);" +
        "    }).catch(function(e) {" +
        "      console.warn('TeaVMReview: request failed', e);" +
        "      finish(false);" +
        "    });" +
        "  } else {" +
        "    finish(!!result);" +
        "  }" +
        "} catch (e) {" +
        "  console.warn('TeaVMReview: error', e);" +
        "  finish(false);" +
        "}")
    public static native void requestReviewAsync(ReviewCallback callback);
}
