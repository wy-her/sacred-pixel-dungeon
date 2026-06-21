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
import com.sacredpixel.sacredpixeldungeon.GamesInProgress;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.InterlevelScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.scenes.StartScene;
import com.sacredpixel.sacredpixeldungeon.sprites.HeroSprite;
import com.sacredpixel.sacredpixeldungeon.ui.ActionIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;
import com.watabou.utils.DateCompat;

import java.util.Locale;

public class WndGameInProgress extends Window {
	
	private static final int WIDTH    = 149;
	private static final int BTN_HEIGHT = 16;

	private int GAP	  = 6;
	
	private float pos;
	
	public WndGameInProgress(final int slot){
		
		final GamesInProgress.Info info = GamesInProgress.check(slot);
		
		String className = null;
		if (info.subClass != HeroSubClass.NONE){
			className = info.subClass.title();
		} else {
			className = info.heroClass.title();
		}
		
		IconTitle title = new IconTitle();
		title.icon( HeroSprite.avatar(info.heroClass, info.armorTier) );
		title.label((Messages.get(this, "title", info.level, className)).toUpperCase(Locale.ENGLISH));
		title.color(Window.TITLE_COLOR);
		title.setRect( 0, 0, WIDTH, 0 );
		add(title);
		
		if (info.challenges > 0) GAP -= 2;
		
		pos = title.bottom() + GAP;
		
		if (info.challenges > 0) {
			RedButton btnChallenges = new RedButton( Messages.get(this, "challenges") ) {
				@Override
				protected void onClick() {
					Game.scene().add( new WndChallenges( info.challenges, false ) );
				}
			};
			btnChallenges.multiline = true;
			btnChallenges.setRect( 0, pos, WIDTH, 16 );
			add( btnChallenges );
			addFocusableButton( btnChallenges );

			pos = btnChallenges.bottom() + GAP;
		}
		
		pos += GAP;

		int strBonus = info.strBonus;
		if (strBonus > 0)           statSlot( Messages.get(this, "str"), info.str + " + " + strBonus );
		else if (strBonus < 0)      statSlot( Messages.get(this, "str"), info.str + " - " + -strBonus );
		else                        statSlot( Messages.get(this, "str"), info.str );
		if (info.shld > 0)  statSlot( Messages.get(this, "health"), info.hp + "+" + info.shld + "/" + info.ht );
		else                statSlot( Messages.get(this, "health"), (info.hp) + "/" + info.ht );
		statSlot( Messages.get(this, "exp"), info.exp + "/" + Hero.maxExp(info.level) );
		
		pos += GAP;
		statSlot( Messages.get(this, "gold"), DateCompat.formatNumber(info.goldCollected, Messages.locale()) );
		statSlot( Messages.get(this, "depth"), DateCompat.formatNumber(info.maxDepth, Messages.locale()) );
		if (!info.customSeed.isEmpty()){
			statSlot( Messages.get(this, "custom_seed"), "_" + info.customSeed + "_" );
		} else {
			statSlot( Messages.get(this, "dungeon_seed"), DungeonSeed.convertToCode(info.seed) );
		}
		
		pos += GAP;
		
		RedButton cont = new RedButton(Messages.get(this, "continue")){
			@Override
			protected void onClick() {
				super.onClick();
				
				GamesInProgress.curSlot = slot;
				
				Dungeon.hero = null;
				ActionIndicator.clearAction();
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
				SacredPixelDungeon.switchScene(InterlevelScene.class);
			}
		};
		
		RedButton erase = new RedButton( Messages.get(this, "erase")){
			@Override
			protected void onClick() {
				super.onClick();
				
				SacredPixelDungeon.scene().add(new WndOptions(Icons.get(Icons.WARNING),
						Messages.get(WndGameInProgress.class, "erase_warn_title"),
						Messages.get(WndGameInProgress.class, "erase_warn_body"),
						Messages.get(WndGameInProgress.class, "erase_warn_yes"),
						Messages.get(WndGameInProgress.class, "erase_warn_no") ) {
					@Override
					protected void onSelect( int index ) {
						if (index == 0) {
							Dungeon.deleteGame(slot, true);
							SacredPixelDungeon.switchNoFade(StartScene.class);
						}
					}
				} );
			}
		};

		cont.setRect(0, pos, WIDTH/2 -1, BTN_HEIGHT);
		add(cont);
		addFocusableButton(cont);

		erase.setRect(WIDTH/2 + 1, pos, WIDTH/2 - 1, BTN_HEIGHT);
		add(erase);
		addFocusableButton(erase);
		
		resize(WIDTH, (int)cont.bottom()+1);
	}
	
	private void statSlot( String label, String value ) {

		int size = 7;
		RenderedTextBlock txt;
		do {
			txt = PixelScene.renderTextBlock( label, size );
			size--;
		} while (txt.width() >= WIDTH * 0.67f);
		txt.setPos(0, pos + (6 - txt.height())/2);
		PixelScene.align(txt);
		add( txt );

		size = 7;
		do {
			txt = PixelScene.renderTextBlock( value, size );
			size--;
		} while (txt.width() >= WIDTH * 0.33f);
		txt.setPos(WIDTH * 0.67f, pos + (6 - txt.height())/2);
		PixelScene.align(txt);
		add( txt );
		
		pos += GAP + txt.height();
	}
	
	private void statSlot( String label, int value ) {
		statSlot( label, DateCompat.formatNumber( value, Messages.locale() ) );
	}
}
