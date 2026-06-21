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
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Sheep;
import com.sacredpixel.sacredpixeldungeon.effects.CellEmitter;
import com.sacredpixel.sacredpixeldungeon.effects.Speck;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class FlockTrap extends Trap {

	{
		color = WHITE;
		shape = WAVES;
	}


	@Override
	public void activate() {
		PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 2 );
		ArrayList<Integer> spawnPoints = new ArrayList<>();
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				spawnPoints.add(i);
			}
		}

		//collect traps to activate after all sheep are spawned, to avoid cascading issues
		ArrayList<Trap> trapsToActivate = new ArrayList<>();
		for (int i : spawnPoints){
			if (Dungeon.level.insideMap(i)
					&& Actor.findChar(i) == null
					&& !(Dungeon.level.pit[i])) {
				Sheep sheep = new Sheep();
				sheep.initialize(6);
				sheep.pos = i;
				GameScene.add(sheep);
				CellEmitter.get(i).burst(Speck.factory(Speck.WOOL), 4);
				//before the tile is pressed, directly trigger traps to avoid sfx spam
				Trap t;
				if ((t = Dungeon.level.traps.get(i)) != null && t.active){
					if (t.disarmedByActivation) t.disarm();
					t.reveal();
					Bestiary.setSeen(t.getClass());
					Bestiary.countEncounter(t.getClass());
					trapsToActivate.add(t);
				}
				Dungeon.level.occupyCell(sheep);
			} else if (Actor.findChar(i) instanceof Mob){
				Buff.prolong(Actor.findChar(i), Trap.HazardAssistTracker.class, HazardAssistTracker.DURATION);
			}
		}
		//activate traps after all sheep placed
		for (Trap t : trapsToActivate){
			t.activate();
		}
		Sample.INSTANCE.play(Assets.Sounds.PUFF);
		Sample.INSTANCE.play(Assets.Sounds.SHEEP);
	}

}
