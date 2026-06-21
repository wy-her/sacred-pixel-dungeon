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
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class BadgesList extends ScrollPane {

	public ArrayList<ListItem> items = new ArrayList<>();

	// Keyboard navigation
	private Signal.Listener<KeyEvent> navKeyListener;
	private int focusedIndex = -1;
	private boolean keyboardActive = false;

	public BadgesList( boolean global ) {
		super( new Component() );

		for (Badges.Badge badge : Badges.filterReplacedBadges( global )) {

			if (badge.type == Badges.BadgeType.HIDDEN) {
				continue;
			}

			ListItem item = new ListItem( badge );
			content.add( item );
			items.add( item );
		}
	}

	@Override
	protected void layout() {

		float pos = 0;

		int size = items.size();
		for (int i=0; i < size; i++) {
			items.get( i ).setRect( 0, pos, width, ListItem.HEIGHT );
			pos += ListItem.HEIGHT;
		}

		content.setSize( width, pos );

		super.layout();
	}

	@Override
	public void onClick( float x, float y ) {
		int size = items.size();
		for (int i=0; i < size; i++) {
			if (items.get( i ).onClick( x, y )) {
				break;
			}
		}
	}

	// Keyboard navigation methods

	public void setKeyboardActive(boolean active) {
		if (this.keyboardActive == active) return;
		this.keyboardActive = active;

		if (active) {
			navKeyListener = new Signal.Listener<KeyEvent>() {
				@Override
				public boolean onSignal(KeyEvent event) {
					if (!event.pressed || items.isEmpty() || !visible || !BadgesList.this.active) return false;

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

					GameAction action = KeyBindings.getActionForKey(event);

					// Up/Down navigation
					if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE
							|| action == SPDAction.W) {
						moveFocus(-1);
						return true;
					} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE
							|| action == SPDAction.E) {
						moveFocus(1);
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
			KeyEvent.addKeyListener(navKeyListener);
			// Don't auto-focus on activation - let user navigate to first item
		} else {
			if (navKeyListener != null) {
				KeyEvent.removeKeyListener(navKeyListener);
				navKeyListener = null;
			}
			clearFocus();
		}
	}

	public boolean isKeyboardActive() {
		return keyboardActive;
	}

	public void moveFocus(int direction) {
		if (items.isEmpty()) return;

		// Clear previous highlight
		if (focusedIndex >= 0 && focusedIndex < items.size()) {
			items.get(focusedIndex).setFocused(false);
		}

		if (focusedIndex == -1) {
			focusedIndex = direction > 0 ? 0 : items.size() - 1;
		} else {
			focusedIndex += direction;
			if (focusedIndex < 0) focusedIndex = items.size() - 1;
			if (focusedIndex >= items.size()) focusedIndex = 0;
		}

		if (focusedIndex >= 0 && focusedIndex < items.size()) {
			items.get(focusedIndex).setFocused(true);
			scrollToItem(focusedIndex);
		}
	}

	private void scrollToItem(int index) {
		if (index >= 0 && index < items.size()) {
			ListItem item = items.get(index);
			float itemTop = item.top();
			float itemBottom = item.bottom();

			if (itemTop < content.camera.scroll.y) {
				scrollTo(0, itemTop - 2);
			} else if (itemBottom > content.camera.scroll.y + height) {
				scrollTo(0, itemBottom - height + 2);
			}
		}
	}

	public void activateFocused() {
		if (focusedIndex >= 0 && focusedIndex < items.size()) {
			ListItem item = items.get(focusedIndex);
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			item.onClick(item.left() + item.width()/2, item.top() + item.height()/2);
		}
	}

	public void clearFocus() {
		if (focusedIndex >= 0 && focusedIndex < items.size()) {
			items.get(focusedIndex).setFocused(false);
		}
		focusedIndex = -1;
	}

	@Override
	public void destroy() {
		if (navKeyListener != null) {
			KeyEvent.removeKeyListener(navKeyListener);
			navKeyListener = null;
		}
		super.destroy();
	}

	public class ListItem extends Component {

		private static final float HEIGHT	= 18;

		private Badges.Badge badge;
		private boolean focused = false;

		private Image icon;
		private RenderedTextBlock label;

		public ListItem( Badges.Badge badge ) {
			super();

			this.badge = badge;
			icon.copy( BadgeBanner.image( badge.image ));
			label.text( badge.title() );
		}

		@Override
		protected void createChildren() {
			icon = new Image();
			add( icon );

			label = PixelScene.renderTextBlock( 6 );
			add( label );
		}

		@Override
		protected void layout() {
			icon.x = x;
			icon.y = y + (height - icon.height) / 2;
			PixelScene.align(icon);

			label.setPos(
					icon.x + icon.width + 2,
					y + (height - label.height()) / 2
			);
			PixelScene.align(label);
		}

		public boolean onClick( float x, float y ) {
			if (inside( x, y )) {
				Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );

				// Find parent window and deactivate it while child window is open
				final Window parentWindow = findParentWindow();
				if (parentWindow != null) {
					parentWindow.active = false;
				}

				Game.scene().addToFront( new WndBadge( badge, true ) {
					@Override
					public void destroy() {
						super.destroy();
						if (parentWindow != null && parentWindow.parent != null) {
							parentWindow.active = true;
						}
					}
				});
				return true;
			} else {
				return false;
			}
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

		public void setFocused(boolean focused) {
			this.focused = focused;
			if (focused) {
				icon.brightness(1.5f);
				label.hardlight(Window.TITLE_COLOR);
			} else {
				icon.resetColor();
				label.resetColor();
			}
		}
	}
}
