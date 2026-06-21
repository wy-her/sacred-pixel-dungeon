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

package com.sacredpixel.sacredpixeldungeon.actors.mobs;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Cripple;
import com.sacredpixel.sacredpixeldungeon.effects.Chains;
import com.sacredpixel.sacredpixeldungeon.effects.Effects;
import com.sacredpixel.sacredpixeldungeon.effects.Pushing;
import com.sacredpixel.sacredpixeldungeon.items.Generator;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.mechanics.Ballistica;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.sprites.GuardSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Guard extends Mob {

	//they can only use their chains once
	private boolean chainsUsed = false;

	{
		spriteClass = GuardSprite.class;

		HP = HT = 40;
		defenseSkill = 10;

		EXP = 7;
		maxLvl = 14;

		loot = Generator.Category.ARMOR;
		lootChance = 0.2f; //by default, see lootChance()

		properties.add(Property.UNDEAD);

		HUNTING = new Hunting();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(4, 12);
	}

	private boolean chain(int target){
		if (chainsUsed || enemy.properties().contains(Property.IMMOVABLE))
			return false;

		Ballistica chain = new Ballistica(pos, target, Ballistica.PROJECTILE);

		if (chain.collisionPos != enemy.pos
				|| chain.path.size() < 2
				|| Dungeon.level.pit[chain.path.get(1)])
			return false;
		else {
			int newPos = -1;
			for (int i : chain.subPath(1, chain.dist)){
				//find the closest position to the guard that's open for the target
				//exclude the guard's own position and the enemy's current position
				if (i != pos && i != enemy.pos
						&& !Dungeon.level.solid[i] && Actor.findChar(i) == null
						&& (Dungeon.level.openSpace[i] || !Char.hasProp(enemy, Property.LARGE))){
					newPos = i;
					break;
				}
			}

			if (newPos == -1){
				return false;
			} else {
				final int newPosFinal = newPos;
				this.target = newPos;

				if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos]) {
					yell(Messages.get(this, "scorpion"));
					new Item().throwSound();
					Sample.INSTANCE.play(Assets.Sounds.CHAINS);

					//Chains and Pushing each manage their own VFX blockers via setBlockingVfx()
					sprite.parent.add(new Chains(sprite.center(),
							enemy.sprite.destinationCenter(),
							Effects.Type.CHAIN,
							new Callback() {
						public void call() {
							Actor.add(new Pushing(enemy, enemy.pos, newPosFinal, new Callback() {
								public void call() {
									pullEnemy(enemy, newPosFinal);
								}
							}).setBlockingVfx());
							next();
						}
					}).setBlockingVfx());
				} else {
					pullEnemy(enemy, newPos);
				}
			}
		}
		//consume the guard's turn so it can't move during the chain pull animation
		spend(TICK);
		chainsUsed = true;
		return true;
	}

	private void pullEnemy( Char enemy, int pullPos ){
		if (Actor.findChar(pullPos) != null && Actor.findChar(pullPos) != enemy) {
			// Target cell is occupied, find nearest valid cell
			int fallback = -1;
			for (int i : PathFinder.NEIGHBOURS8) {
				int adj = pullPos + i;
				if (Dungeon.level.passable[adj] && Actor.findChar(adj) == null) {
					fallback = adj;
					break;
				}
			}
			if (fallback != -1) {
				pullPos = fallback;
			} else {
				return; // no valid cell, abort pull
			}
		}

		if (enemy == Dungeon.hero) {
			Dungeon.hero.interrupt();
		}

		enemy.pos = pullPos;
		enemy.sprite.place(pullPos);
		Dungeon.level.occupyCell(enemy);
		Cripple.prolong(enemy, Cripple.class, 4f);
		if (enemy == Dungeon.hero) {
			Dungeon.observe();
			GameScene.updateFog();
		} else {
			enemy.sprite.visible = Dungeon.level.heroFOV[pullPos];
		}
	}

	@Override
	public int attackSkill( Char target ) {
		return 12;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 7);
	}

	@Override
	public float lootChance() {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/5, 1/15, 1/45, 1/135, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.GUARD_ARM.count);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.GUARD_ARM.count++;
		return super.createLoot();
	}

	private final String CHAINSUSED = "chainsused";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHAINSUSED, chainsUsed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		chainsUsed = bundle.getBoolean(CHAINSUSED);
	}

	private class Hunting extends Mob.Hunting{
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;

			if (!chainsUsed
					&& enemyInFOV
					&& !isCharmedBy( enemy )
					&& !canAttack( enemy )
					&& Dungeon.level.distance( pos, enemy.pos ) < 5
					&& chain(enemy.pos)){
				return !(Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos]);
			} else {
				return super.act( enemyInFOV, justAlerted );
			}

		}
	}
}
