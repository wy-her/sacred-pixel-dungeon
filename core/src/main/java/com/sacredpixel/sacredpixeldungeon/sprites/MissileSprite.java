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

package com.sacredpixel.sacredpixeldungeon.sprites;

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.HolyLance;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollGeomancer;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.weapon.SpiritBow;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.Crossbow;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.Bolas;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.FishingSpear;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.Javelin;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.Kunai;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.Shuriken;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.ThrowingSpear;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.Trident;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.sacredpixel.sacredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Visual;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

import java.util.HashMap;
import com.watabou.utils.Compat;

public class MissileSprite extends ItemSprite implements Tweener.Listener {

	private static final float SPEED	= 200f;

	private Callback callback;

	//tracks whether this missile is blocking actor processing while in flight
	private boolean blockingVfx = false;
	
	public void reset( int from, int to, Item item, Callback listener ) {
		reset(Dungeon.level.solid[from] ? DungeonTilemap.raisedTileCenterToWorld(from) : DungeonTilemap.tileCenterToWorld(from),
				Dungeon.level.solid[to] ? DungeonTilemap.raisedTileCenterToWorld(to) : DungeonTilemap.tileCenterToWorld(to),
				item, listener);
	}

	public void reset( Visual from, int to, Item item, Callback listener ) {
		reset(from.center(),
				Dungeon.level.solid[to] ? DungeonTilemap.raisedTileCenterToWorld(to) : DungeonTilemap.tileCenterToWorld(to),
				item, listener );
	}

	public void reset( int from, Visual to, Item item, Callback listener ) {
		reset(Dungeon.level.solid[from] ? DungeonTilemap.raisedTileCenterToWorld(from) : DungeonTilemap.tileCenterToWorld(from),
				to.center(),
				item, listener );
	}

	public void reset( Visual from, Visual to, Item item, Callback listener ) {
		reset(from.center(), to.center(), item, listener );
	}

	public void reset( PointF from, PointF to, Item item, Callback listener) {
		revive();

		if (item == null)   view(0, null);
		else                view( item );

		setup( from,
				to,
				item,
				listener );
	}
	
	private static final int DEFAULT_ANGULAR_SPEED = 720;
	
	private static final HashMap<Class<?extends Item>, Integer> ANGULAR_SPEEDS = new HashMap<>();
	static {
		ANGULAR_SPEEDS.put(Dart.class,          0);
		ANGULAR_SPEEDS.put(ThrowingKnife.class, 0);
		ANGULAR_SPEEDS.put(ThrowingSpike.class, 0);
		ANGULAR_SPEEDS.put(FishingSpear.class,  0);
		ANGULAR_SPEEDS.put(ThrowingSpear.class, 0);
		ANGULAR_SPEEDS.put(Kunai.class,         0);
		ANGULAR_SPEEDS.put(Javelin.class,       0);
		ANGULAR_SPEEDS.put(Trident.class,       0);
		
		ANGULAR_SPEEDS.put(SpiritBow.SpiritArrow.class,       0);
		ANGULAR_SPEEDS.put(ScorpioSprite.ScorpioShot.class,   0);
		ANGULAR_SPEEDS.put(HolyLance.HolyLanceVFX.class,      0);
		
		//720 is default

		ANGULAR_SPEEDS.put(GnollGeomancer.Boulder.class,   90);
		
		ANGULAR_SPEEDS.put(HeavyBoomerang.class,1440);
		ANGULAR_SPEEDS.put(Bolas.class,         1440);
		
		ANGULAR_SPEEDS.put(Shuriken.class,                  2160);
		ANGULAR_SPEEDS.put(TenguSprite.TenguShuriken.class, 2160);
	}

	//TODO it might be nice to have a source and destination angle, to improve thrown weapon visuals
	private void setup( PointF from, PointF to, Item item, Callback listener ){

		originToCenter();

		//adjust points so they work with the center of the missile sprite, not the corner
		from.x -= width()/2;
		to.x -= width()/2;
		from.y -= height()/2;
		to.y -= height()/2;

		this.callback = listener;

		point( from );

		PointF d = PointF.diff( to, from );
		speed.set(d).normalize().scale(SPEED);
		
		angularSpeed = DEFAULT_ANGULAR_SPEED;
		for (Class<?extends Item> cls : ANGULAR_SPEEDS.keySet()){
			if (Compat.isAssignableFrom(cls, item.getClass())){
				angularSpeed = ANGULAR_SPEEDS.get(cls);
				break;
			}
		}
		
		angle = 135 - (float)(Math.atan2( d.x, d.y ) / 3.1415926 * 180);
		
		if (d.x >= 0){
			flipHorizontal = false;
			updateFrame();
			
		} else {
			angularSpeed = -angularSpeed;
			angle += 90;
			flipHorizontal = true;
			updateFrame();
		}

		if (item instanceof GnollGeomancer.Boulder){
			angle = 0;
			flipHorizontal = false;
			updateFrame();
		}
		
		// Use tile-based duration so missiles match character movement speed
		float tiles = d.length() / DungeonTilemap.SIZE;
		float duration = tiles * CharSprite.DEFAULT_MOVE_INTERVAL * 0.6f;

		if (item instanceof Dart
				&& (Dungeon.hero.belongings.weapon() instanceof Crossbow
				|| Dungeon.hero.belongings.secondWep() instanceof Crossbow)){
			duration /= 3f;

		} else if (item instanceof SpiritBow.SpiritArrow
				|| item instanceof ScorpioSprite.ScorpioShot
				|| item instanceof TenguSprite.TenguShuriken){
			duration /= 1.5f;
		}

		PosTweener tweener = new PosTweener( this, to, duration );
		tweener.listener = this;
		parent.add( tweener );

		//Block actor processing while the missile is in flight so that
		//the hero cannot act before the projectile arrives and damage is applied.
		if (this.callback != null) {
			blockingVfx = true;
			Actor.addVfxBlocker();
		}
	}

	@Override
	public void onComplete( Tweener tweener ) {
		//clear blockingVfx before kill() so kill() doesn't remove the blocker prematurely
		boolean wasBlocking = blockingVfx;
		blockingVfx = false;
		kill();
		if (callback != null) {
			try {
				callback.call();
			} catch (Exception e) {
				com.watabou.noosa.Game.reportException(e);
			}
		}
		//remove the VFX blocker AFTER the callback has applied damage and called next()
		if (wasBlocking) {
			Actor.removeVfxBlocker();
		}
	}

	@Override
	public void kill() {
		//safety: if missile is destroyed mid-flight without completing, release the VFX blocker
		if (blockingVfx) {
			blockingVfx = false;
			Actor.removeVfxBlocker();
		}
		super.kill();
	}
}
