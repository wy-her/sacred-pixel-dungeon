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

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.FireImbue;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.effects.particles.FlameParticle;
import com.sacredpixel.sacredpixeldungeon.items.potions.exotic.PotionOfDragonsBreath;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class ElixirOfDragonsBlood extends Elixir {
	
	{
		image = ItemSpriteSheet.ELIXIR_DRAGON;
	}
	
	@Override
	public void apply(Hero hero) {
		Buff.affect(hero, FireImbue.class).set(FireImbue.DURATION);
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
		hero.sprite.emitter().burst(FlameParticle.FACTORY, 10);
	}
	
	public static class Recipe extends com.sacredpixel.sacredpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfDragonsBreath.class};
			inQuantity = new int[]{1};
			
			cost = 10;
			
			output = ElixirOfDragonsBlood.class;
			outQuantity = 1;
		}
		
	}
}
