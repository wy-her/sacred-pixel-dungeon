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
 *
 * IMPORTANT: Success means the Apps in Toss API returned { key: string }.
 * A mere JS function call without exception does NOT mean success.
 */
public class Promotion {

    // Platform-specific implementation (set by platform launcher)
    public static PromotionImpl impl = null;

    // Tutorial completion reward amount (in Toss Points)
    // Values are injected from local.properties via PromotionConfig (generated at build time)
    public static final int TUTORIAL_REWARD_AMOUNT = PromotionConfig.TUTORIAL_REWARD_AMOUNT;

    // Promotion code (injected from local.properties via PromotionConfig)
    public static final String TUTORIAL_PROMO_CODE = PromotionConfig.TUTORIAL_PROMO_CODE;

    // Prevent duplicate requests while one is in flight
    private static boolean rewardInFlight = false;

    public interface PromotionImpl {
        /**
         * Grant promotion reward to user.
         * @param promotionCode The promotion code registered in Appsintoss console
         * @param amount The amount of Toss Points to grant
         * @param callback Called with result (true = API returned { key }, false = any failure)
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
     * Check if promotion is properly configured in build.
     * Returns false if promotion.code is "DISABLED" or amount is 0.
     */
    public static boolean isConfigured() {
        return TUTORIAL_REWARD_AMOUNT > 0
                && TUTORIAL_PROMO_CODE != null
                && !TUTORIAL_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(TUTORIAL_PROMO_CODE);
    }

    /**
     * Check if promotion system is available (Appsintoss only).
     */
    public static boolean isAvailable() {
        return isConfigured() && impl != null && impl.isAvailable();
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
     *
     * Success (callback true) means the Apps in Toss API returned { key }.
     * Any other result (undefined, ERROR, errorCode, reject) means failure.
     */
    public static void grantTutorialReward(RewardCallback callback) {
        if (!isConfigured()) {
            if (callback != null) {
                callback.onResult(false, "Promotion is not configured");
            }
            return;
        }

        if (impl == null || !impl.isAvailable()) {
            if (callback != null) {
                callback.onResult(false, "Promotion API is not available");
            }
            return;
        }

        if (rewardInFlight) {
            if (callback != null) {
                callback.onResult(false, "Promotion reward request is already in progress");
            }
            return;
        }

        rewardInFlight = true;

        impl.grantReward(TUTORIAL_PROMO_CODE, TUTORIAL_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }
}
