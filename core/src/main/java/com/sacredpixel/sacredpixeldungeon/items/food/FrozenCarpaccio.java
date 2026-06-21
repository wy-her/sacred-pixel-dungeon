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

package com.sacredpixel.sacredpixeldungeon.items.food;

import com.sacredpixel.sacredpixeldungeon.actors.buffs.Barkskin;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Hunger;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Invisibility;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.effects.FloatingText;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfHealing;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.sprites.CharSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class FrozenCarpaccio extends Food {

	{
		image = ItemSpriteSheet.CARPACCIO;
		energy = Hunger.HUNGRY/2f;
	}
	
	@Override
	protected void satisfy(Hero hero) {
		super.satisfy(hero);
		effect(hero);
	}
	
	public int value() {
		return 10 * quantity;
	}

	public static void effect(Hero hero){
		switch (Random.Int( 5 )) {
			case 0:
				GLog.i( Messages.get(FrozenCarpaccio.class, "invis") );
				Buff.affect( hero, Invisibility.class, Invisibility.DURATION );
				break;
			case 1:
				GLog.i( Messages.get(FrozenCarpaccio.class, "hard") );
				Barkskin.conditionallyAppend( hero, hero.HT / 4, 1 );
				break;
			case 2:
				GLog.i( Messages.get(FrozenCarpaccio.class, "refresh") );
				PotionOfHealing.cure(hero);
				break;
			case 3:
				GLog.i( Messages.get(FrozenCarpaccio.class, "better") );
				hero.HP = Math.min( hero.HP + hero.HT / 4, hero.HT );
				hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(hero.HT / 4), FloatingText.HEALING );
				break;
		}
	}
	
	public static Food cook( MysteryMeat ingredient ) {
		FrozenCarpaccio result = new FrozenCarpaccio();
		result.quantity = ingredient.quantity();
		return result;
	}
}
