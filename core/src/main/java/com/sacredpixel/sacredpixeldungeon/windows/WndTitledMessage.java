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

import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class WndTitledMessage extends Window {

	protected static final int WIDTH_MIN    = 149;
	protected static final int WIDTH_MAX    = 251;
	protected static final int GAP	= 2;

	//tracks whether dialog was closed via Enter (advance) or ESC (close without advancing)
	protected boolean advanceDialog = false;

	@Override
	public void onConfirm() {
		advanceDialog = true;
		hide();
	}

	@Override
	public void onBackPressed() {
		advanceDialog = false;
		hide();
	}

	public WndTitledMessage( Image icon, String title, String message ) {

		this( new IconTitle( icon, title ), message );

	}

	public WndTitledMessage( Component titlebar, String message ) {

		super();

		int width = WIDTH_MIN;

		titlebar.setRect( 0, 0, width, 0 );
		add(titlebar);

		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		if (!useHighlighting()) text.setHightlighting(false);
		text.text( message, width );
		text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
		add( text );

		while (PixelScene.landscape()
				&& text.bottom() > targetHeight()
				&& width < WIDTH_MAX){
			width += 20;
			titlebar.setRect(0, 0, width, 0);
			text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
			text.maxWidth(width);
		}

		bringToFront(titlebar);

		resize( width, (int)text.bottom() + 2 );
	}

	protected boolean useHighlighting(){
		return true;
	}

	protected float targetHeight() {
		return PixelScene.MIN_HEIGHT_L - 10;
	}
}
