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
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Visual;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class Pushing extends Actor {

	private CharSprite sprite;
	private int from;
	private int to;
	
	private Effect effect;
	private Char ch;

	private Callback callback;
	private boolean blockingVfx = false;

	{
		actPriority = VFX_PRIO+10;
	}
	
	public Pushing( Char ch, int from, int to ) {
		this.ch = ch;
		sprite = ch.sprite;
		this.from = from;
		this.to = to;
		this.callback = null;

		if (ch == Dungeon.hero){
			Camera.main.panFollow(ch.sprite, 20f);
		}
	}

	public Pushing( Char ch, int from, int to, Callback callback ) {
		this(ch, from, to);
		this.callback = callback;
	}

	public boolean isBlockingVfx(){ return blockingVfx; }

	public Pushing setBlockingVfx(){
		blockingVfx = true;
		//Don't add VFX blocker here - add it in act() instead.
		//On HTML5, adding the blocker here prevents Actor.process() from
		//ever calling this Pushing's act(), causing a deadlock.
		return this;
	}

	//safety: if this Pushing actor is removed before its Effect completes,
	//release the VFX blocker to prevent game deadlock
	public void releaseVfxBlocker(){
		if (blockingVfx) {
			blockingVfx = false;
			Actor.removeVfxBlocker();
		}
	}

	@Override
	protected boolean act() {
		Actor.remove( Pushing.this );

		if (sprite != null && sprite.parent != null) {
			//Add VFX blocker now that act() is running (safe on HTML5)
			if (blockingVfx) Actor.addVfxBlocker();
			if (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to]){
				sprite.visible = true;
			}
			if (effect == null) {
				new Effect();
			}
		} else {
			blockingVfx = false; //never added, so don't remove
			if (callback != null) callback.call();
			return true;
		}

		//so that all pushing effects at the same time go simultaneously
		for ( Actor actor : Actor.all() ){
			if (actor instanceof Pushing && actor.cooldown() == 0)
				return true;
		}
		return false;

	}

	public static boolean pushingExistsForChar(Char ch) {
		for (Actor a : all()){
			if (a instanceof Pushing && ((Pushing)a).ch == ch){
				return true;
			}
		}
		return false;
	}

	public class Effect extends Visual {

		private static final float DELAY = 0.15f;
		
		private PointF end;
		
		private float delay;
		
		public Effect() {
			super( 0, 0, 0, 0 );
			
			point( sprite.worldToCamera( from ) );
			end = sprite.worldToCamera( to );
			
			speed.set( 2 * (end.x - x) / DELAY, 2 * (end.y - y) / DELAY );
			acc.set( -speed.x / DELAY, -speed.y / DELAY );
			
			delay = 0;

			if (sprite.parent != null)
				sprite.parent.add( this );
		}
		
		@Override
		public void update() {
			super.update();

			if ((delay += Game.elapsed) < DELAY) {

				sprite.x = x;
				sprite.y = y;

			} else {

				sprite.point(end);

				killAndErase();
				Actor.remove(Pushing.this);
				//clear blockingVfx before callback so kill doesn't double-remove
				boolean wasBlocking = blockingVfx;
				blockingVfx = false;
				try {
					if (callback != null) callback.call();
				} catch (Exception e) {
					Game.reportException(e);
				}
				if (wasBlocking) Actor.removeVfxBlocker();
				GameScene.sortMobSprites();

				next();
			}
		}

		@Override
		public void kill() {
			//safety: if Effect is destroyed mid-animation, release the VFX blocker
			releaseVfxBlocker();
			super.kill();
		}
	}

}
