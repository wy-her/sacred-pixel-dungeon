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

package com.sacredpixel.sacredpixeldungeon.levels;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.AscensionChallenge;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.LostInventory;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Imp;
import com.sacredpixel.sacredpixeldungeon.effects.particles.ElmoParticle;
import com.sacredpixel.sacredpixeldungeon.items.armor.ClassArmor;
import com.sacredpixel.sacredpixeldungeon.items.armor.ClothArmor;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.Artifact;
import com.sacredpixel.sacredpixeldungeon.items.quest.EscapeCrystal;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.sacredpixel.sacredpixeldungeon.items.wands.Wand;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.sacredpixel.sacredpixeldungeon.levels.features.LevelTransition;
import com.sacredpixel.sacredpixeldungeon.levels.painters.CityPainter;
import com.sacredpixel.sacredpixeldungeon.levels.painters.Painter;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.Room;
import com.sacredpixel.sacredpixeldungeon.levels.traps.BlazingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.CorrosionTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.CursingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.DisarmingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.DisintegrationTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.DistortionTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.FlashingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.FrostTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GatewayTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GeyserTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GuardianTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.PitfallTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.RockfallTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.StormTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.SummoningTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.WarpingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.WeakeningTrap;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.tiles.DungeonTilemap;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CityLevel extends RegularLevel {

	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
	}

	public static final String[] CITY_TRACK_LIST
			= new String[]{Assets.Music.CITY_1, Assets.Music.CITY_2, Assets.Music.CITY_2,
			Assets.Music.CITY_1, Assets.Music.CITY_3, Assets.Music.CITY_3};
	public static final float[] CITY_TRACK_CHANCES = new float[]{1f, 1f, 0.5f, 0.25f, 1f, 0.5f};

	@Override
	public void playLevelMusic() {
		if (Statistics.amuletObtained){
			Music.INSTANCE.play(Assets.Music.CITY_TENSE, true);
		} else {
			Music.INSTANCE.playTracks(CITY_TRACK_LIST, CITY_TRACK_CHANCES, false);
		}
	}

	@Override
	protected int standardRooms(boolean forceMax) {
		if (forceMax) return 8;
		//6 to 8, average 7
		return 6+Random.chances(new float[]{1, 3, 1});
	}

	@Override
	protected int specialRooms(boolean forceMax) {
		if (forceMax) return 3;
		//2 to 3, average 2.33
		return 2 + Random.chances(new float[]{2, 1});
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CITY;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CITY;
	}

	@Override
	protected Painter painter() {
		return new CityPainter()
				.setWater(feeling == Feeling.WATER ? 0.90f : 0.30f, 4)
				.setGrass(feeling == Feeling.GRASS ? 0.80f : 0.20f, 3)
				.setTraps(nTraps(), trapClasses(), trapChances());
	}

	@Override
	protected Class<?>[] trapClasses() {
		return new Class[]{
				FrostTrap.class, StormTrap.class, CorrosionTrap.class, BlazingTrap.class, DisintegrationTrap.class,
				RockfallTrap.class, FlashingTrap.class, GuardianTrap.class, WeakeningTrap.class,
				DisarmingTrap.class, SummoningTrap.class, WarpingTrap.class, CursingTrap.class, PitfallTrap.class, DistortionTrap.class, GatewayTrap.class, GeyserTrap.class };
	}

	@Override
	protected float[] trapChances() {
		return new float[]{
				4, 4, 4, 4, 4,
				2, 2, 2, 2,
				1, 1, 1, 1, 1, 1, 1, 1 };
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition.type == LevelTransition.Type.BRANCH_EXIT) {
			// Vault quest is disabled for now
			return false;

		} else {
			return super.activateTransition(hero, transition);
		}
	}

	@Override
	protected ArrayList<Room> initRooms() {
		return Imp.Quest.spawn(super.initRooms());
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.WATER:
				return Messages.get(CityLevel.class, "water_name");
			case Terrain.HIGH_GRASS:
				return Messages.get(CityLevel.class, "high_grass_name");
			case Terrain.REGION_DECO:
			case Terrain.REGION_DECO_ALT:
				return Messages.get(CityLevel.class, "region_deco_name");
			default:
				return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
			case Terrain.ENTRANCE:
			case Terrain.ENTRANCE_SP:
				return Messages.get(CityLevel.class, "entrance_desc");
			case Terrain.EXIT:
				return Messages.get(CityLevel.class, "exit_desc");
			case Terrain.WALL_DECO:
			case Terrain.EMPTY_DECO:
				return Messages.get(CityLevel.class, "deco_desc");
			case Terrain.EMPTY_SP:
				return Messages.get(CityLevel.class, "sp_desc");
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Messages.get(CityLevel.class, "statue_desc");
			case Terrain.BOOKSHELF:
				return Messages.get(CityLevel.class, "bookshelf_desc");
			case Terrain.REGION_DECO:
			case Terrain.REGION_DECO_ALT:
				return Messages.get(CityLevel.class, "region_deco_desc");
			default:
				return super.tileDesc( tile );
		}
	}

	@Override
	public Group addVisuals() {
		super.addVisuals();
		addCityVisuals( this, visuals );
		return visuals;
	}

	public static void addCityVisuals( Level level, Group group ) {
		for (int i=0; i < level.length(); i++) {
			if (level.map[i] == Terrain.WALL_DECO) {
				group.add( new Smoke( i ) );
			}
		}
	}

	@Override
	public Group addWallVisuals() {
		super.addWallVisuals();
		addCityWallVisuals( this, wallVisuals );
		return wallVisuals;
	}

	public static void addCityWallVisuals( Level level, Group group ) {
		for (int i=0; i < level.length(); i++) {
			if (level.map[i] == Terrain.REGION_DECO || level.map[i] == Terrain.REGION_DECO_ALT) {
				group.add( new GreenFlame( i ) );
			}
		}
	}

	public static class GreenFlame extends Emitter {

		private int pos;

		public static final Emitter.Factory factory = new Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				GreenFlameParticle p = (GreenFlameParticle)emitter.recycle( GreenFlameParticle.class );
				p.reset( x, y );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};

		public GreenFlame( int pos ) {
			super();

			this.pos = pos;

			PointF p = DungeonTilemap.raisedTileCenterToWorld( pos );
			pos( p.x - 2, p.y - 5, 4, 4 );

			pour( factory, 0.1f );
		}

		@Override
		public void update() {
			if (visible = (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
				super.update();
			}
		}

	}

	public static class GreenFlameParticle extends ElmoParticle {

		public GreenFlameParticle(){
			super();
			acc.set( 0, -40 );
		}

	}


	public static class Smoke extends Emitter {

		private int pos;

		public static final Emitter.Factory factory = new Factory() {

			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				SmokeParticle p = (SmokeParticle)emitter.recycle( SmokeParticle.class );
				p.reset( x, y );
			}
		};

		public Smoke( int pos ) {
			super();

			this.pos = pos;

			PointF p = DungeonTilemap.tileCenterToWorld( pos );
			pos( p.x - 6, p.y - 4, 12, 12 );

			pour( factory, 0.2f );
		}

		@Override
		public void update() {
			if (visible = (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
				super.update();
			}
		}
	}

	public static final class SmokeParticle extends PixelParticle {

		public SmokeParticle() {
			super();

			color( 0x000000 );
			speed.set( Random.Float( -2, 4 ), -Random.Float( 3, 6 ) );
		}

		public void reset( float x, float y ) {
			revive();

			this.x = x;
			this.y = y;

			left = lifespan = 2f;
		}

		@Override
		public void update() {
			super.update();
			float p = left / lifespan;
			am = p > 0.8f ? 1 - p : p * 0.25f;
			size( 6 - p * 3 );
		}
	}
}
