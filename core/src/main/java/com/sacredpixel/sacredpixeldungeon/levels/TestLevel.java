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
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Imp;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.sacredpixel.sacredpixeldungeon.items.Amulet;
import com.sacredpixel.sacredpixeldungeon.items.wands.Wand;
import com.sacredpixel.sacredpixeldungeon.items.armor.PlateArmor;
import com.sacredpixel.sacredpixeldungeon.items.BrokenSeal;
import com.sacredpixel.sacredpixeldungeon.items.Generator;
import com.sacredpixel.sacredpixeldungeon.items.Heap;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.*;
import com.sacredpixel.sacredpixeldungeon.items.food.Food;
import com.sacredpixel.sacredpixeldungeon.items.potions.Potion;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfHealing;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfStrength;
import com.sacredpixel.sacredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.sacredpixel.sacredpixeldungeon.items.rings.RingOfAccuracy;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.Scroll;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.sacredpixel.sacredpixeldungeon.items.spells.Alchemize;
import com.sacredpixel.sacredpixeldungeon.items.spells.BeaconOfReturning;
import com.sacredpixel.sacredpixeldungeon.items.spells.CurseInfusion;
import com.sacredpixel.sacredpixeldungeon.items.spells.MagicalInfusion;
import com.sacredpixel.sacredpixeldungeon.items.spells.PhaseShift;
import com.sacredpixel.sacredpixeldungeon.items.spells.ReclaimTrap;
import com.sacredpixel.sacredpixeldungeon.items.spells.Recycle;
import com.sacredpixel.sacredpixeldungeon.items.spells.SummonElemental;
import com.sacredpixel.sacredpixeldungeon.items.spells.TelekineticGrab;
import com.sacredpixel.sacredpixeldungeon.items.spells.UnstableSpell;
import com.sacredpixel.sacredpixeldungeon.items.spells.WildEnergy;
import com.sacredpixel.sacredpixeldungeon.items.stones.Runestone;
import com.sacredpixel.sacredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfDisintegration;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfFireblast;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfMagicMissile;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.Greatsword;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.MagesStaff;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.sacredpixel.sacredpixeldungeon.items.TengusMask;
import com.sacredpixel.sacredpixeldungeon.items.KingsCrown;
import com.sacredpixel.sacredpixeldungeon.levels.features.LevelTransition;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.quest.BlacksmithRoom;
import com.watabou.utils.Reflection;

import com.watabou.noosa.audio.Music;

import java.util.ArrayList;
import java.util.Arrays;

public class TestLevel extends Level {

	private static final int W = 32;
	private static final int H = 32;

	{
		color1 = 0x4b6636;
		color2 = 0x556b2f;
		viewDistance = 8;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CITY;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CITY;
	}

