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
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.sacredpixel.sacredpixeldungeon.items.KingsCrown;
import com.sacredpixel.sacredpixeldungeon.items.armor.Armor;
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

public class WndChooseAbility extends Window {

	private static final int WIDTH_MIN = 149;
	private static final int WIDTH_MAX = 251;
	private static final float GAP = 2;
	private static final int BTN_HEIGHT = 16;

	public WndChooseAbility(final KingsCrown crown, final Armor armor, final Hero hero){

		super();

		int width = WIDTH_MIN;

		//crown can be null if hero is choosing from armor
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( crown == null ? armor.image() : crown.image(), null ) );
		titlebar.label( Messages.titleCase(crown == null ? armor.name() : crown.name()) );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		RenderedTextBlock body = PixelScene.renderTextBlock( 6 );
		if (crown != null) {
			body.text(Messages.get(this, "message"), width);
		} else {
			body.text(Messages.get(this, "message_no_crown"), width);
		}
		body.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( body );

		float pos = body.bottom() + 3*GAP;

		ArrayList<RedButton> buttons = new ArrayList<>();
		ArrayList<IconButton> infoButtons = new ArrayList<>();

		for (ArmorAbility ability : hero.heroClass.armorAbilities()) {

			RedButton abilityButton = new RedButton(ability.shortDesc(), 6){
				@Override
				protected void onClick() {
					GameScene.show(new WndOptions( new HeroIcon( ability ),
							Messages.titleCase(ability.name()),
							Messages.get(WndChooseAbility.this, "are_you_sure"),
							Messages.get(WndChooseAbility.this, "yes"),
							Messages.get(WndChooseAbility.this, "no")){

						@Override
						protected void onSelect(int index) {
							hide();
							if (index == 0 && WndChooseAbility.this.parent != null){
								WndChooseAbility.this.hide();
								if (crown != null) {
									crown.upgradeArmor(hero, armor, ability);
								} else {
									new KingsCrown().upgradeArmor(hero, null, ability);
								}
							}
						}
					});
				}
			};
			abilityButton.leftJustify = true;
			abilityButton.multiline = true;
			abilityButton.setSize(width-20, abilityButton.reqHeight()+2);
			abilityButton.setRect(0, pos, width-20, abilityButton.reqHeight()+2);
			add(abilityButton);
			addFocusableButton(abilityButton);
			buttons.add(abilityButton);

			IconButton abilityInfo = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					GameScene.show(new WndInfoArmorAbility(Dungeon.hero.heroClass, ability));
				}
			};
			abilityInfo.setRect(width-20, abilityButton.top() + (abilityButton.height()-20)/2, 20, 20);
			add(abilityInfo);
			addFocusableButton(abilityInfo);
			infoButtons.add(abilityInfo);

			pos = abilityButton.bottom() + GAP;
		}

		RedButton cancelButton = new RedButton(Messages.get(this, "cancel")){
			@Override
			protected void onClick() {
				hide();
			}
		};
		cancelButton.setRect(0, pos, width, BTN_HEIGHT);
		add(cancelButton);
		addFocusableButton(cancelButton);
		pos = cancelButton.bottom() + GAP;

		float totalHeight = pos;

		// Dynamic width expansion in landscape mode
		while (PixelScene.landscape()
				&& totalHeight > targetHeight()
				&& width < WIDTH_MAX) {
			width += 20;

			// Re-layout titlebar
			titlebar.setRect(0, 0, width, 0);

			// Re-layout body
			body.maxWidth(width);
			body.setPos(titlebar.left(), titlebar.bottom() + GAP);

			pos = body.bottom() + 3*GAP;

			// Re-layout ability buttons
			for (int i = 0; i < buttons.size(); i++) {
				RedButton abilityButton = buttons.get(i);
				abilityButton.setSize(width-20, abilityButton.reqHeight()+2);
				abilityButton.setRect(0, pos, width-20, abilityButton.reqHeight()+2);

				IconButton abilityInfo = infoButtons.get(i);
				abilityInfo.setRect(width-20, abilityButton.top() + (abilityButton.height()-20)/2, 20, 20);

				pos = abilityButton.bottom() + GAP;
			}

			// Re-layout cancel button
			cancelButton.setRect(0, pos, width, BTN_HEIGHT);
			pos = cancelButton.bottom() + GAP;

			totalHeight = pos;
		}

		resize(width, (int)(cancelButton.bottom()));

	}

	protected float targetHeight() {
		return PixelScene.MIN_HEIGHT_L - 10;
	}

}
