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
 * TeaVM-specific promotion bridge for Appsintoss.
 * Communicates with parent window via window.parent calls.
 *
 * IMPORTANT: The parent wrapper (__grantPromotionReward__) returns a Promise
 * that resolves to the Apps in Toss SDK result. Success is ONLY when the
 * result contains { key: string }. Any other result is a failure.
 */
public class TeaVMPromotion {

    /**
     * Callback interface for async promotion result.
     */
    @JSFunctor
    public interface PromotionCallback extends JSObject {
        void onComplete(boolean success, String message);
    }

    /**
     * Check if promotion API is available.
     * Returns true if running in Appsintoss and the bridge function exists.
     */
    @JSBody(script =
        "try {" +
        "  return !!(window.parent && " +
        "         window.parent.__APPSINTOSS__ === true && " +
        "         typeof window.parent.__grantPromotionReward__ === 'function');" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isAvailable();

    /**
     * Grant promotion reward to user asynchronously.
     * Waits for the actual Apps in Toss API result (Promise).
     *
     * Success criteria: result is an object with { key: string }
     * Failure cases: undefined, 'ERROR', { errorCode, message }, Promise reject, exception
     *
     * @param promotionCode The promotion code
     * @param amount The amount of Toss Points
     * @param callback Called with (true, key) on success, (false, errorMessage) on failure
     */
    @JSBody(params = {"promotionCode", "amount", "callback"},
        script =
        // Helper to stringify errors safely
        "function stringifyError(e) {" +
        "  try {" +
        "    if (!e) return 'Unknown error';" +
        "    if (typeof e === 'string') return e;" +
        "    if (e.errorCode && e.message) return String(e.errorCode) + ': ' + String(e.message);" +
        "    if (e.code && e.message) return String(e.code) + ': ' + String(e.message);" +
        "    if (e.message) return String(e.message);" +
        "    if (e.errorCode) return String(e.errorCode);" +
        "    if (e.code) return String(e.code);" +
        "    return JSON.stringify(e);" +
        "  } catch (_) {" +
        "    return String(e);" +
        "  }" +
        "}" +

        // Wrapper to invoke callback safely (only once)
        "var finished = false;" +
        "function finish(success, message) {" +
        "  if (finished) return;" +
        "  finished = true;" +
        "  try { callback(!!success, message || (success ? 'Reward granted' : 'Reward failed')); }" +
        "  catch (e) { console.error('TeaVMPromotion callback error', e); }" +
        "}" +

        "try {" +
        "  var api = window.parent && window.parent.__grantPromotionReward__;" +
        "  if (typeof api !== 'function') {" +
        "    finish(false, 'Promotion API not available');" +
        "    return;" +
        "  }" +

        // Call the parent wrapper
        "  var result;" +
        "  try {" +
        "    result = api(promotionCode, amount);" +
        "  } catch (e) {" +
        "    finish(false, stringifyError(e));" +
        "    return;" +
        "  }" +

        // Handle Promise result
        "  if (result && typeof result.then === 'function') {" +
        "    result.then(function(value) {" +
        "      if (!value) {" +
        "        finish(false, 'Promotion API returned no result (unsupported app version?)');" +
        "      } else if (value === 'ERROR') {" +
        "        finish(false, 'Promotion API returned ERROR');" +
        "      } else if (value && typeof value === 'object' && value.key) {" +
        "        finish(true, String(value.key));" +
        "      } else if (value && typeof value === 'object' && value.errorCode) {" +
        "        finish(false, stringifyError(value));" +
        "      } else {" +
        "        finish(false, 'Promotion API returned unverifiable result: ' + stringifyError(value));" +
        "      }" +
        "    }).catch(function(e) {" +
        "      finish(false, stringifyError(e));" +
        "    });" +
        "    return;" +
        "  }" +

        // Handle synchronous result (unlikely but handle for safety)
        "  if (!result) {" +
        "    finish(false, 'Promotion API returned no verifiable result');" +
        "  } else if (result === 'ERROR') {" +
        "    finish(false, 'Promotion API returned ERROR');" +
        "  } else if (typeof result === 'object' && result.key) {" +
        "    finish(true, String(result.key));" +
        "  } else if (typeof result === 'object' && result.errorCode) {" +
        "    finish(false, stringifyError(result));" +
        "  } else {" +
        "    finish(false, 'Promotion API returned unverifiable result: ' + stringifyError(result));" +
        "  }" +
        "} catch (e) {" +
        "  finish(false, stringifyError(e));" +
        "}")
    public static native void grantRewardAsync(
            String promotionCode,
            int amount,
            PromotionCallback callback
    );

    /**
     * @deprecated Use grantRewardAsync instead. This method does not wait for
     * the actual API result and may report false positives.
     */
    @Deprecated
    @JSBody(params = {"promotionCode", "amount"},
        script =
        "try {" +
        "  if (window.parent && typeof window.parent.__grantPromotionReward__ === 'function') {" +
        "    window.parent.__grantPromotionReward__(promotionCode, amount);" +
        "    return true;" +
        "  } else {" +
        "    return false;" +
        "  }" +
        "} catch(e) {" +
        "  console.error('TeaVMPromotion: error granting reward', e);" +
        "  return false;" +
        "}")
    public static native boolean grantReward(String promotionCode, int amount);
}
