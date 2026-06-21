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

import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

public class WndVictoryCongrats extends Window {

	public WndVictoryCongrats(){
		int width = PixelScene.landscape() ? 251 : 149;
		int height = 0;

		IconTitle title = new IconTitle( new ItemSprite(ItemSpriteSheet.AMULET), Messages.get(this, "title"));
		title.setRect( 0, 0, width, 0 );
		add(title);

		RenderedTextBlock text = PixelScene.renderTextBlock( Messages.get(this, "start_text"), 6 );
		text.maxWidth( width );
		text.setPos( 0, title.bottom() + 4 );
		add( text );

		height = (int)text.bottom() + 6;

		Image chalImg = Icons.CHALLENGE_COLOR.get();
		chalImg.y = height;
		chalImg.x = (16-chalImg.width())/2f;
		PixelScene.align(chalImg);
		add(chalImg);

		RenderedTextBlock chalTxt = PixelScene.renderTextBlock(Messages.get(this, "challenges"), 6);
		chalTxt.maxWidth(width - 16);
		chalTxt.setPos(16, height);
		add(chalTxt);

		if (chalTxt.height() > chalImg.height()){
			chalImg.y = chalImg.y + (chalTxt.height() - chalImg.height())/2f;
			PixelScene.align(chalImg);
		}

		height += Math.max(chalImg.height(), chalTxt.height()) + 6;

		RenderedTextBlock finalTxt = PixelScene.renderTextBlock(Messages.get(this, "thank_you"), 6);
		finalTxt.maxWidth(width);
		finalTxt.setPos(0, height);
		add(finalTxt);

		height = (int) finalTxt.bottom() + 4;

		resize(width, height);

	}

	@Override
	public void onBackPressed() {
		hide();
	}
}
