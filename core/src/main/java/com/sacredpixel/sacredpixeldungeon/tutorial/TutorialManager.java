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

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.items.Heap;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfFrost;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.levels.Terrain;
import com.sacredpixel.sacredpixeldungeon.levels.TutorialLevel;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.ui.AttackIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.StatusPane;
import com.sacredpixel.sacredpixeldungeon.ui.Toolbar;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.sacredpixel.sacredpixeldungeon.windows.WndStory;
import com.sacredpixel.sacredpixeldungeon.windows.WndTutorial;
import com.watabou.utils.Bundle;

/**
 * Manages tutorial state and events.
 */
public class TutorialManager {

	private static TutorialState state = TutorialState.NOT_STARTED;

	// Pending action to execute after hero moves
	private static Runnable pendingAction = null;

	// Move counter for delayed actions
	private static int moveCounter = 0;
	private static final int MOVES_REQUIRED = 5;  // Number of moves required before next step
	private static final int RESPAWN_DELAY_MOVES = 5;  // Delay before potion respawn
	private static final int SCROLL_HINT_DELAY = 1;   // Delay for scroll hint after fire cleared
	private static int respawnCounter = 0;  // Counter for respawn delay

	// Position constants for snake spawn trigger
	private static final int CORRIDOR_POS = 3 * 16 + 12; // (12, 3) - corridor beyond door, hero waits here

	// Saved journal state for tutorial isolation
	// When entering tutorial, we backup the real journal state and reset to empty
	// When exiting tutorial, we restore the original state
	private static Bundle savedJournalState = null;

	public static TutorialState getState() {
		return state;
	}

	public static void setState(TutorialState newState) {
		state = newState;
	}

	public static void reset() {
		state = TutorialState.NOT_STARTED;
		pendingAction = null;
		moveCounter = 0;
		respawnCounter = 0;
		// Reset all flashing flags
		stopFlashHeroInfo();
		stopFlashExamine();
		stopFlashAttack();
		stopFlashWait();
	}

	/**
	 * Called when entering tutorial mode.
	 * Backs up current journal state (Document, Catalog, Bestiary) and resets to empty.
	 * This ensures tutorial starts with a clean slate and doesn't affect real game progress.
	 */
	public static void enterTutorial() {
		// If already in tutorial mode (re-entry), restore first to avoid losing original state
		if (savedJournalState != null) {
			restoreJournalState();
		}

		// Backup current journal state
		savedJournalState = new Bundle();
		Document.store(savedJournalState);
		Catalog.store(savedJournalState);
		Bestiary.store(savedJournalState);

		// Reset to empty state for tutorial
		Document.reset();
		Catalog.reset();
		Bestiary.reset();
	}

	/**
	 * Called when exiting tutorial mode.
	 * Restores the original journal state that was backed up when entering tutorial.
	 */
	public static void exitTutorial() {
		if (savedJournalState != null) {
			restoreJournalState();
		}
	}

	/**
	 * Restores journal state from backup and clears the backup.
	 */
	private static void restoreJournalState() {
		if (savedJournalState != null) {
			// Reset first to clear tutorial data
			Document.reset();
			Catalog.reset();
			Bestiary.reset();

			// Restore original state
			Document.restore(savedJournalState);
			Catalog.restore(savedJournalState);
			Bestiary.restore(savedJournalState);

			savedJournalState = null;
		}
	}

	/**
	 * Returns true if tutorial journal isolation is active.
	 */
	public static boolean isJournalIsolated() {
		return savedJournalState != null;
	}

	public static boolean isTutorialLevel() {
		return Dungeon.level instanceof TutorialLevel;
	}

	public static boolean isActive() {
		return isTutorialLevel() && state != TutorialState.NOT_STARTED && state != TutorialState.COMPLETED;
	}

	/**
	 * Returns true if movement and general cell actions are restricted.
	 * During SURPRISE_GUIDE_SHOWN: waiting for guide to close
	 * During WAIT_FOR_SNAKE: only wait is allowed
	 * During SNAKE_AT_DOOR: only attack is allowed
	 */
	public static boolean isMovementRestricted() {
		if (!isTutorialLevel()) return false;
		return state == TutorialState.SURPRISE_GUIDE_SHOWN
			|| state == TutorialState.WAIT_FOR_SNAKE
			|| state == TutorialState.SNAKE_AT_DOOR;
	}