	@Override
	protected boolean build() {
		setSize(W, H);
		Arrays.fill(map, Terrain.WALL);

		for (int y = 1; y < H - 1; y++) {
			for (int x = 1; x < W - 1; x++) {
				map[y * W + x] = Terrain.EMPTY;
			}
		}

		// No entrance (ascending stairs) - hero spawns at center
		int spawnPos = 16 * W + 16;
		map[spawnPos] = Terrain.EMPTY;

		// Exit 1: goes to depth 20 (City Boss)
		int exit1Pos = 18 * W + 14;
		map[exit1Pos] = Terrain.EXIT;
		transitions.add(new LevelTransition(this, exit1Pos, LevelTransition.Type.REGULAR_EXIT,
				20, 0, LevelTransition.Type.REGULAR_ENTRANCE));

		// Exit 2: goes to depth 5 (Sewers boss - Goo)
		int exit2Pos = 18 * W + 18;
		map[exit2Pos] = Terrain.EXIT;
		transitions.add(new LevelTransition(this, exit2Pos, LevelTransition.Type.REGULAR_EXIT,
				5, 0, LevelTransition.Type.REGULAR_ENTRANCE));

		// Alchemy room (5x5) in top-left area
		// Room boundaries: x=5-9, y=5-9
		for (int y = 5; y <= 9; y++) {
			for (int x = 5; x <= 9; x++) {
				if (y == 5 || y == 9 || x == 5 || x == 9) {
					map[y * W + x] = Terrain.WALL;
				} else {
					map[y * W + x] = Terrain.EMPTY_SP;
				}
			}
		}
		// Alchemy pot in center
		map[7 * W + 7] = Terrain.ALCHEMY;
		// Door on bottom side (unlocked)
		map[9 * W + 7] = Terrain.DOOR;

		// Shop room (7x7) in top-right area
		// Room boundaries: x=22-28, y=5-11
		for (int y = 5; y <= 11; y++) {
			for (int x = 22; x <= 28; x++) {
				if (y == 5 || y == 11 || x == 22 || x == 28) {
					map[y * W + x] = Terrain.WALL;
				} else {
					map[y * W + x] = Terrain.EMPTY_SP;
				}
			}
		}
		// Door on bottom side (unlocked)
		map[11 * W + 25] = Terrain.DOOR;

		// Imp Quest Room (9x9) in bottom-left area - like AmbitiousImpRoom
		// Room boundaries: x=1-9, y=22-30
		int impRoomCenterX = 5;
		int impRoomCenterY = 26;
		for (int y = 22; y <= 30; y++) {
			for (int x = 1; x <= 9; x++) {
				if (y == 22 || y == 30 || x == 1 || x == 9) {
					map[y * W + x] = Terrain.WALL;
				} else {
					// Only center 3x3 (inside pillars at ±2) gets carpet
					if (Math.abs(x - impRoomCenterX) <= 1 && Math.abs(y - impRoomCenterY) <= 1) {
						map[y * W + x] = Terrain.EMPTY_SP;  // Carpet only in center 3x3
					} else {
						map[y * W + x] = Terrain.EMPTY;     // Regular tiles elsewhere
					}
				}
			}
		}
		// Statues at corners (like AmbitiousImpRoom) - at center ±2
		map[(impRoomCenterY - 2) * W + (impRoomCenterX - 2)] = Terrain.STATUE;
		map[(impRoomCenterY - 2) * W + (impRoomCenterX + 2)] = Terrain.STATUE;
		map[(impRoomCenterY + 2) * W + (impRoomCenterX - 2)] = Terrain.STATUE;
		map[(impRoomCenterY + 2) * W + (impRoomCenterX + 2)] = Terrain.STATUE;
		// Door on right side
		map[26 * W + 9] = Terrain.DOOR;

		// Blacksmith room area (near blacksmith at x=16, y=20)
		// Mining entrance at x=14, y=20
		int mineEntrancePos = 20 * W + 14;
		map[mineEntrancePos] = Terrain.EXIT;

		// Add visual for the mine entrance
		BlacksmithRoom.QuestEntrance vis = new BlacksmithRoom.QuestEntrance();
		vis.pos(mineEntrancePos % W, mineEntrancePos / W);
		customTiles.add(vis);

		transitions.add(new LevelTransition(this, mineEntrancePos,
			LevelTransition.Type.BRANCH_EXIT,
			14, 1, LevelTransition.Type.BRANCH_ENTRANCE));

		return true;
	}

	@Override
	protected void createMobs() {
		// Set depth to 14 (Caves quest floor)
		Dungeon.depth = 14;

		// Shopkeeper in shop room center (x=25, y=8)
		Shopkeeper shopkeeper = new Shopkeeper();
		shopkeeper.pos = 8 * W + 25;
		mobs.add(shopkeeper);

		// === Imp Quest NPC (quest spawned but not given) ===
		// Imp at center of Imp room (x=5, y=26) - mimicking AmbitiousImpRoom
		Imp imp = new Imp();
		imp.pos = 26 * W + 5;
		mobs.add(imp);
		// Set quest state: spawned but NOT given (for testing quest dialog)
		Imp.Quest.spawned = true;
		Imp.Quest.given = false;
		Imp.Quest.completed = true;  // Completed for shop spawning on floor 20
		Imp.Quest.reward = (com.sacredpixel.sacredpixeldungeon.items.rings.Ring) Generator.random(Generator.Category.RING);

		// === Blacksmith Quest (spawned but not started - for testing) ===
		// Blacksmith (x=16, y=20)
		Blacksmith blacksmith = new Blacksmith();
		blacksmith.pos = 20 * W + 16;
		mobs.add(blacksmith);
		// Set quest state: spawned but NOT given (for testing quest dialog)
		// Random quest type for testing: CRYSTAL or GNOLL (FUNGI not implemented)
		Blacksmith.Quest.type = com.watabou.utils.Random.Int(2) == 0
			? Blacksmith.Quest.CRYSTAL
			: Blacksmith.Quest.GNOLL;
		Blacksmith.Quest.spawned = true;
		Blacksmith.Quest.given = false;
		Blacksmith.Quest.started = false;
		Blacksmith.Quest.completed = false;
		Blacksmith.Quest.bossBeaten = false;
		Blacksmith.Quest.favor = 0;
	}

