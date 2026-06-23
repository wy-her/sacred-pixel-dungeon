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

package com.sacredpixel.sacredpixeldungeon.levels;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Blob;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Rat;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Snake;
import com.sacredpixel.sacredpixeldungeon.items.journal.GuidePage;
import com.sacredpixel.sacredpixeldungeon.items.journal.Guidebook;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfFrost;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialManager;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialState;
import com.watabou.noosa.audio.Music;

import java.util.Arrays;

public class TutorialLevel extends Level {

	private static final int W = 16;
	private static final int H = 20;  // Extended for 7x7 fire room below main room

	// Map layout:
	// Main room: x=3-9, y=3-9 (7x7 room)
	// Extended alcove: (10, 3) - one tile extending right from top-right corner
	// Hidden corridor: (12, 3) - beyond the hidden door
	// Hidden door: (11, 3) - on the right wall of extended alcove
	// Hero spawn: (6, 6) - room center
	// Guidebook: (6, 8) - south of hero
	// Search page: (10, 3) - in the extended alcove
	// Snake spawn: (10, 3) - same as search page position
	//
	// Fire room (below main room, 7x7 like MagicalFireRoom):
	// Doorway: (3, 10) - blocked by barricade
	// Fire room interior: x=1-7, y=11-17 (7x7)
	// Fire barrier: y=14, x=1-7 (horizontal EternalFire line, center of room)
	// Behind fire: y=15-17, x=1-7 (EMPTY_SP with scroll)

	// Position constants
	public static final int EXTENDED_ALCOVE_POS = 3 * W + 10; // (10, 3) - search page & snake spawn
	public static final int DOOR_POS = 3 * W + 11;            // (11, 3) - hidden door
	public static final int CORRIDOR_POS = 3 * W + 12;        // (12, 3) - hero waits here for snake

