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

package com.sacredpixel.sacredpixeldungeon.scenes;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.items.potions.Potion;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.Scroll;
import com.sacredpixel.sacredpixeldungeon.journal.Journal;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.sacredpixel.sacredpixeldungeon.ui.BadgesGrid;
import com.sacredpixel.sacredpixeldungeon.ui.BadgesList;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.ColorBlock;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.sacredpixel.sacredpixeldungeon.windows.WndJournal;
import com.watabou.noosa.Camera;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;

public class JournalScene extends PixelScene {

	public static final int WIDTH_P     = 149;
	public static final int WIDTH_L     = 251;

	private static int lastIDX = 0;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private ArrayList<StyledButton> tabButtons = new ArrayList<>();

	// Content-specific button focus
	private ArrayList<com.sacredpixel.sacredpixeldungeon.ui.Button> focusableButtons = new ArrayList<>();
	private int focusIndex = -1;
	private boolean contentFocusMode = false;

	// References for content keyboard navigation
	private WndJournal.BadgesTab badgesTab;
	private WndJournal.CatalogTab catalogTab;
	private WndJournal.GuideTab guideTab;
	private WndJournal.AlchemyTab alchemyTab;

	@Override
	public void create() {

		super.create();

		Dungeon.hero = null;
		Badges.loadGlobal();
		Journal.loadGlobal();

		Potion.clearColors();
		Scroll.clearLabels();
		Ring.clearGems();

		//need to re-initialize the texture here, as it may be invalid
		new TerrainFeaturesTilemap(new SparseArray<>(), new SparseArray<>());

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;

		RectF insets = getCommonInsets();

		ColorBlock BG = new ColorBlock(w, h, 0xFF000000);
		//BG added later

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;

		float top = 20;

		IconTitle title = new IconTitle( Icons.JOURNAL.get(), Messages.get(this, "title") );
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (top - title.height()) / 2f
		);
		align(title);
		add(title);

		NinePatch panel = Chrome.get(Chrome.Type.TOAST);

		int pw = (landscape() ? WIDTH_L : WIDTH_P) + panel.marginHor();
		int ph = h - 50 + panel.marginVer();

		panel.size(pw, ph);
		panel.x = insets.left + (w - pw) / 2f;
		panel.y = insets.top + top;
		add(panel);

		switch (lastIDX){
			case 0: default:
				badgesTab = new WndJournal.BadgesTab();
				add(badgesTab);
				badgesTab.setRect(panel.x + panel.marginLeft(),
						panel.y + panel.marginTop(),
						panel.width() - panel.marginHor(),
						panel.height() - panel.marginVer());
				// Ensure badges tab and its components are active for mouse clicks
				badgesTab.active = true;
				if (badgesTab.badgesGlobal != null) {
					badgesTab.badgesGlobal.active = true;
				}
				break;
			case 1:
				catalogTab = new WndJournal.CatalogTab();
				add(catalogTab);
				catalogTab.setRect(panel.x + panel.marginLeft(),
						panel.y + panel.marginTop(),
						panel.width() - panel.marginHor(),
						panel.height() - panel.marginVer());
				catalogTab.updateList();
				// Ensure catalog tab and its components are active for mouse clicks
				catalogTab.active = true;
				if (catalogTab.grid != null) {
					catalogTab.grid.active = true;
				}
				if (catalogTab.itemButtons != null) {
					for (com.sacredpixel.sacredpixeldungeon.ui.RedButton btn : catalogTab.itemButtons) {
						btn.active = true;
					}
				}
				break;
			case 2:
				guideTab = new WndJournal.GuideTab();
				add(guideTab);
				guideTab.setRect(panel.x + panel.marginLeft(),
						panel.y + panel.marginTop(),
						panel.width() - panel.marginHor(),
						panel.height() - panel.marginVer());
				guideTab.updateList();
				// Ensure guide tab and its list are active for mouse clicks
				guideTab.active = true;
				if (guideTab.list != null) {
					guideTab.list.active = true;
				}
				break;
			case 3:
				alchemyTab = new WndJournal.AlchemyTab();
				add(alchemyTab);
				alchemyTab.setRect(panel.x + panel.marginLeft(),
						panel.y + panel.marginTop(),
						panel.width() - panel.marginHor(),
						panel.height() - panel.marginVer());
				// Ensure alchemy tab and its components are active for mouse clicks
				alchemyTab.active = true;
				if (alchemyTab.list != null) {
					alchemyTab.list.active = true;
				}
				if (alchemyTab.pageButtons != null) {
					for (com.sacredpixel.sacredpixeldungeon.ui.RedButton btn : alchemyTab.pageButtons) {
						if (btn.active) { // Preserve enabled/disabled state
							btn.active = true;
						}
					}
				}
				break;
		}

