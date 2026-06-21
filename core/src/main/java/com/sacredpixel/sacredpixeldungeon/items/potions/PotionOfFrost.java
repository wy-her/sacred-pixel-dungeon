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

package com.sacredpixel.sacredpixeldungeon.items.potions;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Blob;
import com.sacredpixel.sacredpixeldungeon.actors.blobs.Freezing;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class PotionOfFrost extends Potion {

	{
		icon = ItemSpriteSheet.Icons.POTION_FROST;
	}
	
	@Override
	public void shatter( int cell ) {

		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			identify();

			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		}
		
		for (int offset : PathFinder.NEIGHBOURS9){
			if (!Dungeon.level.solid[cell+offset]) {
				
				GameScene.add(Blob.seed(cell + offset, 10, Freezing.class));
				
			}
		}
		
	}
	
	@Override
	public int value() {
		return isKnown() ? 30 * quantity : super.value();
	}
}