	/**
	 * Returns true if wait action is currently allowed.
	 */
	public static boolean isWaitAllowed() {
		if (!isTutorialLevel()) return true;
		// Wait is only allowed during WAIT_FOR_SNAKE
		if (state == TutorialState.WAIT_FOR_SNAKE) return true;
		// If not in restricted state, wait is allowed
		if (state != TutorialState.SNAKE_AT_DOOR) return true;
		return false;
	}

	/**
	 * Returns true if attack action is currently allowed.
	 */
	public static boolean isAttackAllowed() {
		if (!isTutorialLevel()) return true;
		// Attack is allowed during SNAKE_AT_DOOR
		if (state == TutorialState.SNAKE_AT_DOOR) return true;
		// If not in restricted state, attack is allowed
		if (state != TutorialState.WAIT_FOR_SNAKE) return true;
		return false;
	}

	/**
	 * Called when the hero moves. Executes any pending tutorial action after required moves.
	 */
	public static void onHeroMove() {
		if (!isTutorialLevel()) return;

		// Increment move counter and execute pending action after enough moves
		if (pendingAction != null) {
			moveCounter++;
			if (moveCounter >= MOVES_REQUIRED) {
				Runnable action = pendingAction;
				pendingAction = null;
				moveCounter = 0;
				action.run();
			}
		}

		// State-based continuous checks for potion respawn
		if (state == TutorialState.LIQUID_FLAME_HINT) {
			TutorialLevel level = (TutorialLevel) Dungeon.level;
			if (level.isBarricadeDestroyed()) {
				// Success: proceed to frost potion
				setState(TutorialState.FROST_HINT);
				respawnCounter = 0;
				level.spawnFrostPotion();
				GameScene.show(WndTutorial.createFrostHint());
			} else {
				// Check respawn
				respawnCounter++;
				if (respawnCounter >= RESPAWN_DELAY_MOVES) {
					respawnLiquidFlameIfNeeded();
					respawnCounter = 0;
				}
			}
		}

		if (state == TutorialState.FROST_HINT) {
			TutorialLevel level = (TutorialLevel) Dungeon.level;
			if (level.isEternalFireCleared()) {
				// Success: proceed to scroll hint after 1 move
				setState(TutorialState.SCROLL_HINT);
				respawnCounter = 0;
				moveCounter = MOVES_REQUIRED - SCROLL_HINT_DELAY;  // Only need 1 more move
				pendingAction = () -> {
					GameScene.show(WndTutorial.createScrollHint());
				};
			} else {
				// Check respawn
				respawnCounter++;
				if (respawnCounter >= RESPAWN_DELAY_MOVES) {
					respawnFrostIfNeeded();
					respawnCounter = 0;
				}
			}
		}

		// Check if hero entered the corridor position (for snake spawn)
		if (state == TutorialState.DOOR_FOUND && Dungeon.hero.pos == CORRIDOR_POS) {
			// Show surprise guide WndStory first
			setState(TutorialState.SURPRISE_GUIDE_SHOWN);
			showGuideStory(Document.GUIDE_SURPRISE_ATKS, () -> {
				// After surprise guide is closed, spawn snake and show wait hint
				setState(TutorialState.WAIT_FOR_SNAKE);
				((TutorialLevel) Dungeon.level).spawnTutorialSnake();
				GameScene.show(WndTutorial.createWaitHint());
				flashWait();
			});
		}
	}

