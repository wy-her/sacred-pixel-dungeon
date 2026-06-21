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

import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.sacredpixel.sacredpixeldungeon.ui.TalentButton;
import com.sacredpixel.sacredpixeldungeon.ui.TalentsPane;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndInfoArmorAbility extends WndTitledMessage {

	private TalentsPane.TalentTierPane talentPane;

	public WndInfoArmorAbility(HeroClass cls, ArmorAbility ability){
		super( new HeroIcon(ability), Messages.titleCase(ability.name()), ability.desc());

		ArrayList<LinkedHashMap<Talent, Integer>> talentList = new ArrayList<>();
		Talent.initArmorTalents(ability, talentList);

		Ratmogrify.useRatroicEnergy = ability instanceof Ratmogrify;
		talentPane = new TalentsPane.TalentTierPane(talentList.get(3), 4, TalentButton.Mode.INFO);
		talentPane.title.text( Messages.titleCase(Messages.get(WndHeroInfo.class, "talents")));
		talentPane.setRect(0, height + 5, width, talentPane.height());
		add(talentPane);
		resize(width, (int) talentPane.bottom());

		// Add talent buttons to focusable list for keyboard navigation
		for (TalentButton btn : talentPane.buttons) {
			addFocusableButton(btn);
		}
	}

	@Override
	protected float targetHeight() {
		return super.targetHeight()-40;
	}

	@Override
	public void onConfirm() {
		// If there's a focused button, activate it instead of closing
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			activateFocused();
		}
		// Don't close the window on Enter when there are focusable elements
	}
}
