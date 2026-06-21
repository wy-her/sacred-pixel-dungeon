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
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalWisp;
import com.sacredpixel.sacredpixeldungeon.effects.Beam;
import com.sacredpixel.sacredpixeldungeon.effects.TorchHalo;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.PointF;

public abstract class CrystalWispSprite extends MobSprite {

	private TorchHalo light;

	public CrystalWispSprite() {
		super();

		int c = texOffset();

		texture( Assets.Sprites.CRYSTAL_WISP );

		TextureFilm frames = new TextureFilm( texture, 12, 14 );

		idle = new Animation( 1, true );
		idle.frames( frames, c+0 );

		run = new Animation( 12, true );
		run.frames( frames, c+0, c+0, c+0, c+1 );

		attack = new Animation( 16, false );
		attack.frames( frames, c+2, c+3, c+4, c+5 );

		zap = attack.clone();

		die = new Animation( 15, false );
		die.frames( frames, c+6, c+7, c+8, c+9, c+10, c+11, c+12, c+11 );

		play( idle );
	}

	private int zapCell;

	public synchronized void zap( int cell ) {

		zapCell = cell;
		super.zap( cell );

		// Store visibility state at zap start for visual effects
		final boolean wasVisible = visible;

		// Only add visual tweener if light and parent are available
		// The actual game logic (onZapComplete) is handled in onComplete(Animation)
		// to match Eye's robust callback pattern
		if (light != null && parent != null) {
			parent.add(new AlphaTweener(light, 1f, 0.2f) {
				@Override
				public void onComplete() {
					if (light != null) light.alpha(0.3f);
					// Visual effects only - game logic handled in onComplete(Animation)
					if (wasVisible && parent != null) {
						Beam ray = new Beam.LightRay(center(), DungeonTilemap.raisedTileCenterToWorld(cell));
						Sample.INSTANCE.play( Assets.Sounds.RAY );
						ray.hardlight(blood() & 0x00FFFFFF);
						parent.add( ray );
					}
				}
			});
		}
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
			// Always call onZapComplete to complete the turn - matches Eye's pattern
			// This ensures game logic completes even if AlphaTweener fails
			if (ch != null && ch instanceof CrystalWisp) {
				((CrystalWisp)ch).onZapComplete();
			}
		}
		super.onComplete( anim );
	}

	@Override
	public synchronized void attack(int cell) {
		super.attack(cell);
		parent.add(new AlphaTweener(light, 1f, 0.2f) {
			@Override
			public void onComplete() {
				light.alpha(0.3f);
			}
		});
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		light = new TorchHalo( this );
		light.hardlight(blood() & 0x00FFFFFF);
		light.alpha(0.3f);
		light.radius(10);

		GameScene.effect(light);
	}

	@Override
	public void die() {
		// If zap animation was in progress, complete game logic before dying
		// Otherwise onZapComplete() would never be called and turn would stall
		if (curAnim == zap && ch != null && ch instanceof CrystalWisp) {
			((CrystalWisp)ch).onZapComplete();
		}
		super.die();
		if (light != null){
			light.putOut();
		}
	}

	@Override
	public void kill() {
		super.kill();
		if (light != null){
			light.killAndErase();
		}
	}

	private float baseY = Float.NaN;

	@Override
	public void place(int cell) {
		super.place(cell);
		baseY = y;
	}

	@Override
	public PointF point(PointF p) {
		super.point(p);
		baseY = y;
		return p;
	}

	@Override
	public void move(int from, int to) {
		super.move(from, to);
		// Reset baseY to NaN during movement - will be recalculated when movement completes
		baseY = Float.NaN;
	}

	@Override
	public void onComplete(com.watabou.noosa.tweeners.Tweener tweener) {
		super.onComplete(tweener);
		// Sync baseY after movement completes to prevent position drift
		if (tweener == motion) {
			baseY = y;
		}
	}

	@Override
	public void update() {
		super.update();

		// Don't apply floating animation during movement (isMoving check)
		// This prevents Y-coordinate conflict between PosTweener and floating animation
		if (!paused && curAnim != die && !isMoving){
			if (Float.isNaN(baseY)) baseY = y;
			y = baseY + Math.abs((float)Math.sin(Game.timeTotal));
			shadowOffset = 0.25f - 0.8f*Math.abs((float)Math.sin(Game.timeTotal));
		}

		if (light != null){
			light.visible = visible;
			light.point(center());

		}
	}

	@Override
	public void turnTo(int from, int to) {
		//do nothing
	}

	protected abstract int texOffset();

	public static class Blue extends CrystalWispSprite {
		@Override
		protected int texOffset() {
			return 0;
		}
		@Override
		public int blood() {
			return 0xFF66B3FF;
		}
	}

	public static class Green extends CrystalWispSprite {
		@Override
		protected int texOffset() {
			return 13;
		}
		@Override
		public int blood() {
			return 0xFF2EE62E;
		}
	}

	public static class Red extends CrystalWispSprite {
		@Override
		protected int texOffset() {
			return 26;
		}
		@Override
		public int blood() {
			return 0xFFFF7F00;
		}
	}

}
