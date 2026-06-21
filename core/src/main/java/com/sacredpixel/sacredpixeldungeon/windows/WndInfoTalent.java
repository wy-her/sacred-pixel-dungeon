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

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.TalentIcon;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.utils.Callback;

public class WndInfoTalent extends Window {

	private static final float GAP	= 2;

	private static final int WIDTH_MIN = 149;
	private static final int WIDTH_MAX = 251;
	private static final int BTN_HEIGHT = 16;

	public WndInfoTalent(Talent talent, int points, TalentButtonCallback buttonCallback){
		super();

		int width = WIDTH_MIN;

		IconTitle titlebar = new IconTitle();

		titlebar.icon( new TalentIcon( talent ) );
		String title = Messages.titleCase(talent.title());
		if (points > 0){
			title += " +" + points;
		}
		titlebar.label( title, Window.TITLE_COLOR );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		boolean metaDesc = (buttonCallback != null && buttonCallback.metamorphDesc()) ||
				(Dungeon.hero != null && Dungeon.hero.metamorphedTalents.containsValue(talent));

		RenderedTextBlock txtInfo = PixelScene.renderTextBlock(talent.desc(metaDesc), 6);
		txtInfo.maxWidth(width);
		txtInfo.setPos(titlebar.left(), titlebar.bottom() + 2*GAP);
		add( txtInfo );

		while (PixelScene.landscape()
				&& txtInfo.height() > 120
				&& width < WIDTH_MAX){
			width += 20;
			txtInfo.maxWidth(width);
		}
		titlebar.setRect( 0, 0, width, 0 );
		resize( width, (int)(txtInfo.bottom() + GAP) );

		if (buttonCallback != null) {
			RedButton button = new RedButton( buttonCallback.prompt() ) {
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					buttonCallback.call();
				}
			};
			button.setRect(0, txtInfo.bottom() + 2*GAP, width, BTN_HEIGHT);
			add(button);
			addFocusableButton(button);
			resize( width, (int)button.bottom()+1 );
		}

	}

	public static abstract class TalentButtonCallback implements Callback {

		public abstract String prompt();

		public boolean metamorphDesc(){
			return false;
		}

	}

}
