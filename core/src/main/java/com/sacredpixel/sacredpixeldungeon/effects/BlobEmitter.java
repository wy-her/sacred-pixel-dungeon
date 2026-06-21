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

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Blob;
import com.sacredpixel.sacredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

public class BlobEmitter extends Emitter {
	
	private Blob blob;
	
	public BlobEmitter( Blob blob ) {
		
		super();
		
		this.blob = blob;
		blob.use( this );
	}

	public RectF bound = new RectF(0, 0, 1, 1);
	
	@Override
	protected void emit( int index ) {
		
		if (blob.volume <= 0) {
			return;
		}

		if (blob.area.isEmpty())
			blob.setupArea();
		
		int[] map = blob.cur;
		float size = DungeonTilemap.SIZE;
		boolean html5 = DeviceCompat.isHTML5();

		int cell;
		//On HTML5, count eligible cells first, then emit with random skip
		//to distribute particles evenly across the blob (not biased to top-left)
		int emitted = 0;
		for (int i = blob.area.left; i < blob.area.right; i++) {
			for (int j = blob.area.top; j < blob.area.bottom; j++) {
				//On HTML5, use checkerboard pattern to halve particle count
				if (html5 && ((i + j) % 2 != index % 2)) continue;
				cell = i + j*Dungeon.level.width();
				if (cell < Dungeon.level.heroFOV.length
						&& (Dungeon.level.heroFOV[cell] || blob.alwaysVisible)
						&& map[cell] > 0) {
					//On HTML5, randomly skip ~60% of particles for large blobs
					//This distributes particles evenly instead of biasing to one corner
					if (html5 && emitted >= 6 && Random.Float() < 0.6f) continue;
					float x = (i + Random.Float(bound.left, bound.right)) * size;
					float y = (j + Random.Float(bound.top, bound.bottom)) * size;
					factory.emit(this, index, x, y);
					emitted++;
				}
			}
		}
	}
}
