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

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Corruption;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.FlavourBuff;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.cleric.Trinity;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Wraith;
import com.sacredpixel.sacredpixeldungeon.items.armor.ClassArmor;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.Artifact;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.DriedRose;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.EtherealChains;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.HolyTome;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.HornOfPlenty;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.SandalsOfNature;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.SkeletonKey;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.UnstableSpellbook;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.sacredpixel.sacredpixeldungeon.items.rings.RingOfMight;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.plants.Blindweed;
import com.sacredpixel.sacredpixeldungeon.plants.Fadeleaf;
import com.sacredpixel.sacredpixeldungeon.plants.Firebloom;
import com.sacredpixel.sacredpixeldungeon.plants.Icecap;
import com.sacredpixel.sacredpixeldungeon.plants.Sorrowmoss;
import com.sacredpixel.sacredpixeldungeon.plants.Stormvine;
import com.sacredpixel.sacredpixeldungeon.plants.Swiftthistle;
import com.sacredpixel.sacredpixeldungeon.scenes.AlchemyScene;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.ui.BuffIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.sacredpixel.sacredpixeldungeon.ui.QuickSlotButton;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpiritForm extends ClericSpell {

	public static SpiritForm INSTANCE = new SpiritForm();

	@Override
	public int icon() {
		return HeroIcon.SPIRIT_FORM;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", ringLevel(), artifactLevel()) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	@Override
	public float chargeUse(Hero hero) {
		return 4;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.hasTalent(Talent.SPIRIT_FORM);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		GameScene.show(new Trinity.WndItemtypeSelect(tome, this));

	}

	public static int ringLevel(){
		return Dungeon.hero.pointsInTalent(Talent.SPIRIT_FORM);
	}

	public static int artifactLevel(){
		return 2 + 2*Dungeon.hero.pointsInTalent(Talent.SPIRIT_FORM);
	}

	public static class SpiritFormBuff extends FlavourBuff{

		{
			type = buffType.POSITIVE;
		}

		public static final float DURATION = 20f;

		private Bundlable effect;

		@Override
		public int icon() {
			return BuffIndicator.TRINITY_FORM;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0, 1, 0);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		public void setEffect(Bundlable effect){
			this.effect = effect;
			if (effect instanceof RingOfMight){
				((Ring) effect).level(ringLevel());
				Dungeon.hero.updateHT( false );
			}
		}

		@Override
		public void detach() {
			super.detach();
			if (effect instanceof RingOfMight){
				Dungeon.hero.updateHT( false );
			}
		}

		public Ring ring(){
			if (effect instanceof Ring){
				((Ring) effect).level(ringLevel());
				return (Ring) effect;
			}
			return null;
		}

		public Artifact artifact(){
			if (effect instanceof Artifact){
				if (((Artifact) effect).visiblyUpgraded() < artifactLevel()){
					((Artifact) effect).transferUpgrade(artifactLevel() - ((Artifact) effect).visiblyUpgraded());
				}
				return (Artifact) effect;
			}
			return null;
		}

		@Override
		public String desc() {
			if (ring() != null){
				return Messages.get(this, "desc", Messages.titleCase(ring().name()), dispTurns());
			} else if (artifact() != null){
				return Messages.get(this, "desc", Messages.titleCase(artifact().name()), dispTurns());
			}
			return super.desc();
		}

		private static final String EFFECT = "effect";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(EFFECT, effect);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			effect = bundle.get(EFFECT);
		}

	}

	public static void applyActiveArtifactEffect(ClassArmor armor, Artifact effect){
		if (effect instanceof AlchemistsToolkit){
			Talent.onArtifactUsed(Dungeon.hero);
			AlchemyScene.assignToolkit((AlchemistsToolkit) effect);
			Game.switchScene(AlchemyScene.class);

		} else if (effect instanceof DriedRose){
			ArrayList<Integer> spawnPoints = new ArrayList<>();
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				int p = Dungeon.hero.pos + PathFinder.NEIGHBOURS8[i];
				if (Actor.findChar(p) == null && !Dungeon.level.solid[p]) {
					spawnPoints.add(p);
				}
			}
			if (!spawnPoints.isEmpty()) {
				Wraith w = Wraith.spawnAt(Random.element(spawnPoints), Wraith.class);

				w.HP = w.HT = 20 + 8*artifactLevel();
				Buff.affect(w, Corruption.class);
			}
			Talent.onArtifactUsed(Dungeon.hero);
			Dungeon.hero.spendAndNext(1f);

		} else if (effect instanceof EtherealChains){
			GameScene.selectCell(((EtherealChains) effect).caster);
			if (Dungeon.quickslot.contains(armor)) {
				QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(armor));
			}

		} else if (effect instanceof HornOfPlenty){
			((HornOfPlenty) effect).doEatEffect(Dungeon.hero, 1);

		} else if (effect instanceof MasterThievesArmband){
			GameScene.selectCell(((MasterThievesArmband) effect).targeter);
			if (Dungeon.quickslot.contains(armor)) {
				QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(armor));
			}

		} else if (effect instanceof SandalsOfNature){
			((SandalsOfNature) effect).curSeedEffect = Random.oneOf(
					Blindweed.Seed.class, Fadeleaf.Seed.class, Firebloom.Seed.class,
					Icecap.Seed.class, Sorrowmoss.Seed.class, Stormvine.Seed.class
			);

			GameScene.selectCell(((SandalsOfNature) effect).cellSelector);
			if (Dungeon.quickslot.contains(armor)) {
				QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(armor));
			}

		} else if (effect instanceof TalismanOfForesight){
			GameScene.selectCell(((TalismanOfForesight) effect).scry);

		} else if (effect instanceof TimekeepersHourglass){
			Buff.affect(Dungeon.hero, Swiftthistle.TimeBubble.class).reset(artifactLevel());
			Dungeon.hero.spendAndNext(1f);

		} else if (effect instanceof UnstableSpellbook){
			((UnstableSpellbook) effect).doReadEffect(Dungeon.hero);

		} else if (effect instanceof SkeletonKey){
			GameScene.selectCell(((SkeletonKey) effect).targeter);
		}
	}

}
