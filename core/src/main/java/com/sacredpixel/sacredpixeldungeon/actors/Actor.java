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

package com.sacredpixel.sacredpixeldungeon.actors;

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Blob;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.levels.VaultLevel;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;
import com.watabou.utils.ThreadCompat;

import java.util.Collections;
import java.util.HashSet;

public abstract class Actor implements Bundlable {
	
	public static final float TICK	= 1f;

	private float time;

	private int id = 0;

	//default priority values for general actor categories
	//note that some specific actors pick more specific values
	//e.g. a buff acting before all normal buffs might have priority BUFF_PRIO + 1
	protected static final int VFX_PRIO    = 100;   //visual effects take priority
	protected static final int HERO_PRIO   = 0;     //positive is before hero, negative after
	protected static final int BLOB_PRIO   = -10;   //blobs act after hero, before mobs
	protected static final int MOB_PRIO    = -20;   //mobs act between buffs and blobs
	protected static final int BUFF_PRIO   = -30;   //buffs act last in a turn
	private static final int   DEFAULT     = -100;  //if no priority is given, act after all else

	//used to determine what order actors act in if their time is equal. Higher values act earlier.
	protected int actPriority = DEFAULT;

	//On HTML5, tracks whether this actor returned false from act() and is waiting for a callback
	//to call next(). While true, the actor is skipped in the process() selection loop.
	protected boolean waitingForCallback = false;

	public boolean isWaitingForCallback() {
		return waitingForCallback;
	}

	protected abstract boolean act();

	//Always spends exactly the specified amount of time, regardless of time-influencing factors
	protected void spendConstant( float time ){
		this.time += time;
		//if time is very close to a whole number, round to a whole number to fix errors
		float ex = Math.abs(this.time % 1f);
		if (ex < .001f){
			this.time = Math.round(this.time);
		}
	}

	//sends time, but the amount can be influenced
	protected void spend( float time ) {
		spendConstant( time );
	}

	public void spendToWhole(){
		time = (float)Math.ceil(time);
	}
	
	protected void postpone( float time ) {
		if (this.time < now + time) {
			this.time = now + time;
			//if time is very close to a whole number, round to a whole number to fix errors
			float ex = Math.abs(this.time % 1f);
			if (ex < .001f){
				this.time = Math.round(this.time);
			}
		}
	}
	
	public float cooldown() {
		return time - now;
	}

	public void clearTime() {
		spendConstant(-Actor.now());
		if (this instanceof Char){
			for (Buff b : ((Char) this).buffs()){
				b.spendConstant(-Actor.now());
			}
		}
	}

	public void timeToNow() {
		time = now;
	}
	
	protected void diactivate() {
		time = Float.MAX_VALUE;
	}
	
	protected void onAdd() {}
	
	protected void onRemove() {}

