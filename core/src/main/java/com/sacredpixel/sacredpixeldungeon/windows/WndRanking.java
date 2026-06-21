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

package com.sacredpixel.sacredpixeldungeon.windows;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Challenges;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.GamesInProgress;
import com.sacredpixel.sacredpixeldungeon.QuickSlot;
import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.scenes.InterlevelScene;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Belongings;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.items.EquipableItem;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.trinkets.Trinket;
import com.sacredpixel.sacredpixeldungeon.items.wands.Wand;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.HeroSprite;
import com.sacredpixel.sacredpixeldungeon.ui.BadgesGrid;
import com.sacredpixel.sacredpixeldungeon.ui.BadgesList;
import com.sacredpixel.sacredpixeldungeon.ui.Button;
import com.sacredpixel.sacredpixeldungeon.ui.CheckBox;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.ItemSlot;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.TalentButton;
import com.sacredpixel.sacredpixeldungeon.ui.TalentsPane;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DateCompat;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Signal;
import com.watabou.input.KeyEvent;
import com.watabou.input.KeyBindings;
import com.watabou.input.GameAction;
import com.sacredpixel.sacredpixeldungeon.SPDAction;

import java.util.ArrayList;
import java.util.Locale;

public class WndRanking extends WndTabbed {

	private static final int WIDTH = 149;
	private static final int HEIGHT = 152;
	private static final int BTN_HEIGHT = 16;

	private static WndRanking INSTANCE;

	private String gameID;
	private Rankings.Record record;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private int currentTabIndex = 0;

	// Tab references
	private StatsTab statsTab;
	private TalentsTab talentsTab;
	private ItemsTab itemsTab;
	private BadgesTab badgesTab;
	private ChallengesTab challengesTab;

	// Items tab focus
	private ArrayList<Button> focusableItems = new ArrayList<>();
	private int itemsFocusIndex = -1;

	// Badges component (BadgesGrid or BadgesList)
	private Component badgesComponent;

	public WndRanking(final Rankings.Record rec) {

		super();
		resize(WIDTH, HEIGHT);

		if (INSTANCE != null) {
			INSTANCE.hide();
		}
		INSTANCE = this;

		this.gameID = rec.gameID;
		this.record = rec;

		try {
			Badges.loadGlobal();
			Rankings.INSTANCE.loadGameData(rec);
			createControls();
		} catch (Exception e) {
			Game.reportException(new RuntimeException("Rankings Display Failed!", e));
			Dungeon.hero = null;
			createControls();
		}
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		// Deactivate badges component keyboard if active
		if (badgesComponent instanceof BadgesGrid) {
			((BadgesGrid) badgesComponent).setKeyboardActive(false);
		} else if (badgesComponent instanceof BadgesList) {
			((BadgesList) badgesComponent).setKeyboardActive(false);
		}
		super.destroy();
		if (INSTANCE == this) {
			INSTANCE = null;
		}
	}

