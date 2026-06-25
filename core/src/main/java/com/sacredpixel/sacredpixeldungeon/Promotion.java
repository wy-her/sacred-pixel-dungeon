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

package com.sacredpixel.sacredpixeldungeon;

/**
 * Platform-independent promotion interface for granting Toss Points.
 * Only available on Appsintoss platform.
 *
 * Usage:
 * - Call Promotion.grantTutorialReward() when tutorial is completed
 * - The implementation handles Appsintoss-specific API calls
 */
public class Promotion {

    // Platform-specific implementation (set by platform launcher)
    public static PromotionImpl impl = null;

    // Tutorial completion reward amount (in Toss Points)
    // Values are injected from local.properties via PromotionConfig (generated at build time)
    public static final int TUTORIAL_REWARD_AMOUNT = PromotionConfig.TUTORIAL_REWARD_AMOUNT;

    // Promotion code (injected from local.properties via PromotionConfig)
    public static final String TUTORIAL_PROMO_CODE = PromotionConfig.TUTORIAL_PROMO_CODE;

    public interface PromotionImpl {
        /**
         * Grant promotion reward to user.
         * @param promotionCode The promotion code registered in Appsintoss console
         * @param amount The amount of Toss Points to grant
         * @param callback Called with result (true = success, false = failure)
         */
        void grantReward(String promotionCode, int amount, RewardCallback callback);

        /**
         * Check if promotion API is available.
         */
        boolean isAvailable();
    }

    public interface RewardCallback {
        void onResult(boolean success, String message);
    }

    /**
     * Check if promotion system is available (Appsintoss only).
     */
    public static boolean isAvailable() {
        return impl != null && impl.isAvailable();
    }

    /**
     * Grant tutorial completion reward.
     * Only works on Appsintoss platform.
     */
    public static void grantTutorialReward() {
        grantTutorialReward(null);
    }

    /**
     * Grant tutorial completion reward with callback.
     * Only works on Appsintoss platform.
     */
    public static void grantTutorialReward(RewardCallback callback) {
        if (impl != null && impl.isAvailable()) {
            impl.grantReward(TUTORIAL_PROMO_CODE, TUTORIAL_REWARD_AMOUNT, callback);
        } else {
            // Not on Appsintoss - silently ignore
            if (callback != null) {
                callback.onResult(false, "Promotion not available");
            }
        }
    }
}