		StyledButton btnBadges =  new StyledButton(Chrome.Type.GREY_BUTTON_TR, ""){
			@Override
			protected void onClick() {
				if (lastIDX != 0) {
					lastIDX = 0;
				}
				SacredPixelDungeon.seamlessResetScene();
				super.onClick();
			}

			@Override
			protected String hoverText() {
				return Messages.get(WndJournal.BadgesTab.class, "title");
			}
		};
		btnBadges.icon(Icons.BADGES.get());
		btnBadges.setRect(panel.x, panel.y + ph - 3, pw/4f + 1.5f, lastIDX == 0 ? 25 : 20);
		align(btnBadges);
		if (lastIDX != 0) btnBadges.icon().brightness(0.6f);
		addToBack(btnBadges);
		tabButtons.add(btnBadges);

		StyledButton btnCatalog =  new StyledButton(Chrome.Type.GREY_BUTTON_TR, ""){
			@Override
			protected void onClick() {
				if (lastIDX != 1) {
					lastIDX = 1;
				}
				SacredPixelDungeon.seamlessResetScene();
				super.onClick();
			}
			@Override
			protected String hoverText() {
				return Messages.get(WndJournal.CatalogTab.class, "title");
			}
		};
		btnCatalog.icon(Icons.CATALOG.get());
		btnCatalog.setRect(btnBadges.right()-2, btnBadges.top(), pw/4f + 1.5f, lastIDX == 1 ? 25 : 20);
		align(btnCatalog);
		if (lastIDX != 1) btnCatalog.icon().brightness(0.6f);
		addToBack(btnCatalog);
		tabButtons.add(btnCatalog);

		StyledButton btnGuide =  new StyledButton(Chrome.Type.GREY_BUTTON_TR, ""){
			@Override
			protected void onClick() {
				if (lastIDX != 2) {
					lastIDX = 2;
				}
				SacredPixelDungeon.seamlessResetScene();
				super.onClick();
			}
			@Override
			protected String hoverText() {
				return Messages.get(WndJournal.GuideTab.class, "title");
			}
		};
		btnGuide.icon(new ItemSprite(ItemSpriteSheet.MASTERY));
		btnGuide.setRect(btnCatalog.right()-2, btnBadges.top(), pw/4f + 1.5f, lastIDX == 2 ? 25 : 20);
		align(btnGuide);
		if (lastIDX != 2) btnGuide.icon().brightness(0.6f);
		addToBack(btnGuide);
		tabButtons.add(btnGuide);

		StyledButton btnAlchemy =  new StyledButton(Chrome.Type.GREY_BUTTON_TR, ""){
			@Override
			protected void onClick() {
				if (lastIDX != 3) {
					lastIDX = 3;
				}
				SacredPixelDungeon.seamlessResetScene();
				super.onClick();
			}
			@Override
			protected String hoverText() {
				return Messages.get(WndJournal.AlchemyTab.class, "title");
			}
		};
		btnAlchemy.icon(Icons.ALCHEMY.get());
		btnAlchemy.setRect(btnGuide.right()-2, btnBadges.top(), pw/4f + 1.5f, lastIDX == 3 ? 25 : 20);
		align(btnAlchemy);
		if (lastIDX != 3) btnAlchemy.icon().brightness(0.6f);
		addToBack(btnAlchemy);
		tabButtons.add(btnAlchemy);

		addToBack(BG);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( insets.left + w - btnExit.width(), insets.top );
		add( btnExit );

		// Setup keyboard navigation
		setupKeyboardNavigation();

