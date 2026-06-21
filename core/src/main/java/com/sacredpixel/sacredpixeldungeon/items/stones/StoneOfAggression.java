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

package com.sacredpixel.sacredpixeldungeon.items.stones;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.FlavourBuff;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.effects.CellEmitter;
import com.sacredpixel.sacredpixeldungeon.effects.Speck;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;

public class StoneOfAggression extends Runestone {
	
	{
		image = ItemSpriteSheet.STONE_AGGRESSION;
	}
	
	@Override
	protected void activate(int cell) {
		
		Char ch = Actor.findChar( cell );
		
		if (ch != null) {
			if (Char.hasProp(ch, Char.Property.BOSS) || Char.hasProp(ch, Char.Property.MINIBOSS)) {
				Buff.prolong(ch, Aggression.class, Aggression.DURATION / 4f);
			} else {
				Buff.prolong(ch, Aggression.class, Aggression.DURATION);
			}
		}

		CellEmitter.center(cell).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
		Sample.INSTANCE.play( Assets.Sounds.READ );
		
	}

	public static class Aggression extends FlavourBuff {
		
		public static final float DURATION = 20f;
		
		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		@Override
		public int icon() {
			return BuffIndicator.TARGETED;
		}

		@Override
		public float iconFadePercent() {
			if (Char.hasProp(target, Char.Property.BOSS) || Char.hasProp(target, Char.Property.MINIBOSS)){
				return Math.max(0, (DURATION/4f - visualcooldown()) / (DURATION/4f));
			} else {
				return Math.max(0, (DURATION - visualcooldown()) / DURATION);
			}
		}

		@Override
		public void detach() {
			//if our target is an enemy, reset any enemy-to-enemy aggro involving it
			if (target.isAlive()) {
				if (target.alignment == Char.Alignment.ENEMY) {
					for (Mob m : Dungeon.level.mobs) {
						if (m.alignment == Char.Alignment.ENEMY && m.isTargeting(target)) {
							m.aggro(null);
						}
						if (target instanceof Mob && ((Mob) target).isTargeting(m)){
							((Mob) target).aggro(null);
						}
					}
				}
			}
			super.detach();
			
		}

	}
	
}
