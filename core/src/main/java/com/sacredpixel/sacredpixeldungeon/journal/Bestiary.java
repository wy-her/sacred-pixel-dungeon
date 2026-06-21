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

package com.sacredpixel.sacredpixeldungeon.journal;

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Acidic;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Albino;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.ArmoredBrute;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.ArmoredStatue;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Bandit;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Bat;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Bee;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Brute;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CausticSlime;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Crab;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalGuardian;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalMimic;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalSpire;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalWisp;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.DM100;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.DM200;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.DM201;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.DM300;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.DemonSpawner;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.DwarfKing;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.EbonyMimic;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Elemental;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Eye;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.FetidRat;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Ghoul;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Gnoll;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollExile;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollGeomancer;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollGuard;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollSapper;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollTrickster;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GoldenMimic;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Golem;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Goo;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.GreatCrab;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Guard;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.HermitCrab;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mimic;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Monk;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Necromancer;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.PhantomPiranha;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Piranha;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Pylon;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Rat;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.RipperDemon;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.RotHeart;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.RotLasher;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Scorpio;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Senior;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Shaman;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Skeleton;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Slime;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Snake;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.SpectralNecromancer;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Spinner;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Statue;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Succubus;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Swarm;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Tengu;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Thief;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.TormentedSpirit;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Warlock;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Wraith;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.YogDzewa;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.YogFist;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Ghost;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Imp;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.RatKing;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Sheep;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.DriedRose;
import com.sacredpixel.sacredpixeldungeon.items.quest.CorpseDust;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfLivingEarth;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfRegrowth;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfWarding;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.special.SentryRoom;
import com.sacredpixel.sacredpixeldungeon.levels.traps.AlarmTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.BlazingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.BurningTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.ChillingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.ConfusionTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.CorrosionTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.CursingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.DisarmingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.DisintegrationTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.DistortionTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.ExplosiveTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.FlashingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.FlockTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.FrostTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GatewayTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GeyserTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GnollRockfallTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GrimTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GrippingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GuardianTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.OozeTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.PitfallTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.PoisonDartTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.RockfallTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.ShockingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.StormTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.SummoningTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.TeleportationTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.TenguDartTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.ToxicTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.WarpingTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.WeakeningTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.WornDartTrap;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.plants.BlandfruitBush;
import com.sacredpixel.sacredpixeldungeon.plants.Blindweed;
import com.sacredpixel.sacredpixeldungeon.plants.Earthroot;
import com.sacredpixel.sacredpixeldungeon.plants.Fadeleaf;
import com.sacredpixel.sacredpixeldungeon.plants.Firebloom;
import com.sacredpixel.sacredpixeldungeon.plants.Icecap;
import com.sacredpixel.sacredpixeldungeon.plants.Mageroyal;
import com.sacredpixel.sacredpixeldungeon.plants.Rotberry;
import com.sacredpixel.sacredpixeldungeon.plants.Sorrowmoss;
import com.sacredpixel.sacredpixeldungeon.plants.Starflower;
import com.sacredpixel.sacredpixeldungeon.plants.Stormvine;
import com.sacredpixel.sacredpixeldungeon.plants.Sungrass;
import com.sacredpixel.sacredpixeldungeon.plants.Swiftthistle;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

//contains all the game's various entities, mostly enemies, NPCS, and allies, but also traps and plants
public enum Bestiary {

	REGIONAL,
	BOSSES,
	UNIVERSAL,
	RARE,
	QUEST,
	NEUTRAL,
	ALLY,
	TRAP,
	PLANT;

	//tracks whether an entity has been encountered
	private final LinkedHashMap<Class<?>, Boolean> seen = new LinkedHashMap<>();
	//tracks enemy kills, trap activations, plant tramples, or just sets to 1 for seen on allies
	private final LinkedHashMap<Class<?>, Integer> encounterCount = new LinkedHashMap<>();

	//should only be used when initializing
	private void addEntities(Class<?>... classes ){
		for (Class<?> cls : classes){
			seen.put(cls, false);
			encounterCount.put(cls, 0);
		}
	}

	public Collection<Class<?>> entities(){
		return seen.keySet();
	}

	public String title(){
		return Messages.get(this, name() + ".title");
	}

	public int totalEntities(){
		return seen.size();
	}

	public int totalSeen(){
		int seenTotal = 0;
		for (boolean entitySeen : seen.values()){
			if (entitySeen) seenTotal++;
		}
		return seenTotal;
	}

