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
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Blob;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.ConfusionGas;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class ConfusionTrap extends Trap {

	{
		color = TEAL;
		shape = GRILL;
	}

	@Override
	public void activate() {

		GameScene.add(Blob.seed(pos, 300 + 20 * scalingDepth(), ConfusionGas.class));
		Sample.INSTANCE.play(Assets.Sounds.GAS);

		for( int i : PathFinder.NEIGHBOURS9) {
			if (Actor.findChar(pos+i) instanceof Mob){
				Buff.prolong(Actor.findChar(pos+i), Trap.HazardAssistTracker.class, HazardAssistTracker.DURATION);
			}
		}

	}
}
