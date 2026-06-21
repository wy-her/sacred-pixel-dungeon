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

package com.sacredpixel.sacredpixeldungeon.tutorial;

/**
 * Tutorial progression states.
 */
public enum TutorialState {
	NOT_STARTED,          // Tutorial not started
	GUIDEBOOK_PLACED,     // Guidebook placed, waiting for pickup
	INTRO_SHOWN,          // Introduction story window shown
	JOURNAL_HINT,         // Journal button flashing + hint
	HERO_INFO_HINT,       // Hero info button flashing + hint
	EXAMINE_GUIDE_SHOWN,  // Examine guide WndStory shown
	EXAMINE_HINT,         // Examine button flashing + hint
	RAT_COMBAT,           // Rat (HP=1) spawned + combat hint
	SCROLL_HINT,          // Scroll spawned + pickup hint
	SCROLL_USE,           // Waiting for scroll use
	POTION_HINT,          // Potion spawned + pickup hint
	POTION_USE,           // Waiting for potion use
	SEARCH_PAGE_SPAWNED,  // Search page spawned, waiting for pickup
	SEARCH_GUIDE_SHOWN,   // Search guide WndStory shown
	SEARCH_HINT,          // Search hint shown
	DOOR_FOUND,           // Hidden door found, waiting for hero to enter corridor
	SURPRISE_GUIDE_SHOWN, // Surprise guide WndStory shown
	WAIT_FOR_SNAKE,       // Waiting for hero to press wait button
	SNAKE_AT_DOOR,        // Snake moved to door, waiting for hero to attack
	COMPLETED             // Tutorial completed
}
