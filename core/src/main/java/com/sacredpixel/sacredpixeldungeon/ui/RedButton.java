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

import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;

public class RedButton extends StyledButton {

	private int initSize;
	private int savedTextColor = -1;
	private int curSize;
	private float adjustedAtWidth = -1;

	public RedButton( String label ) {
		this(label, 7);
	}

	@Override
	public void textColor(int value) {
		savedTextColor = value;
		super.textColor(value);
	}

	public RedButton( String label, int size ){
		super( Chrome.Type.RED_BUTTON, label, size);
		this.initSize = size;
		this.curSize = size;
	}

	@Override
	protected void layout() {
		super.layout();

		if (text == null || text.text().equals("") || multiline || width <= 0) return;

		// Only re-evaluate if width changed since last adjustment
		if (adjustedAtWidth == width) return;

		// Save text color before potential re-creation

		// Reset to initial size first if width changed
		if (curSize != initSize) {
			String label = text.text();
			remove(text);
			curSize = initSize;
			text = PixelScene.renderTextBlock(label, curSize);
			if (savedTextColor != -1) text.hardlight(savedTextColor);
			add(text);
			super.layout();
		}

		float componentWidth = (icon != null) ? icon.width() + 2 : 0;
		float availW = width - componentWidth - bg.marginHor() + 2;
		int minSize = Math.max(initSize - 2, 5);

		if (text.width() > availW && curSize > minSize) {
			String label = text.text();
			remove(text);
			curSize--;
			text = PixelScene.renderTextBlock(label, curSize);
			if (savedTextColor != -1) text.hardlight(savedTextColor);
			add(text);
			super.layout();

			if (text.width() > availW && curSize > minSize) {
				remove(text);
				curSize--;
				text = PixelScene.renderTextBlock(label, curSize);
				if (savedTextColor != -1) text.hardlight(savedTextColor);
				add(text);
				super.layout();
			}
		}
		adjustedAtWidth = width;
	}

}
