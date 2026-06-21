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
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class ScrollingListPane extends ScrollPane {

	private ArrayList<Component> items = new ArrayList<>();
	private ArrayList<ColorBlock> hSeparators = new ArrayList<>();
	private ArrayList<ColorBlock> vSeparators = new ArrayList<>();

	private static final int ITEM_HEIGHT	= 18;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private int focusedItemIndex = -1;
	private ArrayList<ListItem> listItems = new ArrayList<>();
	private boolean keyboardEnabled = true;

	public void setKeyboardEnabled(boolean enabled) {
		this.keyboardEnabled = enabled;
		if (!enabled) {
			clearFocus();
		}
	}

	public boolean isKeyboardEnabled() {
		return keyboardEnabled;
	}

	public ScrollingListPane(){
		super(new Component());

		// Setup keyboard navigation
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed || listItems.isEmpty()) return false;

				// Check if keyboard navigation is enabled
				if (!keyboardEnabled) return false;

				// Check visibility
				if (!visible || !active) return false;
				com.watabou.noosa.Group p = parent;
				Window parentWindow = null;
				while (p != null) {
					if (!p.visible || !p.active) return false;
					if (p instanceof Window) {
						parentWindow = (Window) p;
					}
					if (p instanceof com.watabou.noosa.Group) {
						p = ((com.watabou.noosa.Group) p).parent;
					} else {
						break;
					}
				}

				// Don't handle keys if parent window is not topmost (another window is on top)
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
				// Enter to activate focused item
				else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
						|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
					activateFocusedItem();
					return true;
				}
				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		super.destroy();
	}

	private void moveFocus(int direction) {
		if (listItems.isEmpty()) return;

		// Clear previous highlight
		if (focusedItemIndex >= 0 && focusedItemIndex < listItems.size()) {
			listItems.get(focusedItemIndex).setFocused(false);
		}

		if (focusedItemIndex == -1) {
			focusedItemIndex = direction > 0 ? 0 : listItems.size() - 1;
		} else {
			focusedItemIndex += direction;
			if (focusedItemIndex < 0) focusedItemIndex = listItems.size() - 1;
			if (focusedItemIndex >= listItems.size()) focusedItemIndex = 0;
		}

		// Apply highlight
		if (focusedItemIndex >= 0 && focusedItemIndex < listItems.size()) {
			listItems.get(focusedItemIndex).setFocused(true);
			scrollToItem(focusedItemIndex);
		}
	}

	private void scrollToItem(int index) {
		if (index >= 0 && index < listItems.size()) {
			ListItem item = listItems.get(index);
			float itemTop = item.top();
			float itemBottom = item.bottom();

			if (itemTop < content.camera.scroll.y) {
				// When scrolling up, check if this is the first item - scroll to very top to show title
				if (index == 0) {
					scrollTo(0, 0);
				} else {
					scrollTo(0, itemTop - 2);
				}
			} else if (itemBottom > content.camera.scroll.y + height) {
				scrollTo(0, itemBottom - height + 2);
			}
		}
	}

	private void activateFocusedItem() {
		if (focusedItemIndex >= 0 && focusedItemIndex < listItems.size()) {
			ListItem item = listItems.get(focusedItemIndex);
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			item.onClick(item.left() + item.width()/2, item.top() + item.height()/2);
		}
	}

	public void clearFocus() {
		if (focusedItemIndex >= 0 && focusedItemIndex < listItems.size()) {
			listItems.get(focusedItemIndex).setFocused(false);
		}
		focusedItemIndex = -1;
	}

	@Override
	public void onClick(float x, float y) {
		// Don't process clicks if blocked
		if (shouldBlockClicks()) {
			return;
		}
		for (Component item : items) {
			if ((item instanceof ListItem) && ((ListItem) item).onClick( x, y )) {
				break;
			}
		}
	}

	// Check if clicks should be blocked
	private boolean shouldBlockClicks() {
		// Find parent window by traversing up the hierarchy
		com.watabou.noosa.Group p = parent;
		Window parentWindow = null;
		while (p != null) {
			if (p instanceof Window) {
				parentWindow = (Window) p;
				break;
			}
			if (p instanceof com.watabou.noosa.Group) {
				p = ((com.watabou.noosa.Group) p).parent;
			} else {
				break;
			}
		}

		if (parentWindow != null) {
			// If we're inside a window, only block if that window is not topmost
			return !parentWindow.isTopmost();
		} else {
			// If we're not inside a window (e.g., in a Scene directly),
			// block if any window is open in the scene
			return isWindowOpenInScene();
		}
	}

	// Check if any Window is open in the current scene
	private boolean isWindowOpenInScene() {
		com.watabou.noosa.Game game = com.watabou.noosa.Game.instance;
		if (game == null) return false;
		com.watabou.noosa.Scene scene = com.watabou.noosa.Game.scene();
		if (scene == null) return false;

		for (int i = 0; i < scene.length; i++) {
			com.watabou.noosa.Gizmo g = scene.getMember(i);
			if (g instanceof Window) {
				return true;
			}
		}
		return false;
	}

	public void addItem( Image icon, String iconText, String text ){
		addItem( new ListItem(icon, iconText, text) );
	}

	public void addItem( ListItem item ){
		content.add(item);
		items.add(item);
		listItems.add(item);
		layout();
	}

	public void addTitle( String text ){
		ListTitle title = new ListTitle(text);
		content.add(title);
		items.add(title);
		layout();
	}

	@Override
	public synchronized void clear() {
		content.clear();
		items.clear();
		hSeparators.clear();
		vSeparators.clear();
		listItems.clear();
		focusedItemIndex = -1;
	}

	@Override
	protected void layout() {
		super.layout();

		int hSepsUsed = 0;
		int vSepsUsed = 0;

		if (false) { //always single column
			//landscape: 2-column layout for ListItems, full-width for ListTitles
			float pos = 0;
			float colWidth = width / 2f;
			//pendingLeft tracks a ListItem placed in the left column waiting for a right-column partner
			Component pendingLeft = null;

			for (Component item : items) {
				if (item instanceof ListTitle) {
					//flush pending left-column item first
					if (pendingLeft != null) {
						//add horizontal separator after the unpaired item
						if (pos > 0) {
							ColorBlock hSep = getOrCreateHSep(hSepsUsed++);
							hSep.size(width, 1);
							hSep.x = 0;
							hSep.y = pos;
							pos += 1;
						}
						pendingLeft = null;
					}
					item.setRect(0, pos, width, ITEM_HEIGHT);
					pos += item.height();
				} else if (item instanceof ListItem) {
					if (pendingLeft == null) {
						//place in left column
						item.setRect(0, pos, colWidth - 1, ITEM_HEIGHT);
						pendingLeft = item;
					} else {
						//place in right column, same row as pendingLeft
						item.setRect(colWidth + 1, pos, colWidth - 1, ITEM_HEIGHT);

						//add vertical separator between columns
						ColorBlock vSep = getOrCreateVSep(vSepsUsed++);
						vSep.size(1, ITEM_HEIGHT);
						vSep.x = colWidth;
						vSep.y = pos;

						pos += ITEM_HEIGHT;

						//add horizontal separator after the pair
						ColorBlock hSep = getOrCreateHSep(hSepsUsed++);
						hSep.size(width, 1);
						hSep.x = 0;
						hSep.y = pos;
						pos += 1;

						pendingLeft = null;
					}
				}
			}

			//flush last unpaired left item
			if (pendingLeft != null) {
				pos += ITEM_HEIGHT;
			}

			//clean up unused separators
			cleanupHSeps(hSepsUsed);
			cleanupVSeps(vSepsUsed);

			content.setSize(width, pos);

		} else {
			//portrait: single column with horizontal separators between ListItems
			float pos = 0;
			boolean lastWasListItem = false;

			for (Component item : items) {
				if (item instanceof ListItem) {
					//add separator before this ListItem if the previous item was also a ListItem
					if (lastWasListItem) {
						ColorBlock hSep = getOrCreateHSep(hSepsUsed++);
						hSep.size(width, 1);
						hSep.x = 0;
						hSep.y = pos;
						pos += 1;
					}
					item.setRect(0, pos, width, ITEM_HEIGHT);
					pos += item.height();
					lastWasListItem = true;
				} else {
					item.setRect(0, pos, width, ITEM_HEIGHT);
					pos += item.height();
					lastWasListItem = false;
				}
			}

			//clean up unused separators
			cleanupHSeps(hSepsUsed);
			cleanupVSeps(vSepsUsed);

			content.setSize(width, pos);
		}
	}

	private ColorBlock getOrCreateHSep(int index) {
		if (hSeparators.size() > index) {
			return hSeparators.get(index);
		} else {
			ColorBlock sep = new ColorBlock(1, 1, 0xFF222222);
			hSeparators.add(sep);
			content.add(sep);
			return sep;
		}
	}

	private ColorBlock getOrCreateVSep(int index) {
		if (vSeparators.size() > index) {
			return vSeparators.get(index);
		} else {
			ColorBlock sep = new ColorBlock(1, 1, 0xFF222222);
			vSeparators.add(sep);
			content.add(sep);
			return sep;
		}
	}

	private void cleanupHSeps(int used) {
		while (hSeparators.size() > used) {
			ColorBlock sep = hSeparators.remove(used);
			content.remove(sep);
		}
	}

	private void cleanupVSeps(int used) {
		while (vSeparators.size() > used) {
			ColorBlock sep = vSeparators.remove(used);
			content.remove(sep);
		}
	}

	public static class ListItem extends Component {

		protected Image icon;
		protected BitmapText iconLabel;
		protected RenderedTextBlock label;
		protected ColorBlock bg;
		private boolean focused = false;
		private int iconHardlightColor = -1;  // -1 means no hardlight applied

		public ListItem( Image icon, String text ) {
			this(icon, null, text);
		}

		public ListItem( Image icon, String iconText, String text ) {
			super();

			if (icon != null) {
				this.icon.copy(icon);
			} else {
				remove(label);
				label = PixelScene.renderTextBlock(8);
				add(label);
			}

			label.text( text );

			if (iconText != null) {
				iconLabel.text(iconText);
				iconLabel.measure();
			}
		}

		public boolean onClick( float x, float y ){
			return false;
		}

		public void hardlight( int color ){
			iconLabel.hardlight(color);
			label.hardlight(color);
		}

		public void hardlightIcon( int color ){
			icon.hardlight(color);
			iconHardlightColor = color;
		}

		public void setFocused(boolean focused) {
			this.focused = focused;
			if (focused) {
				bg.brightness(1.5f);
				icon.brightness(1.3f);
			} else {
				bg.resetColor();
				icon.resetColor();
				// Restore hardlight color if one was set (e.g., for "missing" items)
				if (iconHardlightColor != -1) {
					icon.hardlight(iconHardlightColor);
				}
			}
		}

		@Override
		protected void createChildren() {
			bg = new ColorBlock( 1, 1, 0x9953564D);
			add( bg );

			icon = new Image();
			add( icon );

			iconLabel = new BitmapText( PixelScene.pixelFont);
			add( iconLabel );

			label = PixelScene.renderTextBlock( 7 );
			add( label );

		}

		@Override
		protected void layout() {

			bg.x = x;
			bg.y = y;
			bg.size(width(), height());

			icon.y = y + 1 + (height() - 1 - icon.height()) / 2f;
			icon.x = x + (16 - icon.width())/2f;
			PixelScene.align(icon);

			iconLabel.x = icon.x + (icon.width - iconLabel.width()) / 2f;
			iconLabel.y = icon.y + (icon.height - iconLabel.height()) / 2f + 0.5f;
			PixelScene.align(iconLabel);

			label.maxWidth((int)(width - 16 - 1));
			label.setPos(x + 17, y + (height() - label.height()) / 2f);
			PixelScene.align(label);
		}
	}

	public static class ListTitle extends Component {

		protected RenderedTextBlock label;
		protected ColorBlock bg;

		public ListTitle (String title){
			super();
			label.text(title);
		}

		@Override
		protected void createChildren() {
			// Add transparent background to help with rendering
			bg = new ColorBlock(1, 1, 0x00000000);
			add(bg);

			label = PixelScene.renderTextBlock( 8 );
			label.hardlight(Window.TITLE_COLOR);
			add( label );
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;
			bg.size(width, height());

			label.maxWidth((int)(width - 1));
			label.align(RenderedTextBlock.CENTER_ALIGN);
			label.setPos(x + (width - label.width())/2f, y + 2);
			PixelScene.align(label);
		}

		@Override
		public float height() {
			return label.height() + 4;
		}

	}


}