	static {

		REGIONAL.addEntities(Rat.class, Snake.class, Gnoll.class, Swarm.class, Crab.class, Slime.class,
				Skeleton.class, Thief.class, DM100.class, Guard.class, Necromancer.class,
				Bat.class, Brute.class, Shaman.RedShaman.class, Shaman.BlueShaman.class, Shaman.PurpleShaman.class, Spinner.class, DM200.class,
				Ghoul.class, Elemental.FireElemental.class, Elemental.FrostElemental.class, Elemental.ShockElemental.class, Warlock.class, Monk.class, Golem.class,
				RipperDemon.class, DemonSpawner.class, Succubus.class, Eye.class, Scorpio.class);

		BOSSES.addEntities(Goo.class,
				Tengu.class,
				Pylon.class, DM300.class,
				DwarfKing.class,
				YogDzewa.Larva.class, YogFist.BurningFist.class, YogFist.SoiledFist.class, YogFist.RottingFist.class, YogFist.RustedFist.class,YogFist.BrightFist.class, YogFist.DarkFist.class, YogDzewa.class);

		UNIVERSAL.addEntities(Wraith.class, Piranha.class, Mimic.class, GoldenMimic.class, EbonyMimic.class, Statue.class, GuardianTrap.Guardian.class, SentryRoom.Sentry.class);

		RARE.addEntities(Albino.class, GnollExile.class, HermitCrab.class, CausticSlime.class,
				Bandit.class, SpectralNecromancer.class,
				ArmoredBrute.class, DM201.class,
				Elemental.ChaosElemental.class, Senior.class,
				Acidic.class,
				TormentedSpirit.class, PhantomPiranha.class, CrystalMimic.class, ArmoredStatue.class);

		QUEST.addEntities(FetidRat.class, GnollTrickster.class, GreatCrab.class,
				Elemental.NewbornFireElemental.class, RotLasher.class, RotHeart.class,
				CrystalWisp.class, CrystalGuardian.class, CrystalSpire.class, GnollGuard.class, GnollSapper.class, GnollGeomancer.class);

		NEUTRAL.addEntities(Ghost.class, RatKing.class, Shopkeeper.class, Wandmaker.class, Blacksmith.class, Imp.class, Sheep.class, Bee.class);

		ALLY.addEntities(MirrorImage.class, PrismaticImage.class,
				DriedRose.GhostHero.class,
				WandOfWarding.Ward.class, WandOfWarding.Ward.WardSentry.class, WandOfLivingEarth.EarthGuardian.class,
				ShadowClone.ShadowAlly.class, SmokeBomb.NinjaLog.class, SpiritHawk.HawkAlly.class, PowerOfMany.LightAlly.class);

		TRAP.addEntities(WornDartTrap.class, PoisonDartTrap.class, DisintegrationTrap.class, GatewayTrap.class,
				ChillingTrap.class, BurningTrap.class, ShockingTrap.class, AlarmTrap.class, GrippingTrap.class, TeleportationTrap.class, OozeTrap.class,
				FrostTrap.class, BlazingTrap.class, StormTrap.class, GuardianTrap.class, FlashingTrap.class, WarpingTrap.class,
				ConfusionTrap.class, ToxicTrap.class, CorrosionTrap.class,
				FlockTrap.class, SummoningTrap.class, WeakeningTrap.class, CursingTrap.class,
				GeyserTrap.class, ExplosiveTrap.class, RockfallTrap.class, PitfallTrap.class,
				DistortionTrap.class, DisarmingTrap.class, GrimTrap.class);

		PLANT.addEntities(Rotberry.class, Sungrass.class, Fadeleaf.class, Icecap.class,
				Firebloom.class, Sorrowmoss.class, Swiftthistle.class, Blindweed.class,
				Stormvine.class, Earthroot.class, Mageroyal.class, Starflower.class,
				BlandfruitBush.class,
				WandOfRegrowth.Dewcatcher.class, WandOfRegrowth.Seedpod.class, WandOfRegrowth.Lotus.class);

	}

	//some mobs and traps have different internal classes in some cases, so need to convert here
	private static final HashMap<Class<?>, Class<?>> classConversions = new HashMap<>();
	static {
		classConversions.put(CorpseDust.DustWraith.class,      Wraith.class);

		classConversions.put(Necromancer.NecroSkeleton.class,  Skeleton.class);

		classConversions.put(TenguDartTrap.class,              PoisonDartTrap.class);
		classConversions.put(GnollRockfallTrap.class,          RockfallTrap.class);

		classConversions.put(DwarfKing.DKGhoul.class,          Ghoul.class);
		classConversions.put(DwarfKing.DKWarlock.class,        Warlock.class);
		classConversions.put(DwarfKing.DKMonk.class,           Monk.class);
		classConversions.put(DwarfKing.DKGolem.class,          Golem.class);

		classConversions.put(YogDzewa.YogRipper.class,         RipperDemon.class);
		classConversions.put(YogDzewa.YogEye.class,            Eye.class);
		classConversions.put(YogDzewa.YogScorpio.class,        Scorpio.class);
	}

