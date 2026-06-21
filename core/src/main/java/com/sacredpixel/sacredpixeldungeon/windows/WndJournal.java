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
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalSpire;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mimic;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Pylon;
import com.sacredpixel.sacredpixeldungeon.items.EnergyCrystal;
import com.sacredpixel.sacredpixeldungeon.items.Gold;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.armor.Armor;
import com.sacredpixel.sacredpixeldungeon.items.armor.ClassArmor;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.Artifact;
import com.sacredpixel.sacredpixeldungeon.items.potions.Potion;
import com.sacredpixel.sacredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.Scroll;
import com.sacredpixel.sacredpixeldungeon.items.trinkets.Trinket;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfRegrowth;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfWarding;
import com.sacredpixel.sacredpixeldungeon.items.weapon.SpiritBow;
import com.sacredpixel.sacredpixeldungeon.items.weapon.Weapon;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.MagesStaff;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.journal.Notes;
import com.sacredpixel.sacredpixeldungeon.levels.traps.Trap;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.plants.Plant;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialManager;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialState;
import com.sacredpixel.sacredpixeldungeon.sprites.CharSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.sacredpixel.sacredpixeldungeon.ui.BadgesGrid;
import com.sacredpixel.sacredpixeldungeon.ui.BadgesList;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.QuickRecipe;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.ScrollPane;
import com.sacredpixel.sacredpixeldungeon.ui.ScrollingGridPane;
import com.sacredpixel.sacredpixeldungeon.ui.ScrollingListPane;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.badlogic.gdx.Input;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;
import com.watabou.utils.Compat;

public class WndJournal extends WndTabbed {

	public static final int WIDTH_P     = 149;
	public static final int HEIGHT_P    = 180;

	public static final int WIDTH_L     = 251;
	public static final int HEIGHT_L    = 120;

	private static final int ITEM_HEIGHT	= 18;
	private static final int BTN_HEIGHT		= 16;

	private GuideTab guideTab;
	private AlchemyTab alchemyTab;
	private NotesTab notesTab;
	private CatalogTab catalogTab;
	private BadgesTab badgesTab;

	public static int last_index = 0;

	private static WndJournal INSTANCE = null;

	// Track active tab for focus management
	private int activeTabIndex = 0;
	// Track focus mode: false = button focus, true = content focus
	private boolean contentFocusMode = false;

	public WndJournal(){

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		int height = PixelScene.landscape() ? HEIGHT_L : HEIGHT_P;
		
		resize(width, height);
		
		guideTab = new GuideTab();
		add(guideTab);
		guideTab.setRect(0, 0, width, height);
		guideTab.updateList();
		
		alchemyTab = new AlchemyTab();
		add(alchemyTab);
		alchemyTab.setRect(0, 0, width, height);
		
		notesTab = new NotesTab();
		add(notesTab);
		notesTab.setRect(0, 0, width, height);
		notesTab.updateList();
		
		catalogTab = new CatalogTab();
		add(catalogTab);
		catalogTab.setRect(0, 0, width, height);
		catalogTab.updateList();

		badgesTab = new BadgesTab();
		add(badgesTab);
		badgesTab.setRect(0, 0, width, height);
		badgesTab.updateList();
		
		Tab[] tabs = {
				new IconTab( Icons.JOURNAL.get() ) {
					protected void select( boolean value ) {
						super.select( value );
						notesTab.active = notesTab.visible = value;
						// Also set internal component active/visible to prevent click events on inactive tabs
						if (notesTab.grid != null) {
							notesTab.grid.active = notesTab.grid.visible = value;
						}
						if (value) {
							last_index = 0;
							activeTabIndex = 0;
							contentFocusMode = false;
							deactivateAllContentKeyboards();
							clearFocus();
							rebuildFocusableButtons();
						}
					}

					@Override
					protected String hoverText() {
						return Messages.get(notesTab, "title");
					}
				},
				new IconTab( new ItemSprite(ItemSpriteSheet.MASTERY, null) ) {
					protected void select( boolean value ) {
						super.select( value );
						guideTab.active = guideTab.visible = value;
						// Also set internal component active/visible to prevent click events on inactive tabs
						if (guideTab.list != null) {
							guideTab.list.active = guideTab.list.visible = value;
						}
						if (value) {
							last_index = 1;
							activeTabIndex = 1;
							contentFocusMode = false;
							deactivateAllContentKeyboards();
							clearFocus();
							rebuildFocusableButtons();
						}
					}

					@Override
					protected String hoverText() {
						return Messages.get(guideTab, "title");
					}
				},
				new IconTab( Icons.ALCHEMY.get() ) {
					protected void select( boolean value ) {
						super.select( value );
						alchemyTab.active = alchemyTab.visible = value;
						// Also set internal component active/visible to prevent click events on inactive tabs
						if (alchemyTab.list != null) {
							alchemyTab.list.active = alchemyTab.list.visible = value;
						}
						// Set page buttons active/visible
						if (alchemyTab.pageButtons != null) {
							for (int i = 0; i < alchemyTab.pageButtons.length; i++) {
								RedButton btn = alchemyTab.pageButtons[i];
								btn.active = value && Document.ALCHEMY_GUIDE.isPageFound(i);
								btn.visible = value;
							}
						}
						if (value) {
							last_index = 2;
							activeTabIndex = 2;
							contentFocusMode = false;
							deactivateAllContentKeyboards();
							clearFocus();
							rebuildFocusableButtons();
						}
					}

					@Override
					protected String hoverText() {
						return Messages.get(alchemyTab, "title");
					}
				},
				new IconTab( Icons.CATALOG.get() ) {
					protected void select( boolean value ) {
						super.select( value );
						catalogTab.active = catalogTab.visible = value;
						// Also set internal component active/visible to prevent click events on inactive tabs
						if (catalogTab.grid != null) {
							catalogTab.grid.active = catalogTab.grid.visible = value;
						}
						// Set item buttons active/visible
						if (catalogTab.itemButtons != null) {
							for (RedButton btn : catalogTab.itemButtons) {
								btn.active = value;
								btn.visible = value;
							}
						}
						if (value) {
							last_index = 3;
							activeTabIndex = 3;
							contentFocusMode = false;
							deactivateAllContentKeyboards();
							clearFocus();
							rebuildFocusableButtons();
						}
					}

					@Override
					protected String hoverText() {
						return Messages.get(catalogTab, "title");
					}
				},
				new IconTab( Icons.BADGES.get() ) {
					protected void select( boolean value ) {
						super.select( value );
						badgesTab.active = badgesTab.visible = value;
						// Also set internal component active/visible to prevent click events on inactive tabs
						if (badgesTab.badgesLocal != null) {
							badgesTab.badgesLocal.active = badgesTab.badgesLocal.visible = value && !BadgesTab.global;
						}
						if (badgesTab.badgesGlobal != null) {
							badgesTab.badgesGlobal.active = badgesTab.badgesGlobal.visible = value && BadgesTab.global;
						}
						if (value) {
							last_index = 4;
							activeTabIndex = 4;
							contentFocusMode = false;
							deactivateAllContentKeyboards();
							clearFocus();
							rebuildFocusableButtons();
						}
					}

					@Override
					protected String hoverText() {
						return Messages.get(badgesTab, "title");
					}
				}
		};

		for (Tab tab : tabs) {
			add( tab );
		}
		
		layoutTabs();

		select(last_index);

		INSTANCE = this;

		// Build initial focus list based on active tab
		rebuildFocusableButtons();
	}

