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

package com.sacredpixel.sacredpixeldungeon.actors.blobs;

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Paralysis;
import com.sacredpixel.sacredpixeldungeon.effects.BlobEmitter;
import com.sacredpixel.sacredpixeldungeon.effects.particles.SparkParticle;
import com.sacredpixel.sacredpixeldungeon.items.Heap;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.wands.Wand;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.MagesStaff;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayDeque;

public class Electricity extends Blob {
	
	{
		//acts after mobs, to give them a chance to resist paralysis
		actPriority = MOB_PRIO - 1;
	}
	
	private boolean[] water;
	
	@Override
	protected void evolve() {
		
		water = Dungeon.level.water;
		int cell;
		
		//spread first..
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j*Dungeon.level.width();
				
				if (cur[cell] > 0) {
					spreadFromCell(cell, cur[cell]);
				}
			}
		}
		
		//..then decrement/shock
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j*Dungeon.level.width();
				if (cur[cell] > 0) {
					Char ch = Actor.findChar( cell );
					if (ch != null && !ch.isImmune(this.getClass())) {
						if (ch.buff(Paralysis.class) == null){
							Buff.prolong( ch, Paralysis.class, cur[cell]);
						}
						if (cur[cell] % 2 == 1) {
							ch.damage(Math.round(Random.Float(2 + Dungeon.scalingDepth() / 5f)), this);
							if (!ch.isAlive() && ch == Dungeon.hero){
								Dungeon.fail( this );
								GLog.n( Messages.get(this, "ondeath") );
							}
						}
					}
					
					Heap h = Dungeon.level.heaps.get( cell );
					if (h != null){
						Item toShock = h.peek();
						if (toShock instanceof Wand){
							((Wand) toShock).gainCharge(0.333f);
						} else if (toShock instanceof MagesStaff){
							((MagesStaff) toShock).gainCharge(0.333f);
						}
					}
					
					off[cell] = cur[cell] - 1;
					volume += off[cell];
				} else {
					off[cell] = 0;
				}
			}
		}
		
	}
	
	//iterative BFS to avoid stack overflow on GWT/HTML5 with large water areas
	private void spreadFromCell( int cell, int power ){
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(new int[]{cell, power});

		while (!queue.isEmpty()){
			int[] entry = queue.poll();
			int c = entry[0];
			int p = entry[1];

			if (cur[c] >= p) continue;

			if (cur[c] == 0) {
				area.union(c % Dungeon.level.width(), c / Dungeon.level.width());
			}
			cur[c] = Math.max(cur[c], p);

			for (int n : PathFinder.NEIGHBOURS4){
				int adj = c + n;
				if (adj >= 0 && adj < water.length && water[adj] && cur[adj] < p){
					queue.add(new int[]{adj, p});
				}
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( SparkParticle.FACTORY, 0.05f, 0 );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
	
}
