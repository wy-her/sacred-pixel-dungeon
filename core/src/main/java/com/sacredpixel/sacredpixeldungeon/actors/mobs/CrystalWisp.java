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

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Invisibility;
import com.sacredpixel.sacredpixeldungeon.levels.Terrain;
import com.sacredpixel.sacredpixeldungeon.mechanics.Ballistica;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.sprites.CharSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.CrystalWispSprite;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class CrystalWisp extends Mob{

	{
		spriteClass = CrystalWispSprite.class;

		HP = HT = 30;
		defenseSkill = 16;

		EXP = 7;
		maxLvl = -2;

		flying = true;

		properties.add(Property.INORGANIC);
	}

	public CrystalWisp(){
		super();
		switch (Random.Int(3)){
			case 0: default:
				spriteClass = CrystalWispSprite.Blue.class;
				break;
			case 1:
				spriteClass = CrystalWispSprite.Green.class;
				break;
			case 2:
				spriteClass = CrystalWispSprite.Red.class;
				break;
		}
	}

	@Override
	public boolean[] modifyPassable(boolean[] passable) {
		for (int i = 0; i < Dungeon.level.length(); i++){
			passable[i] = passable[i] || Dungeon.level.map[i] == Terrain.MINE_CRYSTAL;
		}
		return passable;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 5, 10 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 18;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 5);
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		// Must be able to see the enemy to attack (fixes FOV boundary bugs)
		// Use both mob's fieldOfView AND heroFOV to ensure consistency
		// This prevents attacking when either party can't see the other
		if (!fieldOfView[enemy.pos]) return false;
		// Also check if at least one party is visible in hero's FOV for consistency
		if (!Dungeon.level.heroFOV[pos] && !Dungeon.level.heroFOV[enemy.pos]) return false;
		return super.canAttack(enemy)
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}

	protected boolean doAttack(Char enemy ) {

		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

			return super.doAttack( enemy );

		} else {

			// Reset zapCompleted flag for new zap attack
			zapCompleted = false;

			// Use heroFOV (game state) instead of sprite.visible (render state)
			// to avoid FOV boundary race conditions on HTML5
			boolean inFOV = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos];
			if (sprite != null && inFOV) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				// Out of FOV path - still need to validate attack is legal
				// Re-check fieldOfView to ensure we can actually attack
				if (fieldOfView[enemy.pos]) {
					zap();
					next();
				} else {
					// Cannot attack - enemy left FOV, spend time and move on
					spend(attackDelay());
					next();
				}
				return true;
			}
		}
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}

	//used so resistances can differentiate between melee and magical attacks
	public static class LightBeam {}

	// Flag to prevent onZapComplete() from being called twice
	// (race condition between die() and onComplete() callbacks)
	private boolean zapCompleted = false;

	private void zap() {
		spend( 1f );

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		// Safety check: enemy may have died or become null during callback delay
		if (enemy == null || !enemy.isAlive()) {
			return;
		}
		if (hit( this, enemy, true )) {

			int dmg = Random.NormalIntRange( 5, 10 );
			enemy.damage( dmg, new LightBeam() );

			if (!enemy.isAlive() && enemy == Dungeon.hero) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "beam_kill") );
			}
		} else {
			// Safety check: sprite may be null if enemy is outside FOV
			if (enemy.sprite != null) {
				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
			}
		}
	}

	public void onZapComplete() {
		// Prevent double-call (race condition between die() and onComplete())
		if (zapCompleted) return;
		zapCompleted = true;
		zap();
		next();
	}

	public static final String SPRITE = "sprite";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPRITE, spriteClass);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spriteClass = bundle.getClass(SPRITE);
	}
}
