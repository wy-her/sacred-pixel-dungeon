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

import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.watabou.noosa.ColorBlock;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;

public class SupporterScene extends PixelScene {

	// Button constants removed - Patreon link button no longer used

	@Override
	public void create() {
		super.create();

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;
		RectF insets = getCommonInsets();

		int elementWidth = PixelScene.landscape() ? 202 : 120;

		ColorBlock BG = new ColorBlock(w, h, 0xFF000000);
		add(BG);

		w -= insets.right + insets.left;
		h -= insets.top + insets.bottom;

		ExitButton btnExit = new ExitButton();
		btnExit.setPos(insets.left + w - btnExit.width(), insets.top);
		add(btnExit);

		IconTitle title = new IconTitle(Icons.GOLD.get(), Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (20 - title.height()) / 2f
		);
		align(title);
		add(title);

		SupporterMessage msg = new SupporterMessage();
		msg.setSize(elementWidth, 0);
		add(msg);

		// Patreon link button removed

		float elementHeight = msg.height();

		float top = insets.top + 16 + (h - 16 - elementHeight)/2f;
		float left = insets.left + (w-elementWidth)/2f;

		msg.setPos(left, top);
		align(msg);

	}

	@Override
	protected void onBackPressed() {
		SacredPixelDungeon.switchNoFade( TitleScene.class );
	}

	private static class SupporterMessage extends Component {

		NinePatch bg;
		RenderedTextBlock text;
		Image icon;

		@Override
		protected void createChildren() {
			bg = Chrome.get(Chrome.Type.GREY_BUTTON_TR);
			add(bg);

			// Message content removed

			text = PixelScene.renderTextBlock("", 6);
			add(text);

			icon = Icons.get(Icons.SHPX);
			add(icon);

		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			text.maxWidth((int)width - bg.marginHor());
			text.setPos(x + bg.marginLeft(), y + bg.marginTop() + 1);

			icon.y = text.bottom() - icon.height() + 4;
			icon.x = x + 25;

			height = (text.bottom() + 3) - y;

			height += bg.marginBottom();

			bg.size(width, height);

		}

	}

}
