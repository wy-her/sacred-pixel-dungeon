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
 * Platform-independent review request interface.
 * Only available on Appsintoss platform.
 *
 * Usage:
 * - Call Review.request() at appropriate moments (e.g., tutorial completion)
 * - The implementation handles platform-specific API calls
 */
public class Review {

    // Platform-specific implementation (set by platform launcher)
    public static ReviewImpl impl = null;

    public interface ReviewImpl {
        /**
         * Request app review.
         * @param callback Called when review request completes (success/failure)
         */
        void request(ReviewCallback callback);

        /**
         * Check if review API is available.
         */
        boolean isAvailable();
    }

    public interface ReviewCallback {
        void onComplete(boolean success);
    }

    /**
     * Check if review system is available (Appsintoss only).
     */
    public static boolean isAvailable() {
        return impl != null && impl.isAvailable();
    }

    /**
     * Request app review.
     * Only works on Appsintoss platform.
     * Call at appropriate moments when user is likely satisfied.
     */
    public static void request() {
        request(null);
    }

    /**
     * Request app review with callback.
     * Only works on Appsintoss platform.
     */
    public static void request(ReviewCallback callback) {
        if (impl != null && impl.isAvailable()) {
            impl.request(callback);
        } else {
            // Not on Appsintoss - silently ignore
            if (callback != null) {
                callback.onComplete(false);
            }
        }
    }
}