	/**
	 * Called when a tutorial action occurs.
	 */
	public static void onAction(TutorialAction action) {
		if (!isTutorialLevel()) return;

		switch (action) {
			case GUIDEBOOK_PICKED_UP:
				if (state == TutorialState.GUIDEBOOK_PLACED) {
					// Story window is shown automatically by Guidebook.doPickUp()
					setState(TutorialState.INTRO_SHOWN);
				}
				break;

			case STORY_WINDOW_CLOSED:
				if (state == TutorialState.INTRO_SHOWN) {
					// Show journal hint immediately (no move required)
					setState(TutorialState.JOURNAL_HINT);
					GameScene.show(WndTutorial.createJournalHint());
					flashJournal();
				}
				break;

			case JOURNAL_CLOSED:
				if (state == TutorialState.JOURNAL_HINT) {
					stopFlashJournal();
					// Wait for hero to move 5 times before showing next hint
					setState(TutorialState.HERO_INFO_HINT);
					moveCounter = 0;
					pendingAction = () -> {
						GameScene.show(WndTutorial.createHeroInfoHint());
						flashHeroInfo();
					};
				}
				break;

			case HERO_INFO_CLOSED:
				if (state == TutorialState.HERO_INFO_HINT) {
					stopFlashHeroInfo();
					// Wait for hero to move 5 times before showing examine guide WndStory
					setState(TutorialState.EXAMINE_GUIDE_SHOWN);
					moveCounter = 0;
					pendingAction = () -> {
						showGuideStory(Document.GUIDE_EXAMINING, () -> {
							// After examine guide is closed, show examine hint immediately
							setState(TutorialState.EXAMINE_HINT);
							GameScene.show(WndTutorial.createExamineHint());
							flashExamine();
						});
					};
				}
				break;

			case TILE_INFO_CLOSED:
				if (state == TutorialState.EXAMINE_HINT) {
					// Wait for hero to move 5 times before spawning rat and showing combat hint
					setState(TutorialState.RAT_COMBAT);
					moveCounter = 0;
					pendingAction = () -> {
						((TutorialLevel) Dungeon.level).spawnTutorialRat();
						GameScene.show(WndTutorial.createCombatHint());
						flashAttack();
					};
				}
				break;

			case RAT_KILLED:
				if (state == TutorialState.RAT_COMBAT) {
					stopFlashAttack();
					// Wait for hero to move 5 times before showing identification guide and spawning liquid flame potion
					setState(TutorialState.LIQUID_FLAME_HINT);
					moveCounter = 0;
					pendingAction = () -> {
						// Spawn liquid flame potion first
						((TutorialLevel) Dungeon.level).spawnLiquidFlamePotion();
						// Show identification guide WndStory
						showGuideStory(Document.GUIDE_IDING, () -> {
							// After guide is closed, show liquid flame hint
							GameScene.show(WndTutorial.createLiquidFlameHint());
						});
					};
				}
				break;

			case FIRE_POTION_USED:
				// Potion used - respawn counter is checked in onHeroMove()
				// Reset respawn counter when potion is used
				if (state == TutorialState.LIQUID_FLAME_HINT) {
					respawnCounter = 0;
				}
				break;

			case FROST_POTION_USED:
				// Potion used - respawn counter is checked in onHeroMove()
				// Reset respawn counter when potion is used
				if (state == TutorialState.FROST_HINT) {
					respawnCounter = 0;
				}
				break;

			case SCROLL_USED:
				if (state == TutorialState.SCROLL_HINT) {
					// Wait for hero to move 5 times before spawning search page
					setState(TutorialState.SEARCH_PAGE_SPAWNED);
					moveCounter = 0;
					pendingAction = () -> {
						((TutorialLevel) Dungeon.level).spawnSearchPage();
						// Search page will trigger SEARCH_PAGE_PICKED_UP when picked up
					};
				}
				break;

			case SEARCH_PAGE_PICKED_UP:
				if (state == TutorialState.SEARCH_PAGE_SPAWNED) {
					// Search guide WndStory is shown automatically when page is picked up
					// After the guide is closed, show search hint
					setState(TutorialState.SEARCH_GUIDE_SHOWN);
				}
				break;

			case SEARCH_GUIDE_CLOSED:
				if (state == TutorialState.SEARCH_GUIDE_SHOWN) {
					// Check if door is already discovered (player found it early)
					if (Dungeon.level.map[TutorialLevel.DOOR_POS] != Terrain.SECRET_DOOR) {
						// Door already found, skip search hint and go directly to DOOR_FOUND
						setState(TutorialState.DOOR_FOUND);
					} else {
						// Show search hint immediately after guide is closed
						setState(TutorialState.SEARCH_HINT);
						GameScene.show(WndTutorial.createSearchHint());
						flashExamine();
					}
				}
				break;

			case HIDDEN_DOOR_FOUND:
				if (state == TutorialState.SEARCH_HINT) {
					stopFlashExamine();
					// Wait for hero to move to door position
					setState(TutorialState.DOOR_FOUND);
					// Snake will be spawned when hero enters the door (checked in onHeroMove)
				}
				break;

			case HERO_WAITED:
				if (state == TutorialState.WAIT_FOR_SNAKE) {
					stopFlashWait();
					// Snake will move on its turn and trigger SNAKE_AT_DOOR
				}
				break;

			case SNAKE_AT_DOOR:
				if (state == TutorialState.WAIT_FOR_SNAKE) {
					// Snake is now at the door, show attack hint
					setState(TutorialState.SNAKE_AT_DOOR);
					GameScene.show(WndTutorial.createSurpriseAttackHint());
					flashAttack();
				}
				break;

			case SNAKE_KILLED:
				if (state == TutorialState.SNAKE_AT_DOOR) {
					stopFlashAttack();
					// Show completion immediately
					setState(TutorialState.COMPLETED);
					GameScene.show(WndTutorial.createCompletion());
				}
				break;
		}
	}

