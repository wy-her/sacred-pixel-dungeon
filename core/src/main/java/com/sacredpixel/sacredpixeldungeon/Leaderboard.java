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
 * Platform-independent leaderboard interface.
 * Each platform (Appsintoss, etc.) can provide its own implementation.
 */
public class Leaderboard {

    // Platform-specific implementation (set by platform launcher)
    public static LeaderboardImpl impl = null;

    public interface LeaderboardImpl {
        void openLeaderboard();
        void submitScore(int score);
    }

    public static boolean isAvailable() {
        return impl != null;
    }

    public static void open() {
        if (impl != null) impl.openLeaderboard();
    }

    public static void submit(int score) {
        if (impl != null) impl.submitScore(score);
    }
}