	@Override
	protected void createItems() {
		Dungeon.hero.HP = Dungeon.hero.HT = 999;
		Dungeon.hero.STR = 20;
		Dungeon.hero.lvl = 24;  // Level 24

		Dungeon.gold = 10000;

		// Mark all bosses as defeated (for depth 20 and 25 boss levels)
		Statistics.bossScores[0] = 1000; // Goo
		Statistics.bossScores[1] = 2000; // Tengu
		Statistics.bossScores[2] = 3000; // DM-300
		Statistics.bossScores[3] = 4000; // Dwarf King
		Statistics.bossScores[4] = 5000; // Yog-Dzewa

		// Rat King is NOT awoken (sleeping state for testing)
		Statistics.ratKingAwoken = false;

		// Remove default starting items (ThrowingKnife and Food)
		ArrayList<Item> toRemove = new ArrayList<>();
		for (Item item : Dungeon.hero.belongings.backpack.items) {
			if (item instanceof ThrowingKnife || item instanceof Food) {
				toRemove.add(item);
			}
		}
		for (Item item : toRemove) {
			Dungeon.hero.belongings.backpack.items.remove(item);
		}

		// Hero equipment
		Greatsword sword = new Greatsword();
		sword.upgrade(6);
		sword.identify();
		Dungeon.hero.belongings.weapon = sword;

		PlateArmor armor = new PlateArmor();
		armor.upgrade(6);
		armor.identify();
		Dungeon.hero.belongings.armor = armor;

		RingOfAccuracy ring = new RingOfAccuracy();
		ring.upgrade(9);
		ring.identify();
		Dungeon.hero.belongings.ring = ring;

		// Items for quests (in inventory)
		Dungeon.hero.belongings.backpack.items.add(new TengusMask());
		Dungeon.hero.belongings.backpack.items.add(new KingsCrown());
		Dungeon.hero.belongings.backpack.items.add(new BrokenSeal());

		// Unidentified items for testing
		Dungeon.hero.belongings.backpack.items.add(Generator.random(Generator.Category.RING));  // Unidentified ring
		Dungeon.hero.belongings.backpack.items.add(Generator.random(Generator.Category.SCROLL));  // Unidentified scroll
		Dungeon.hero.belongings.backpack.items.add(new ScrollOfMagicMapping());  // Unidentified for alchemy testing

		// Shop items (in shop room: x=23-27, y=6-10)
		drop(new PotionOfHealing().identify(), 6 * W + 23).type = Heap.Type.FOR_SALE;
		drop(new PotionOfHealing().identify(), 6 * W + 24).type = Heap.Type.FOR_SALE;
		drop(new PotionOfStrength().identify(), 6 * W + 26).type = Heap.Type.FOR_SALE;
		drop(new PotionOfStrength().identify(), 6 * W + 27).type = Heap.Type.FOR_SALE;
		drop(new ScrollOfUpgrade().identify(), 7 * W + 23).type = Heap.Type.FOR_SALE;
		drop(new ScrollOfUpgrade().identify(), 7 * W + 27).type = Heap.Type.FOR_SALE;
		drop(Generator.random(Generator.Category.WEAPON).identify(), 9 * W + 23).type = Heap.Type.FOR_SALE;
		drop(Generator.random(Generator.Category.ARMOR).identify(), 9 * W + 27).type = Heap.Type.FOR_SALE;
		drop(Generator.random(Generator.Category.RING).identify(), 10 * W + 24).type = Heap.Type.FOR_SALE;
		drop(Generator.random(Generator.Category.WAND).identify(), 10 * W + 26).type = Heap.Type.FOR_SALE;

		// === Drop all items near hero spawn (center at 16,16) ===
		int spawnPos = 16 * W + 16;
		int exit1Pos = 18 * W + 14;  // → depth 20
		int exit2Pos = 18 * W + 18;  // → depth 5
		ArrayList<Integer> dropPositions = new ArrayList<>();

		// Generate positions in expanding rings around spawn
		// Skip spawn and exits
		for (int radius = 1; radius <= 14; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dy = -radius; dy <= radius; dy++) {
					if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue; // Only edge of square
					int x = 16 + dx;
					int y = 16 + dy;
					if (x >= 1 && x < W-1 && y >= 1 && y < H-1) {
						int pos = y * W + x;
						if (pos != spawnPos && pos != exit1Pos && pos != exit2Pos && map[pos] == Terrain.EMPTY) {
							dropPositions.add(pos);
						}
					}
				}
			}
		}

		int dropIdx = 0;

		// === All Artifacts (identified, uncursed, +10) ===
		for (Class<?> artifactClass : Generator.Category.ARTIFACT.classes) {
			try {
				Artifact artifact = (Artifact) Reflection.newInstance(artifactClass);
				artifact.cursed = false;
				artifact.upgrade(10);  // Most artifacts have levelCap=10
				artifact.identify();
				if (dropIdx < dropPositions.size()) {
					drop(artifact, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// === All Wands (identified, uncursed, +5) ===
		for (Class<?> wandClass : Generator.Category.WAND.classes) {
			try {
				Wand wand = (Wand) Reflection.newInstance(wandClass);
				wand.cursed = false;
				wand.upgrade(5);
				wand.identify();
				if (dropIdx < dropPositions.size()) {
					drop(wand, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// === Mage's Staff ===
		MagesStaff staff = new MagesStaff();
		staff.upgrade(5);
		staff.identify();
		if (dropIdx < dropPositions.size()) {
			drop(staff, dropPositions.get(dropIdx++));
		}

		// === TrinketCatalyst ===
		if (dropIdx < dropPositions.size()) {
			drop(new TrinketCatalyst(), dropPositions.get(dropIdx++));
		}

		// === All Runestones (identified) ===
		for (Class<?> stoneClass : Generator.Category.STONE.classes) {
			try {
				Runestone stone = (Runestone) Reflection.newInstance(stoneClass);
				stone.identify();
				if (dropIdx < dropPositions.size()) {
					drop(stone, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// === All Scrolls (unidentified) ===
		for (Class<?> scrollClass : Generator.Category.SCROLL.classes) {
			try {
				Scroll scroll = (Scroll) Reflection.newInstance(scrollClass);
				if (dropIdx < dropPositions.size()) {
					drop(scroll, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// === All Exotic Scrolls (unidentified) ===
		for (Class<?> exoScrollClass : ExoticScroll.regToExo.values()) {
			try {
				ExoticScroll exoScroll = (ExoticScroll) Reflection.newInstance(exoScrollClass);
				if (dropIdx < dropPositions.size()) {
					drop(exoScroll, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// === All Potions (unidentified) ===
		for (Class<?> potionClass : Generator.Category.POTION.classes) {
			try {
				Potion potion = (Potion) Reflection.newInstance(potionClass);
				if (dropIdx < dropPositions.size()) {
					drop(potion, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// === All Exotic Potions (unidentified) ===
		for (Class<?> exoPotionClass : ExoticPotion.regToExo.values()) {
			try {
				ExoticPotion exoPotion = (ExoticPotion) Reflection.newInstance(exoPotionClass);
				if (dropIdx < dropPositions.size()) {
					drop(exoPotion, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// === All Spells ===
		Class<?>[] spellClasses = {
			Alchemize.class,
			BeaconOfReturning.class,
			CurseInfusion.class,
			MagicalInfusion.class,
			PhaseShift.class,
			ReclaimTrap.class,
			Recycle.class,
			SummonElemental.class,
			TelekineticGrab.class,
			UnstableSpell.class,
			WildEnergy.class
		};
		for (Class<?> spellClass : spellClasses) {
			try {
				Item spell = (Item) Reflection.newInstance(spellClass);
				spell.identify();
				if (dropIdx < dropPositions.size()) {
					drop(spell, dropPositions.get(dropIdx++));
				}
			} catch (Exception e) {
				// Log but continue - test level should spawn what it can
			}
		}

		// Amulet of Yendor
		if (dropIdx < dropPositions.size()) {
			drop(new Amulet(), dropPositions.get(dropIdx++));
		}
	}

	@Override
	public int entrance() {
		// Hero spawns at center (no actual entrance stairs)
		return 16 * W + 16;
	}

	@Override
	public int randomRespawnCell(Char ch) {
		return -1;
	}

	@Override
	public void playLevelMusic() {
		//Play Caves (floor 5) music in Test Level
		Music.INSTANCE.playTracks(CavesLevel.CAVES_TRACK_LIST, CavesLevel.CAVES_TRACK_CHANCES, false);
	}
}