	/**
	 * Shows a guide WndStory and executes callback when closed.
	 */
	private static void showGuideStory(String page, Runnable onClose) {
		Document doc = Document.ADVENTURERS_GUIDE;
		if (doc.pageNames().contains(page)) {
			GameScene.show(new WndStory(
					doc.pageSprite(page),
					doc.pageTitle(page),
					doc.pageBody(page)
			) {
				@Override
				public void hide() {
					super.hide();
					if (onClose != null) {
						onClose.run();
					}
				}
			});
			doc.readPage(page);
		}
	}

	// Flash helper methods

	public static void flashJournal() {
		// Flash the journal button in MenuPane
		GameScene.flashJournalButton(Document.ADVENTURERS_GUIDE, Document.GUIDE_INTRO);
	}

	public static void stopFlashJournal() {
		GameScene.stopFlashJournalButton();
	}

	public static void flashHeroInfo() {
		// Reuse existing talent blink code
		StatusPane.talentBlink = 10f;
	}

	public static void stopFlashHeroInfo() {
		StatusPane.talentBlink = 0;
	}

	public static void flashExamine() {
		Toolbar.searchFlashing = true;
	}

	public static void stopFlashExamine() {
		Toolbar.searchFlashing = false;
	}

	public static void flashAttack() {
		AttackIndicator.tutorialFlashing = true;
	}

	public static void stopFlashAttack() {
		AttackIndicator.tutorialFlashing = false;
	}

	public static void flashWait() {
		Toolbar.waitFlashing = true;
	}

	public static void stopFlashWait() {
		Toolbar.waitFlashing = false;
	}

	// Respawn helper methods

	/**
	 * Respawns liquid flame potion if barricade still exists and no potion available.
	 */
	private static void respawnLiquidFlameIfNeeded() {
		TutorialLevel level = (TutorialLevel) Dungeon.level;

		// Already destroyed - shouldn't happen but check anyway
		if (level.isBarricadeDestroyed()) return;

		// Check inventory
		if (Dungeon.hero.belongings.getItem(PotionOfLiquidFlame.class) != null) return;

		// Check map heaps
		for (Heap heap : Dungeon.level.heaps.valueList()) {
			for (Item item : heap.items) {
				if (item instanceof PotionOfLiquidFlame) return;
			}
		}

		// Respawn
		level.spawnLiquidFlamePotion();
		GLog.w(Messages.get(TutorialManager.class, "potion_respawn"));
	}

	/**
	 * Respawns frost potion if eternal fire still exists and no potion available.
	 */
	private static void respawnFrostIfNeeded() {
		TutorialLevel level = (TutorialLevel) Dungeon.level;

		// Already cleared - shouldn't happen but check anyway
		if (level.isEternalFireCleared()) return;

		// Check inventory
		if (Dungeon.hero.belongings.getItem(PotionOfFrost.class) != null) return;

		// Check map heaps
		for (Heap heap : Dungeon.level.heaps.valueList()) {
			for (Item item : heap.items) {
				if (item instanceof PotionOfFrost) return;
			}
		}

		// Respawn
		level.spawnFrostPotion();
		GLog.w(Messages.get(TutorialManager.class, "potion_respawn"));
	}

	/**
	 * Tutorial action types.
	 */
	public enum TutorialAction {
		GUIDEBOOK_PICKED_UP,
		STORY_WINDOW_CLOSED,
		JOURNAL_CLOSED,
		HERO_INFO_CLOSED,
		TILE_INFO_CLOSED,
		RAT_KILLED,
		FIRE_POTION_USED,
		FROST_POTION_USED,
		SCROLL_USED,
		SEARCH_PAGE_PICKED_UP,
		SEARCH_GUIDE_CLOSED,
		HIDDEN_DOOR_FOUND,
		HERO_WAITED,
		SNAKE_AT_DOOR,
		SNAKE_KILLED
	}
}
