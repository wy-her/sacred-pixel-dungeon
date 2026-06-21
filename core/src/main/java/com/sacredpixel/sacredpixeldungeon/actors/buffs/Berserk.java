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

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.effects.FloatingText;
import com.sacredpixel.sacredpixeldungeon.effects.SpellSprite;
import com.sacredpixel.sacredpixeldungeon.items.BrokenSeal.WarriorShield;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.CharSprite;
import com.sacredpixel.sacredpixeldungeon.ui.ActionIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.BuffIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

public class Berserk extends ShieldBuff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;

		detachesAtZero = false;
		shieldUsePriority = -1; //other shielding buffs are always consumed first
	}

	private enum State{
		NORMAL, BERSERK, RECOVERING
	}
	private State state = State.NORMAL;

	private static final float LEVEL_RECOVER_START = 4f;
	private float levelRecovery;

	private static final int TURN_RECOVERY_START = 100;
	private int turnRecovery;
	private int turnRecoveryMax = TURN_RECOVERY_START; // Tracks max for icon fade percent

	public int powerLossBuffer = 0;
	private float power = 0;

	private static final String STATE = "state";
	private static final String LEVEL_RECOVERY = "levelrecovery";
	private static final String TURN_RECOVERY = "turn_recovery";
	private static final String TURN_RECOVERY_MAX = "turn_recovery_max";
	private static final String POWER = "power";
	private static final String POWER_BUFFER = "power_buffer";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STATE, state);
		bundle.put(POWER, power);
		bundle.put(POWER_BUFFER, powerLossBuffer);
		bundle.put(LEVEL_RECOVERY, levelRecovery);
		bundle.put(TURN_RECOVERY, turnRecovery);
		bundle.put(TURN_RECOVERY_MAX, turnRecoveryMax);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		state = bundle.getEnum(STATE, State.class);
		power = bundle.getFloat(POWER);
		powerLossBuffer = bundle.getInt(POWER_BUFFER);
		levelRecovery = bundle.getFloat(LEVEL_RECOVERY);
		turnRecovery = bundle.getInt(TURN_RECOVERY);
		turnRecoveryMax = bundle.getInt(TURN_RECOVERY_MAX);
		if (turnRecoveryMax == 0) turnRecoveryMax = TURN_RECOVERY_START; // Fallback for old saves

		if (power >= 1f && state == State.NORMAL){
			ActionIndicator.setAction(this);
		}
	}

	@Override
	public boolean act() {
		if (state == State.BERSERK){
			if (target.shielding() > 0) {
				//lose 2.5% of shielding per turn, but no less than 1
				float dmg = (float)Math.ceil(target.shielding() * 0.025f) * HoldFast.buffDecayFactor(target);
				if (Random.Float() < dmg % 1){
					dmg++;
				}

				ShieldBuff.processDamage(target, (int)dmg, this);

				if (target.shielding() <= 0){
					state = State.RECOVERING;
					power = 0f;
					BuffIndicator.refreshHero();
					if (!target.isAlive()){
						target.die(this);
						if (!target.isAlive()) Dungeon.fail(this);
					}
				}

			} else {
				state = State.RECOVERING;
				power = 0f;
				if (!target.isAlive()){
					target.die(this);
					if (!target.isAlive()) Dungeon.fail(this);
				}

			}
		} else if (state == State.NORMAL) {
			if (powerLossBuffer > 0){
				powerLossBuffer--;
			} else {
				float minPower = getEndlessRageMinimum();
				float previousPower = power;
				power -= GameMath.gate(0.1f, power, 1f) * 0.05f * Math.pow((target.HP / (float) target.HT), 2);

				// Endless Rage protection:
				// - If at or above minimum: don't decay below minimum
				// - If below minimum (e.g., after talent upgrade): don't decay at all
				//   Player must build rage through combat to reach the new minimum
				if (minPower > 0) {
					float effectiveFloor = previousPower < minPower ? previousPower : minPower;
					if (power < effectiveFloor) {
						power = effectiveFloor;
					}
				}

				if (power < 1f){
					ActionIndicator.clearAction(this);
				} else {
					ActionIndicator.refresh();
				}

				// Detach if power drops to 0 or below
				if (power <= 0) {
					detach();
				}
			}
		} else if (state == State.RECOVERING && Regeneration.regenOn()){
			// Turn-based recovery counts down independently of level-based recovery
			// Both counters must reach 0 before returning to NORMAL state
			if (turnRecovery > 0) {
				turnRecovery--;
			}
			if (turnRecovery <= 0 && levelRecovery <= 0){
				turnRecovery = 0;
				levelRecovery = 0;
				state = State.NORMAL;
			}
		}
		spend(TICK);
		return true;
	}

	@Override
	public void detach() {
		super.detach();
		if (state == State.BERSERK) {
			state = State.RECOVERING;
		}
		ActionIndicator.clearAction(this);
	}

	public float enchantFactor(float chance){
		return chance + ((Math.min(1f, power) * 0.15f) * ((Hero) target).pointsInTalent(Talent.ENRAGED_CATALYST));
	}

	public float damageFactor(float dmg){
		return dmg * Math.min(1.5f, 1f + (power / 2f));
	}

	public boolean berserking(){
		if (target.HP == 0
				&& state == State.NORMAL
				&& power >= getDeathlessFuryThreshold()
				&& ((Hero)target).hasTalent(Talent.DEATHLESS_FURY)){
			startBerserking();
			ActionIndicator.clearAction(this);
		}

		return state == State.BERSERK && target.shielding() > 0;
	}

	// Returns the rage threshold for Deathless Fury activation: always 100% for all levels
	private float getDeathlessFuryThreshold(){
		int talentLevel = ((Hero)target).pointsInTalent(Talent.DEATHLESS_FURY);
		if (talentLevel > 0) {
			return 1.0f;  // 100% rage required for all talent levels
		}
		return 999f;  // No talent = impossible threshold (rage max is 100%)
	}

	// Returns the turn-based cooldown for Deathless Fury: 300/200/100 turns at level 1/2/3
	private int getDeathlessFuryCooldown(){
		int talentLevel = ((Hero)target).pointsInTalent(Talent.DEATHLESS_FURY);
		switch (talentLevel){
			case 1: return 300;
			case 2: return 200;
			case 3: return 100;
			default: return 0;
		}
	}

	// Returns the minimum rage floor that doesn't decay: 10%/20%/30% at talent level 1/2/3
	private float getEndlessRageMinimum(){
		int talentLevel = ((Hero)target).pointsInTalent(Talent.ENDLESS_RAGE);
		switch (talentLevel){
			case 1: return 0.10f;  // 10%
			case 2: return 0.20f;  // 20%
			case 3: return 0.30f;  // 30%
			default: return 0f;    // No talent = no minimum
		}
	}

	private void startBerserking(){
		state = State.BERSERK;
		SpellSprite.show(target, SpellSprite.BERSERK);
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
		GameScene.flash(0xFF0000);

		if (target.HP > 0) {
			turnRecovery = TURN_RECOVERY_START;
			turnRecoveryMax = TURN_RECOVERY_START;
			levelRecovery = 0;
		} else {
			// Deathless Fury: Turn-based cooldown (300/200/100 turns at level 1/2/3)
			// No level-based cooldown - only turn-based recovery
			turnRecovery = getDeathlessFuryCooldown();
			turnRecoveryMax = turnRecovery;
			levelRecovery = 0;
		}

		int shieldAmount = currentShieldBoost();
		setShield(shieldAmount);
		target.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(shieldAmount), FloatingText.SHIELDING );

		BuffIndicator.refreshHero();
	}

	public int currentShieldBoost(){
		//base multiplier scales at 1/1.5/2/2.5/3x at 100/37/20/9/0% HP
		float shieldMultiplier = 1f + 2*(float)Math.pow((1f-(target.HP/(float)target.HT)), 3);

		// Endless Rage no longer increases power above 100%, so no bonus multiplier

		int baseShield = 8;
		if (target instanceof Hero && ((Hero) target).belongings.armor() != null){
			baseShield += 2*((Hero) target).belongings.armor().buffedLvl();
		}
		return Math.round(baseShield * shieldMultiplier);
	}

	//not accounting for talents
	public int maxShieldBoost(){
		int baseShield = 8;
		if (target instanceof Hero && ((Hero) target).belongings.armor() != null){
			baseShield += 2*((Hero) target).belongings.armor().buffedLvl();
		}
		return baseShield*3;
	}
	
	public void damage(int damage){
		if (state != State.NORMAL) return;
		float maxPower = 1.0f; // Max rage is always 100%
		power = Math.min(maxPower, power + (damage/(float)target.HT)/4f );
		BuffIndicator.refreshHero(); //show new power immediately
		powerLossBuffer = 3; //2 turns until rage starts dropping
		if (power >= 1f){
			ActionIndicator.setAction(this);
		}
	}

	public void recover(float percent){
		if (state == State.RECOVERING && levelRecovery > 0){
			levelRecovery -= percent;
			if (levelRecovery <= 0) {
				levelRecovery = 0;
				if (turnRecovery == 0){
					state = State.NORMAL;
				}
			}
		}
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.BERSERK;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text((int) (power * 100) + "%");
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x660000;
	}

	@Override
	public void doAction() {
		WarriorShield shield = target.buff(WarriorShield.class);
		if (shield != null && shield.maxShield() > 0) {
			startBerserking();
			ActionIndicator.clearAction(this);
		} else {
			GLog.w(Messages.get(this, "no_seal"));
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.BERSERK;
	}
	
	@Override
	public void tintIcon(Image icon) {
		switch (state){
			case NORMAL: default:
				if (power < 1f) icon.hardlight(1f, 0.5f, 0f);
				else            icon.hardlight(1f, 0f, 0f);
				break;
			case BERSERK:
				icon.hardlight(1f, 0f, 0f);
				break;
			case RECOVERING:
				icon.hardlight(0, 0, 1f);
				break;
		}
	}
	
	@Override
	public float iconFadePercent() {
		switch (state){
			case NORMAL: default:
				// Max power is always 1.0f (100%)
				return (1f - power);
			case BERSERK:
				return 1f - shielding() / (float)maxShieldBoost();
			case RECOVERING:
				// Use turnRecoveryMax for correct percentage calculation
				return turnRecoveryMax > 0 ? turnRecovery/(float)turnRecoveryMax : 0;
		}
	}

	public String iconTextDisplay(){
		switch (state){
			case NORMAL: default:
				return (int)(power*100) + "%";
			case BERSERK:
				return Integer.toString(shielding());
			case RECOVERING:
				// Always show turn recovery (level-based cooldown removed for Deathless Fury)
				return Integer.toString(turnRecovery);
		}
	}

	@Override
	public String name() {
		switch (state){
			case NORMAL: default:
				return Messages.get(this, "angered");
			case BERSERK:
				return Messages.get(this, "berserk");
			case RECOVERING:
				return Messages.get(this, "recovering");
		}
	}

	@Override
	public String desc() {
		float dispDamage = ((int)damageFactor(10000) / 100f) - 100f;
		switch (state){
			case NORMAL: default:
				return Messages.get(this, "angered_desc", Math.floor(power * 100f), dispDamage, currentShieldBoost());
			case BERSERK:
				return Messages.get(this, "berserk_desc", shielding());
			case RECOVERING:
				// Always show turn-based recovery (level-based cooldown removed for Deathless Fury)
				return Messages.get(this, "recovering_desc") + "\n\n" + Messages.get(this, "recovering_desc_turns", turnRecovery);
		}
		
	}
}
