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
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.sacredpixel.sacredpixeldungeon.mechanics.Ballistica;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.sprites.MissileSprite;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class WornDartTrap extends Trap {

	{
		color = GREY;
		shape = CROSSHAIR;

		canBeHidden = false;
		avoidsHallways = true;
	}

	@Override
	public void activate() {

		//we handle this inside of a separate actor as the trap may produce a visual effect we need to pause for
		Actor.add(new Actor() {

			{
				actPriority = VFX_PRIO;
			}

			@Override
			protected boolean act() {
				Char target = Actor.findChar(pos);

				//find the closest char that can be aimed at
				//can't target beyond view distance, with a min of 6 (torch range)
				//add 0.5 for better consistency with vision radius shape
				float range = Math.max(6, Dungeon.level.viewDistance)+0.5f;
				if (target == null){
					float closestDist = Float.MAX_VALUE;
					for (Char ch : Actor.chars()){
						if (!ch.isAlive()) continue;
						float curDist = Dungeon.level.trueDistance(pos, ch.pos);
						//invis targets are considered to be at max range
						if (ch.invisible > 0) curDist = Math.max(curDist, range);
						Ballistica bolt = new Ballistica(pos, ch.pos, Ballistica.PROJECTILE);
						if (bolt.collisionPos == ch.pos
								&& ( curDist < closestDist || (curDist == closestDist && target instanceof Hero))){
							target = ch;
							closestDist = curDist;
						}
					}
					if (closestDist > range){
						target = null;
					}
				}

				if (target != null) {
					if (target instanceof Mob){
						Buff.prolong(target, Trap.HazardAssistTracker.class, HazardAssistTracker.DURATION);
					}
					final Char finalTarget = target;
					final boolean recl = reclaimed;

					//block actor queue while dart animation plays, apply damage on impact
					if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[finalTarget.pos]) {
						try {
							if (finalTarget.sprite != null) {
								Actor.addVfxBlocker();
								((MissileSprite) SacredPixelDungeon.scene().recycle(MissileSprite.class)).
										reset(pos, finalTarget.sprite, new Dart(), new Callback() {
											@Override
											public void call() {
												//apply damage on dart impact
												int dmg = Math.max(0, Random.NormalIntRange(4, 8) - finalTarget.drRoll());
												finalTarget.damage(dmg, WornDartTrap.this);
												if (finalTarget == Dungeon.hero && !finalTarget.isAlive()) {
													Dungeon.fail(WornDartTrap.this);
													GLog.n(Messages.get(WornDartTrap.class, "ondeath"));
													if (recl) Badges.validateDeathFromFriendlyMagic();
												}
												Sample.INSTANCE.play(Assets.Sounds.HIT, 1, 1, Random.Float(0.8f, 1.25f));
												if (finalTarget.sprite != null) {
													finalTarget.sprite.bloodBurstA(finalTarget.sprite.center(), dmg);
													finalTarget.sprite.flash();
												}
												Actor.removeVfxBlocker();
											}
										});
								Actor.remove(this);
								return true;
							}
						} catch (Exception e) {
							com.watabou.noosa.Game.reportException(e);
						}
					}
					//not in FOV: apply damage immediately (no visual)
					int dmg = Math.max(0, Random.NormalIntRange(4, 8) - finalTarget.drRoll());
					finalTarget.damage(dmg, WornDartTrap.this);
					if (finalTarget == Dungeon.hero && !finalTarget.isAlive()) {
						Dungeon.fail(WornDartTrap.this);
						GLog.n(Messages.get(WornDartTrap.class, "ondeath"));
						if (reclaimed) Badges.validateDeathFromFriendlyMagic();
					}
					Sample.INSTANCE.play(Assets.Sounds.HIT, 1, 1, Random.Float(0.8f, 1.25f));
					if (finalTarget.sprite != null) {
						finalTarget.sprite.bloodBurstA(finalTarget.sprite.center(), dmg);
						finalTarget.sprite.flash();
					}
					Actor.remove(this);
					return true;
				} else {
					Actor.remove(this);
					return true;
				}
			}

		});
	}
}
