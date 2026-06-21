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

package com.sacredpixel.sacredpixeldungeon.items.keys;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.SkeletonKey;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Notes;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.windows.WndJournal;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public abstract class Key extends Item {

	public static final float TIME_TO_UNLOCK = 1f;
	
	{
		stackable = true;
		unique = true;
	}

	//TODO currently keys can only appear on branch = 0, add branch support here if that changes
	public int depth;
	
	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar(item) && ((Key)item).depth == depth;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		Catalog.setSeen(getClass());
		Statistics.itemTypesDiscovered.add(getClass());
		GameScene.pickUpJournal(this, pos);
		WndJournal.last_index = 0;
		Notes.add(this);
		Sample.INSTANCE.play( Assets.Sounds.ITEM );
		hero.spendAndNext( pickupDelay() );
		GameScene.updateKeyDisplay();

		if (hero.buff(SkeletonKey.KeyReplacementTracker.class) != null){
			hero.buff(SkeletonKey.KeyReplacementTracker.class).processExcessKeys();
		}

		return true;
	}

	private static final String DEPTH = "depth";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEPTH, depth );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		depth = bundle.getInt( DEPTH );
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}

}
