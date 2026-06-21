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

package com.sacredpixel.sacredpixeldungeon.items.weapon.enchantments;

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Burning;
import com.sacredpixel.sacredpixeldungeon.effects.particles.FlameParticle;
import com.sacredpixel.sacredpixeldungeon.items.weapon.Weapon;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Blazing extends Weapon.Enchantment {

	private static ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFF4400 );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		float procChance = (level+1f)/(level+3f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {

			float powerMulti = Math.max(1f, procChance);

			if (defender.buff(Burning.class) == null){
				Buff.affect(defender, Burning.class).reignite(defender, 8f);
				powerMulti -= 1;
			}

			if (powerMulti > 0){
				int burnDamage = Random.NormalIntRange( 1, 3 + Dungeon.scalingDepth()/4 );
				burnDamage = Math.round(burnDamage * 0.67f * powerMulti);
				if (burnDamage > 0) {
					defender.damage(burnDamage, this);
				}
			}
			
			if (defender.isAlive() && defender.sprite != null) {
				defender.sprite.emitter().burst( FlameParticle.FACTORY, level + 1 );
			}
			
		}

		return damage;

	}
	
	@Override
	public Glowing glowing() {
		return ORANGE;
	}
}