		fadeIn();
	}

	private void setupKeyboardNavigation() {
		// Build initial focusable buttons list based on current tab
		rebuildFocusableButtons();

		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

				// Block all keys (except BACK) if a window is open
				for (Object v : members) {
					if (v instanceof Window) {
						GameAction action = KeyBindings.getActionForKey(event);
						if (action == SPDAction.BACK) {
							return false; // Let child window handle ESC
						}
						return true; // Block all other keys
					}
				}

				GameAction action = KeyBindings.getActionForKey(event);

				// Handle ESC to exit content focus mode or go back
				if (action == SPDAction.BACK) {
					if (contentFocusMode) {
						exitContentFocusMode();
						return true;
					}
					onBackPressed();
					return true;
				}

				// Tab/Space to cycle tabs
				if (action == SPDAction.CYCLE) {
					int nextIdx = (lastIDX + 1) % tabButtons.size();
					clearFocus();
					tabButtons.get(nextIdx).click();
					return true;
				}

				// In content focus mode, handle content navigation
				if (contentFocusMode) {
					// Handle Alchemy tab recipe navigation
					if (lastIDX == 3 && alchemyTab != null && alchemyTab.isRecipeNavigationActive()) {
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
						} else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
								|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
							// Enter - activate focused item
							alchemyTab.activateFocusedItem();
							return true;
						}
					}
					// Let other content types handle their own navigation
					return false;
				}

				// If no focusable buttons (badges tab from main menu), directly enter content mode on arrow keys
				if (focusableButtons.isEmpty()) {
					if (action == SPDAction.N || action == SPDAction.S ||
						action == SPDAction.E || action == SPDAction.W ||
						action == SPDAction.NE || action == SPDAction.NW ||
						action == SPDAction.SE || action == SPDAction.SW) {
						enterContentFocusMode();
						return true;
					}
					return false;
				}

				// For Catalog (1) and Alchemy (3) tabs, only use left/right for button navigation
				if (lastIDX == 1 || lastIDX == 3) {
					// Block up/down keys, only allow left/right
					if (action == SPDAction.W || action == SPDAction.SW || action == SPDAction.NW) {
						moveFocus(-1);
						return true;
					} else if (action == SPDAction.E || action == SPDAction.SE || action == SPDAction.NE) {
						moveFocus(1);
						return true;
					} else if (action == SPDAction.N || action == SPDAction.S) {
						// Block up/down keys for page navigation
						return true;
					}
				} else {
					// Button focus navigation with arrow keys (all directions)
					if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE
							|| action == SPDAction.W) {
						moveFocus(-1);
						return true;
					} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE
							|| action == SPDAction.E) {
						moveFocus(1);
						return true;
					}
				}

				// Enter to activate focused button AND enter content focus mode
				if (event.code == com.badlogic.gdx.Input.Keys.ENTER
						|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
					if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
						Sample.INSTANCE.play(Assets.Sounds.CLICK);
						focusableButtons.get(focusIndex).click();
						// Enter content focus mode after button activation
						// For tabs with scrollable/navigable content: Badges (0), Catalog (1), Alchemy (3)
						// Guide (2) is directly navigable via ScrollingListPane
						if (lastIDX == 0 || lastIDX == 1 || lastIDX == 3) {
							enterContentFocusMode();
						}
						return true;
					}
				}

				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);
	}

	private void rebuildFocusableButtons() {
		focusableButtons.clear();
		focusIndex = -1;

		switch (lastIDX) {
			case 0: // Badges tab - local/global buttons (if they exist)
				if (badgesTab != null) {
					if (badgesTab.btnLocal != null) {
						focusableButtons.add(badgesTab.btnLocal);
						focusableButtons.add(badgesTab.btnGlobal);
					}
					// If no buttons (main menu), badges grid handles keyboard directly
				}
				break;
			case 1: // Catalog tab - category buttons
				if (catalogTab != null && catalogTab.itemButtons != null) {
					for (com.sacredpixel.sacredpixeldungeon.ui.RedButton btn : catalogTab.itemButtons) {
						focusableButtons.add(btn);
					}
				}
				break;
			case 2: // Guide tab - ScrollingListPane handles its own keyboard
				// No buttons to focus, content is directly navigable
				break;
			case 3: // Alchemy tab - page buttons
				if (alchemyTab != null && alchemyTab.pageButtons != null) {
					for (com.sacredpixel.sacredpixeldungeon.ui.RedButton btn : alchemyTab.pageButtons) {
						if (btn.active) {
							focusableButtons.add(btn);
						}
					}
				}
				break;
		}
	}

	private void clearFocus() {
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			focusableButtons.get(focusIndex).setFocused(false);
		}
		focusIndex = -1;
	}

	private void moveFocus(int direction) {
		if (focusableButtons.isEmpty()) return;

		// Clear previous focus
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			focusableButtons.get(focusIndex).setFocused(false);
		}

		// Move index
		if (focusIndex == -1) {
			focusIndex = direction > 0 ? 0 : focusableButtons.size() - 1;
		} else {
			focusIndex += direction;
			if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
			if (focusIndex >= focusableButtons.size()) focusIndex = 0;
		}

		// Skip disabled buttons
		int checked = 0;
		while (!focusableButtons.get(focusIndex).active && checked < focusableButtons.size()) {
			focusIndex += direction > 0 ? 1 : -1;
			if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
			if (focusIndex >= focusableButtons.size()) focusIndex = 0;
			checked++;
		}

		// Set new focus
		focusableButtons.get(focusIndex).setFocused(true);
	}

	private void enterContentFocusMode() {
		contentFocusMode = true;

		// Clear button focus
		clearFocus();

		// Enable content keyboard navigation based on current tab
		switch (lastIDX) {
			case 0: // Badges - activate badge keyboard navigation
				if (badgesTab != null) {
					if (badgesTab.btnLocal != null) {
						if (WndJournal.BadgesTab.global) {
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
				}
				break;
			case 1: // Catalog - enable grid keyboard navigation
				if (catalogTab != null && catalogTab.grid != null) {
					catalogTab.grid.setKeyboardEnabled(true);
				}
				break;
			case 2: // Guide - ScrollingListPane handles its own keyboard
				// Already active
				break;
			case 3: // Alchemy - enable recipe keyboard navigation
				if (alchemyTab != null) {
					alchemyTab.activateRecipeNavigation();
				}
				break;
		}
	}

	private void exitContentFocusMode() {
		contentFocusMode = false;

		// Deactivate content keyboards
		deactivateAllContentKeyboards();

		// Rebuild focusable buttons
		rebuildFocusableButtons();
	}

	private void deactivateAllContentKeyboards() {
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

		if (catalogTab != null && catalogTab.grid != null) {
			catalogTab.grid.setKeyboardEnabled(false);
		}

		if (alchemyTab != null) {
			alchemyTab.deactivateRecipeNavigation();
		}
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}

		Badges.saveGlobal();

		super.destroy();
	}

	@Override
	protected void onBackPressed() {
		SacredPixelDungeon.switchNoFade( TitleScene.class );
	}

}
