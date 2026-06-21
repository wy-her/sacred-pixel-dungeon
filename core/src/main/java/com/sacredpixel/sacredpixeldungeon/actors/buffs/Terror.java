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

package com.sacredpixel.sacredpixeldungeon.actors.buffs;

import com.sacredpixel.sacredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Terror extends FlavourBuff {

	public int object = 0;

	private static final String OBJECT    = "object";
	private static final String IGNORE_NEXT_HIT = "ignoreNextHit";

	public static final float DURATION = 20f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(OBJECT, object);
		bundle.put(IGNORE_NEXT_HIT, ignoreNextHit);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		object = bundle.getInt( OBJECT );
		ignoreNextHit = bundle.getBoolean( IGNORE_NEXT_HIT );
	}

	@Override
	public int icon() {
		return BuffIndicator.TERROR;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	public boolean ignoreNextHit = false;

	public void recover() {
		if (ignoreNextHit){
			ignoreNextHit = false;
			return;
		}
		spend(-5f);
		if (cooldown() <= 0){
			detach();
		}
	}
}
