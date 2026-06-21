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

package com.sacredpixel.sacredpixeldungeon.actors.hero.spells;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Blindness;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.FlavourBuff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Paralysis;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.effects.Beam;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.HolyTome;
import com.sacredpixel.sacredpixeldungeon.items.wands.Wand;
import com.sacredpixel.sacredpixeldungeon.mechanics.Ballistica;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.tiles.DungeonTilemap;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.sacredpixel.sacredpixeldungeon.ui.QuickSlotButton;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Sunray extends TargetedClericSpell {

	public static final Sunray INSTANCE = new Sunray();

	@Override
	public int icon() {
		return HeroIcon.SUNRAY;
	}

	@Override
	public String desc() {
		int min = Dungeon.hero.pointsInTalent(Talent.SUNRAY) == 2 ? 6 : 4;
		int max = Dungeon.hero.pointsInTalent(Talent.SUNRAY) == 2 ? 12 : 8;
		int dur = Dungeon.hero.pointsInTalent(Talent.SUNRAY) == 2 ? 6 : 4;
		return Messages.get(this, "desc", min, max, dur) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.hasTalent(Talent.SUNRAY);
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		Ballistica aim = new Ballistica(hero.pos, target,  targetingFlags());

		if (Actor.findChar( aim.collisionPos ) == hero){
			GLog.i( Messages.get(Wand.class, "self_target") );
			return;
		}

		if (Actor.findChar(aim.collisionPos) != null) {
			QuickSlotButton.target(Actor.findChar(aim.collisionPos));
		} else {
			QuickSlotButton.target(Actor.findChar(target));
		}

		hero.busy();
		Sample.INSTANCE.play( Assets.Sounds.RAY );
		hero.sprite.zap(target);

		hero.sprite.parent.add(
				new Beam.SunRay(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(aim.collisionPos)));

		Char ch = Actor.findChar( aim.collisionPos );
		if (ch != null) {
			ch.sprite.burst(0xFFFFFF44, 5);

			if (Char.hasProp(ch, Char.Property.UNDEAD) || Char.hasProp(ch, Char.Property.DEMONIC)){
				if (hero.pointsInTalent(Talent.SUNRAY) == 2) {
					ch.damage(12, Sunray.this);
				} else {
					ch.damage(8, Sunray.this);
				}
			} else {
				if (hero.pointsInTalent(Talent.SUNRAY) == 2) {
					ch.damage(Random.NormalIntRange(6, 12), Sunray.this);
				} else {
					ch.damage(Random.NormalIntRange(4, 8), Sunray.this);
				}
			}

			if (ch.isAlive()) {
				if (ch.buff(Blindness.class) != null && ch.buff(SunRayRecentlyBlindedTracker.class) != null) {
					Buff.prolong(ch, Paralysis.class, 2f + 2f*hero.pointsInTalent(Talent.SUNRAY));
					ch.buff(SunRayRecentlyBlindedTracker.class).detach();
				} else if (ch.buff(SunRayUsedTracker.class) == null) {
					Buff.prolong(ch, Blindness.class, 2f + 2f*hero.pointsInTalent(Talent.SUNRAY));
					Buff.prolong(ch, SunRayRecentlyBlindedTracker.class, 2f + 2f*hero.pointsInTalent(Talent.SUNRAY));
					Buff.affect(ch, SunRayUsedTracker.class);
				}
				if (hero.subClass == HeroSubClass.PRIEST){
					Buff.affect(ch, GuidingLight.Illuminated.class);
				}
			}
		}

		hero.spend( 1f );
		hero.next();

		onSpellCast(tome, hero);

	}

	public static class SunRayUsedTracker extends Buff {}
	public static class SunRayRecentlyBlindedTracker extends FlavourBuff {}

}
