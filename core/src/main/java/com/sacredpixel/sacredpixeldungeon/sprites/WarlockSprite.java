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

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Warlock;
import com.sacredpixel.sacredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class WarlockSprite extends MobSprite {

	public WarlockSprite() {
		super();
		
		texture( Assets.Sprites.WARLOCK );
		
		TextureFilm frames = new TextureFilm( texture, 12, 15 );
		
		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );
		
		run = new Animation( 15, true );
		run.frames( frames, 0, 2, 3, 4 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 0, 5, 6 );
		
		zap = attack.clone();
		
		die = new Animation( 15, false );
		die.frames( frames, 0, 7, 8, 8, 9, 10 );
		
		play( idle );
	}
	
	public void zap( int cell ) {

		super.zap( cell );

		// MagicMissile callback handles game logic when projectile arrives
		// This ensures damage timing matches the visual (missile lands → damage)
		MagicMissile.boltFromChar( parent,
				MagicMissile.SHADOW,
				this,
				cell,
				new Callback() {
					@Override
					public void call() {
						if (ch != null && ch instanceof Warlock) {
							((Warlock)ch).onZapComplete();
						}
					}
				} );
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}
}
