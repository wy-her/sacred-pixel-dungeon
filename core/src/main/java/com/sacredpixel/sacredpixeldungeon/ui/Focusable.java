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

/**
 * Interface for UI components that can receive keyboard focus.
 * Used by Window's focus navigation system to support both Button and non-Button components.
 */
public interface Focusable {

	/**
	 * Sets the focus state of this component.
	 * When focused, the component should display a visual indicator (e.g., highlight).
	 */
	void setFocused(boolean focused);

	/**
	 * Saves the current visual state before focus changes.
	 * Called before setFocused(true) to preserve the original appearance.
	 */
	void saveFocusState();

	/**
	 * Restores the visual state saved by saveFocusState().
	 * Called when focus moves away from this component.
	 */
	void restoreFocusState();

	/**
	 * Programmatically triggers the component's primary action.
	 * For buttons, this triggers onClick(). For sliders, this may toggle focus off.
	 */
	void click();

	/**
	 * Returns whether this component is currently active/enabled.
	 * Inactive components should be skipped during focus navigation.
	 */
	boolean isActive();
}
