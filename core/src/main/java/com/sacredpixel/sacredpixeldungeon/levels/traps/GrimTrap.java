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
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.effects.CellEmitter;
import com.sacredpixel.sacredpixeldungeon.effects.MagicMissile;
import com.sacredpixel.sacredpixeldungeon.effects.particles.ShadowParticle;
import com.sacredpixel.sacredpixeldungeon.mechanics.Ballistica;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.tiles.DungeonTilemap;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GrimTrap extends Trap {

	{
		color = GREY;
		shape = LARGE_DOT;

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

					//block actor queue while magic missile animation plays, apply damage on impact
					if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[finalTarget.pos]) {
						try {
							if (finalTarget.sprite != null && finalTarget.sprite.parent != null) {
								Actor.addVfxBlocker();
								((MagicMissile) finalTarget.sprite.parent.recycle(MagicMissile.class)).reset(
										MagicMissile.SHADOW,
										DungeonTilemap.tileCenterToWorld(pos),
										finalTarget.sprite.center(),
										new Callback() {
											@Override
											public void call() {
												//apply damage on missile impact
												int damage = Math.round(finalTarget.HT/2f + finalTarget.HP/2f);
												if (finalTarget == Dungeon.hero){
													damage = (int)Math.min(damage, finalTarget.HT*0.9f);
												}
												finalTarget.damage(damage, GrimTrap.this);
												if (finalTarget == Dungeon.hero) {
													Sample.INSTANCE.play(Assets.Sounds.CURSED);
													if (!finalTarget.isAlive()) {
														Badges.validateDeathFromGrimOrDisintTrap();
														Dungeon.fail(GrimTrap.this);
														GLog.n(Messages.get(GrimTrap.class, "ondeath"));
														if (recl) Badges.validateDeathFromFriendlyMagic();
													}
												} else {
													Sample.INSTANCE.play(Assets.Sounds.BURNING);
												}
												if (finalTarget.sprite != null && finalTarget.sprite.emitter() != null) {
													finalTarget.sprite.emitter().burst(ShadowParticle.UP, 10);
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
					int damage = Math.round(finalTarget.HT/2f + finalTarget.HP/2f);
					if (finalTarget == Dungeon.hero){
						damage = (int)Math.min(damage, finalTarget.HT*0.9f);
					}
					finalTarget.damage(damage, GrimTrap.this);
					if (finalTarget == Dungeon.hero) {
						Sample.INSTANCE.play(Assets.Sounds.CURSED);
						if (!finalTarget.isAlive()) {
							Badges.validateDeathFromGrimOrDisintTrap();
							Dungeon.fail(GrimTrap.this);
							GLog.n(Messages.get(GrimTrap.class, "ondeath"));
							if (reclaimed) Badges.validateDeathFromFriendlyMagic();
						}
					} else {
						Sample.INSTANCE.play(Assets.Sounds.BURNING);
					}
					if (finalTarget.sprite != null && finalTarget.sprite.emitter() != null) {
						finalTarget.sprite.emitter().burst(ShadowParticle.UP, 10);
					}
					Actor.remove(this);
					return true;
				} else {
					Actor.remove(this);
					CellEmitter.get(pos).burst(ShadowParticle.UP, 10);
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
					return true;
				}
			}

		});
	}
}
