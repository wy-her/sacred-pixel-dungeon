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

import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.Window;

public class WndSupportPrompt extends Window {

	protected static final int WIDTH_P    = 149;
	protected static final int WIDTH_L    = 251;
	private static final int BTN_HEIGHT   = 16;

	public WndSupportPrompt(){

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle title = new IconTitle(Icons.get(Icons.SHPX), Messages.get(WndSupportPrompt.class, "title"));
		title.setRect( 0, 0, width, 0 );
		add(title);

		// Message content removed

		RedButton close = new RedButton(Messages.get(this, "close")){
			@Override
			protected void onClick() {
				super.onClick();
				SPDSettings.supportNagged(true);
				WndSupportPrompt.super.hide();
			}
		};
		close.setRect(0, title.bottom() + 4, width, BTN_HEIGHT);
		add(close);
		addFocusableButton(close);

		resize(width, (int)close.bottom());

	}

	@Override
	public void hide() {
		//do nothing, have to close via the close button
	}
}
