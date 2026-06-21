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
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndOptions extends Window {

	protected static final int WIDTH_MIN = 149;
	protected static final int WIDTH_MAX = 251;

	protected static final int MARGIN 		= 2;
	protected static final int BUTTON_HEIGHT	= 16;

	// For dynamic width recalculation
	protected Component titlebar;
	protected boolean hasIconTitle = false;

	public WndOptions(Image icon, String title, String message, String... options) {
		super();

		if (title != null) {
			IconTitle tfTitle = new IconTitle(icon, title);
			titlebar = tfTitle;
			hasIconTitle = true;
			add(tfTitle);
		}

		layoutBody(message, options);
	}

	public WndOptions( String title, String message, String... options ) {
		super();

		if (title != null) {
			RenderedTextBlock tfTitle = PixelScene.renderTextBlock(title, 8);
			tfTitle.hardlight(TITLE_COLOR);
			titlebar = tfTitle;
			hasIconTitle = false;
			add(tfTitle);
		}

		layoutBody(message, options);
	}

	protected void layoutBody(String message, String... options){
		int width = WIDTH_MIN;

		// Layout title
		float pos = MARGIN;
		if (titlebar != null) {
			if (hasIconTitle) {
				((IconTitle)titlebar).setRect(0, pos, width, 0);
			} else {
				RenderedTextBlock tfTitle = (RenderedTextBlock) titlebar;
				tfTitle.maxWidth(width - MARGIN * 2);
				tfTitle.setPos((width - tfTitle.width()) / 2f, pos);
				PixelScene.align(tfTitle);
			}
			pos = titlebar.bottom() + 2*MARGIN;
		}

		RenderedTextBlock tfMessage = PixelScene.renderTextBlock( 6 );
		tfMessage.text(message, width);
		tfMessage.setPos( 0, pos );
		add( tfMessage );

		pos = tfMessage.bottom() + 2*MARGIN;

		int btnTextSize = 7;

		ArrayList<RedButton> buttons = new ArrayList<>();
		ArrayList<IconButton> infoButtons = new ArrayList<>();

		for (int i=0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton( options[i], btnTextSize ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			if (hasIcon(i)) btn.icon(getIcon(i));
			btn.multiline = true;
			add( btn );
			addFocusableButton( btn );
			buttons.add(btn);

			if (!hasInfo(i)) {
				btn.setRect(0, pos, width, BUTTON_HEIGHT);
			} else {
				btn.setRect(0, pos, width - BUTTON_HEIGHT, BUTTON_HEIGHT);
				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						onInfo( index );
					}
				};
				info.setRect(width-BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
				add(info);
				addFocusable(info);
				infoButtons.add(info);
			}

			btn.enable(enabled(i));

			pos += BUTTON_HEIGHT + MARGIN;
		}

		float totalHeight = pos - MARGIN;

		// Dynamic width expansion in landscape mode
		while (PixelScene.landscape()
				&& totalHeight > targetHeight()
				&& width < WIDTH_MAX) {
			width += 20;

			// Re-layout title
			pos = MARGIN;
			if (titlebar != null) {
				if (hasIconTitle) {
					((IconTitle)titlebar).setRect(0, pos, width, 0);
				} else {
					RenderedTextBlock tfTitle = (RenderedTextBlock) titlebar;
					tfTitle.maxWidth(width - MARGIN * 2);
					tfTitle.setPos((width - tfTitle.width()) / 2f, pos);
					PixelScene.align(tfTitle);
				}
				pos = titlebar.bottom() + 2*MARGIN;
			}

			// Re-layout message
			tfMessage.maxWidth(width);
			tfMessage.setPos(0, pos);
			pos = tfMessage.bottom() + 2*MARGIN;

			// Re-layout buttons
			int infoIdx = 0;
			for (int i = 0; i < buttons.size(); i++) {
				RedButton btn = buttons.get(i);
				if (!hasInfo(i)) {
					btn.setRect(0, pos, width, BUTTON_HEIGHT);
				} else {
					btn.setRect(0, pos, width - BUTTON_HEIGHT, BUTTON_HEIGHT);
					if (infoIdx < infoButtons.size()) {
						infoButtons.get(infoIdx).setRect(width - BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
						infoIdx++;
					}
				}
				pos += BUTTON_HEIGHT + MARGIN;
			}

			totalHeight = pos - MARGIN;
		}

		resize( width, (int)totalHeight );
	}

	protected float targetHeight() {
		return PixelScene.MIN_HEIGHT_L - 10;
	}

	protected boolean enabled( int index ){
		return true;
	}

	protected void onSelect( int index ) {}

	protected boolean hasInfo( int index ) {
		return false;
	}

	protected void onInfo( int index ) {}

	protected boolean hasIcon( int index ) {
		return false;
	}

	protected Image getIcon( int index ) {
		return null;
	}
}