	@Override
	public void hide() {
		super.hide();
		// Trigger tutorial progression when journal is closed
		if (TutorialManager.isTutorialLevel() && TutorialManager.getState() == TutorialState.JOURNAL_HINT) {
			TutorialManager.onAction(TutorialManager.TutorialAction.JOURNAL_CLOSED);
		}
	}

	private void rebuildFocusableButtons() {
		focusableButtons.clear();
		focusIndex = -1;

		switch (activeTabIndex) {
			case 0: // Notes tab - uses ScrollingGridPane with built-in keyboard
				// ScrollingGridPane handles its own keyboard navigation
				break;
			case 1: // Guide tab - uses ScrollingListPane with built-in keyboard
				// ScrollingListPane handles its own keyboard navigation
				break;
			case 2: // Alchemy tab - page buttons only (content accessed via Enter)
				if (alchemyTab.pageButtons != null) {
					for (RedButton btn : alchemyTab.pageButtons) {
						if (btn.active) {
							addFocusableButton(btn);
						}
					}
				}
				break;
			case 3: // Catalog tab - category buttons only (content accessed via Enter)
				if (catalogTab.itemButtons != null) {
					for (RedButton btn : catalogTab.itemButtons) {
						addFocusableButton(btn);
					}
				}
				break;
			case 4: // Badges tab - local/global toggle buttons only (badges accessed via Enter)
				if (badgesTab.btnLocal != null) {
					addFocusableButton(badgesTab.btnLocal);
				}
				if (badgesTab.btnGlobal != null) {
					addFocusableButton(badgesTab.btnGlobal);
				}
				break;
		}
	}

	private void deactivateAllContentKeyboards() {
		// Deactivate badges keyboard
		if (badgesTab != null) {
			if (badgesTab.badgesLocal instanceof BadgesGrid) {
				((BadgesGrid) badgesTab.badgesLocal).setKeyboardActive(false);
			} else if (badgesTab.badgesLocal instanceof BadgesList) {
				((BadgesList) badgesTab.badgesLocal).setKeyboardActive(false);
			}
			if (badgesTab.badgesGlobal instanceof BadgesGrid) {
				((BadgesGrid) badgesTab.badgesGlobal).setKeyboardActive(false);
			} else if (badgesTab.badgesGlobal instanceof BadgesList) {
				((BadgesList) badgesTab.badgesGlobal).setKeyboardActive(false);
			}
		}

		// Disable catalog grid keyboard navigation
		if (catalogTab != null && catalogTab.grid != null) {
			catalogTab.grid.setKeyboardEnabled(false);
		}
	}

	@Override
	public boolean onSignal(KeyEvent event) {
		// Don't process keyboard events if window is not active (another window is on top)
		if (!active) return super.onSignal(event);

		if (event.pressed && KeyBindings.getActionForKey( event ) == SPDAction.JOURNAL) {
			onBackPressed();
			return true;
		}

		// Don't handle keyboard navigation if not topmost (child window is open)
		// Block all input except ESC/BACK to prevent focus navigation while child window is shown
		if (!isTopmost()) {
			if (event.pressed) {
				com.watabou.input.GameAction action = KeyBindings.getActionForKey(event);
				// Allow BACK to close child window, block everything else
				if (action == SPDAction.BACK) {
					return false; // Let child window handle it
				}
				// Block all other keys - prevents focus navigation and game input
				return true;
			}
			return true; // Block key release too
		}

		// Handle ESC key to switch back to button focus mode
		if (event.pressed && KeyBindings.getActionForKey(event) == SPDAction.BACK) {
			if (contentFocusMode) {
				exitContentFocusMode();
				return true;
			}
		}

		// Handle Enter key on focused button - activate AND enter content focus mode
		if (event.pressed && (event.code == Input.Keys.ENTER || event.code == Input.Keys.NUMPAD_ENTER)) {
			// When in content focus mode, activate the focused content item
			if (contentFocusMode) {
				// Handle Notes tab - activate focused grid item
				if (activeTabIndex == 0 && notesTab != null && notesTab.grid != null) {
					notesTab.grid.activateFocusedItem();
					return true;
				}
				if (activeTabIndex == 2 && alchemyTab != null && alchemyTab.isRecipeNavigationActive()) {
					alchemyTab.activateFocusedItem();
					return true;
				}
				// Handle Badges tab - activate focused badge
				if (activeTabIndex == 4 && badgesTab != null) {
					Component badges = BadgesTab.global ? badgesTab.badgesGlobal : badgesTab.badgesLocal;
					if (badges instanceof BadgesGrid) {
						((BadgesGrid) badges).activateFocused();
						return true;
					} else if (badges instanceof BadgesList) {
						((BadgesList) badges).activateFocused();
						return true;
					}
				}
				// For other tabs, Enter can be handled differently
				return true;
			}

			if (focusIndex >= 0) {
				// First activate the focused button
				activateFocused();
				// Enter content focus mode for tabs with navigable/scrollable content:
				// Notes (0) - section-based navigation
				// Alchemy (2) - scrollable recipes
				// Catalog (3) - navigable grid
				// Badges (4) - navigable badge icons
				if (activeTabIndex == 0 || activeTabIndex == 2 || activeTabIndex == 3 || activeTabIndex == 4) {
					enterContentFocusMode();
				}
				return true;
			}
			// Block Enter from closing WndJournal when on tabs with navigable content
			// (notes, alchemy, catalog, badges)
			if (activeTabIndex == 0 || activeTabIndex == 2 || activeTabIndex == 3 || activeTabIndex == 4) {
				return true;
			}
		}

		// If no focusable buttons on current tab, enter content mode directly on arrow keys
		if (event.pressed && !contentFocusMode && focusableButtons.isEmpty()) {
			com.watabou.input.GameAction action = KeyBindings.getActionForKey(event);
			if (action == SPDAction.N || action == SPDAction.S ||
				action == SPDAction.E || action == SPDAction.W ||
				action == SPDAction.NE || action == SPDAction.NW ||
				action == SPDAction.SE || action == SPDAction.SW) {
				enterContentFocusMode();
				return true;
			}
		}

		// IMPORTANT: When in content focus mode, handle arrow keys for content navigation
		// This prevents arrow keys from reaching Window's button focus navigation
		if (contentFocusMode && event.pressed) {
			com.watabou.input.GameAction action = KeyBindings.getActionForKey(event);

			// Handle Notes tab section navigation
			// Up/Down moves between sections (floors), Left/Right moves within current section
			if (activeTabIndex == 0 && notesTab != null && notesTab.grid != null) {
				if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE) {
					// Up - move to previous section
					notesTab.grid.moveFocusBySection(-1);
					return true;
				} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE) {
					// Down - move to next section
					notesTab.grid.moveFocusBySection(1);
					return true;
				} else if (action == SPDAction.W) {
					// Left - move within section
					notesTab.grid.moveFocusWithinSection(-1);
					return true;
				} else if (action == SPDAction.E) {
					// Right - move within section
					notesTab.grid.moveFocusWithinSection(1);
					return true;
				}
			}

