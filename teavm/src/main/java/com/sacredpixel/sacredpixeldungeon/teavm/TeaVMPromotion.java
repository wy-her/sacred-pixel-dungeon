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
 * TeaVM-specific promotion bridge for Appsintoss.
 * Communicates with parent window via window.parent calls.
 */
public class TeaVMPromotion {

    /**
     * Check if promotion API is available.
     * Returns true if running in Appsintoss and the bridge function exists.
     */
    @JSBody(script =
        "try {" +
        "  return window.parent && " +
        "         window.parent.__APPSINTOSS__ === true && " +
        "         typeof window.parent.__grantPromotionReward__ === 'function';" +
        "} catch(e) {" +
        "  return false;" +
        "}")
    public static native boolean isAvailable();

    /**
     * Grant promotion reward to user.
     * Calls the parent window's __grantPromotionReward__ function.
     *
     * @param promotionCode The promotion code
     * @param amount The amount of Toss Points
     */
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
