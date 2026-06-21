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

package com.sacredpixel.sacredpixeldungeon.items.armor.glyphs;

import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Charm;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Degrade;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Hex;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.MagicalSleep;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Vulnerable;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Weakness;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.GuidingLight;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.HolyLance;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.Judgement;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.Smite;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.Sunray;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalWisp;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.DM100;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Eye;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Shaman;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Warlock;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.YogFist;
import com.sacredpixel.sacredpixeldungeon.items.armor.Armor;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.sacredpixel.sacredpixeldungeon.items.bombs.ArcaneBomb;
import com.sacredpixel.sacredpixeldungeon.items.bombs.HolyBomb;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.sacredpixel.sacredpixeldungeon.items.wands.CursedWand;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfBlastWave;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfDisintegration;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfFireblast;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfFrost;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfLightning;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfLivingEarth;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfMagicMissile;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfTransfusion;
import com.sacredpixel.sacredpixeldungeon.items.wands.WandOfWarding;
import com.sacredpixel.sacredpixeldungeon.items.weapon.enchantments.Blazing;
import com.sacredpixel.sacredpixeldungeon.items.weapon.enchantments.Grim;
import com.sacredpixel.sacredpixeldungeon.items.weapon.enchantments.Shocking;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.darts.HolyDart;
import com.sacredpixel.sacredpixeldungeon.levels.traps.DisintegrationTrap;
import com.sacredpixel.sacredpixeldungeon.levels.traps.GrimTrap;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class AntiMagic extends Armor.Glyph {

	private static ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x88EEFF );
	
	public static final HashSet<Class> RESISTS = new HashSet<>();
	static {
		RESISTS.add( MagicalSleep.class );
		RESISTS.add( Charm.class );
		RESISTS.add( Weakness.class );
		RESISTS.add( Vulnerable.class );
		RESISTS.add( Hex.class );
		RESISTS.add( Degrade.class );
		
		RESISTS.add( DisintegrationTrap.class );
		RESISTS.add( GrimTrap.class );

		RESISTS.add( ArcaneBomb.class );
		RESISTS.add( HolyBomb.HolyDamage.class );
		RESISTS.add( ScrollOfRetribution.class );
		RESISTS.add( ScrollOfPsionicBlast.class );
		RESISTS.add( ScrollOfTeleportation.class );
		RESISTS.add( HolyDart.class );

		RESISTS.add( GuidingLight.class );
		RESISTS.add( HolyWeapon.class );
		RESISTS.add( Sunray.class );
		RESISTS.add( HolyLance.class );
		RESISTS.add( Smite.class );
		RESISTS.add( Judgement.class );

		RESISTS.add( ElementalBlast.class );
		RESISTS.add( CursedWand.class );
		RESISTS.add( WandOfBlastWave.class );
		RESISTS.add( WandOfDisintegration.class );
		RESISTS.add( WandOfFireblast.class );
		RESISTS.add( WandOfFrost.class );
		RESISTS.add( WandOfLightning.class );
		RESISTS.add( WandOfLivingEarth.class );
		RESISTS.add( WandOfMagicMissile.class );
		RESISTS.add( WandOfPrismaticLight.class );
		RESISTS.add( WandOfTransfusion.class );
		RESISTS.add( WandOfWarding.Ward.class );

		RESISTS.add( ChaliceOfBlood.class );

		RESISTS.add( ElementalStrike.class );
		RESISTS.add( Blazing.class );
		RESISTS.add( Shocking.class );
		RESISTS.add( Grim.class );

		RESISTS.add( WarpBeacon.class );
		
		RESISTS.add( DM100.LightningBolt.class );
		RESISTS.add( Shaman.EarthenBolt.class );
		RESISTS.add( CrystalWisp.LightBeam.class );
		RESISTS.add( Warlock.DarkBolt.class );
		RESISTS.add( Eye.DeathGaze.class );
		RESISTS.add( YogFist.BrightFist.LightBeam.class );
		RESISTS.add( YogFist.DarkFist.DarkBolt.class );
	}
	
	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		//no proc effect, triggers in Char.damage
		return damage;
	}
	
	public static int drRoll( Char owner, int level ){
		if (level == -1){
			return 0;
		} else {
			return Random.NormalIntRange(
					Math.round(level * genericProcChanceMultiplier(owner)),
					Math.round((3 + (level * 1.5f)) * genericProcChanceMultiplier(owner)));
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return TEAL;
	}

}