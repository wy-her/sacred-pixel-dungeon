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
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.items.TengusMask;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndChooseSubclass extends Window {

	private static final int WIDTH_MIN = 149;
	private static final int WIDTH_MAX = 251;
	private static final float GAP = 2;

	public WndChooseSubclass(final TengusMask tome, final Hero hero ) {

		super();

		int width = WIDTH_MIN;

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( tome.image(), null ) );
		titlebar.label( tome.name() );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock( 6 );
		message.text( Messages.get(this, "message"), width );
		message.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( message );

		float pos = message.bottom() + 3*GAP;

		ArrayList<RedButton> buttons = new ArrayList<>();
		ArrayList<IconButton> infoButtons = new ArrayList<>();

		for (HeroSubClass subCls : hero.heroClass.subClasses()){
			RedButton btnCls = new RedButton( subCls.shortDesc(), 6 ) {
				@Override
				protected void onClick() {
					GameScene.show(new WndOptions(new HeroIcon(subCls),
							Messages.titleCase(subCls.title()),
							Messages.get(WndChooseSubclass.this, "are_you_sure"),
							Messages.get(WndChooseSubclass.this, "yes"),
							Messages.get(WndChooseSubclass.this, "no")){
						@Override
						protected void onSelect(int index) {
							hide();
							if (index == 0 && WndChooseSubclass.this.parent != null){
								WndChooseSubclass.this.hide();
								tome.choose( subCls );
							}
						}
					});
				}
			};
			btnCls.leftJustify = true;
			btnCls.multiline = true;
			btnCls.setSize(width-20, btnCls.reqHeight()+2);
			btnCls.setRect( 0, pos, width-20, btnCls.reqHeight()+2);
			add( btnCls );
			addFocusableButton( btnCls );
			buttons.add(btnCls);

			IconButton clsInfo = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					GameScene.show(new WndInfoSubclass(Dungeon.hero.heroClass, subCls));
				}
			};
			clsInfo.setRect(width-20, btnCls.top() + (btnCls.height()-20)/2, 20, 20);
			add(clsInfo);
			addFocusableButton(clsInfo);
			infoButtons.add(clsInfo);

			pos = btnCls.bottom() + GAP;
		}

		RedButton btnCancel = new RedButton( Messages.get(this, "cancel") ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setRect( 0, pos, width, 18 );
		add( btnCancel );
		addFocusableButton( btnCancel );

		float totalHeight = btnCancel.bottom();

		// Dynamic width expansion in landscape mode
		while (PixelScene.landscape()
				&& totalHeight > targetHeight()
				&& width < WIDTH_MAX) {
			width += 20;

			// Re-layout titlebar
			titlebar.setRect(0, 0, width, 0);

			// Re-layout message
			message.maxWidth(width);
			message.setPos(titlebar.left(), titlebar.bottom() + GAP);

			pos = message.bottom() + 3*GAP;

			// Re-layout subclass buttons
			for (int i = 0; i < buttons.size(); i++) {
				RedButton btnCls = buttons.get(i);
				btnCls.setSize(width-20, btnCls.reqHeight()+2);
				btnCls.setRect(0, pos, width-20, btnCls.reqHeight()+2);

				IconButton clsInfo = infoButtons.get(i);
				clsInfo.setRect(width-20, btnCls.top() + (btnCls.height()-20)/2, 20, 20);

				pos = btnCls.bottom() + GAP;
			}

			// Re-layout cancel button
			btnCancel.setRect(0, pos, width, 18);

			totalHeight = btnCancel.bottom();
		}

		resize( width, (int)btnCancel.bottom() );
	}

	protected float targetHeight() {
		return PixelScene.MIN_HEIGHT_L - 10;
	}
}
