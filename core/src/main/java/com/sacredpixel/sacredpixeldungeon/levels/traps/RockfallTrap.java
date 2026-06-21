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

package com.sacredpixel.sacredpixeldungeon.levels.traps;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Paralysis;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.effects.CellEmitter;
import com.sacredpixel.sacredpixeldungeon.effects.Speck;
import com.sacredpixel.sacredpixeldungeon.levels.RegularLevel;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.Room;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class RockfallTrap extends Trap {

	{
		color = GREY;
		shape = DIAMOND;
		
		canBeHidden = false;
		avoidsHallways = true;
	}
	
	@Override
	public void activate() {
		
		ArrayList<Integer> rockCells = new ArrayList<>();
		
		//determines if the trap is actually in the world, or if it is being spawned for its effect
		boolean onGround = Dungeon.level.traps.get(pos) == this;
		Room r = null;
		if (Dungeon.level instanceof RegularLevel){
			r = ((RegularLevel) Dungeon.level).room(pos);
		}
		
		if (onGround && r != null){
			int cell;
			for (Point p : r.getPoints()){
				cell = Dungeon.level.pointToCell(p);
				if (!Dungeon.level.solid[cell]){
					rockCells.add(cell);
				}
			}
			
		//if we don't have a room, then just do 5x5
		} else {
			PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 2 );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE) {
					rockCells.add(i);
				}
			}
		}
		
		boolean seen = false;
		for (int cell : rockCells){

			if (Dungeon.level.heroFOV[ cell ]){
				CellEmitter.get( cell - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
				seen = true;
			}

			Char ch = Actor.findChar( cell );

			if (ch != null && ch.isAlive()){
				if (ch instanceof Mob) {
					Buff.prolong(ch, Trap.HazardAssistTracker.class, HazardAssistTracker.DURATION);
				}
				int damage = Random.NormalIntRange(5+scalingDepth(), 10+scalingDepth()*2);
				damage -= ch.drRoll();
				ch.damage( Math.max(damage, 0) , this);

				if (ch.isActive()) {
					Buff.prolong(ch, Paralysis.class, Paralysis.DURATION);
				} else if (!ch.isAlive() && ch == Dungeon.hero){
					Dungeon.fail( this );
					GLog.n( Messages.get(this, "ondeath") );
					if (reclaimed) Badges.validateDeathFromFriendlyMagic();
				}
			}
		}
		
		if (seen){
			PixelScene.shake(3, 0.7f);
			Sample.INSTANCE.play(Assets.Sounds.ROCKS);
		}

	}
}
