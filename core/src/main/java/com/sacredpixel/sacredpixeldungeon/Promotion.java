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

    // Tutorial completion reward (test version)
    // Values are injected from local.properties via PromotionConfig (generated at build time)
    public static final int TUTORIAL_COMPLETE_REWARD_AMOUNT = PromotionConfig.TUTORIAL_COMPLETE_REWARD_AMOUNT;
    public static final String TUTORIAL_COMPLETE_PROMO_CODE = PromotionConfig.TUTORIAL_COMPLETE_PROMO_CODE;

    // Tutorial step 1 reward (guidebook pickup)
    public static final int TUTORIAL_STEP1_REWARD_AMOUNT = PromotionConfig.TUTORIAL_STEP1_REWARD_AMOUNT;
    public static final String TUTORIAL_STEP1_PROMO_CODE = PromotionConfig.TUTORIAL_STEP1_PROMO_CODE;

    // Tutorial step 2 reward (rat killed - first combat)
    public static final int TUTORIAL_STEP2_REWARD_AMOUNT = PromotionConfig.TUTORIAL_STEP2_REWARD_AMOUNT;
    public static final String TUTORIAL_STEP2_PROMO_CODE = PromotionConfig.TUTORIAL_STEP2_PROMO_CODE;

    // Tutorial step 3 reward (snake killed - surprise attack learned)
    public static final int TUTORIAL_STEP3_REWARD_AMOUNT = PromotionConfig.TUTORIAL_STEP3_REWARD_AMOUNT;
    public static final String TUTORIAL_STEP3_PROMO_CODE = PromotionConfig.TUTORIAL_STEP3_PROMO_CODE;

    // Tutorial entry reward (granted when entering tutorial level)
    public static final int TUTORIAL_ENTRY_REWARD_AMOUNT = PromotionConfig.TUTORIAL_ENTRY_REWARD_AMOUNT;
    public static final String TUTORIAL_ENTRY_PROMO_CODE = PromotionConfig.TUTORIAL_ENTRY_PROMO_CODE;

    // Third play reward (granted on 3rd run start, floor 1 entry)
    public static final int THIRD_PLAY_REWARD_AMOUNT = PromotionConfig.THIRD_PLAY_REWARD_AMOUNT;
    public static final String THIRD_PLAY_PROMO_CODE = PromotionConfig.THIRD_PLAY_PROMO_CODE;

    // 10-day streak reward (10 consecutive days with 3+ plays per day)
    public static final int STREAK_10_REWARD_AMOUNT = PromotionConfig.STREAK_10_REWARD_AMOUNT;
    public static final String STREAK_10_PROMO_CODE = PromotionConfig.STREAK_10_PROMO_CODE;

    // 30-day streak reward (30 consecutive days with 3+ plays per day)
    public static final int STREAK_30_REWARD_AMOUNT = PromotionConfig.STREAK_30_REWARD_AMOUNT;
    public static final String STREAK_30_PROMO_CODE = PromotionConfig.STREAK_30_PROMO_CODE;

    // All Classes Victory badge reward (VICTORY_ALL_CLASSES)
    public static final int ALL_CLASSES_REWARD_AMOUNT = PromotionConfig.ALL_CLASSES_REWARD_AMOUNT;
    public static final String ALL_CLASSES_PROMO_CODE = PromotionConfig.ALL_CLASSES_PROMO_CODE;

    // Jack of All Trades badge reward (BOSS_SLAIN_3_ALL_SUBCLASSES - 만물 박사)
    public static final int JACK_OF_ALL_TRADES_REWARD_AMOUNT = PromotionConfig.JACK_OF_ALL_TRADES_REWARD_AMOUNT;
    public static final String JACK_OF_ALL_TRADES_PROMO_CODE = PromotionConfig.JACK_OF_ALL_TRADES_PROMO_CODE;

    // Champion 1 badge reward (Bronze - 1+ challenges)
    public static final int CHAMPION_1_REWARD_AMOUNT = PromotionConfig.CHAMPION_1_REWARD_AMOUNT;
    public static final String CHAMPION_1_PROMO_CODE = PromotionConfig.CHAMPION_1_PROMO_CODE;

    // Champion 2 badge reward (Silver - 3+ challenges)
    public static final int CHAMPION_2_REWARD_AMOUNT = PromotionConfig.CHAMPION_2_REWARD_AMOUNT;
    public static final String CHAMPION_2_PROMO_CODE = PromotionConfig.CHAMPION_2_PROMO_CODE;

    // Champion 3 badge reward (Gold - 6+ challenges)
    public static final int CHAMPION_3_REWARD_AMOUNT = PromotionConfig.CHAMPION_3_REWARD_AMOUNT;
    public static final String CHAMPION_3_PROMO_CODE = PromotionConfig.CHAMPION_3_PROMO_CODE;

    // Taking the Mick badge reward
    public static final int TAKING_THE_MICK_REWARD_AMOUNT = PromotionConfig.TAKING_THE_MICK_REWARD_AMOUNT;
    public static final String TAKING_THE_MICK_PROMO_CODE = PromotionConfig.TAKING_THE_MICK_PROMO_CODE;

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
     * Check if tutorial completion promotion is properly configured in build.
     * Returns false if promotion code is "DISABLED" or amount is 0.
     */
    public static boolean isConfigured() {
        return TUTORIAL_COMPLETE_REWARD_AMOUNT > 0
                && TUTORIAL_COMPLETE_PROMO_CODE != null
                && !TUTORIAL_COMPLETE_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(TUTORIAL_COMPLETE_PROMO_CODE);
    }

    /**
     * Check if tutorial completion promotion is available (Appsintoss only).
     */
    public static boolean isAvailable() {
        return isConfigured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant tutorial completion reward (test version).
     * Only works on Appsintoss platform.
     */
    public static void grantTutorialReward() {
        grantTutorialReward(null);
    }

    /**
     * Grant tutorial completion reward with callback (test version).
     * Only works on Appsintoss platform.
     *
     * Success (callback true) means the Apps in Toss API returned { key }.
     * Any other result (undefined, ERROR, errorCode, reject) means failure.
     */
    public static void grantTutorialReward(RewardCallback callback) {
        if (!isConfigured()) {
            if (callback != null) {
                callback.onResult(false, "Tutorial completion promotion is not configured");
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

        impl.grantReward(TUTORIAL_COMPLETE_PROMO_CODE, TUTORIAL_COMPLETE_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    /**
     * Check if tutorial step 1 promotion is properly configured in build.
     */
    public static boolean isTutorialStep1Configured() {
        return TUTORIAL_STEP1_REWARD_AMOUNT > 0
                && TUTORIAL_STEP1_PROMO_CODE != null
                && !TUTORIAL_STEP1_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(TUTORIAL_STEP1_PROMO_CODE);
    }

    /**
     * Check if tutorial step 1 promotion is available.
     */
    public static boolean isTutorialStep1Available() {
        return isTutorialStep1Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant tutorial step 1 reward (guidebook pickup).
     * Only works on Appsintoss platform.
     */
    public static void grantTutorialStep1Reward(RewardCallback callback) {
        if (!isTutorialStep1Configured()) {
            if (callback != null) {
                callback.onResult(false, "Tutorial step 1 promotion is not configured");
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

        impl.grantReward(TUTORIAL_STEP1_PROMO_CODE, TUTORIAL_STEP1_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== Tutorial Step 2 Promotion (Rat Killed) ====================

    /**
     * Check if tutorial step 2 promotion is properly configured in build.
     */
    public static boolean isTutorialStep2Configured() {
        return TUTORIAL_STEP2_REWARD_AMOUNT > 0
                && TUTORIAL_STEP2_PROMO_CODE != null
                && !TUTORIAL_STEP2_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(TUTORIAL_STEP2_PROMO_CODE);
    }

    /**
     * Check if tutorial step 2 promotion is available.
     */
    public static boolean isTutorialStep2Available() {
        return isTutorialStep2Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant tutorial step 2 reward (rat killed - first combat).
     * Only works on Appsintoss platform.
     */
    public static void grantTutorialStep2Reward(RewardCallback callback) {
        if (!isTutorialStep2Configured()) {
            if (callback != null) {
                callback.onResult(false, "Tutorial step 2 promotion is not configured");
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

        impl.grantReward(TUTORIAL_STEP2_PROMO_CODE, TUTORIAL_STEP2_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== Tutorial Step 3 Promotion (Snake Killed) ====================

    /**
     * Check if tutorial step 3 promotion is properly configured in build.
     */
    public static boolean isTutorialStep3Configured() {
        return TUTORIAL_STEP3_REWARD_AMOUNT > 0
                && TUTORIAL_STEP3_PROMO_CODE != null
                && !TUTORIAL_STEP3_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(TUTORIAL_STEP3_PROMO_CODE);
    }

    /**
     * Check if tutorial step 3 promotion is available.
     */
    public static boolean isTutorialStep3Available() {
        return isTutorialStep3Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant tutorial step 3 reward (snake killed - surprise attack learned).
     * Only works on Appsintoss platform.
     */
    public static void grantTutorialStep3Reward(RewardCallback callback) {
        if (!isTutorialStep3Configured()) {
            if (callback != null) {
                callback.onResult(false, "Tutorial step 3 promotion is not configured");
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

        impl.grantReward(TUTORIAL_STEP3_PROMO_CODE, TUTORIAL_STEP3_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    /**
     * Check if third play promotion is properly configured in build.
     */
    public static boolean isThirdPlayConfigured() {
        return THIRD_PLAY_REWARD_AMOUNT > 0
                && THIRD_PLAY_PROMO_CODE != null
                && !THIRD_PLAY_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(THIRD_PLAY_PROMO_CODE);
    }

    /**
     * Check if third play promotion is available.
     */
    public static boolean isThirdPlayAvailable() {
        return isThirdPlayConfigured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant third play reward (on 3rd run start, floor 1 entry).
     * Only works on Appsintoss platform.
     */
    public static void grantThirdPlayReward(RewardCallback callback) {
        if (!isThirdPlayConfigured()) {
            if (callback != null) {
                callback.onResult(false, "Third play promotion is not configured");
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

        impl.grantReward(THIRD_PLAY_PROMO_CODE, THIRD_PLAY_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    /**
     * Check if tutorial entry promotion is properly configured in build.
     */
    public static boolean isTutorialEntryConfigured() {
        return TUTORIAL_ENTRY_REWARD_AMOUNT > 0
                && TUTORIAL_ENTRY_PROMO_CODE != null
                && !TUTORIAL_ENTRY_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(TUTORIAL_ENTRY_PROMO_CODE);
    }

    /**
     * Check if tutorial entry promotion is available.
     */
    public static boolean isTutorialEntryAvailable() {
        return isTutorialEntryConfigured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant tutorial entry reward (when entering tutorial level).
     * Only works on Appsintoss platform.
     */
    public static void grantTutorialEntryReward(RewardCallback callback) {
        if (!isTutorialEntryConfigured()) {
            if (callback != null) {
                callback.onResult(false, "Tutorial entry promotion is not configured");
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

        impl.grantReward(TUTORIAL_ENTRY_PROMO_CODE, TUTORIAL_ENTRY_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== 10-day Streak Promotion ====================

    /**
     * Check if 10-day streak promotion is properly configured in build.
     */
    public static boolean isStreak10Configured() {
        return STREAK_10_REWARD_AMOUNT > 0
                && STREAK_10_PROMO_CODE != null
                && !STREAK_10_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(STREAK_10_PROMO_CODE);
    }

    /**
     * Check if 10-day streak promotion is available.
     */
    public static boolean isStreak10Available() {
        return isStreak10Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant 10-day streak reward.
     * Only works on Appsintoss platform.
     */
    public static void grantStreak10Reward(RewardCallback callback) {
        if (!isStreak10Configured()) {
            if (callback != null) {
                callback.onResult(false, "10-day streak promotion is not configured");
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

        impl.grantReward(STREAK_10_PROMO_CODE, STREAK_10_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== 30-day Streak Promotion ====================

    /**
     * Check if 30-day streak promotion is properly configured in build.
     */
    public static boolean isStreak30Configured() {
        return STREAK_30_REWARD_AMOUNT > 0
                && STREAK_30_PROMO_CODE != null
                && !STREAK_30_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(STREAK_30_PROMO_CODE);
    }

    /**
     * Check if 30-day streak promotion is available.
     */
    public static boolean isStreak30Available() {
        return isStreak30Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant 30-day streak reward.
     * Only works on Appsintoss platform.
     */
    public static void grantStreak30Reward(RewardCallback callback) {
        if (!isStreak30Configured()) {
            if (callback != null) {
                callback.onResult(false, "30-day streak promotion is not configured");
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

        impl.grantReward(STREAK_30_PROMO_CODE, STREAK_30_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== All Classes Victory Badge Promotion ====================

    /**
     * Check if All Classes Victory badge promotion is properly configured in build.
     */
    public static boolean isAllClassesConfigured() {
        return ALL_CLASSES_REWARD_AMOUNT > 0
                && ALL_CLASSES_PROMO_CODE != null
                && !ALL_CLASSES_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(ALL_CLASSES_PROMO_CODE);
    }

    /**
     * Check if All Classes Victory badge promotion is available.
     */
    public static boolean isAllClassesAvailable() {
        return isAllClassesConfigured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant All Classes Victory badge reward (VICTORY_ALL_CLASSES).
     * Only works on Appsintoss platform.
     */
    public static void grantAllClassesReward(RewardCallback callback) {
        if (!isAllClassesConfigured()) {
            if (callback != null) {
                callback.onResult(false, "All Classes promotion is not configured");
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

        impl.grantReward(ALL_CLASSES_PROMO_CODE, ALL_CLASSES_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== Jack of All Trades Badge Promotion (BOSS_SLAIN_3_ALL_SUBCLASSES - 만물 박사) ====================

    /**
     * Check if Jack of All Trades (만물 박사) badge promotion is properly configured in build.
     */
    public static boolean isJackOfAllTradesConfigured() {
        return JACK_OF_ALL_TRADES_REWARD_AMOUNT > 0
                && JACK_OF_ALL_TRADES_PROMO_CODE != null
                && !JACK_OF_ALL_TRADES_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(JACK_OF_ALL_TRADES_PROMO_CODE);
    }

    /**
     * Check if Jack of All Trades badge promotion is available.
     */
    public static boolean isJackOfAllTradesAvailable() {
        return isJackOfAllTradesConfigured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant Jack of All Trades (만물 박사) badge reward (BOSS_SLAIN_3_ALL_SUBCLASSES).
     * Only works on Appsintoss platform.
     */
    public static void grantJackOfAllTradesReward(RewardCallback callback) {
        if (!isJackOfAllTradesConfigured()) {
            if (callback != null) {
                callback.onResult(false, "Jack of All Trades promotion is not configured");
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

        impl.grantReward(JACK_OF_ALL_TRADES_PROMO_CODE, JACK_OF_ALL_TRADES_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== Champion 1 Badge Promotion (Bronze - 1+ challenges) ====================

    /**
     * Check if Champion 1 badge promotion is properly configured in build.
     */
    public static boolean isChampion1Configured() {
        return CHAMPION_1_REWARD_AMOUNT > 0
                && CHAMPION_1_PROMO_CODE != null
                && !CHAMPION_1_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(CHAMPION_1_PROMO_CODE);
    }

    /**
     * Check if Champion 1 badge promotion is available.
     */
    public static boolean isChampion1Available() {
        return isChampion1Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant Champion 1 badge reward (Bronze - 1+ challenges).
     * Only works on Appsintoss platform.
     */
    public static void grantChampion1Reward(RewardCallback callback) {
        if (!isChampion1Configured()) {
            if (callback != null) {
                callback.onResult(false, "Champion 1 promotion is not configured");
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

        impl.grantReward(CHAMPION_1_PROMO_CODE, CHAMPION_1_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== Champion 2 Badge Promotion (Silver - 3+ challenges) ====================

    /**
     * Check if Champion 2 badge promotion is properly configured in build.
     */
    public static boolean isChampion2Configured() {
        return CHAMPION_2_REWARD_AMOUNT > 0
                && CHAMPION_2_PROMO_CODE != null
                && !CHAMPION_2_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(CHAMPION_2_PROMO_CODE);
    }

    /**
     * Check if Champion 2 badge promotion is available.
     */
    public static boolean isChampion2Available() {
        return isChampion2Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant Champion 2 badge reward (Silver - 3+ challenges).
     * Only works on Appsintoss platform.
     */
    public static void grantChampion2Reward(RewardCallback callback) {
        if (!isChampion2Configured()) {
            if (callback != null) {
                callback.onResult(false, "Champion 2 promotion is not configured");
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

        impl.grantReward(CHAMPION_2_PROMO_CODE, CHAMPION_2_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== Champion 3 Badge Promotion (Gold - 6+ challenges) ====================

    /**
     * Check if Champion 3 badge promotion is properly configured in build.
     */
    public static boolean isChampion3Configured() {
        return CHAMPION_3_REWARD_AMOUNT > 0
                && CHAMPION_3_PROMO_CODE != null
                && !CHAMPION_3_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(CHAMPION_3_PROMO_CODE);
    }

    /**
     * Check if Champion 3 badge promotion is available.
     */
    public static boolean isChampion3Available() {
        return isChampion3Configured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant Champion 3 badge reward (Gold - 6+ challenges).
     * Only works on Appsintoss platform.
     */
    public static void grantChampion3Reward(RewardCallback callback) {
        if (!isChampion3Configured()) {
            if (callback != null) {
                callback.onResult(false, "Champion 3 promotion is not configured");
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

        impl.grantReward(CHAMPION_3_PROMO_CODE, CHAMPION_3_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }

    // ==================== Taking the Mick Badge Promotion ====================

    /**
     * Check if Taking the Mick badge promotion is properly configured in build.
     */
    public static boolean isTakingTheMickConfigured() {
        return TAKING_THE_MICK_REWARD_AMOUNT > 0
                && TAKING_THE_MICK_PROMO_CODE != null
                && !TAKING_THE_MICK_PROMO_CODE.isEmpty()
                && !"DISABLED".equals(TAKING_THE_MICK_PROMO_CODE);
    }

    /**
     * Check if Taking the Mick badge promotion is available.
     */
    public static boolean isTakingTheMickAvailable() {
        return isTakingTheMickConfigured() && impl != null && impl.isAvailable();
    }

    /**
     * Grant Taking the Mick badge reward.
     * Only works on Appsintoss platform.
     */
    public static void grantTakingTheMickReward(RewardCallback callback) {
        if (!isTakingTheMickConfigured()) {
            if (callback != null) {
                callback.onResult(false, "Taking the Mick promotion is not configured");
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

        impl.grantReward(TAKING_THE_MICK_PROMO_CODE, TAKING_THE_MICK_REWARD_AMOUNT, (success, message) -> {
            rewardInFlight = false;
            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }
}