	private void createControls() {
		// If Dungeon.hero is null (gameData load failed), show only basic stats
		if (Dungeon.hero == null) {
			statsTab = new StatsTab();
			add(statsTab);
			statsTab.init();
			return;
		}

		Icons[] icons =
				{Icons.RANKINGS, Icons.TALENT, Icons.BACKPACK_LRG, Icons.BADGES, Icons.CHALLENGE_COLOR};

		// Create empty tab groups first
		statsTab = new StatsTab();
		talentsTab = new TalentsTab();
		itemsTab = new ItemsTab();
		badgesTab = new BadgesTab();

		Group[] pages = {statsTab, talentsTab, itemsTab, badgesTab, null};

		if (Dungeon.challenges != 0) {
			challengesTab = new ChallengesTab();
			pages[4] = challengesTab;
		}

		// Add tabs to window first (so they have proper camera inheritance)
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] == null) {
				break;
			}

			add(pages[i]);

			Tab tab = new RankingTab(icons[i], pages[i], i);
			add(tab);
		}

		// Now initialize tab content (after they're added to window)
		statsTab.init();
		talentsTab.init();
		itemsTab.init();
		badgesTab.init();
		if (challengesTab != null) {
			challengesTab.init();
		}

		layoutTabs();
		select(0);

		setupKeyListener();
	}

	private void setupKeyListener() {
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

				// Don't handle keys if this window is inactive (dialog open)
				if (!WndRanking.this.active) return false;

				GameAction action = KeyBindings.getActionForKey(event);
				boolean isUp = action == SPDAction.N || action == SPDAction.NW || action == SPDAction.W || action == SPDAction.SW;
				boolean isDown = action == SPDAction.S || action == SPDAction.SE || action == SPDAction.E || action == SPDAction.NE;
				boolean isEnter = event.code == com.badlogic.gdx.Input.Keys.ENTER
						|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER;

				switch (currentTabIndex) {
					case 0: // Stats tab
						if (statsTab != null && statsTab.visible) {
							if (isUp) {
								statsTab.moveFocus(-1);
								return true;
							} else if (isDown) {
								statsTab.moveFocus(1);
								return true;
							} else if (isEnter) {
								statsTab.activateFocused();
								return true;
							}
						}
						break;

					case 1: // Talents tab
						if (talentsTab != null && talentsTab.visible && talentsTab.talentsPane != null) {
							if (action == SPDAction.N || action == SPDAction.NW) {
								talentsTab.talentsPane.moveTierFocus(-1);
								return true;
							} else if (action == SPDAction.S || action == SPDAction.SE) {
								talentsTab.talentsPane.moveTierFocus(1);
								return true;
							} else if (action == SPDAction.W || action == SPDAction.SW) {
								talentsTab.talentsPane.moveTalentFocus(-1);
								return true;
							} else if (action == SPDAction.E || action == SPDAction.NE) {
								talentsTab.talentsPane.moveTalentFocus(1);
								return true;
							} else if (isEnter) {
								talentsTab.talentsPane.activateFocused();
								return true;
							}
						}
						break;

					case 2: // Items tab
						if (itemsTab != null && itemsTab.visible && !focusableItems.isEmpty()) {
							if (isUp) {
								moveItemsFocus(-1);
								return true;
							} else if (isDown) {
								moveItemsFocus(1);
								return true;
							} else if (isEnter) {
								if (itemsFocusIndex >= 0 && itemsFocusIndex < focusableItems.size()) {
									Button btn = focusableItems.get(itemsFocusIndex);
									if (btn.active) {
										Sample.INSTANCE.play(Assets.Sounds.CLICK);
										btn.click();
									}
								}
								return true;
							}
						}
						break;

					case 3: // Badges tab - handled by BadgesGrid/BadgesList own keyListener
						return false;

					case 4: // Challenges tab
						if (challengesTab != null && challengesTab.visible) {
							if (isUp) {
								challengesTab.moveFocus(-1);
								return true;
							} else if (isDown) {
								challengesTab.moveFocus(1);
								return true;
							} else if (isEnter) {
								challengesTab.activateFocused();
								return true;
							}
						}
						break;
				}
				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);
	}

	private void moveItemsFocus(int direction) {
		if (focusableItems.isEmpty()) return;

		// Clear previous focus
		if (itemsFocusIndex >= 0 && itemsFocusIndex < focusableItems.size()) {
			Button btn = focusableItems.get(itemsFocusIndex);
			if (btn instanceof ItemButton) {
				((ItemButton) btn).setFocused(false);
			} else if (btn instanceof QuickSlotButton) {
				((QuickSlotButton) btn).setFocused(false);
			}
		}

		// Move index
		if (itemsFocusIndex == -1) {
			itemsFocusIndex = direction > 0 ? 0 : focusableItems.size() - 1;
		} else {
			itemsFocusIndex += direction;
			if (itemsFocusIndex < 0) itemsFocusIndex = focusableItems.size() - 1;
			if (itemsFocusIndex >= focusableItems.size()) itemsFocusIndex = 0;
		}

		// Set focus on new item
		Button btn = focusableItems.get(itemsFocusIndex);
		if (btn instanceof ItemButton) {
			((ItemButton) btn).setFocused(true);
		} else if (btn instanceof QuickSlotButton) {
			((QuickSlotButton) btn).setFocused(true);
		}
	}

	private class RankingTab extends IconTab {

		private Group page;
		private int tabIndex;

		public RankingTab(Icons icon, Group page, int tabIndex) {
			super(Icons.get(icon));
			this.page = page;
			this.tabIndex = tabIndex;
		}

		@Override
		protected void select(boolean value) {
			super.select(value);
			if (page != null) {
				page.visible = page.active = selected;

				if (selected) {
					currentTabIndex = tabIndex;
					activateTabKeyboard(tabIndex);
				} else {
					deactivateTabKeyboard(tabIndex);
				}
			}
		}
	}

	private void activateTabKeyboard(int tabIndex) {
		switch (tabIndex) {
			case 0: // Stats
				if (statsTab != null) statsTab.resetFocus();
				break;
			case 1: // Talents
				if (talentsTab != null && talentsTab.talentsPane != null) {
					talentsTab.talentsPane.clearFocus();
				}
				break;
			case 2: // Items
				itemsFocusIndex = -1;
				break;
			case 3: // Badges
				if (badgesComponent instanceof BadgesGrid) {
					((BadgesGrid) badgesComponent).setKeyboardActive(true);
				} else if (badgesComponent instanceof BadgesList) {
					((BadgesList) badgesComponent).setKeyboardActive(true);
				}
				break;
			case 4: // Challenges
				if (challengesTab != null) challengesTab.resetFocus();
				break;
		}
	}

	private void deactivateTabKeyboard(int tabIndex) {
		switch (tabIndex) {
			case 3: // Badges
				if (badgesComponent instanceof BadgesGrid) {
					((BadgesGrid) badgesComponent).setKeyboardActive(false);
				} else if (badgesComponent instanceof BadgesList) {
					((BadgesList) badgesComponent).setKeyboardActive(false);
				}
				break;
		}
	}

	private class StatsTab extends Group {

		private static final int GAP = 3;
		private ArrayList<Button> focusableButtons = new ArrayList<>();
		private int focusIndex = -1;

		public StatsTab() {
			super();
		}

		public void init() {
			String heroClass = Dungeon.hero != null ? Dungeon.hero.className() : record.heroClass.name();

			IconTitle title = new IconTitle();
			title.icon(HeroSprite.avatar(record.heroClass, record.armorTier));
			title.label(Messages.get(this, "title", record.herolevel, heroClass).toUpperCase(Locale.ENGLISH));
			title.color(Window.TITLE_COLOR);
			title.setRect(0, 0, WIDTH, 0);
			add(title);

			float pos = title.bottom() + 1;

			// Date and version
			RenderedTextBlock date = PixelScene.renderTextBlock(record.date, 7);
			date.hardlight(0xCCCCCC);
			date.setPos(0, pos);
			add(date);

			RenderedTextBlock version = PixelScene.renderTextBlock(record.version, 7);
			version.hardlight(0xCCCCCC);
			version.setPos(WIDTH - version.width(), pos);
			add(version);

			pos = date.bottom() + 5;

			Locale loc = Messages.locale();

			// Score with info button
			pos = statSlot(this, Messages.get(this, "score"), DateCompat.formatNumber(Statistics.totalScore, loc), pos);

			IconButton scoreInfo = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void onClick() {
					super.onClick();
					WndRanking.this.active = false;
					SacredPixelDungeon.scene().addToFront(new WndScoreBreakdown() {
						@Override
						public void destroy() {
							super.destroy();
							if (WndRanking.this.parent != null) {
								WndRanking.this.active = true;
							}
						}
					});
				}
			};
			scoreInfo.setSize(16, 16);
			scoreInfo.setPos(WIDTH - scoreInfo.width(), 0);
			add(scoreInfo);
			focusableButtons.add(scoreInfo);

			pos += GAP + 2;

			// STR (only if hero data is available)
			if (Dungeon.hero != null) {
				int strBonus = Dungeon.hero.STR() - Dungeon.hero.STR;
				if (strBonus > 0)
					pos = statSlot(this, Messages.get(this, "str"), Dungeon.hero.STR + " + " + strBonus, pos);
				else if (strBonus < 0)
					pos = statSlot(this, Messages.get(this, "str"), Dungeon.hero.STR + " - " + -strBonus, pos);
				else
					pos = statSlot(this, Messages.get(this, "str"), Integer.toString(Dungeon.hero.STR), pos);
			}

			// Duration
			pos = statSlot(this, Messages.get(this, "duration"), DateCompat.formatNumber((int) Statistics.duration, loc), pos);

			// Depth or Ascent
			if (Statistics.highestAscent == 0) {
				pos = statSlot(this, Messages.get(this, "depth"), DateCompat.formatNumber(Statistics.deepestFloor, loc), pos);
			} else {
				pos = statSlot(this, Messages.get(this, "ascent"), DateCompat.formatNumber(Statistics.highestAscent, loc), pos);
			}

			// Seed
			if (Dungeon.seed != -1) {
				if (!Dungeon.customSeedText.isEmpty()) {
					pos = statSlot(this, Messages.get(this, "custom_seed"), "_" + Dungeon.customSeedText + "_", pos);
				} else {
					pos = statSlot(this, Messages.get(this, "seed"), DungeonSeed.convertToCode(Dungeon.seed), pos);
				}
			} else {
				pos += GAP + 5;
			}

			pos += GAP + 2;

			// Game statistics
			pos = statSlot(this, Messages.get(this, "enemies"), DateCompat.formatNumber(Statistics.enemiesSlain, loc), pos);
			pos = statSlot(this, Messages.get(this, "gold"), DateCompat.formatNumber(Statistics.goldCollected, loc), pos);
			pos = statSlot(this, Messages.get(this, "food"), DateCompat.formatNumber(Statistics.foodEaten, loc), pos);
			pos = statSlot(this, Messages.get(this, "alchemy"), DateCompat.formatNumber(Statistics.itemsCrafted, loc), pos);

			// Play Again button at bottom (if seed is available)
			int buttontop = HEIGHT - BTN_HEIGHT;

			// Use the record's seed and challenges, not the current Dungeon state
			final long seed = record.dungeonSeed;
			final int challenges = record.challenges;

			if (seed != -1 && (DeviceCompat.isDebug() || Badges.isUnlocked(Badges.Badge.VICTORY))) {
				RedButton btnPlayAgain = new RedButton(Messages.get(this, "play_again")) {
					@Override
					protected void onClick() {
						super.onClick();

						GamesInProgress.checkAll();
						int emptySlot = -1;
						for (int i = 1; i <= GamesInProgress.MAX_SLOTS; i++) {
							if (GamesInProgress.check(i) == null) {
								emptySlot = i;
								break;
							}
						}

						if (emptySlot == -1) {
							SacredPixelDungeon.scene().addToFront(
									new WndMessage(Messages.get(WndRanking.StatsTab.this, "no_slot")));
							return;
						}

						final int slot = emptySlot;
						WndRanking.this.active = false;

						SacredPixelDungeon.scene().addToFront(new WndOptions(
								Icons.SEED.get(),
								Messages.get(WndRanking.StatsTab.this, "play_again"),
								Messages.get(WndRanking.StatsTab.this, "replay_warn"),
								Messages.get(WndRanking.StatsTab.this, "play_again"),
								Messages.get(WndRanking.class, "cancel")) {

							@Override
							protected void onSelect(int index) {
								if (index == 0) {
									String seedCode = DungeonSeed.convertToCode(seed);

									SPDSettings.customSeed(seedCode);
									SPDSettings.challenges(challenges);
									GamesInProgress.selectedClass = record.heroClass;
									GamesInProgress.curSlot = slot;
									Dungeon.hero = null;
									// Initialize seed from SPDSettings BEFORE switching scene
									Dungeon.initSeed();

									InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
									WndRanking.this.hide();
									SacredPixelDungeon.switchNoFade(InterlevelScene.class);
								}
							}

							@Override
							public void destroy() {
								super.destroy();
								if (WndRanking.this.parent != null) {
									WndRanking.this.active = true;
								}
							}
						});
					}
				};
				btnPlayAgain.setRect(0, buttontop, WIDTH, BTN_HEIGHT);
				add(btnPlayAgain);
				focusableButtons.add(btnPlayAgain);
			}
		}

		public void resetFocus() {
			if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
				focusableButtons.get(focusIndex).setFocused(false);
			}
			focusIndex = -1;
		}

		public void moveFocus(int direction) {
			if (focusableButtons.isEmpty()) return;

			// Clear previous
			if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
				focusableButtons.get(focusIndex).setFocused(false);
			}

			if (focusIndex == -1) {
				focusIndex = direction > 0 ? 0 : focusableButtons.size() - 1;
			} else {
				focusIndex += direction;
				if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
				if (focusIndex >= focusableButtons.size()) focusIndex = 0;
			}

			// Highlight new
			focusableButtons.get(focusIndex).setFocused(true);
		}

		public void activateFocused() {
			if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
				Button btn = focusableButtons.get(focusIndex);
				Sample.INSTANCE.play(Assets.Sounds.CLICK);
				btn.click();
			}
		}

		private float statSlot(Group parent, String label, String value, float pos) {

			int size = 7;
			RenderedTextBlock txt;
			do {
				txt = PixelScene.renderTextBlock(label, size);
				size--;
			} while (txt.width() >= WIDTH * 0.67f);
			txt.setPos(0, pos + (6 - txt.height()) / 2);
			PixelScene.align(txt);
			parent.add(txt);

			size = 7;
			do {
				txt = PixelScene.renderTextBlock(value, size);
				size--;
			} while (txt.width() >= WIDTH * 0.33f);
			txt.setPos(WIDTH * 0.67f, pos + (6 - txt.height()) / 2);
			PixelScene.align(txt);
			parent.add(txt);

			return pos + GAP + txt.height();
		}
	}

	private class TalentsTab extends Group {

		public TalentsPane talentsPane;

		public TalentsTab() {
			super();
		}

		public void init() {
			int tiers = 1;
			if (Dungeon.hero.lvl >= 6) tiers++;
			if (Dungeon.hero.lvl >= 12 && Dungeon.hero.subClass != HeroSubClass.NONE) tiers++;
			if (Dungeon.hero.lvl >= 20 && Dungeon.hero.armorAbility != null) tiers++;
			while (Dungeon.hero.talents.size() > tiers) {
				Dungeon.hero.talents.remove(Dungeon.hero.talents.size() - 1);
			}

			talentsPane = new TalentsPane(TalentButton.Mode.INFO);
			add(talentsPane);
			talentsPane.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	private class ItemsTab extends Group {

		private float pos;

		public ItemsTab() {
			super();
		}

		public void init() {
			focusableItems.clear();
			itemsFocusIndex = -1;

			Belongings stuff = Dungeon.hero.belongings;
			if (stuff.weapon() != null) {
				addItem(stuff.weapon());
			}
			if (stuff.armor() != null) {
				addItem(stuff.armor());
			}
			if (stuff.artifact() != null) {
				addItem(stuff.artifact());
			}
			if (stuff.misc() != null) {
				addItem(stuff.misc());
			}
			if (stuff.ring() != null) {
				addItem(stuff.ring());
			}

			pos = 0;

			int slotsActive = 0;
			for (int i = 0; i < QuickSlot.SIZE; i++) {
				if (Dungeon.quickslot.isNonePlaceholder(i)) {
					slotsActive++;
				}
			}

			Trinket trinket = stuff.getItem(Trinket.class);
			if (trinket != null) {
				slotsActive++;
			}

			float slotWidth = Math.min(28, ((WIDTH - slotsActive + 1) / (float) slotsActive));

			for (int i = -1; i < QuickSlot.SIZE; i++) {
				Item item = null;
				if (i == -1) {
					item = trinket;
				} else if (Dungeon.quickslot.isNonePlaceholder(i)) {
					item = Dungeon.quickslot.getItem(i);
				}
				if (item != null) {
					QuickSlotButton slot = new QuickSlotButton(item);
					slot.setRect(pos, 125, slotWidth, 24);
					PixelScene.align(slot);
					add(slot);
					focusableItems.add(slot);
					pos += slotWidth + 1;
				}
			}
		}

		private void addItem(Item item) {
			ItemButton slot = new ItemButton(item);
			slot.setRect(0, pos, width, ItemButton.HEIGHT);
			add(slot);
			focusableItems.add(slot);
			pos += slot.height() + 1;
		}
	}

	private class BadgesTab extends Group {

		public BadgesTab() {
			super();
		}

		public void init() {
			// Use BadgesList for few badges, BadgesGrid for many
			Component badges;
			int badgeCount = Badges.filterReplacedBadges(false).size();

			if (badgeCount <= 8) {
				badges = new BadgesList(false);
			} else {
				badges = new BadgesGrid(false);
			}
			add(badges);
			badges.setRect(0, 0, WIDTH, HEIGHT);
			badgesComponent = badges;
		}
	}

	private class ChallengesTab extends Group {

		private ArrayList<IconButton> infoButtons = new ArrayList<>();
		private int focusIndex = -1;

		public ChallengesTab() {
			super();
		}

		public void init() {
			int challenges = Dungeon.challenges;

			float pos = 0;

			for (int i = 0; i < Challenges.NAME_IDS.length; i++) {

				final String challenge = Challenges.NAME_IDS[i];

				CheckBox cb = new CheckBox(Messages.titleCase(Messages.get(Challenges.class, challenge)));
				cb.checked((challenges & Challenges.MASKS[i]) != 0);
				cb.active = false;

				if (i > 0) {
					pos += 1;
				}
				cb.setRect(0, pos, WIDTH - 16, BTN_HEIGHT);
				add(cb);

				IconButton info = new IconButton(Icons.get(Icons.INFO)) {
					@Override
					protected void onClick() {
						super.onClick();
						WndRanking.this.active = false;
						SacredPixelDungeon.scene().add(new WndMessage(Messages.get(Challenges.class, challenge + "_desc")) {
							@Override
							public void destroy() {
								super.destroy();
								if (WndRanking.this.parent != null) {
									WndRanking.this.active = true;
								}
							}
						});
					}
				};
				info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
				add(info);
				infoButtons.add(info);

				pos = cb.bottom();
			}
		}

		public void resetFocus() {
			if (focusIndex >= 0 && focusIndex < infoButtons.size()) {
				infoButtons.get(focusIndex).setFocused(false);
			}
			focusIndex = -1;
		}

		public void moveFocus(int direction) {
			if (infoButtons.isEmpty()) return;

			if (focusIndex >= 0 && focusIndex < infoButtons.size()) {
				infoButtons.get(focusIndex).setFocused(false);
			}

			if (focusIndex == -1) {
				focusIndex = direction > 0 ? 0 : infoButtons.size() - 1;
			} else {
				focusIndex += direction;
				if (focusIndex < 0) focusIndex = infoButtons.size() - 1;
				if (focusIndex >= infoButtons.size()) focusIndex = 0;
			}

			infoButtons.get(focusIndex).setFocused(true);
		}

		public void activateFocused() {
			if (focusIndex >= 0 && focusIndex < infoButtons.size()) {
				Sample.INSTANCE.play(Assets.Sounds.CLICK);
				infoButtons.get(focusIndex).click();
			}
		}
	}

	private class ItemButton extends Button {

		public static final int HEIGHT = 24;

		private Item item;

		private ItemSlot slot;
		private ColorBlock bg;
		private RenderedTextBlock name;

		public ItemButton(Item item) {

			super();

			this.item = item;

			slot.item(item);
			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.3f;
				bg.ga = -0.15f;
				bg.ba = -0.15f;
			} else if (!item.isIdentified()) {
				if ((item instanceof EquipableItem || item instanceof Wand) && item.cursedKnown) {
					bg.ba = +0.3f;
					bg.ra = -0.1f;
				} else {
					bg.ra = +0.35f;
					bg.ba = +0.35f;
				}
			}
		}

		@Override
		protected void createChildren() {

			bg = new ColorBlock(28, HEIGHT, 0x9953564D);
			add(bg);

			slot = new ItemSlot();
			add(slot);

			name = PixelScene.renderTextBlock(7);
			add(name);

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			slot.setRect(x, y, 28, HEIGHT);
			PixelScene.align(slot);

			name.maxWidth((int) (width - slot.width() - 2));
			name.text(Messages.titleCase(item.name()));
			name.setPos(
					slot.right() + 2,
					y + (height - name.height()) / 2
			);
			PixelScene.align(name);

			super.layout();
		}

		@Override
		protected void onPointerDown() {
			bg.brightness(1.5f);
			Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
		}

		protected void onPointerUp() {
			bg.brightness(1.0f);
		}

		@Override
		protected void onClick() {
			WndRanking.this.active = false;
			Game.scene().add(new WndInfoItem(item) {
				@Override
				public void destroy() {
					super.destroy();
					if (WndRanking.this.parent != null) {
						WndRanking.this.active = true;
					}
				}
			});
		}

		public void setFocused(boolean focused) {
			bg.brightness(focused ? 1.5f : 1.0f);
		}
	}

	private class QuickSlotButton extends ItemSlot {

		private Item item;
		private ColorBlock bg;

		QuickSlotButton(Item item) {
			super(item);
			this.item = item;

			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.2f;
				bg.ga = -0.1f;
			} else if (!item.isIdentified()) {
				bg.ra = 0.1f;
				bg.ba = 0.1f;
			}
		}

		@Override
		protected void createChildren() {
			bg = new ColorBlock(1, 1, 0x9953564D);
			add(bg);

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			bg.size(width(), height());

			super.layout();
		}

		@Override
		protected void onPointerDown() {
			bg.brightness(1.5f);
			Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
		}

		protected void onPointerUp() {
			bg.brightness(1.0f);
		}

		@Override
		protected void onClick() {
			WndRanking.this.active = false;
			Game.scene().add(new WndInfoItem(item) {
				@Override
				public void destroy() {
					super.destroy();
					if (WndRanking.this.parent != null) {
						WndRanking.this.active = true;
					}
				}
			});
		}

		public void setFocused(boolean focused) {
			bg.brightness(focused ? 1.5f : 1.0f);
		}
	}
}