	public static boolean isSeen(Class<?> cls){
		for (Bestiary cat : values()) {
			if (cat.seen.containsKey(cls)) {
				return cat.seen.get(cls);
			}
		}
		return false;
	}

	public static void setSeen(Class<?> cls){
		if (classConversions.containsKey(cls)){
			cls = classConversions.get(cls);
		}
		for (Bestiary cat : values()) {
			if (cat.seen.containsKey(cls) && !cat.seen.get(cls)) {
				cat.seen.put(cls, true);
				Journal.saveNeeded = true;
			}
		}
		Badges.validateCatalogBadges();
	}

	public static int encounterCount(Class<?> cls) {
		for (Bestiary cat : values()) {
			if (cat.encounterCount.containsKey(cls)) {
				return cat.encounterCount.get(cls);
			}
		}
		return 0;
	}

	//used primarily when bosses are killed and need to clean up their minions
	public static boolean skipCountingEncounters = false;

	public static void countEncounter(Class<?> cls){
		countEncounters(cls, 1);
	}

	public static void countEncounters(Class<?> cls, int encounters){
		if (skipCountingEncounters){
			return;
		}
		if (classConversions.containsKey(cls)){
			cls = classConversions.get(cls);
		}
		for (Bestiary cat : values()) {
			if (cat.encounterCount.containsKey(cls) && cat.encounterCount.get(cls) != Integer.MAX_VALUE){
				cat.encounterCount.put(cls, cat.encounterCount.get(cls)+encounters);
				if (cat.encounterCount.get(cls) < -1_000_000_000){ //to catch cases of overflow
					cat.encounterCount.put(cls, Integer.MAX_VALUE);
				}
				Journal.saveNeeded = true;
			}
		}
	}

	/**
	 * Resets all bestiary data (seen status and encounter counts).
	 * Used for data import with overwrite policy.
	 */
	public static void reset() {
		for (Bestiary cat : values()) {
			for (Class<?> entity : cat.entities()) {
				cat.seen.put(entity, false);
				cat.encounterCount.put(entity, 0);
			}
		}
		Journal.saveNeeded = true;
	}

	private static final String BESTIARY_CLASSES    = "bestiary_classes";
	private static final String BESTIARY_SEEN       = "bestiary_seen";
	private static final String BESTIARY_ENCOUNTERS = "bestiary_encounters";

	public static void store( Bundle bundle ){

		ArrayList<Class<?>> classes = new ArrayList<>();
		ArrayList<Boolean> seen = new ArrayList<>();
		ArrayList<Integer> encounters = new ArrayList<>();

		for (Bestiary cat : values()) {
			for (Class<?> entity : cat.entities()) {
				if (cat.seen.get(entity) || cat.encounterCount.get(entity) > 0){
					classes.add(entity);
					seen.add(cat.seen.get(entity));
					encounters.add(cat.encounterCount.get(entity));
				}
			}
		}

		Class<?>[] storeCls = new Class[classes.size()];
		boolean[] storeSeen = new boolean[seen.size()];
		int[] storeEncounters = new int[encounters.size()];

		for (int i = 0; i < storeCls.length; i++){
			storeCls[i] = classes.get(i);
			storeSeen[i] = seen.get(i);
			storeEncounters[i] = encounters.get(i);
		}

		bundle.put( BESTIARY_CLASSES, storeCls );
		bundle.put( BESTIARY_SEEN, storeSeen );
		bundle.put( BESTIARY_ENCOUNTERS, storeEncounters );

	}

	public static void restore( Bundle bundle ){

		if (bundle.contains(BESTIARY_CLASSES)
				&& bundle.contains(BESTIARY_SEEN)
				&& bundle.contains(BESTIARY_ENCOUNTERS)){
			Class<?>[] classes = bundle.getClassArray(BESTIARY_CLASSES);
			boolean[] seen = bundle.getBooleanArray(BESTIARY_SEEN);
			int[] encounters = bundle.getIntArray(BESTIARY_ENCOUNTERS);

			for (int i = 0; i < classes.length; i++){
				for (Bestiary cat : values()){
					if (cat.seen.containsKey(classes[i])){
						cat.seen.put(classes[i], seen[i]);
						cat.encounterCount.put(classes[i], encounters[i]);
					}
				}
			}
		}

	}

}
