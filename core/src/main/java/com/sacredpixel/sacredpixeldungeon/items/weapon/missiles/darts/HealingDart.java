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

package com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.darts;

import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Healing;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfHealing;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;

public class HealingDart extends TippedDart {
	
	{
		image = ItemSpriteSheet.HEALING_DART;
		usesTargeting = false; //you never want to throw this at an enemy
	}

	@Override
	public int damageRoll(Char owner) {
		if (owner instanceof Hero) {
			if (((Hero) owner).attackTarget().alignment == owner.alignment){
				return 0; //does not deal damage to allies
			}
		}
		return super.damageRoll(owner);
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {

		//do nothing to the hero or enemies when processing charged shot
		if (processingChargedShot && (defender == attacker || attacker.alignment != defender.alignment)){
			return super.proc(attacker, defender, damage);
		}
		
		//heals 30 hp at base, scaling with enemy HT
		PotionOfHealing.cure( defender );
		Buff.affect( defender, Healing.class ).setHeal((int)(0.5f*defender.HT + 30), 0.25f, 0);

		return super.proc(attacker, defender, damage);
	}
	
}
