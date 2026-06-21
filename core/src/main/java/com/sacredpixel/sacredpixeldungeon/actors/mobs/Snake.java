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

package com.sacredpixel.sacredpixeldungeon.actors.mobs;

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.items.Generator;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.sprites.SnakeSprite;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialManager;
import com.watabou.utils.Random;

public class Snake extends Mob {
	
	{
		spriteClass = SnakeSprite.class;
		
		HP = HT = 4;
		defenseSkill = 25;
		
		EXP = 2;
		maxLvl = 7;
		
		loot = Generator.Category.SEED;
		lootChance = 0.25f;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 4 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		// In tutorial level, only surprise attacks can hit the snake
		if (TutorialManager.isTutorialLevel()) {
			// If snake can't see the hero (fieldOfView is null or hero not in FOV),
			// allow the attack to hit (return 0 defense)
			if (fieldOfView == null || !fieldOfView[enemy.pos]) {
				return 0; // Can be hit - surprise attack!
			}
			return INFINITE_EVASION; // Snake sees hero - cannot be hit
		}
		return super.defenseSkill(enemy);
	}

	@Override
	public void damage( int dmg, Object src ) {
		// In tutorial level, if snake can't see the hero, it's a surprise attack - instant kill
		if (TutorialManager.isTutorialLevel() && Dungeon.hero != null) {
			// Check if this snake can see the hero
			if (fieldOfView == null || !fieldOfView[Dungeon.hero.pos]) {
				dmg = HP; // Snake couldn't see hero - surprise attack!
			}
		}
		super.damage(dmg, src);
	}

	private static int dodges = 0;

	@Override
	public String defenseVerb() {
		if (Dungeon.level.heroFOV[pos]) {
			dodges++;
		}
		if ((dodges >= 2 && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS))
				|| (dodges >= 4 && !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_1))){
			GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS);
			dodges = 0;
		}
		return super.defenseVerb();
	}
}
