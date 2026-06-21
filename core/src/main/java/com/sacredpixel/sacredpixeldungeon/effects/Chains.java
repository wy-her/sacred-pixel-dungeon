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

package com.sacredpixel.sacredpixeldungeon.effects;

import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class Chains extends Group {

	private static final double A = 180 / Math.PI;

	private float spent = 0f;
	private float duration;

	private Callback callback;
	private boolean blockingVfx = false;

	private Image[] chains;
	private int numChains;
	private float distance;
	private float rotation = 0;

	private PointF from, to;

	public Chains(int from, int to, Effects.Type type, Callback callback){
		this(DungeonTilemap.tileCenterToWorld(from),
				DungeonTilemap.tileCenterToWorld(to),
				type,
				callback);
	}

	public Chains(PointF from, PointF to, Effects.Type type, Callback callback){
		super();

		this.callback = callback;

		this.from = from;
		this.to = to;

		float dx = to.x - from.x;
		float dy = to.y - from.y;
		distance = (float)Math.hypot(dx, dy);

		//base of 200ms, plus 50ms per tile travelled
		duration = distance/320f + 0.2f;

		rotation = (float)(Math.atan2( dy, dx ) * A) + 90f;

		numChains = Math.round(distance/6f)+1;

		chains = new Image[numChains];
		for (int i = 0; i < chains.length; i++){
			chains[i] = new Image(Effects.get(type));
			chains[i].angle = rotation;
			chains[i].origin.set( chains[i].width()/ 2, chains[i].height() );
			add(chains[i]);
		}
	}

	public Chains setBlockingVfx(){
		blockingVfx = true;
		Actor.addVfxBlocker();
		return this;
	}

	@Override
	public synchronized void kill() {
		//safety: if chains are destroyed mid-animation, release the VFX blocker
		if (blockingVfx) {
			blockingVfx = false;
			Actor.removeVfxBlocker();
		}
		super.kill();
	}

	@Override
	public void update() {
		if ((spent += Game.elapsed) > duration) {

			//clear blockingVfx before callback so kill() doesn't double-remove
			boolean wasBlocking = blockingVfx;
			blockingVfx = false;
			killAndErase();
			if (callback != null) {
				callback.call();
			}
			if (wasBlocking) Actor.removeVfxBlocker();

		} else {
			float dx = to.x - from.x;
			float dy = to.y - from.y;
			for (int i = 0; i < chains.length; i++) {
				chains[i].center(new PointF(
						from.x + ((dx * (i / (float)chains.length)) * (spent/duration)),
						from.y + ((dy * (i / (float)chains.length)) * (spent/duration))
				));
			}
		}
	}

}
