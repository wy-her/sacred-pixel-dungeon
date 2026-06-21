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
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Imp;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.quest.DwarfToken;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;

public class WndImp extends Window {
	
	private static final int WIDTH_P    = 149;
	private static final int WIDTH_L    = 251;
	private static final int MARGIN     = 2;
	private static final int BTN_HEIGHT = 16;

	public WndImp( final Imp imp, final DwarfToken tokens ) {

		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( tokens.image(), null ) );
		titlebar.label( Messages.titleCase( tokens.name() ) );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		int tokenCount = tokens.quantity();
		String messageKey = getRewardMessageKey(tokenCount);
		RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(this, messageKey), 6 );
		message.maxWidth(width);
		message.setPos(0, titlebar.bottom() + 2*MARGIN);
		add( message );

		RedButton btnReward = new RedButton( Messages.get(this, "reward") ) {
			@Override
			protected void onClick() {
				takeReward( imp, tokens, Imp.Quest.reward );
			}
		};
		btnReward.setRect( 0, message.bottom() + 2*MARGIN, width, BTN_HEIGHT );
		add( btnReward );
		addFocusableButton( btnReward );

		resize( width, (int)btnReward.bottom() );
	}

	private String getRewardMessageKey(int tokenCount) {
		if (tokenCount >= 5) {
			return "message_5"; // +3 uncursed
		} else if (tokenCount == 4) {
			return "message_4"; // +3 cursed
		} else if (tokenCount == 3) {
			return "message_3"; // +2 cursed
		} else if (tokenCount == 2) {
			return "message_2"; // +1 cursed
		} else {
			return "message_1"; // +0 cursed
		}
	}
	
	private void takeReward( Imp imp, DwarfToken tokens, Item reward ) {

		hide();

		int tokenCount = tokens.quantity();
		tokens.detachAll( Dungeon.hero.belongings.backpack );

		// Get adjusted reward based on token count
		reward = Imp.Quest.getAdjustedReward(tokenCount);
		if (reward == null) return;

		reward.identify(false);
		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i( Messages.capitalize(Messages.get(Dungeon.hero, "you_now_have", reward.name())) );
		} else {
			Dungeon.level.drop( reward, imp.pos ).sprite.drop();
		}

		imp.flee();

		Imp.Quest.complete();
	}
}
