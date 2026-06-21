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

package com.sacredpixel.sacredpixeldungeon.items.potions.elixirs;

import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.ToxicImbue;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.effects.particles.PoisonParticle;
import com.sacredpixel.sacredpixeldungeon.items.potions.exotic.PotionOfCorrosiveGas;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;

public class ElixirOfToxicEssence extends Elixir {
	
	{
		image = ItemSpriteSheet.ELIXIR_TOXIC;
	}
	
	@Override
	public void apply(Hero hero) {
		Buff.affect(hero, ToxicImbue.class).set(ToxicImbue.DURATION);
		hero.sprite.emitter().burst(PoisonParticle.SPLASH, 10);
	}
	
	public static class Recipe extends com.sacredpixel.sacredpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfCorrosiveGas.class};
			inQuantity = new int[]{1};
			
			cost = 8;
			
			output = ElixirOfToxicEssence.class;
			outQuantity = 1;
		}
		
	}
	
}
