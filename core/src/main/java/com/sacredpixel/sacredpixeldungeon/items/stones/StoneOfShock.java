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
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Paralysis;
import com.sacredpixel.sacredpixeldungeon.effects.CellEmitter;
import com.sacredpixel.sacredpixeldungeon.effects.Lightning;
import com.sacredpixel.sacredpixeldungeon.effects.particles.EnergyParticle;
import com.sacredpixel.sacredpixeldungeon.effects.particles.SparkParticle;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class StoneOfShock extends Runestone {
	
	{
		image = ItemSpriteSheet.STONE_SHOCK;
	}
	
	@Override
	protected void activate(int cell) {
		
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		
		ArrayList<Lightning.Arc> arcs = new ArrayList<>();
		int hits = 0;
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 2 );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Char n = Actor.findChar(i);
				if (n != null) {
					arcs.add(new Lightning.Arc(cell, n.sprite.center()));
					Buff.prolong(n, Paralysis.class, 1f);
					hits++;
				}
			}
		}
		
		CellEmitter.center( cell ).burst( SparkParticle.FACTORY, 3 );
		
		if (hits > 0) {
			curUser.sprite.parent.addToFront( new Lightning( arcs, null ) );
			curUser.sprite.centerEmitter().burst(EnergyParticle.FACTORY, 10);
			Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
			
			curUser.belongings.charge(1f + hits);
		}
	
	}
}
