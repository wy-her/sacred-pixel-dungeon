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
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class ScrollingGridPane extends ScrollPane {

	private ArrayList<Component> items = new ArrayList<>();
	private ArrayList<ColorBlock> separators = new ArrayList<>();
	private ArrayList<ColorBlock> hSeparators = new ArrayList<>();

	private static final int ITEM_SIZE	= 17;
	private static final int MIN_GROUP_SIZE = 4*(ITEM_SIZE+1);

	private boolean allowHorizontalGrouping = true;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private int focusedItemIndex = -1;
	private ArrayList<GridItem> gridItems = new ArrayList<>();
	private boolean keyboardEnabled = true;
	private boolean sectionNavigationMode = false; // Up/Down moves between sections, Left/Right within section

	public void setAllowHorizontalGrouping(boolean allow){
		this.allowHorizontalGrouping = allow;
	}

	public void setKeyboardEnabled(boolean enabled) {
		this.keyboardEnabled = enabled;
		if (!enabled) {
			clearFocus();
		}
	}

	public boolean isKeyboardEnabled() {
		return keyboardEnabled;
	}

	public void setSectionNavigationMode(boolean enabled) {
		this.sectionNavigationMode = enabled;
	}

	public boolean isSectionNavigationMode() {
		return sectionNavigationMode;
	}

	public ScrollingGridPane(){
		super(new Component());

		// Setup keyboard navigation
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed || gridItems.isEmpty()) return false;

				// Check if keyboard navigation is enabled
				if (!keyboardEnabled) return false;

				// Check visibility - don't handle keys if not visible/active
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
				if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE) {
					moveFocusGrid(0, -1);
					return true;
				} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE) {
					moveFocusGrid(0, 1);
					return true;
				}
				// Left/Right navigation
				else if (action == SPDAction.W) {
					moveFocusGrid(-1, 0);
					return true;
				} else if (action == SPDAction.E) {
					moveFocusGrid(1, 0);
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

	@Override
	public void onClick(float x, float y) {
		// Don't process clicks if blocked
		if (shouldBlockClicks()) {
			return;
		}
		for (Component item : items) {
			if ((item instanceof ScrollingGridPane.GridItem) && ((ScrollingGridPane.GridItem) item).onClick( x, y )) {
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

	public void addItem( ScrollingGridPane.GridItem item ){
		content.add(item);
		items.add(item);
		gridItems.add(item);
	}

	public void addHeader( String text ){
		addHeader( text, 7, false );
	}

	public void addHeader( String text, int size, boolean center ){
		GridHeader header = new GridHeader(text, size, center);
		content.add(header);
		items.add(header);
	}

	@Override
	public synchronized void clear() {
		content.clear();
		items.clear();
		separators.clear();
		hSeparators.clear();
		gridItems.clear();
		focusedItemIndex = -1;
	}

	@Override
	protected void layout() {

		float left = 0;
		float top = 0;

		int sepsUsed = 0;
		int hSepsUsed = 0;
		boolean hadContent = false; //tracks whether we've placed any group yet (to skip first hSep)

		//these variables help control logic for laying out multiple grid groups on one line
		boolean freshRow = true; //whether the previous group is still on its first row
		boolean lastWasSmallheader = false; //whether the last UI element was a header on its own
		float widthThisGroup = 0; //how wide the current group is (we use a min of 3 items)

		//track the last vertical separator so we can update its height after the group is fully laid out
		ColorBlock lastVSep = null;
		float lastVSepY = 0;

		for (int i = 0; i < items.size(); i++){
			Component item = items.get(i);
			if (item instanceof GridHeader){
				//we can sometimes get two smaller headers next to each other if a group has no items in it
				//so we need to treat it as if there were grid items for proper layout
				if (left > 0 || lastWasSmallheader){

					//update the previous vertical separator to cover the full height of the group that just ended
					if (lastVSep != null){
						float groupBottom = (left > 0) ? top + ITEM_SIZE + 2 : top + 1;
						lastVSep.size(1, groupBottom - lastVSepY);
						lastVSep = null;
					}

					//this bit of logic exists so that multiple headers can be on one row
					// if all of their groups have a small number of items, with a min space for 3
					float spacing = Math.max(0, MIN_GROUP_SIZE - widthThisGroup);
					float spaceLeft = width() - (left + spacing);
					int spaceReq = 0;
					for (int j = i+1; j < items.size(); j++){
						if (items.get(j) instanceof GridItem){
							spaceReq += ITEM_SIZE+1;
						} else {
							break;
						}
					}
					spaceReq = Math.max(spaceReq, MIN_GROUP_SIZE);
					if (allowHorizontalGrouping && !((GridHeader) item).center && freshRow && spaceLeft >= spaceReq){
						//snap to next equal-width column
						int maxGroups = Math.max(2, (int)(width() / MIN_GROUP_SIZE));
						float colWidth = width() / (float)maxGroups;
						//find which column slot the current left falls into, advance to next
						int nextCol = (int)Math.ceil((left + spacing) / colWidth);
						if (nextCol >= maxGroups) nextCol = maxGroups - 1;
						left = nextCol * colWidth;
						top -= item.height()+1;
						ColorBlock sep;
						if (separators.size() > sepsUsed){
							sep = separators.get(sepsUsed++);
						} else {
							sep = new ColorBlock(1, 1, 0xFF222222);
							separators.add(sep);
							content.add(sep);
							sepsUsed++;
						}
						//set initial size; will be updated when the group ends
						sep.size(1, item.height()+1+ITEM_SIZE);
						sep.x = left-1;
						sep.y = top;
						lastVSep = sep;
						lastVSepY = top;
					} else {
						left = 0;
						top += ITEM_SIZE + 2;
						freshRow = true;

						//add horizontal separator between rows of groups
						if (hadContent) {
							ColorBlock hSep;
							if (hSeparators.size() > hSepsUsed){
								hSep = hSeparators.get(hSepsUsed++);
							} else {
								hSep = new ColorBlock(1, 1, 0xFF222222);
								hSeparators.add(hSep);
								content.add(hSep);
								hSepsUsed++;
							}
							hSep.size(width(), 1);
							hSep.x = 0;
							hSep.y = top - 1;
						}
					}
				}
				item.setRect(left, top, width(), item.height());
				top += item.height()+1;
				widthThisGroup = 0;
				hadContent = true;

				if (!((GridHeader) item).center){
					lastWasSmallheader = true;
				} else {
					lastWasSmallheader = false;
				}

			} else if (item instanceof GridItem){
				if (left + ITEM_SIZE > width()) {
					left = 0;
					widthThisGroup = 0;
					top += ITEM_SIZE+1;
					freshRow = false;
				}
				item.setRect(left, top, ITEM_SIZE, ITEM_SIZE);
				left += ITEM_SIZE+1;
				widthThisGroup += ITEM_SIZE+1;
				lastWasSmallheader = false;
			}

		}

		//update the last vertical separator if there is one still pending
		if (lastVSep != null){
			float groupBottom = (left > 0) ? top + ITEM_SIZE : top;
			lastVSep.size(1, groupBottom - lastVSepY);
			lastVSep = null;
		}

		if (left > 0){
			left = 0;
			top += ITEM_SIZE+1;
		}

		while (separators.size() > sepsUsed){
			ColorBlock sep = separators.remove(sepsUsed);
			content.remove(sep);
		}

		while (hSeparators.size() > hSepsUsed){
			ColorBlock hSep = hSeparators.remove(hSepsUsed);
			content.remove(hSep);
		}

		content.setSize(width, top);
		super.layout();
	}

	// Keyboard navigation methods

	// Helper to get the subsection index for a grid item (based on preceding headers in items list)
	private int getSubsectionIndex(int gridItemIndex) {
		// Map gridItemIndex back to the items list position, counting headers before it
		int gridCount = 0;
		int headerCount = 0;
		for (int i = 0; i < items.size(); i++) {
			Component item = items.get(i);
			if (item instanceof GridHeader) {
				headerCount++;
			} else if (item instanceof GridItem) {
				if (gridCount == gridItemIndex) {
					return headerCount;
				}
				gridCount++;
			}
		}
		return headerCount;
	}

	// Get the first grid item index for a given subsection (header index)
	private int getFirstItemOfSubsection(int subsectionIndex) {
		int headerCount = 0;
		int gridCount = 0;
		for (int i = 0; i < items.size(); i++) {
			Component item = items.get(i);
			if (item instanceof GridHeader) {
				headerCount++;
			} else if (item instanceof GridItem) {
				if (headerCount == subsectionIndex) {
					return gridCount;
				}
				gridCount++;
			}
		}
		return -1;
	}

	// Get the last grid item index for a given subsection (header index)
	private int getLastItemOfSubsection(int subsectionIndex) {
		int headerCount = 0;
		int gridCount = 0;
		int lastInSection = -1;
		for (int i = 0; i < items.size(); i++) {
			Component item = items.get(i);
			if (item instanceof GridHeader) {
				if (headerCount == subsectionIndex && lastInSection != -1) {
					return lastInSection;
				}
				headerCount++;
			} else if (item instanceof GridItem) {
				if (headerCount == subsectionIndex) {
					lastInSection = gridCount;
				}
				gridCount++;
			}
		}
		return lastInSection;
	}

	// Get total number of subsections (headers)
	private int getSubsectionCount() {
		int count = 0;
		for (Component item : items) {
			if (item instanceof GridHeader) {
				count++;
			}
		}
		return count;
	}

	// Section-based navigation for NotesTab (called directly from WndJournal)
	public void moveFocusBySection(int direction) {
		if (gridItems.isEmpty()) return;

		// Clear previous highlight
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			gridItems.get(focusedItemIndex).setFocused(false);
		}

		if (focusedItemIndex == -1) {
			// First focus - select first item that has a section
			int firstSubsection = 1; // Skip title/desc headers
			int firstItem = -1;
			int subsectionCount = getSubsectionCount();
			while (firstSubsection <= subsectionCount && firstItem == -1) {
				firstItem = getFirstItemOfSubsection(firstSubsection);
				firstSubsection++;
			}
			focusedItemIndex = (firstItem != -1) ? firstItem : 0;
		} else {
			// Move to next/previous section
			int currentSubsection = getSubsectionIndex(focusedItemIndex);
			int subsectionCount = getSubsectionCount();
			int targetSubsection = currentSubsection;
			int firstItem = -1;
			int attempts = 0;

			// Subsection indices range from 1 to subsectionCount (items come after headers)
			// Keep searching for a section with items, skipping empty sections
			while (attempts <= subsectionCount) {
				if (direction < 0) {
					// Up - move to previous section
					targetSubsection = targetSubsection - 1;
					if (targetSubsection < 1) {
						targetSubsection = subsectionCount;  // Wrap to last possible section
					}
				} else {
					// Down - move to next section
					targetSubsection = targetSubsection + 1;
					if (targetSubsection > subsectionCount) {
						targetSubsection = 1;  // Wrap to first possible section (skip header-only sections 0)
					}
				}

				firstItem = getFirstItemOfSubsection(targetSubsection);
				if (firstItem != -1) {
					break;
				}
				attempts++;
			}

			if (firstItem != -1) {
				focusedItemIndex = firstItem;
			}
		}

		// Apply highlight to new focused item
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			gridItems.get(focusedItemIndex).setFocused(true);
			scrollToItem(focusedItemIndex);
		}
	}

	// Move focus within current section (left/right)
	public void moveFocusWithinSection(int direction) {
		if (gridItems.isEmpty() || focusedItemIndex == -1) return;

		// Clear previous highlight
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			gridItems.get(focusedItemIndex).setFocused(false);
		}

		int currentSubsection = getSubsectionIndex(focusedItemIndex);
		int firstInSection = getFirstItemOfSubsection(currentSubsection);
		int lastInSection = getLastItemOfSubsection(currentSubsection);

		if (firstInSection != -1 && lastInSection != -1) {
			if (direction > 0) {
				// Move right
				if (focusedItemIndex < lastInSection) {
					focusedItemIndex++;
				} else {
					focusedItemIndex = firstInSection; // Wrap
				}
			} else {
				// Move left
				if (focusedItemIndex > firstInSection) {
					focusedItemIndex--;
				} else {
					focusedItemIndex = lastInSection; // Wrap
				}
			}
		}

		// Apply highlight to new focused item
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			gridItems.get(focusedItemIndex).setFocused(true);
			scrollToItem(focusedItemIndex);
		}
	}

	public void moveFocusGrid(int dx, int dy) {
		if (gridItems.isEmpty()) return;

		// Clear previous highlight
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			gridItems.get(focusedItemIndex).setFocused(false);
		}

		if (focusedItemIndex == -1) {
			// First focus - select first item
			focusedItemIndex = 0;
		} else if (sectionNavigationMode) {
			// Section navigation mode: Up/Down moves between sections, Left/Right within section
			int currentSubsection = getSubsectionIndex(focusedItemIndex);

			if (dx != 0) {
				// Horizontal movement - move within current section
				int firstInSection = getFirstItemOfSubsection(currentSubsection);
				int lastInSection = getLastItemOfSubsection(currentSubsection);

				if (firstInSection != -1 && lastInSection != -1) {
					if (dx > 0) {
						// Move right
						if (focusedItemIndex < lastInSection) {
							focusedItemIndex++;
						} else {
							// Wrap to first item in section
							focusedItemIndex = firstInSection;
						}
					} else {
						// Move left
						if (focusedItemIndex > firstInSection) {
							focusedItemIndex--;
						} else {
							// Wrap to last item in section
							focusedItemIndex = lastInSection;
						}
					}
				}
			} else if (dy != 0) {
				// Vertical movement - move to next/previous section
				int subsectionCount = getSubsectionCount();
				int targetSubsection = currentSubsection;
				int firstItem = -1;
				int attempts = 0;

				// Keep searching for a section with items, skipping empty sections
				while (attempts < subsectionCount) {
					if (dy < 0) {
						// Up - move to previous section (higher floor = lower subsection index in notes)
						targetSubsection = targetSubsection - 1;
						if (targetSubsection < 0) {
							targetSubsection = subsectionCount - 1; // Wrap to last
						}
					} else {
						// Down - move to next section (lower floor = higher subsection index in notes)
						targetSubsection = targetSubsection + 1;
						if (targetSubsection >= subsectionCount) {
							targetSubsection = 0; // Wrap to first
						}
					}

					firstItem = getFirstItemOfSubsection(targetSubsection);
					if (firstItem != -1) {
						break; // Found a section with items
					}
					attempts++;
				}

				if (firstItem != -1) {
					focusedItemIndex = firstItem;
				}
			}
		} else {
			// Standard grid navigation mode
			GridItem currentItem = gridItems.get(focusedItemIndex);
			float currentY = currentItem.top();
			float currentX = currentItem.left();
			int currentSubsection = getSubsectionIndex(focusedItemIndex);

			if (dx != 0) {
				// Horizontal movement - find items on same row
				float rowY = currentY;
				int nextIndex = -1;

				if (dx > 0) {
					// Move right - find next item on same row
					for (int i = focusedItemIndex + 1; i < gridItems.size(); i++) {
						if (Math.abs(gridItems.get(i).top() - rowY) < 2) {
							nextIndex = i;
							break;
						} else {
							break; // Different row
						}
					}
					if (nextIndex == -1) {
						// Wrap to first item on this row
						for (int i = 0; i < focusedItemIndex; i++) {
							if (Math.abs(gridItems.get(i).top() - rowY) < 2) {
								nextIndex = i;
								break;
							}
						}
					}
				} else {
					// Move left - find previous item on same row
					for (int i = focusedItemIndex - 1; i >= 0; i--) {
						if (Math.abs(gridItems.get(i).top() - rowY) < 2) {
							nextIndex = i;
							break;
						} else {
							break; // Different row
						}
					}
					if (nextIndex == -1) {
						// Wrap to last item on this row
						for (int i = gridItems.size() - 1; i > focusedItemIndex; i--) {
							if (Math.abs(gridItems.get(i).top() - rowY) < 2) {
								nextIndex = i;
								break;
							}
						}
					}
				}

				if (nextIndex != -1) {
					focusedItemIndex = nextIndex;
				}
			} else if (dy != 0) {
				// Vertical movement
				float targetY = dy > 0 ? Float.MAX_VALUE : Float.MIN_VALUE;
				int candidateIndex = -1;

				// Find the next row's Y position
				for (int i = 0; i < gridItems.size(); i++) {
					float itemY = gridItems.get(i).top();
					if (dy > 0) {
						// Moving down - find first row below current
						if (itemY > currentY + 2 && itemY < targetY) {
							targetY = itemY;
						}
					} else {
						// Moving up - find first row above current
						if (itemY < currentY - 2 && itemY > targetY) {
							targetY = itemY;
						}
					}
				}

				if (targetY != Float.MAX_VALUE && targetY != Float.MIN_VALUE) {
					// Found a target row - check if we're crossing subsection boundary
					// First, find the first item on the target row
					int firstOnTargetRow = -1;
					for (int i = 0; i < gridItems.size(); i++) {
						if (Math.abs(gridItems.get(i).top() - targetY) < 2) {
							firstOnTargetRow = i;
							break;
						}
					}

					if (firstOnTargetRow != -1) {
						int targetSubsection = getSubsectionIndex(firstOnTargetRow);

						if (targetSubsection != currentSubsection) {
							// Crossing subsection boundary - go to first item of new subsection
							candidateIndex = firstOnTargetRow;
						} else {
							// Same subsection - try to maintain horizontal position
							// Find item on target row closest to current X position
							int bestMatch = firstOnTargetRow;
							float bestDist = Math.abs(gridItems.get(firstOnTargetRow).left() - currentX);

							for (int i = firstOnTargetRow + 1; i < gridItems.size(); i++) {
								if (Math.abs(gridItems.get(i).top() - targetY) < 2) {
									float dist = Math.abs(gridItems.get(i).left() - currentX);
									if (dist < bestDist) {
										bestDist = dist;
										bestMatch = i;
									}
								} else {
									break; // Different row
								}
							}
							candidateIndex = bestMatch;
						}
					}
				}

				if (candidateIndex != -1) {
					focusedItemIndex = candidateIndex;
				} else {
					// Wrap around
					if (dy > 0) {
						focusedItemIndex = 0; // Go to first item
					} else {
						// Find first item on last row
						float lastY = gridItems.get(gridItems.size() - 1).top();
						for (int i = 0; i < gridItems.size(); i++) {
							if (Math.abs(gridItems.get(i).top() - lastY) < 2) {
								focusedItemIndex = i;
								break;
							}
						}
					}
				}
			}
		}

		// Apply highlight to new focused item
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			gridItems.get(focusedItemIndex).setFocused(true);
			scrollToItem(focusedItemIndex);
		}
	}

	private void scrollToItem(int index) {
		if (index >= 0 && index < gridItems.size()) {
			GridItem item = gridItems.get(index);
			// Scroll to make the item visible
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

	public void activateFocusedItem() {
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			GridItem item = gridItems.get(focusedItemIndex);
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			// Call onClick with coordinates inside the item
			item.onClick(item.left() + item.width()/2, item.top() + item.height()/2);
		}
	}

	public void clearFocus() {
		if (focusedItemIndex >= 0 && focusedItemIndex < gridItems.size()) {
			gridItems.get(focusedItemIndex).setFocused(false);
		}
		focusedItemIndex = -1;
	}

	public static class GridItem extends Component {

		protected Image icon;

		protected Visual secondIcon;

		protected ColorBlock bg;

		private boolean focused = false;
		private float savedBgR = 1f, savedBgG = 1f, savedBgB = 1f;
		private boolean iconDarkened = false;

		public GridItem( Image icon ) {
			super();

			if (icon instanceof ItemSprite){
				this.icon = new ItemSprite();
			} else {
				this.icon = new Image();
			}
			this.icon.copy(icon);
			// Check if icon was darkened (lightness 0)
			iconDarkened = (icon.rm == 0 && icon.gm == 0 && icon.bm == 0);
			add(this.icon);
		}

		public void addSecondIcon( Visual icon ){
			secondIcon = icon;
			add(secondIcon);
			layout();
		}

		public void hardLightBG( float r, float g, float b ){
			savedBgR = r;
			savedBgG = g;
			savedBgB = b;
			if (!focused) {
				bg.hardlight(r, g, b);
			}
		}

		public void setFocused(boolean focused) {
			this.focused = focused;
			if (focused) {
				bg.brightness(1.5f);
				// Only brighten icon if it wasn't darkened (unseen items)
				if (!iconDarkened) {
					icon.brightness(1.3f);
				}
			} else {
				bg.resetColor();
				bg.hardlight(savedBgR, savedBgG, savedBgB);
				// Restore icon to its original state
				if (iconDarkened) {
					icon.lightness(0);
				} else {
					icon.resetColor();
				}
			}
		}

		public boolean onClick( float x, float y ){
			return false;
		}

		@Override
		protected void createChildren() {
			bg = new ColorBlock( 1, 1, 0x9953564D);
			add(bg);
		}

		@Override
		protected void layout() {

			bg.x = x;
			bg.y = y;
			bg.size(width(), height());

			icon.y = y + (height() - icon.height()) / 2f;
			icon.x = x + (width() - icon.width())/2f;
			PixelScene.align(icon);

			if (secondIcon != null){
				secondIcon.x = x + width()-secondIcon.width();
				secondIcon.y = y;
			}

		}

	}

	public static class GridHeader extends Component {

		protected RenderedTextBlock text;
		boolean center;

		public GridHeader( String text ){
			this(text, 7, false);
		}

		public GridHeader( String text, int size, boolean center ){
			super();

			this.center = center;
			this.text = PixelScene.renderTextBlock(text, size);
			add(this.text);

		}

		@Override
		protected void createChildren() {
			super.createChildren();
		}

		@Override
		protected void layout() {
			super.layout();

			if (center){
				text.align(RenderedTextBlock.CENTER_ALIGN);
				text.maxWidth((int)width());
				text.setPos(x + (width() - text.width()) / 2, y+2);
			} else {
				text.maxWidth((int)width());
				text.setPos(x, y+2);
			}
		}

		@Override
		public float height() {
			if (center){
				return text.height() + 4;
			} else {
				return text.height() + 4;
			}
		}
	}

}