			// Handle Alchemy tab recipe navigation
			if (activeTabIndex == 2 && alchemyTab != null && alchemyTab.isRecipeNavigationActive()) {
				if (action == SPDAction.W || action == SPDAction.NW || action == SPDAction.SW) {
					// Left
					alchemyTab.moveFocusHorizontal(-1);
					return true;
				} else if (action == SPDAction.E || action == SPDAction.NE || action == SPDAction.SE) {
					// Right
					alchemyTab.moveFocusHorizontal(1);
					return true;
				} else if (action == SPDAction.N) {
					// Up
					alchemyTab.moveFocusVertical(-1);
					return true;
				} else if (action == SPDAction.S) {
					// Down
					alchemyTab.moveFocusVertical(1);
					return true;
				}
			}

			// Handle Badges tab navigation
			if (activeTabIndex == 4 && badgesTab != null) {
				Component badges = BadgesTab.global ? badgesTab.badgesGlobal : badgesTab.badgesLocal;
				if (badges instanceof BadgesGrid) {
					BadgesGrid grid = (BadgesGrid) badges;
					if (action == SPDAction.W || action == SPDAction.SW) {
						grid.moveFocus(-1, 0);
						return true;
					} else if (action == SPDAction.E || action == SPDAction.NE) {
						grid.moveFocus(1, 0);
						return true;
					} else if (action == SPDAction.N || action == SPDAction.NW) {
						grid.moveFocus(0, -1);
						return true;
					} else if (action == SPDAction.S || action == SPDAction.SE) {
						grid.moveFocus(0, 1);
						return true;
					}
				} else if (badges instanceof BadgesList) {
					BadgesList list = (BadgesList) badges;
					if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.W || action == SPDAction.SW) {
						list.moveFocus(-1);
						return true;
					} else if (action == SPDAction.S || action == SPDAction.SE || action == SPDAction.E || action == SPDAction.NE) {
						list.moveFocus(1);
						return true;
					}
				}
			}

			// Block other arrow keys for other content types
			if (action == SPDAction.N || action == SPDAction.S ||
				action == SPDAction.E || action == SPDAction.W ||
				action == SPDAction.NE || action == SPDAction.NW ||
				action == SPDAction.SE || action == SPDAction.SW) {
				return true; // Block arrow keys - let content components handle them
			}
		}

		// For Alchemy (2) and Catalog (3) tabs, only use left/right for page button navigation
		// Block up/down keys from triggering page button focus changes
		if (!contentFocusMode && event.pressed && (activeTabIndex == 2 || activeTabIndex == 3)) {
			com.watabou.input.GameAction action = KeyBindings.getActionForKey(event);
			if (action == SPDAction.N || action == SPDAction.S) {
				// Block up/down keys for page navigation - only left/right should work
				return true;
			}
		}

		// Let parent process other keys (arrow keys for focus navigation)
		boolean handled = super.onSignal(event);

		// Block game movement keys even if not handled by window navigation
		// This prevents hero movement/actions while journal is open
		if (event.pressed && !handled) {
			com.watabou.input.GameAction action = KeyBindings.getActionForKey(event);
			if (action == SPDAction.N || action == SPDAction.S ||
				action == SPDAction.E || action == SPDAction.W ||
				action == SPDAction.NE || action == SPDAction.NW ||
				action == SPDAction.SE || action == SPDAction.SW ||
				action == SPDAction.WAIT || action == SPDAction.REST ||
				action == SPDAction.WAIT_OR_PICKUP || action == SPDAction.EXAMINE) {
				return true; // Block these keys from reaching the game
			}
		}

		return handled;
	}

	private void enterContentFocusMode() {
		contentFocusMode = true;
		clearFocus();
		// Clear focusable buttons to prevent arrow key navigation to buttons while in content mode
		focusableButtons.clear();

		switch (activeTabIndex) {
			case 0: // Notes - enable section navigation mode
				// WndJournal handles Notes tab navigation explicitly (lines 457-475)
				// Disable grid's own keyListener to prevent double navigation
				if (notesTab.grid != null) {
					notesTab.grid.setKeyboardEnabled(false);
					notesTab.grid.setSectionNavigationMode(true);
				}
				break;
			case 2: // Alchemy - enable recipe keyboard navigation
				if (alchemyTab != null) {
					alchemyTab.activateRecipeNavigation();
				}
				break;
			case 3: // Catalog - enable grid keyboard navigation
				if (catalogTab.grid != null) {
					catalogTab.grid.setKeyboardEnabled(true);
				}
				break;
			case 4: // Badges - activate badge keyboard navigation
				if (badgesTab.btnLocal != null) {
					if (BadgesTab.global) {
						if (badgesTab.badgesGlobal instanceof BadgesGrid) {
							((BadgesGrid) badgesTab.badgesGlobal).setKeyboardActive(true);
						} else if (badgesTab.badgesGlobal instanceof BadgesList) {
							((BadgesList) badgesTab.badgesGlobal).setKeyboardActive(true);
						}
					} else {
						if (badgesTab.badgesLocal instanceof BadgesGrid) {
							((BadgesGrid) badgesTab.badgesLocal).setKeyboardActive(true);
						} else if (badgesTab.badgesLocal instanceof BadgesList) {
							((BadgesList) badgesTab.badgesLocal).setKeyboardActive(true);
						}
					}
				} else {
					// No local/global buttons, only global badges
					if (badgesTab.badgesGlobal instanceof BadgesGrid) {
						((BadgesGrid) badgesTab.badgesGlobal).setKeyboardActive(true);
					}
				}
				break;
		}
	}

	private void exitContentFocusMode() {
		contentFocusMode = false;

		// Deactivate content keyboard navigation
		switch (activeTabIndex) {
			case 0: // Notes - disable section navigation mode
				if (notesTab.grid != null) {
					notesTab.grid.setKeyboardEnabled(false);
					notesTab.grid.setSectionNavigationMode(false);
					notesTab.grid.clearFocus();
				}
				break;
			case 2: // Alchemy - deactivate recipe navigation
				if (alchemyTab != null) {
					alchemyTab.deactivateRecipeNavigation();
				}
				break;
			case 3: // Catalog
				if (catalogTab.grid != null) {
					catalogTab.grid.setKeyboardEnabled(false);
				}
				break;
			case 4: // Badges
				if (badgesTab.badgesLocal instanceof BadgesGrid) {
					((BadgesGrid) badgesTab.badgesLocal).setKeyboardActive(false);
				} else if (badgesTab.badgesLocal instanceof BadgesList) {
					((BadgesList) badgesTab.badgesLocal).setKeyboardActive(false);
				}
				if (badgesTab.badgesGlobal instanceof BadgesGrid) {
					((BadgesGrid) badgesTab.badgesGlobal).setKeyboardActive(false);
				} else if (badgesTab.badgesGlobal instanceof BadgesList) {
					((BadgesList) badgesTab.badgesGlobal).setKeyboardActive(false);
				}
				break;
		}

		// Rebuild button focus
		rebuildFocusableButtons();
	}

	@Override
	public void offset(int xOffset, int yOffset) {
		super.offset(xOffset, yOffset);
		guideTab.layout();
		alchemyTab.layout();
		notesTab.layout();
		catalogTab.layout();
	}
	
	public static class GuideTab extends Component {

		public ScrollingListPane list;
		
		@Override
		protected void createChildren() {
			list = new ScrollingListPane();
			add( list );
		}
		
		@Override
		protected void layout() {
			super.layout();
			list.setRect( x, y, width, height);
		}
		
		public void updateList(){
			list.addTitle(Document.ADVENTURERS_GUIDE.title());

			for (String page : Document.ADVENTURERS_GUIDE.pageNames()){
				boolean found = Document.ADVENTURERS_GUIDE.isPageFound(page);
				ScrollingListPane.ListItem item = new ScrollingListPane.ListItem(
						Document.ADVENTURERS_GUIDE.pageSprite(page),
						null,
						found ? Messages.titleCase(Document.ADVENTURERS_GUIDE.pageTitle(page)) : Messages.titleCase(Messages.get( this, "missing" ))
				){
					@Override
					public boolean onClick(float x, float y) {
						if (inside( x, y ) && found) {
							SacredPixelDungeon.scene().addToFront( new WndStory( Document.ADVENTURERS_GUIDE.pageSprite(page),
									Document.ADVENTURERS_GUIDE.pageTitle(page),
									Document.ADVENTURERS_GUIDE.pageBody(page) ));
							Document.ADVENTURERS_GUIDE.readPage(page);
							return true;
						} else {
							return false;
						}
					}
				};
				if (!found){
					item.hardlight(0x999999);
					item.hardlightIcon(0x999999);
				}
				list.addItem(item);
			}

			list.setRect(x, y, width, height);
		}

	}
	
	public static class AlchemyTab extends Component {

		public RedButton[] pageButtons;
		private static final int NUM_BUTTONS = 9;

		// Keyboard navigation for recipes
		private int focusedRecipeIndex = -1;
		private int focusedItemInRecipe = -1;
		private boolean recipeNavigationActive = false;

		private static final int[] sprites = {
				ItemSpriteSheet.SEED_HOLDER,
				ItemSpriteSheet.STONE_HOLDER,
				ItemSpriteSheet.FOOD_HOLDER,
				ItemSpriteSheet.POTION_HOLDER,
				ItemSpriteSheet.SCROLL_HOLDER,
				ItemSpriteSheet.BOMB_HOLDER,
				ItemSpriteSheet.MISSILE_HOLDER,
				ItemSpriteSheet.ELIXIR_HOLDER,
				ItemSpriteSheet.SPELL_HOLDER
		};
		
		public static int currentPageIdx   = 0;
		
		private IconTitle title;
		private RenderedTextBlock body;

		public ScrollPane list;
		private ArrayList<QuickRecipe> recipes = new ArrayList<>();
		
		@Override
		protected void createChildren() {
			pageButtons = new RedButton[NUM_BUTTONS];
			for (int i = 0; i < NUM_BUTTONS; i++){
				final int idx = i;
				pageButtons[i] = new RedButton( "" ){
					@Override
					protected void onClick() {
						currentPageIdx = idx;
						updateList();
					}
				};
				if (Document.ALCHEMY_GUIDE.isPageFound(i)) {
					ItemSprite icon = new ItemSprite(sprites[i], null);
					icon.scale.set(0.75f);
					pageButtons[i].icon(icon);
				} else {
					ItemSprite icon = new ItemSprite(ItemSpriteSheet.SOMETHING, null);
					icon.scale.set(0.75f);
					pageButtons[i].icon(icon);
					pageButtons[i].enable(false);
				}
				add( pageButtons[i] );
			}
			
			title = new IconTitle();
			title.icon( new ItemSprite(ItemSpriteSheet.ALCH_PAGE));
			title.visible = false;

			body = PixelScene.renderTextBlock(6);
			
			list = new ScrollPane(new Component());
			add(list);
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			float buttonWidth = width()/pageButtons.length;
			for (int i = 0; i < NUM_BUTTONS; i++) {
				pageButtons[i].setRect(x + i*buttonWidth, y, buttonWidth, ITEM_HEIGHT);
				PixelScene.align(pageButtons[i]);
			}
			
			list.setRect(x, pageButtons[NUM_BUTTONS-1].bottom() + 1, width,
					height - pageButtons[NUM_BUTTONS-1].bottom() + y - 1);
			
			updateList();
		}
		
		public void updateList() {

			if (currentPageIdx != -1 && !Document.ALCHEMY_GUIDE.isPageFound(currentPageIdx)){
				currentPageIdx = -1;
			}

			for (int i = 0; i < NUM_BUTTONS; i++) {
				if (i == currentPageIdx) {
					pageButtons[i].icon().color(TITLE_COLOR);
				} else {
					pageButtons[i].icon().resetColor();
				}
			}
			
			if (currentPageIdx == -1){
				return;
			}
			
			for (QuickRecipe r : recipes){
				if (r != null) {
					r.killAndErase();
					r.destroy();
				}
			}
			recipes.clear();
			
			Component content = list.content();
			
			content.clear();
			
			title.visible = true;
			title.label(Document.ALCHEMY_GUIDE.pageTitle(currentPageIdx));
			title.setRect(0, 0, width(), 10);
			content.add(title);
			
			body.maxWidth((int)width());
			body.text(Document.ALCHEMY_GUIDE.pageBody(currentPageIdx));
			body.setPos(0, title.bottom());
			content.add(body);

			Document.ALCHEMY_GUIDE.readPage(currentPageIdx);
			
			ArrayList<QuickRecipe> toAdd = QuickRecipe.getRecipes(currentPageIdx);
			
			float left;
			float top = body.bottom()+2;
			int w;
			ArrayList<QuickRecipe> toAddThisRow = new ArrayList<>();
			while (!toAdd.isEmpty()){
				if (toAdd.get(0) == null){
					toAdd.remove(0);
					top += 6;
				}
				
				w = 0;
				while(!toAdd.isEmpty() && toAdd.get(0) != null
						&& w + toAdd.get(0).width() <= (PixelScene.landscape() ? WIDTH_L : WIDTH_P)){
					toAddThisRow.add(toAdd.remove(0));
					w += toAddThisRow.get(0).width();
				}
				
				float spacing = (width() - w)/(toAddThisRow.size() + 1);
				left = spacing;
				while (!toAddThisRow.isEmpty()){
					QuickRecipe r = toAddThisRow.remove(0);
					r.setPos(left, top);
					left += r.width() + spacing;
					if (!toAddThisRow.isEmpty()) {
						ColorBlock spacer = new ColorBlock(1, 16, 0xFF222222);
						spacer.y = top;
						spacer.x = left - spacing / 2 - 0.5f;
						PixelScene.align(spacer);
						content.add(spacer);
					}
					recipes.add(r);
					content.add(r);
				}
				
				if (!toAdd.isEmpty() && toAdd.get(0) == null){
					toAdd.remove(0);
				}
				
				if (!toAdd.isEmpty() && toAdd.get(0) != null) {
					ColorBlock spacer = new ColorBlock(width(), 1, 0xFF222222);
					spacer.y = top + 16;
					spacer.x = 0;
					content.add(spacer);
				}
				top += 17;
				toAddThisRow.clear();
			}
			top -= 1;
			content.setSize(width(), top);
			list.setSize(list.width(), list.height());
			list.scrollTo(0, 0);
		}

		// Activate recipe keyboard navigation
		public void activateRecipeNavigation() {
			recipeNavigationActive = true;
			// Start with no item focused - user must press arrow key to start navigating
			focusedRecipeIndex = -1;
			focusedItemInRecipe = -1;
		}

		// Deactivate recipe keyboard navigation
		public void deactivateRecipeNavigation() {
			recipeNavigationActive = false;
			if (focusedRecipeIndex >= 0 && focusedRecipeIndex < recipes.size()) {
				recipes.get(focusedRecipeIndex).clearItemFocus();
			}
			focusedRecipeIndex = -1;
			focusedItemInRecipe = -1;
		}

		public boolean isRecipeNavigationActive() {
			return recipeNavigationActive;
		}

		// Move focus left/right within current recipe
		public void moveFocusHorizontal(int direction) {
			if (!recipeNavigationActive || recipes.isEmpty()) return;

			// If no recipe is focused yet, start from the first recipe
			if (focusedRecipeIndex < 0) {
				focusedRecipeIndex = 0;
				focusedItemInRecipe = direction > 0 ? 0 : recipes.get(0).getItemCount() - 1;
				recipes.get(0).setFocusedItem(focusedItemInRecipe);
				scrollToRecipe(0);
				return;
			}

			if (focusedRecipeIndex >= recipes.size()) return;

			QuickRecipe recipe = recipes.get(focusedRecipeIndex);
			int newIndex = focusedItemInRecipe + direction;

			// Wrap around within recipe
			if (newIndex < 0) {
				newIndex = recipe.getItemCount() - 1;
			} else if (newIndex >= recipe.getItemCount()) {
				newIndex = 0;
			}

			focusedItemInRecipe = newIndex;
			recipe.setFocusedItem(newIndex);
		}

		// Move focus up/down between recipes
		public void moveFocusVertical(int direction) {
			if (!recipeNavigationActive || recipes.isEmpty()) return;

			// Clear current focus
			if (focusedRecipeIndex >= 0 && focusedRecipeIndex < recipes.size()) {
				recipes.get(focusedRecipeIndex).clearItemFocus();
			}

			// If no recipe is focused yet, start from first or last recipe
			int newIndex;
			if (focusedRecipeIndex < 0) {
				newIndex = direction > 0 ? 0 : recipes.size() - 1;
			} else {
				// Move to next/previous recipe
				newIndex = focusedRecipeIndex + direction;

				// Wrap around
				if (newIndex < 0) {
					newIndex = recipes.size() - 1;
				} else if (newIndex >= recipes.size()) {
					newIndex = 0;
				}
			}

			focusedRecipeIndex = newIndex;

			// Always focus on the first item when moving between recipes
			focusedItemInRecipe = 0;

			QuickRecipe recipe = recipes.get(focusedRecipeIndex);
			recipe.setFocusedItem(focusedItemInRecipe);

			// Scroll to show the focused recipe
			scrollToRecipe(focusedRecipeIndex);
		}

		// Scroll list to show the recipe at given index
		private void scrollToRecipe(int recipeIndex) {
			if (recipeIndex < 0 || recipeIndex >= recipes.size()) return;

			QuickRecipe recipe = recipes.get(recipeIndex);
			float recipeTop = recipe.top() - list.content().top();
			float recipeBottom = recipe.bottom() - list.content().top();

			// Get current scroll position
			float scrollY = list.content().camera.scroll.y;
			float visibleHeight = list.height();

			// Scroll up if recipe is above visible area
			if (recipeTop < scrollY) {
				list.scrollTo(0, recipeTop - 2);
			}
			// Scroll down if recipe is below visible area
			else if (recipeBottom > scrollY + visibleHeight) {
				list.scrollTo(0, recipeBottom - visibleHeight + 2);
			}
		}

		// Activate the currently focused item
		public void activateFocusedItem() {
			if (!recipeNavigationActive) return;
			if (focusedRecipeIndex < 0 || focusedRecipeIndex >= recipes.size()) return;

			recipes.get(focusedRecipeIndex).activateFocusedItem();
		}
	}

	private static class NotesTab extends Component {

		public ScrollingGridPane grid;
		
		@Override
		protected void createChildren() {
			grid = new ScrollingGridPane();
			// Disable grid's own keyboard - WndJournal handles Notes tab navigation
			// with section-based movement (up/down between floors, left/right within floor)
			grid.setKeyboardEnabled(false);
			add(grid);
		}
		
		@Override
		protected void layout() {
			super.layout();
			grid.setRect( x, y, width, height);
		}
		
		private void updateList(){

			grid.addHeader("_" + Messages.get(this, "title") + "_", 8, true);

			grid.addHeader(Messages.get(this, "desc"), 6, true);

			for (int i = Statistics.deepestFloor; i > 0; i--){

				ArrayList<Notes.Record> recs = Notes.getRecords(i);

				if (i == Dungeon.depth) {
					grid.addHeader("_" + Messages.get(this, "floor_header", i) + "_");
				} else {
					grid.addHeader(Messages.get(this, "floor_header", i));
				}
				for( Notes.Record rec : recs){

					ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(rec.icon()){
						@Override
						public boolean onClick(float x, float y) {
							if (inside(x, y)) {
								GameScene.show(new WndJournalItem(rec.icon(),
										Messages.titleCase(rec.title()),
										rec.desc()));
								return true;
							} else {
								return false;
							}
						}
					};

					Visual secondIcon = rec.secondIcon();
					if (secondIcon != null){
						gridItem.addSecondIcon( secondIcon );
					}

					grid.addItem(gridItem);
				}
			}

			grid.setRect(x, y, width, height);

		}
		
	}
	
	public static class CatalogTab extends Component{

		public RedButton[] itemButtons;
		private static final int NUM_BUTTONS = 4;

		public static int currentItemIdx   = 0;
		private static float[] scrollPositions = new float[NUM_BUTTONS];

		//sprite locations
		private static final int EQUIP_IDX = 0;
		private static final int CONSUM_IDX = 1;
		private static final int BESTIARY_IDX = 2;
		private static final int LORE_IDX = 3;

		public ScrollingGridPane grid;
		
		@Override
		protected void createChildren() {
			itemButtons = new RedButton[NUM_BUTTONS];
			for (int i = 0; i < NUM_BUTTONS; i++){
				final int idx = i;
				itemButtons[i] = new RedButton( "" ){
					@Override
					protected void onClick() {
						currentItemIdx = idx;
						updateList();
					}
				};
				add( itemButtons[i] );
			}
			ItemSprite s;
			s = new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER); s.scale.set(0.75f);
			itemButtons[EQUIP_IDX].icon(s);
			s = new ItemSprite(ItemSpriteSheet.POTION_HOLDER); s.scale.set(0.75f);
			itemButtons[CONSUM_IDX].icon(s);
			s = new ItemSprite(ItemSpriteSheet.MOB_HOLDER); s.scale.set(0.75f);
			itemButtons[BESTIARY_IDX].icon(s);
			s = new ItemSprite(ItemSpriteSheet.DOCUMENT_HOLDER); s.scale.set(0.75f);
			itemButtons[LORE_IDX].icon(s);

			grid = new ScrollingGridPane(){
				@Override
				public synchronized void update() {
					super.update();
					scrollPositions[currentItemIdx] = content.camera.scroll.y;
				}
			};
			add( grid );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			int perRow = NUM_BUTTONS;
			float buttonWidth = width()/perRow;
			
			for (int i = 0; i < NUM_BUTTONS; i++) {
				itemButtons[i].setRect(x +(i%perRow) * (buttonWidth),
						y + (i/perRow) * (ITEM_HEIGHT ),
						buttonWidth, ITEM_HEIGHT);
				PixelScene.align(itemButtons[i]);
			}
			
			grid.setRect(x,
					itemButtons[NUM_BUTTONS-1].bottom() + 1,
					width,
					height - itemButtons[NUM_BUTTONS-1].height() - 1);
		}
		
		public void updateList() {
			
			grid.clear();
			grid.setAllowHorizontalGrouping(true);
			
			for (int i = 0; i < NUM_BUTTONS; i++){
				if (i == currentItemIdx){
					itemButtons[i].icon().color(TITLE_COLOR);
				} else {
					itemButtons[i].icon().resetColor();
				}
			}
			
			grid.scrollTo( 0, 0 );

			if (currentItemIdx == EQUIP_IDX) {
				int totalItems = 0;
				int totalSeen = 0;
				for (Catalog catalog : Catalog.equipmentCatalogs){
					totalItems += catalog.totalItems();
					totalSeen += catalog.totalSeen();
				}
				grid.addHeader("_" + Messages.get(this, "title_equipment") + "_ (" + totalSeen + "/" + totalItems + ")", 8, true);

				for (Catalog catalog : Catalog.equipmentCatalogs){
					grid.addHeader("_" + Messages.titleCase(catalog.title()) + "_ (" + catalog.totalSeen() + "/" + catalog.totalItems() + "):");
					addGridItems(grid, catalog.items());
				}

			} else if (currentItemIdx == CONSUM_IDX){
				int totalItems = 0;
				int totalSeen = 0;
				for (Catalog catalog : Catalog.consumableCatalogs){
					totalItems += catalog.totalItems();
					totalSeen += catalog.totalSeen();
				}
				grid.addHeader("_" + Messages.get(this, "title_consumables") + "_ (" + totalSeen + "/" + totalItems + ")", 8, true);

				for (Catalog catalog : Catalog.consumableCatalogs){
					grid.addHeader("_" + Messages.titleCase(catalog.title()) + "_ (" + catalog.totalSeen() + "/" + catalog.totalItems() + "):");
					addGridItems(grid, catalog.items());
				}

			} else if (currentItemIdx == BESTIARY_IDX){
				int totalItems = 0;
				int totalSeen = 0;
				for (Bestiary bestiary : Bestiary.values()){
					totalItems += bestiary.totalEntities();
					totalSeen += bestiary.totalSeen();
				}
				grid.addHeader("_" + Messages.get(this, "title_bestiary") + "_ (" + totalSeen + "/" + totalItems + ")", 8, true);

				for (Bestiary bestiary : Bestiary.values()){
					grid.addHeader("_" + Messages.titleCase(bestiary.title()) + "_ (" + bestiary.totalSeen() + "/" + bestiary.totalEntities() + "):");
					addGridEntities(grid, bestiary.entities());
				}

			} else {
				int totalItems = 0;
				int totalSeen = 0;
				for (Document doc : Document.values()){
					if (!doc.isLoreDoc()){
						continue;
					}
					for (String page : doc.pageNames()){
						totalItems++;
						if (doc.isPageFound(page)){
							totalSeen++;
						}
					}
				}
				grid.setAllowHorizontalGrouping(false);
				grid.addHeader("_" + Messages.get(this, "title_lore") + "_ (" + totalSeen + "/" + totalItems + ")", 8, true);

				for (Document doc : Document.values()){
					if (!doc.isLoreDoc()){
						continue;
					}

					for (String page : doc.pageNames()){
						totalItems++;
						if (doc.isPageFound(page)){
							totalSeen++;
						}
					}
				}
				for (Document doc : Document.values()){
					if (!doc.isLoreDoc()){
						continue;
					}
					totalItems = totalSeen = 0;
					for (String page : doc.pageNames()){
						totalItems++;
						if (doc.isPageFound(page)){
							totalSeen++;
						}
					}
					if (!doc.anyPagesFound()){
						grid.addHeader("_???_ (" + totalSeen + "/" + totalItems + "):");
					} else {
						grid.addHeader("_" + Messages.titleCase(doc.title()) + "_ (" + totalSeen + "/" + totalItems + "):");
					}
					addGridDocuments(grid, doc);
				}
			}

			grid.setRect(x, itemButtons[NUM_BUTTONS-1].bottom() + 1, width,
					height - itemButtons[NUM_BUTTONS-1].height() - 1);

			grid.scrollTo(0, scrollPositions[currentItemIdx]);
		}
		
	}

	//also includes item-like things such as enchantments, glyphs, curses.
	private static void addGridItems( ScrollingGridPane grid, Collection<Class<?>> classes) {
		for (Class<?> itemClass : classes) {

			boolean seen = Catalog.isSeen(itemClass);;
			ItemSprite sprite = null;
			Image secondIcon = null;
			String title = "";
			String desc = "";

			if (Compat.isAssignableFrom(Item.class, itemClass)) {

				Item item = (Item) Reflection.newInstance(itemClass);

				if (seen) {
					if (item instanceof Ring) {
						((Ring) item).anonymize();
					} else if (item instanceof Potion) {
						((Potion) item).anonymize();
					} else if (item instanceof Scroll) {
						((Scroll) item).anonymize();
					}
				}

				sprite = new ItemSprite(item.image, seen ? item.glowing() : null);
				if (!seen)  {
					if (item instanceof ExoticPotion){
						sprite.frame(ItemSpriteSheet.POTION_CRIMSON);
					}
					sprite.lightness(0);
					title = "???";
					desc = Messages.get(CatalogTab.class, "not_seen_item");
					desc += "\n\n" + Messages.get(item, "discover_hint");
				} else {
					title = Messages.titleCase( item.name() );
					//some items don't include direct stats, generally when they're not applicable
					if (item instanceof ClassArmor || item instanceof SpiritBow){
						desc += item.desc();
					} else {
						desc += item.info();
					}

					if (Catalog.useCount(itemClass) > 1) {
						if (item.isUpgradable() || item instanceof Artifact) {
							desc += "\n\n" + Messages.get(CatalogTab.class, "upgrade_count", Catalog.useCount(itemClass));
						} else if (item instanceof Trinket) {
							desc += "\n\n" + Messages.get(CatalogTab.class, "trinket_count", Catalog.useCount(itemClass));
						} else if (item instanceof Gold) {
							desc += "\n\n" + Messages.get(CatalogTab.class, "gold_count", Catalog.useCount(itemClass));
						} else if (item instanceof EnergyCrystal) {
							desc += "\n\n" + Messages.get(CatalogTab.class, "energy_count", Catalog.useCount(itemClass));
						} else {
							desc += "\n\n" + Messages.get(CatalogTab.class, "use_count", Catalog.useCount(itemClass));
						}
					}

					//mage's staff normally has 2 pixels extra at the top for particle effects, we chop that off here
					if (item instanceof MagesStaff){
						RectF frame = sprite.frame();
						frame.top += frame.height()/8f;
						sprite.frame(frame);
					}

					if (item.icon != -1) {
						secondIcon = new Image(Assets.Sprites.ITEM_ICONS);
						secondIcon.frame(ItemSpriteSheet.Icons.film.get(item.icon));
					}
				}

			} else if (Compat.isAssignableFrom(Weapon.Enchantment.class, itemClass)){

				Weapon.Enchantment ench = (Weapon.Enchantment) Reflection.newInstance(itemClass);

				if (seen){
					sprite = new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD, ench.glowing());
					title = Messages.titleCase(ench.name());
					desc = ench.desc();
				} else {
					sprite = new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD);
					sprite.lightness(0f);
					title = "???";
					desc = Messages.get(CatalogTab.class, "not_seen_enchantment");
					desc += "\n\n" + Messages.get(ench, "discover_hint");
				}

			} else if (Compat.isAssignableFrom(Armor.Glyph.class, itemClass)){

				Armor.Glyph glyph = (Armor.Glyph) Reflection.newInstance(itemClass);

				if (seen){
					sprite = new ItemSprite(ItemSpriteSheet.ARMOR_CLOTH, glyph.glowing());
					title = Messages.titleCase(glyph.name());
					desc = glyph.desc();
				} else {
					sprite = new ItemSprite(ItemSpriteSheet.ARMOR_CLOTH);
					sprite.lightness(0f);
					title = "???";
					desc = Messages.get(CatalogTab.class, "not_seen_glyph");
					desc += "\n\n" + Messages.get(glyph, "discover_hint");
				}

			}

			String finalTitle = title;
			String finalDesc = desc;
			ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(sprite) {
				@Override
				public boolean onClick(float x, float y) {
					if (inside(x, y)) {
						Image sprite = new ItemSprite();
						sprite.copy(icon);
						if (SacredPixelDungeon.scene() instanceof GameScene){
							GameScene.show(new WndJournalItem(sprite, finalTitle, finalDesc));
						} else {
							SacredPixelDungeon.scene().addToFront(new WndJournalItem(sprite, finalTitle, finalDesc));
						}
						return true;
					} else {
						return false;
					}
				}
			};
			if (secondIcon != null){
				gridItem.addSecondIcon(secondIcon);
			}
			if (!seen) {
				gridItem.hardLightBG(2f, 1f, 2f);
			}
			grid.addItem(gridItem);
		}
	}

	private static void addGridEntities(ScrollingGridPane grid, Collection<Class<?>> classes) {
		for (Class<?> entityCls : classes){

			boolean seen = Bestiary.isSeen(entityCls);
			Mob mob = null;
			Image icon = null;
			String title = null;
			String desc = null;

			if (Compat.isAssignableFrom(Mob.class, entityCls)) {

				mob = (Mob) Reflection.newInstance(entityCls);
				if (mob == null) continue;

				if (mob instanceof Mimic || mob instanceof Pylon || mob instanceof CrystalSpire) {
					mob.alignment = Char.Alignment.ENEMY;
				}
				if (mob instanceof WandOfWarding.Ward){
					if (mob instanceof WandOfWarding.Ward.WardSentry){
						((WandOfWarding.Ward) mob).upgrade(3);
						((WandOfWarding.Ward) mob).upgrade(3);
						((WandOfWarding.Ward) mob).upgrade(3);
						((WandOfWarding.Ward) mob).upgrade(3);
					} else {
						((WandOfWarding.Ward) mob).upgrade(0);
					}
				}

				CharSprite sprite = mob.sprite();
				sprite.idle();

				icon = new Image(sprite);
				if (seen) {
					title = Messages.titleCase(mob.name());
					desc = mob.description();
					if (Bestiary.encounterCount(entityCls) > 1){
						desc += "\n\n" + Messages.get(CatalogTab.class, "enemy_count", Bestiary.encounterCount(entityCls));
					}
				} else {
					icon.lightness(0f);
					title = "???";
					if (mob instanceof WandOfRegrowth.Lotus){
						desc = Messages.get(CatalogTab.class, "not_seen_plant");
					} else if (mob.alignment == Char.Alignment.ENEMY){
						desc = Messages.get(CatalogTab.class, "not_seen_enemy");
					} else {
						desc = Messages.get(CatalogTab.class, "not_seen_ally");
					}
					desc += "\n\n" + Messages.get(mob, "discover_hint");
				}

				//we have to clip the bounds of the sprite if it's too large
				if (icon.width() >= 17 || icon.height() >= 17) {
					RectF frame = icon.frame();

					float wShrink = frame.width() * (1f - 17f / icon.width());
					if (wShrink > 0) {
						frame.left += wShrink / 2f;
						frame.right -= wShrink / 2f;
					}
					float hShrink = frame.height() * (1f - 17f / icon.height());
					if (hShrink > 0) {
						frame.top += hShrink / 2f;
						frame.bottom -= hShrink / 2f;
					}
					icon.frame(frame);
				}
			} else if (Compat.isAssignableFrom(Trap.class, entityCls)){

				Trap trap = (Trap) Reflection.newInstance(entityCls);
				icon = TerrainFeaturesTilemap.getTrapVisual(trap);

				if (seen) {
					title = Messages.titleCase(trap.name());
					desc = trap.desc();
					if (Bestiary.encounterCount(entityCls) > 1){
						desc += "\n\n" + Messages.get(CatalogTab.class, "trap_count", Bestiary.encounterCount(entityCls));
					}
				} else {
					icon.lightness(0f);
					title = "???";
					desc = Messages.get(CatalogTab.class, "not_seen_trap");
					desc += "\n\n" + Messages.get(trap, "discover_hint");
				}

			} else if (Compat.isAssignableFrom(Plant.class, entityCls)){

				Plant plant = (Plant) Reflection.newInstance(entityCls);
				icon = TerrainFeaturesTilemap.getPlantVisual(plant);

				if (seen) {
					title = Messages.titleCase(plant.name());
					desc = plant.desc();
					if (Bestiary.encounterCount(entityCls) > 1){
						desc += "\n\n" + Messages.get(CatalogTab.class, "plant_count", Bestiary.encounterCount(entityCls));
					}
				} else {
					icon.lightness(0f);
					title = "???";
					desc = Messages.get(CatalogTab.class, "not_seen_plant");
					desc += "\n\n" + Messages.get(plant, "discover_hint");
				}

			}

			Mob finalMob = mob;
			String finalTitle = title;
			String finalDesc = desc;
			ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(icon) {
				@Override
				public boolean onClick(float x, float y) {
					if (inside(x, y)) {
						Image image;
						if (seen && finalMob != null){
							image = finalMob.sprite();
						} else {
							image = new Image(icon);
						}

						if (SacredPixelDungeon.scene() instanceof GameScene){
							GameScene.show(new WndJournalItem(image, finalTitle, finalDesc));
						} else {
							SacredPixelDungeon.scene().addToFront(new WndJournalItem(image, finalTitle, finalDesc));
						}

						return true;
					} else {
						return false;
					}
				}
			};
			if (!seen) {
				gridItem.hardLightBG(2f, 1f, 2f);
			}
			grid.addItem(gridItem);
		}
	};

	private static void addGridDocuments( ScrollingGridPane grid, Document doc ){
		for (String page : doc.pageNames()){

			Image sprite = doc.pageSprite(page);

			boolean seen = doc.isPageFound(page);
			boolean read = doc.isPageRead(page);

			if (!seen){
				sprite.lightness(0f);
			}

			ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(sprite) {
				@Override
				public boolean onClick(float x, float y) {
					if (inside(x, y)) {
						Image sprite = new Image(icon);
						if (seen) {
							if (SacredPixelDungeon.scene() instanceof GameScene){
								GameScene.show(new WndStory(sprite, doc.pageTitle(page), doc.pageBody(page)));
							} else {
								SacredPixelDungeon.scene().addToFront(new WndStory(sprite, doc.pageTitle(page), doc.pageBody(page)));
							}

							doc.readPage(page);
							hardLightBG(1, 1, 1);
						} else {
							if (SacredPixelDungeon.scene() instanceof GameScene){
								GameScene.show(new WndJournalItem(sprite, "???",
										Messages.get(CatalogTab.class, "not_seen_lore") + "\n\n" + doc.discoverHint()));
							} else {
								SacredPixelDungeon.scene().addToFront(new WndJournalItem(sprite, "???",
										Messages.get(CatalogTab.class, "not_seen_lore") + "\n\n" + doc.discoverHint()));
							}

						}
						return true;
					} else {
						return false;
					}
				}
			};

			if (seen){
				BitmapText text = new BitmapText(Integer.toString(doc.pageIdx(page)+1), PixelScene.pixelFont);
				text.measure();
				gridItem.addSecondIcon( text );
				if (!read) {
					gridItem.hardLightBG(0.6f, 1f, 2f);
				}
			} else {
				gridItem.hardLightBG(2.2f, 1f, 2.2f);
			}
			grid.addItem(gridItem);
		}
	}

	public static class BadgesTab extends Component {

		public RedButton btnLocal;
		public RedButton btnGlobal;

		private RenderedTextBlock title;

		public Component badgesLocal;
		public Component badgesGlobal;

		public static boolean global = false;

		@Override
		protected void createChildren() {

			if (Dungeon.hero != null) {
				btnLocal = new RedButton(Messages.get(this, "this_run"), 7) {
					@Override
					protected void onClick() {
						super.onClick();
						global = false;
						updateList();
					}
				};
				add(btnLocal);

				btnGlobal = new RedButton(Messages.get(this, "overall"), 7) {
					@Override
					protected void onClick() {
						super.onClick();
						global = true;
						updateList();
					}
				};
				add(btnGlobal);

				if (Badges.filterReplacedBadges(false).size() <= 8){
					badgesLocal = new BadgesList(false);
				} else {
					badgesLocal = new BadgesGrid(false);
				}
				add( badgesLocal );
			} else {
				title = PixelScene.renderTextBlock(Messages.get(this, "title_main_menu"), 8);
				title.hardlight(Window.TITLE_COLOR);
				add(title);
			}

			badgesGlobal = new BadgesGrid(true);
			add( badgesGlobal );
		}

		@Override
		protected void layout() {
			super.layout();

			if (btnLocal != null) {
				btnLocal.setRect(x, y, width / 2, BTN_HEIGHT);
				btnGlobal.setRect(x + width / 2, y, width / 2, BTN_HEIGHT);

				badgesLocal.setRect(x, y + 20, width, height-20);
				badgesGlobal.setRect( x, y + 20, width, height-20);
			} else {
				title.setPos( x + (width - title.width())/2, y + 2);

				badgesGlobal.setRect( x, y + 14, width, height-14);
			}
		}

		private void updateList(){
			if (btnLocal != null) {
				badgesLocal.visible = badgesLocal.active = !global;
				badgesGlobal.visible = badgesGlobal.active = global;

				btnLocal.textColor(global ? Window.WHITE : Window.TITLE_COLOR);
				btnGlobal.textColor(global ? Window.TITLE_COLOR : Window.WHITE);
			} else {
				badgesGlobal.visible = badgesGlobal.active = true;
			}
		}

	}
	
}
