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
 * Platform-independent interstitial ad interface.
 * Each platform (Appsintoss, etc.) can provide its own implementation.
 *
 * Platforms without ads (Cloudflare, Capacitor) will have impl = null,
 * and show() will immediately call the onComplete callback.
 */
public class InterstitialAd {

    // Platform-specific implementation (set by platform launcher)
    public static InterstitialAdImpl impl = null;

    // Ad blocking flag - when true, ads will not be shown
    // Used to prevent late-loaded ads from appearing after game has progressed
    private static boolean blocked = false;

    public interface InterstitialAdImpl {
        /**
         * Show an interstitial ad.
         * @param onComplete Called when the ad is dismissed or fails to show.
         *                   Must be called even on error to ensure game flow continues.
         */
        void show(Runnable onComplete);

        /**
         * Check if interstitial ads are available on this platform.
         */
        boolean isAvailable();

        /**
         * Preload an interstitial ad in the background.
         * Call this early (e.g., when boss dies) so the ad is ready to show instantly.
         */
        default void preload() {
            // Default implementation does nothing
        }

        /**
         * Check if an ad has been preloaded and is ready to show.
         */
        default boolean isPreloaded() {
            return false;
        }

        /**
         * Poll for async ad completion callback.
         * Should be called from game's update loop.
         * This is needed because JavaScript-to-Java async callbacks may not work reliably.
         */
        default void checkCallback() {
            // Default implementation does nothing
        }

        /**
         * Block any pending ad from showing.
         * Clears pending callbacks so late-loaded ads won't appear.
         */
        default void block() {
            // Default implementation does nothing
        }

        /**
         * Get the timestamp when ad was completed (set by JavaScript).
         * Returns 0 if ad hasn't completed yet.
         * Used as fallback timeout detection when JS callback fails to reach Java.
         */
        default double getAdCompletedTimestamp() {
            return 0;
        }

        /**
         * Clear the ad completed timestamp.
         */
        default void clearAdCompletedTimestamp() {
            // Default implementation does nothing
        }
    }

    /**
     * Block ads from showing. Call this when ad load times out
     * to prevent late-loaded ads from appearing during gameplay.
     */
    public static void block() {
        blocked = true;
        if (impl != null) {
            impl.block();
        }
    }

    /**
     * Reset ad blocking. Call this when preparing for a new ad
     * (e.g., when boss is killed and preload starts).
     */
    public static void resetBlock() {
        blocked = false;
    }

    /**
     * Check if ads are currently blocked.
     */
    public static boolean isBlocked() {
        return blocked;
    }

    /**
     * Check if interstitial ads are available.
     */
    public static boolean isAvailable() {
        return impl != null && impl.isAvailable();
    }

    /**
     * Show an interstitial ad.
     * If ads are not available or blocked, immediately calls onComplete.
     *
     * @param onComplete Called when the ad is dismissed or not available.
     */
    public static void show(Runnable onComplete) {
        // If blocked, skip ad and immediately call callback
        if (blocked) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (impl != null && impl.isAvailable()) {
            impl.show(onComplete);
        } else {
            // No ad implementation - immediately continue
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * Preload an interstitial ad in the background.
     * Call this when a boss dies (floors 5, 10, 15, 20) so the ad is ready
     * to show instantly when the player clicks "Next Stage".
     */
    public static void preload() {
        if (impl != null && impl.isAvailable()) {
            impl.preload();
        }
    }

    /**
     * Check if an ad has been preloaded and is ready to show.
     */
    public static boolean isPreloaded() {
        return impl != null && impl.isPreloaded();
    }

    /**
     * Poll for async ad completion callback.
     * Should be called from game's update loop (e.g., InterlevelScene.update()).
     */
    public static void checkCallback() {
        // If blocked, don't poll for callbacks
        if (blocked) {
            return;
        }
        if (impl != null) {
            impl.checkCallback();
        }
    }

    /**
     * Get the timestamp when ad was completed (set by JavaScript).
     * Returns 0 if ad hasn't completed yet.
     */
    public static double getAdCompletedTimestamp() {
        return impl != null ? impl.getAdCompletedTimestamp() : 0;
    }

    /**
     * Clear the ad completed timestamp.
     */
    public static void clearAdCompletedTimestamp() {
        if (impl != null) {
            impl.clearAdCompletedTimestamp();
        }
    }
}
