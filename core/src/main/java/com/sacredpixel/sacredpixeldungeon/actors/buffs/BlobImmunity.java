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

import com.sacredpixel.sacredpixeldungeon.actors.blobs.Blizzard;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.ConfusionGas;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.CorrosiveGas;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Electricity;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Fire;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Freezing;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Inferno;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.ParalyticGas;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Regrowth;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.SmokeScreen;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.StenchGas;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.StormCloud;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.ToxicGas;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Web;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Tengu;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.sacredpixel.sacredpixeldungeon.ui.BuffIndicator;

public class BlobImmunity extends FlavourBuff {
	
	{
		type = buffType.POSITIVE;
	}
	
	public static final float DURATION	= 20f;
	
	@Override
	public int icon() {
		return BuffIndicator.IMMUNITY;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	{
		//all harmful blobs
		immunities.add( Blizzard.class );
		immunities.add( ConfusionGas.class );
		immunities.add( CorrosiveGas.class );
		immunities.add( Electricity.class );
		immunities.add( Fire.class );
		immunities.add( MagicalFireRoom.EternalFire.class );
		immunities.add( Freezing.class );
		immunities.add( Inferno.class );
		immunities.add( ParalyticGas.class );
		immunities.add( Regrowth.class );
		immunities.add( SmokeScreen.class );
		immunities.add( StenchGas.class );
		immunities.add( StormCloud.class );
		immunities.add( ToxicGas.class );
		immunities.add( Web.class );

		immunities.add(Tengu.FireAbility.FireBlob.class);
	}

}
