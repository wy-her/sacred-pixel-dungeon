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
import com.sacredpixel.sacredpixeldungeon.Challenges;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Light;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Paralysis;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.HolyTome;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class Radiance extends ClericSpell {

	public static final Radiance INSTANCE = new Radiance();

	@Override
	public int icon() {
		return HeroIcon.RADIANCE;
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.subClass == HeroSubClass.PRIEST;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		GameScene.flash( 0x80FFFFFF );
		Sample.INSTANCE.play(Assets.Sounds.BLAST);

		if (Dungeon.level.viewDistance < 6 ){
			Buff.prolong(hero, Light.class, Dungeon.isChallenged(Challenges.DARKNESS) ? 20 : 100);
		}

		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos]) {

				if (mob.buff(GuidingLight.Illuminated.class) != null){
					mob.damage(hero.lvl+5, GuidingLight.class);
				} else {
					Buff.affect(mob, GuidingLight.Illuminated.class);
					Buff.affect(mob, GuidingLight.WasIlluminatedTracker.class);
				}
				if (mob.isActive()) {
					Buff.affect(mob, Paralysis.class, 3f);
				}
			}
		}

		hero.spend( 1f );
		hero.busy();
		hero.sprite.operate(hero.pos);

		onSpellCast(tome, hero);

	}
}
