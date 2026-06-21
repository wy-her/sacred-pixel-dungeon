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

package com.sacredpixel.sacredpixeldungeon.ui;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.effects.BadgeBanner;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.windows.WndBadge;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Random;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class BadgesGrid extends Component {

	public ArrayList<BadgeButton> badgeButtons;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private int focusedIndex = -1;
	private boolean keyboardActive = false;
	private int nCols = 1;

	public BadgesGrid( boolean global ){
		super();
		badgeButtons = new ArrayList<>();

		for (Badges.Badge badge : Badges.filterReplacedBadges( global )) {

			if (badge.type == Badges.BadgeType.HIDDEN) {
				continue;
			}

			BadgeButton button = new BadgeButton( badge, true );
			add( button );
			badgeButtons.add(button);
		}

		if (global) {

			ArrayList<Badges.Badge> lockedBadges = new ArrayList<>();
			for (Badges.Badge badge : Badges.Badge.values()) {
				if (badge.type != Badges.BadgeType.HIDDEN && !Badges.isUnlocked(badge)) {
					lockedBadges.add(badge);
				}
			}
			Badges.filterBadgesWithoutPrerequisites(lockedBadges);

			for (Badges.Badge badge : lockedBadges) {
				BadgeButton button = new BadgeButton( badge, false );
				add(button);
				badgeButtons.add(button);
			}

		}

	}

	@Override
	protected void layout() {
		super.layout();

		//determines roughly how much space each badge will get ideally, determines columns based on that
		if (badgeButtons.size() == 0) return;
		float badgeArea = (float) Math.sqrt(width * height / badgeButtons.size());
		nCols = Math.round(width / badgeArea);
		if (nCols < 1) nCols = 1;

		int nRows = (int) Math.ceil(badgeButtons.size()/(float)nCols);

		float badgeWidth = width()/nCols;
		float badgeHeight = height()/nRows;

		for (int i = 0; i < badgeButtons.size(); i++){
			int row = i / nCols;
			int col = i % nCols;
			BadgeButton button = badgeButtons.get(i);
			button.setPos(
					left() + col * badgeWidth + (badgeWidth - button.width()) / 2,
					top() + row * badgeHeight + (badgeHeight - button.height()) / 2);
			PixelScene.align(button);
		}
	}

	// Keyboard navigation methods

	public void setKeyboardActive(boolean active) {
		if (this.keyboardActive == active) return;
		this.keyboardActive = active;

		if (active) {
			keyListener = new Signal.Listener<KeyEvent>() {
				@Override
				public boolean onSignal(KeyEvent event) {
					if (!event.pressed || badgeButtons.isEmpty() || !visible || !BadgesGrid.this.active) return false;

					// Check if parent window is topmost
					com.watabou.noosa.Group p = parent;
					Window parentWindow = null;
					while (p != null) {
						if (p instanceof Window) {
							parentWindow = (Window) p;
						}
						if (p instanceof com.watabou.noosa.Group) {
							p = ((com.watabou.noosa.Group) p).parent;
						} else {
							break;
						}
					}
					if (parentWindow != null && !parentWindow.isTopmost()) return false;

					// Check if any window is on top of the scene (for JournalScene where
					// BadgesGrid is not inside a Window but WndBadge may be shown on top)
					// This handles the case when a badge description window is open
					if (Game.scene() instanceof com.sacredpixel.sacredpixeldungeon.scenes.PixelScene) {
						com.sacredpixel.sacredpixeldungeon.scenes.PixelScene scene =
							(com.sacredpixel.sacredpixeldungeon.scenes.PixelScene) Game.scene();
						if (scene.hasOpenWindows()) {
							return false; // A window is open on top, block navigation
						}
					}

					GameAction action = KeyBindings.getActionForKey(event);

					// Arrow key navigation
					if (action == SPDAction.W) {
						moveFocus(-1, 0);
						return true;
					} else if (action == SPDAction.E) {
						moveFocus(1, 0);
						return true;
					} else if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE) {
						moveFocus(0, -1);
						return true;
					} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE) {
						moveFocus(0, 1);
						return true;
					}
					// Enter to activate
					else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
							|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
						activateFocused();
						return true;
					}
					return false;
				}
			};
			KeyEvent.addKeyListener(keyListener);
			// Don't auto-focus on activation - let user navigate to first item
		} else {
			if (keyListener != null) {
				KeyEvent.removeKeyListener(keyListener);
				keyListener = null;
			}
			clearFocus();
		}
	}

	public boolean isKeyboardActive() {
		return keyboardActive;
	}

	public void moveFocus(int dx, int dy) {
		if (badgeButtons.isEmpty()) return;

		// Clear previous highlight
		if (focusedIndex >= 0 && focusedIndex < badgeButtons.size()) {
			badgeButtons.get(focusedIndex).setFocused(false);
		}

		if (focusedIndex == -1) {
			focusedIndex = 0;
		} else {
			if (dx != 0) {
				focusedIndex += dx;
				if (focusedIndex < 0) focusedIndex = badgeButtons.size() - 1;
				if (focusedIndex >= badgeButtons.size()) focusedIndex = 0;
			} else if (dy != 0) {
				int targetIndex = focusedIndex + (dy * nCols);
				if (targetIndex >= 0 && targetIndex < badgeButtons.size()) {
					focusedIndex = targetIndex;
				} else if (dy < 0) {
					// Wrap to last row
					int col = focusedIndex % nCols;
					int lastRowStart = (badgeButtons.size() / nCols) * nCols;
					int targetCol = lastRowStart + col;
					focusedIndex = Math.min(targetCol, badgeButtons.size() - 1);
				} else {
					// Wrap to first row
					int col = focusedIndex % nCols;
					focusedIndex = Math.min(col, badgeButtons.size() - 1);
				}
			}
		}

		if (focusedIndex >= 0 && focusedIndex < badgeButtons.size()) {
			badgeButtons.get(focusedIndex).setFocused(true);
		}
	}

	public void activateFocused() {
		if (focusedIndex >= 0 && focusedIndex < badgeButtons.size()) {
			badgeButtons.get(focusedIndex).onClick();
		}
	}

	public void clearFocus() {
		if (focusedIndex >= 0 && focusedIndex < badgeButtons.size()) {
			badgeButtons.get(focusedIndex).setFocused(false);
		}
		focusedIndex = -1;
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		super.destroy();
	}

	public static class BadgeButton extends Button {

		private Badges.Badge badge;
		private boolean unlocked;
		private boolean focused = false;

		private Image icon;

		public BadgeButton( Badges.Badge badge, boolean unlocked ) {
			super();

			this.badge = badge;
			this.unlocked = unlocked;

			icon = BadgeBanner.image(badge.image);
			if (!unlocked) {
				icon.brightness(0.4f);
			}
			add(icon);

			setSize( icon.width(), icon.height() );
		}

		@Override
		protected void layout() {
			super.layout();

			icon.x = x + (width - icon.width()) / 2;
			icon.y = y + (height - icon.height()) / 2;
		}

		@Override
		public void update() {
			super.update();

			if (!focused && unlocked && Random.Float() < Game.elapsed * 0.1) {
				BadgeBanner.highlight( icon, badge.image );
			}
		}

		@Override
		protected void onClick() {
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );

			// Find parent window and deactivate it while child window is open
			final Window parentWindow = findParentWindow();
			if (parentWindow != null) {
				parentWindow.active = false;
			}

			Game.scene().addToFront( new WndBadge( badge, unlocked ) {
				@Override
				public void destroy() {
					super.destroy();
					if (parentWindow != null && parentWindow.parent != null) {
						parentWindow.active = true;
					}
				}
			});
		}

		private Window findParentWindow() {
			com.watabou.noosa.Group p = parent;
			while (p != null) {
				if (p instanceof Window) {
					return (Window) p;
				}
				if (p instanceof com.watabou.noosa.Group) {
					p = ((com.watabou.noosa.Group) p).parent;
				} else {
					break;
				}
			}
			return null;
		}

		@Override
		protected String hoverText() {
			return badge.title();
		}

		public void setFocused(boolean focused) {
			this.focused = focused;
			if (focused) {
				icon.brightness(1.5f);
			} else {
				if (unlocked) {
					icon.resetColor();
				} else {
					icon.brightness(0.4f);
				}
			}
		}
	}

}