	private static final String TIME    = "time";
	private static final String ID      = "id";

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( TIME, time );
		bundle.put( ID, id );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		time = bundle.getFloat( TIME );
		int incomingID = bundle.getInt( ID );
		if (Actor.findById(incomingID) == null){
			id = incomingID;
		} else {
			id = nextID++;
		}
	}

	public int id() {
		if (id > 0) {
			return id;
		} else {
			return (id = nextID++);
		}
	}

	// **********************
	// *** Static members ***
	// **********************
	
	private static HashSet<Actor> all = new HashSet<>();
	private static HashSet<Char> chars = new HashSet<>();
	private static volatile Actor current;

	private static SparseArray<Actor> ids = new SparseArray<>();
	private static int nextID = 1;

	private static float now = 0;

	//Global VFX blocking counter.
	//When > 0, Actor.process() will not process any actors.
	//On HTML5, process() breaks and retries next frame.
	//On desktop, the actor thread sleeps until blockers clear.
	//Incremented when a VFX animation starts, decremented when it finishes.
	private static volatile int vfxBlockers = 0;
	public static void addVfxBlocker() { vfxBlockers++; }
	public static void removeVfxBlocker() { vfxBlockers--; if (vfxBlockers < 0) vfxBlockers = 0; }
	
	public static float now(){
		return now;
	}
	
	public static synchronized void clear() {

		now = 0;

		all.clear();
		chars.clear();

		ids.clear();

		movingStallFrames = 0;
		movingStallActor = null;
		vfxBlockers = 0;
	}

	public static synchronized void fixTime() {
		
		if (all.isEmpty()) return;
		
		float min = Float.MAX_VALUE;
		for (Actor a : all) {
			if (a.time < min) {
				min = a.time;
			}
		}

		//Only pull everything back by whole numbers
		//So that turns always align with a whole number
		min = (int)min;
		for (Actor a : all) {
			a.time -= min;
		}

		if (Dungeon.hero != null && all.contains( Dungeon.hero ) && !(Dungeon.level instanceof VaultLevel)) {
			Statistics.duration += min;
		}
		now -= min;
	}
	
	public static void init() {
		
		add( Dungeon.hero );
		
		for (Mob mob : Dungeon.level.mobs) {
			add( mob );
		}

		//mobs need to remember their targets after every actor is added
		for (Mob mob : Dungeon.level.mobs) {
			mob.restoreEnemy();
		}
		
		for (Blob blob : Dungeon.level.blobs.values()) {
			add( blob );
		}
		
		current = null;
	}

	private static final String NEXTID = "nextid";

	public static void storeNextID( Bundle bundle){
		bundle.put( NEXTID, nextID );
	}

	public static void restoreNextID( Bundle bundle){
		nextID = bundle.getInt( NEXTID );
	}

	public static void resetNextID(){
		nextID = 1;
	}

	/*protected*/public void next() {
		waitingForCallback = false;
		if (current == this) {
			current = null;
		}
	}

	public static boolean processing(){
		return current != null;
	}

	public static int curActorPriority() {
		return current != null ? current.actPriority : HERO_PRIO;
	}
	
	public static boolean keepActorThreadAlive = true;

	//On HTML5, tracks consecutive frames where process() stalls on a moving sprite
	private static int movingStallFrames = 0;
	private static int heroStuckFrames = 0;
	private static int mobStuckFrames = 0;
	private static Actor movingStallActor = null;
	private static Actor stuckMobActor = null;
	private static final int MAX_MOVING_STALL_FRAMES = 45; // ~0.75s at 60fps (was 60)
	private static final int MAX_MOB_STUCK_FRAMES = 90; // ~1.5s at 60fps

	public static void process() {

		boolean doNext;
		boolean interrupted = false;
		//On HTML5, limit actors processed per frame to avoid blocking the render loop
		int actionsThisFrame = 0;

		do {

			//If a VFX animation is blocking, pause actor processing.
			//On HTML5, break and let the next frame try again.
			//On desktop, sleep briefly in a loop until the blocker clears.
			if (vfxBlockers > 0) {
				if (ThreadCompat.currentThread() == null) {
					current = null; //clear so processing() returns false, allowing retry next frame
					break;
				} else {
					//safety timeout: if VFX blockers are stuck for >3 seconds,
					//force-clear them to prevent permanent game deadlock
					long vfxWaitStart = System.currentTimeMillis();
					while (vfxBlockers > 0 && keepActorThreadAlive) {
						try { Thread.sleep(10); } catch (InterruptedException e) { interrupted = true; break; }
						if (System.currentTimeMillis() - vfxWaitStart > 3000) {
							vfxBlockers = 0;
							break;
						}
					}
				}
			}

			current = null;
			if (!interrupted && !Game.switchingScene()) {
				float earliest = Float.MAX_VALUE;
				boolean heroWaiting = false;
				float heroTime = Float.MAX_VALUE;

				//Safety: if hero is stuck waitingForCallback for too many frames, force recovery
				if (ThreadCompat.currentThread() == null && Dungeon.hero != null
						&& Dungeon.hero.waitingForCallback && !Dungeon.hero.ready) {
					heroStuckFrames++;
					if (heroStuckFrames > 180) { //~3 seconds at 60fps
						Dungeon.hero.waitingForCallback = false;
						heroStuckFrames = 0;
					}
				} else {
					heroStuckFrames = 0;
				}

				//Safety: if any mob is stuck waitingForCallback for too many frames, force recovery
				//This prevents permanent game freeze from mobs stuck at FOV boundaries
				if (ThreadCompat.currentThread() == null) {
					Actor foundStuckMob = null;
					synchronized (Actor.class) {
						for (Actor actor : all) {
							if (actor != Dungeon.hero && actor.waitingForCallback) {
								foundStuckMob = actor;
								break;
							}
						}
					}
					if (foundStuckMob != null) {
						if (stuckMobActor == foundStuckMob) {
							mobStuckFrames++;
							if (mobStuckFrames > MAX_MOB_STUCK_FRAMES) {
								foundStuckMob.waitingForCallback = false;
								mobStuckFrames = 0;
								stuckMobActor = null;
							}
						} else {
							stuckMobActor = foundStuckMob;
							mobStuckFrames = 0;
						}
					} else {
						mobStuckFrames = 0;
						stuckMobActor = null;
					}
				}

				synchronized (Actor.class) {
					for (Actor actor : all) {

						//On HTML5, skip actors waiting for an animation callback
						if (ThreadCompat.currentThread() == null && actor.waitingForCallback) {
							//Track if the hero is waiting for player input
							if (actor == Dungeon.hero) {
								heroWaiting = true;
								heroTime = actor.time;
							}
							continue;
						}

						//some actors will always go before others if time is equal.
						if (actor.time < earliest ||
								actor.time == earliest && (current == null || actor.actPriority > current.actPriority)) {
							earliest = actor.time;
							current = actor;
						}

					}
				}
				//On HTML5, if hero is waiting for player input, only allow actors
				//whose time is strictly before the hero's time to act.
				//This lets mobs that should act before the hero finish their turns,
				//while preventing mobs from acting out of order after the hero.
				//Allow VFX actors (like Pushing) to proceed even when hero is waiting,
				//and allow actors whose time is strictly before the hero
				if (heroWaiting && current != null
						&& current.time >= heroTime
						&& current.actPriority < VFX_PRIO) {
					current = null;
				}
			}

			if  (current != null) {

				now = current.time;
				Actor acting = current;

				//On HTML5, break after processing many actors to let the render loop run
				//50 strikes a balance between responsive UI and fast turn resolution
				if (ThreadCompat.currentThread() == null && ++actionsThisFrame > 50) {
					current = null;
					break;
				}

				if (acting instanceof Char && ((Char) acting).sprite != null) {
					// If it's character's turn to act, but its sprite
					// is moving, wait till the movement is over
					if (ThreadCompat.currentThread() != null) {
						try {
							synchronized (((Char) acting).sprite) {
								if (((Char) acting).sprite.isMoving) {
									ThreadCompat.waitOnObject(((Char) acting).sprite);
								}
							}
						} catch (InterruptedException e) {
							interrupted = true;
						}
					} else {
						// HTML5/GWT: can't wait, so break and retry next frame
						if (((Char) acting).sprite.isMoving) {
							//Reset stall counter if a different actor is now stalling
							if (movingStallActor != acting) {
								movingStallFrames = 0;
								movingStallActor = acting;
							}
							movingStallFrames++;
							if (movingStallFrames > MAX_MOVING_STALL_FRAMES) {
								// Sprite movement stuck, force-unstick to prevent freeze
								((Char) acting).sprite.isMoving = false;
								movingStallFrames = 0;
								movingStallActor = null;
							} else {
								current = null;
								break;
							}
						} else {
							movingStallFrames = 0;
							movingStallActor = null;
						}
					}
				}

				interrupted = interrupted || ThreadCompat.interrupted();
				
				if (interrupted){
					doNext = false;
					current = null;
				} else {
					try {
						doNext = acting.act();
					} catch (Exception e) {
						com.watabou.noosa.Game.reportException(e);
						doNext = false;
						acting.waitingForCallback = true;
						current = null;
						continue;
					}
					if (doNext && (Dungeon.hero == null || !Dungeon.hero.isAlive())) {
						doNext = false;
						current = null;
					}

					// On HTML5, when hero's continuous movement returns true,
					// yield this frame so the sprite animation plays and other
					// actors get a fair chance to act between hero steps.
					// The hero is NOT marked waitingForCallback, so next frame
					// it will be selected again based on time priority.
					// The sprite.isMoving check (above) ensures the hero waits
					// for its animation to finish before taking another step.
					if (doNext && ThreadCompat.currentThread() == null
							&& acting == Dungeon.hero) {
						current = null;
						break;
					}
					}
			} else {
				doNext = false;
			}

			if (!doNext){
				//On HTML5, mark the actor as waiting so it isn't re-selected next frame.
				//This prevents mobs from acting multiple times before their animation callback
				//calls next() (e.g. Tengu firing multiple shurikens in one turn).
				if (ThreadCompat.currentThread() == null && current != null) {
					current.waitingForCallback = true;
				}
				Object thread = ThreadCompat.currentThread();
				if (thread != null) {
					synchronized (thread) {
						interrupted = interrupted || ThreadCompat.interrupted();

						if (interrupted){
							current = null;
							interrupted = false;
						}

						//signals to the gamescene that actor processing is finished for now
						ThreadCompat.notifyCurrentThread();

						try {
							ThreadCompat.waitOnCurrentThread();
						} catch (InterruptedException e) {
							interrupted = true;
						}
					}
				} else {
					// HTML5/GWT: no threading, just process synchronously
					current = null;
					break; // Exit loop - will be called again next frame
				}
			}

		} while (keepActorThreadAlive);
	}
	
	public static void add( Actor actor ) {
		add( actor, now );
	}
	
	public static void addDelayed( Actor actor, float delay ) {
		add( actor, now + Math.max(delay, 0) );
	}
	
	private static synchronized void add( Actor actor, float time ) {
		
		if (all.contains( actor )) {
			return;
		}

		ids.put( actor.id(),  actor );

		all.add( actor );
		actor.time += time;
		actor.onAdd();
		
		if (actor instanceof Char) {
			Char ch = (Char)actor;
			chars.add( ch );
			for (Buff buff : ch.buffs()) {
				add(buff);
			}
		}
	}
	
	public static synchronized void remove( Actor actor ) {
		
		if (actor != null) {
			all.remove( actor );
			chars.remove( actor );
			actor.onRemove();

			if (actor.id > 0) {
				ids.remove( actor.id );
			}
		}
	}

	//'freezes' a character in time for a specified amount of time
	//USE CAREFULLY! Manipulating time like this is useful for some gameplay effects but is tricky
	public static void delayChar( Char ch, float time ){
		ch.spendConstant(time);
		for (Buff b : ch.buffs()){
			b.spendConstant(time);
		}
	}
	
	public static synchronized Char findChar( int pos ) {
		for (Char ch : chars){
			if (ch.pos == pos)
				return ch;
		}
		return null;
	}

	public static synchronized Actor findById( int id ) {
		return ids.get( id );
	}

	public static synchronized java.util.Set<Actor> all() {
		return Collections.unmodifiableSet(all);
	}

	public static synchronized java.util.Set<Char> chars() { return Collections.unmodifiableSet(chars); }

	//Returns true if any enemy mob is currently in the middle of an animation (waitingForCallback).
	//Used to prevent hero input during enemy attack animations.
	public static synchronized boolean anyEnemyAnimating() {
		for (Char ch : chars) {
			if (ch instanceof com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob
					&& ch.alignment == Char.Alignment.ENEMY
					&& ch.isWaitingForCallback()) {
				return true;
			}
		}
		return false;
	}
}
