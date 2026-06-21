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

package com.sacredpixel.sacredpixeldungeon.items.weapon.curses;

import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Bleeding;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.items.weapon.Weapon;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Sacrificial extends Weapon.Enchantment {

	private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {

		float procChance = 1/10f * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {
			float missingPercent = 1f - attacker.HP/(float)attacker.HT;
			float bleedAmt = (float)(Math.pow(missingPercent, 2) * attacker.HT)/8f;
			if (bleedAmt > 0) {
				Buff.affect(attacker, Bleeding.class).set(Math.max(1, bleedAmt), getClass());
			}
		}

		return damage;
	}

	@Override
	public boolean curse() {
		return true;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BLACK;
	}

}
