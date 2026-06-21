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
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.cleric.AscendedForm;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.HolyTome;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Judgement extends ClericSpell {

	public static Judgement INSTANCE = new Judgement();

	@Override
	public int icon() {
		return HeroIcon.JUDGEMENT;
	}

	@Override
	public float chargeUse(Hero hero) {
		return 3;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero)
				&& hero.hasTalent(Talent.JUDGEMENT)
				&& hero.buff(AscendedForm.AscendBuff.class) != null;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		hero.sprite.attack(hero.pos, new Callback() {
			@Override
			public void call() {
				GameScene.flash( 0x80FFFFFF );
				Sample.INSTANCE.play(Assets.Sounds.BLAST);

				int damageBase = 5 + 5*hero.pointsInTalent(Talent.JUDGEMENT);
				damageBase += Math.round(damageBase*hero.buff(AscendedForm.AscendBuff.class).spellCasts/3f);

				for (Char ch : Actor.chars()){
					if (ch.alignment != hero.alignment && Dungeon.level.heroFOV[ch.pos]){
						ch.damage( Random.NormalIntRange(damageBase, 2*damageBase), Judgement.this);
						if (hero.subClass == HeroSubClass.PRIEST){
							Buff.affect(ch, GuidingLight.Illuminated.class);
						}
					}
				}

				hero.spendAndNext( 1f );
				onSpellCast(tome, hero);

				hero.buff(AscendedForm.AscendBuff.class).spellCasts = 0;

			}
		});
		hero.busy();

	}

	@Override
	public String desc() {
		int baseDmg = 5 + 5*Dungeon.hero.pointsInTalent(Talent.JUDGEMENT);
		int totalBaseDmg = baseDmg;
		if (Dungeon.hero.buff(AscendedForm.AscendBuff.class) != null) {
			totalBaseDmg += Math.round(baseDmg*Dungeon.hero.buff(AscendedForm.AscendBuff.class).spellCasts/3f);
		}

		return Messages.get(this, "desc", baseDmg, 2*baseDmg, totalBaseDmg, 2*totalBaseDmg) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}
}