	// Fire room positions (bottom-left, separate 7x7 room like MagicalFireRoom)
	public static final int BARRICADE_POS = 10 * W + 3;       // (3, 10) - doorway blocked by barricade
	public static final int FIRE_LINE_Y = 14;                 // Y position of fire barrier (center)
	public static final int FIRE_ROOM_LEFT = 1;               // Fire room interior left edge
	public static final int FIRE_ROOM_RIGHT = 7;              // Fire room interior right edge
	public static final int FIRE_ROOM_TOP = 11;               // Fire room interior top edge
	public static final int FIRE_ROOM_BOTTOM = 17;            // Fire room interior bottom edge
	public static final int SCROLL_POS = 17 * W + 1;          // (1, 17) - behind fire, bottom-left corner

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
		viewDistance = 8;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_SEWERS;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_SEWERS;
	}

	@Override
	protected boolean build() {
		setSize(W, H);
		Arrays.fill(map, Terrain.WALL);

		// Main 7x7 room (x=3-9, y=3-9)
		for (int y = 3; y <= 9; y++) {
			for (int x = 3; x <= 9; x++) {
				map[y * W + x] = Terrain.EMPTY;
			}
		}

		// Extended alcove at (10, 3) - one tile extending right from top-right
		map[EXTENDED_ALCOVE_POS] = Terrain.EMPTY;

		// Hidden corridor at (12, 3) - beyond the hidden door
		map[3 * W + 12] = Terrain.EMPTY;

		// Hidden door at (11, 3) - on the right wall of extended alcove
		map[DOOR_POS] = Terrain.SECRET_DOOR;

		// Fire room (separate 7x7 room below main room, like MagicalFireRoom)
		// Doorway blocked by barricade at (3, 10)
		map[BARRICADE_POS] = Terrain.BARRICADE;

		// Fire room interior: x=1-7, y=11-17 (7x7)
		for (int y = FIRE_ROOM_TOP; y <= FIRE_ROOM_BOTTOM; y++) {
			for (int x = FIRE_ROOM_LEFT; x <= FIRE_ROOM_RIGHT; x++) {
				map[y * W + x] = Terrain.EMPTY;
			}
		}

		// Fire barrier line at y=14 (EMPTY_SP terrain, like MagicalFireRoom)
		for (int x = FIRE_ROOM_LEFT; x <= FIRE_ROOM_RIGHT; x++) {
			map[FIRE_LINE_Y * W + x] = Terrain.EMPTY_SP;
		}

		// Behind fire area at y=15-17 (EMPTY_SP terrain, like MagicalFireRoom)
		for (int y = FIRE_LINE_Y + 1; y <= FIRE_ROOM_BOTTOM; y++) {
			for (int x = FIRE_ROOM_LEFT; x <= FIRE_ROOM_RIGHT; x++) {
				map[y * W + x] = Terrain.EMPTY_SP;
			}
		}

		return true;
	}

	@Override
	protected void createMobs() {
		// Mobs are spawned dynamically during tutorial
	}

	@Override
	protected void createItems() {
		// Place Guidebook at (6, 8)
		int guidebookPos = 8 * W + 6;
		drop(new Guidebook(), guidebookPos);

		// Place Terror scroll behind the fire (unidentified)
		ScrollOfTerror scroll = new ScrollOfTerror();
		drop(scroll, SCROLL_POS);

		// Place EternalFire barrier along the fire line (y=12, x=2-5)
		// Like MagicalFireRoom, seed EternalFire at each position in the barrier
		for (int x = FIRE_ROOM_LEFT; x <= FIRE_ROOM_RIGHT; x++) {
			int fireCell = FIRE_LINE_Y * W + x;
			Blob.seed(fireCell, 1, MagicalFireRoom.EternalFire.class, this);
		}

		// Reset hero HP
		if (Dungeon.hero != null) {
			Dungeon.hero.HP = Dungeon.hero.HT;
		}

		// Initialize tutorial state
		TutorialManager.setState(TutorialState.GUIDEBOOK_PLACED);
	}

	@Override
	public int entrance() {
		// Hero spawns at room center (6, 6)
		return 6 * W + 6;
	}

	@Override
	public int randomRespawnCell(Char ch) {
		return -1;
	}

	@Override
	public void playLevelMusic() {
		// Play sewers music for tutorial
		Music.INSTANCE.playTracks(SewerLevel.SEWER_TRACK_LIST, SewerLevel.SEWER_TRACK_CHANCES, false);
	}

	// Tutorial spawn methods

	/**
	 * Spawns a rat with 1 HP adjacent to the hero.
	 */
	public void spawnTutorialRat() {
		int heroPos = Dungeon.hero.pos;
		int ratPos = heroPos - 1; // Left of hero

		// Find a valid adjacent position
		int[] adjacents = { heroPos - 1, heroPos + 1, heroPos - W, heroPos + W };
		for (int pos : adjacents) {
			if (passable[pos] && Actor.findChar(pos) == null) {
				ratPos = pos;
				break;
			}
		}

		Rat rat = new Rat() {
			{
				HP = HT = 1;
				state = HUNTING;
			}

			@Override
			public void die(Object cause) {
				super.die(cause);
				TutorialManager.onAction(TutorialManager.TutorialAction.RAT_KILLED);
			}
		};
		rat.pos = ratPos;
		GameScene.add(rat);
		mobs.add(rat);
	}

	/**
	 * Spawns a Potion of Liquid Flame (unidentified) near the hero.
	 */
	public void spawnLiquidFlamePotion() {
		int itemPos = findEmptyAdjacentCell(Dungeon.hero.pos);
		PotionOfLiquidFlame potion = new PotionOfLiquidFlame();
		drop(potion, itemPos);
	}

	/**
	 * Spawns a Potion of Frost (unidentified) near the hero.
	 */
	public void spawnFrostPotion() {
		int itemPos = findEmptyAdjacentCell(Dungeon.hero.pos);
		PotionOfFrost potion = new PotionOfFrost();
		drop(potion, itemPos);
	}

	/**
	 * Finds an empty adjacent cell for item spawning.
	 */
	private int findEmptyAdjacentCell(int pos) {
		int[] adjacents = { pos - 1, pos + 1, pos - W, pos + W };
		for (int cell : adjacents) {
			if (cell >= 0 && cell < length() && passable[cell] && findMob(cell) == null && heaps.get(cell) == null) {
				return cell;
			}
		}
		return pos - 1; // Default fallback
	}

	/**
	 * Returns true if the wooden barricade has been destroyed.
	 */
	public boolean isBarricadeDestroyed() {
		return map[BARRICADE_POS] != Terrain.BARRICADE;
	}

	/**
	 * Returns true if the eternal fire has been extinguished.
	 */
	public boolean isEternalFireCleared() {
		Blob fire = blobs.get(MagicalFireRoom.EternalFire.class);
		return fire == null || fire.volume == 0;
	}

	/**
	 * Spawns search page at extended alcove (10, 3).
	 */
	public void spawnSearchPage() {
		GuidePage page = new GuidePage();
		page.page(Document.GUIDE_SEARCHING);
		drop(page, EXTENDED_ALCOVE_POS);
	}

	/**
	 * Spawns a snake with 1 HP at extended alcove (10, 3).
	 * The snake will move to the door (11, 3) on its next turn.
	 */
	public void spawnTutorialSnake() {
		Snake snake = new Snake() {
			{
				HP = HT = 1;
				state = HUNTING;
			}

			@Override
			protected boolean act() {
				// In tutorial, move directly to door position if not there yet
				if (TutorialManager.isTutorialLevel() && pos == EXTENDED_ALCOVE_POS) {
					int oldPos = pos;
					// Move to door position (this will open the door via occupyCell)
					move(DOOR_POS);
					// Animate the movement
					moveSprite(oldPos, pos);
					// Notify tutorial manager that snake is at door
					TutorialManager.onAction(TutorialManager.TutorialAction.SNAKE_AT_DOOR);
					spend(1 / speed());
					return true;
				}
				// After reaching door, wait without updating FOV (for surprise attack)
				if (TutorialManager.isTutorialLevel() && pos == DOOR_POS) {
					// Don't call super.act() - this keeps fieldOfView null so hero can surprise attack
					spend(1 / speed());
					return true;
				}
				return super.act();
			}

			@Override
			public void die(Object cause) {
				super.die(cause);
				TutorialManager.onAction(TutorialManager.TutorialAction.SNAKE_KILLED);
			}
		};
		snake.pos = EXTENDED_ALCOVE_POS;
		GameScene.add(snake);
		mobs.add(snake);
	}

	// Legacy method for compatibility
	@Deprecated
	public void spawnTutorialItems() {
		spawnLiquidFlamePotion();
	}
}
