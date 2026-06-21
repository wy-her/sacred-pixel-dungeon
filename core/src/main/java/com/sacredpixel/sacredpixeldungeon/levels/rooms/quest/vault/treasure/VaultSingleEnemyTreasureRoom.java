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

package com.sacredpixel.sacredpixeldungeon.levels.rooms.quest.vault.treasure;

import com.sacredpixel.sacredpixeldungeon.actors.mobs.VaultRat;
import com.sacredpixel.sacredpixeldungeon.items.Generator;
import com.sacredpixel.sacredpixeldungeon.items.Heap;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.sacredpixel.sacredpixeldungeon.levels.Level;
import com.sacredpixel.sacredpixeldungeon.levels.Terrain;
import com.sacredpixel.sacredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class VaultSingleEnemyTreasureRoom extends VaultTreasureRoom {

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );

		Painter.fillEllipse( level, this, 3, Terrain.EMPTY );

		Painter.drawInside(level, this, entrance(), 3, Terrain.EMPTY);

		VaultRat rat = new VaultRat();
		rat.pos = level.pointToCell(center());
		level.mobs.add(rat);

		int treasurePos;
		if (entrance().x == left){
			treasurePos = rat.pos+2;
		} else if (entrance().y == top){
			treasurePos = rat.pos+2*level.width();
		} else if (entrance().x == right){
			treasurePos = rat.pos-2;
		} else {
			treasurePos = rat.pos-2*level.width();
		}

		Item treasureItem = Generator.randomUsingDefaults(Generator.Category.WEP_T4);
		if (treasureItem.cursed){
			treasureItem.cursed = false;
			if (((MeleeWeapon) treasureItem).hasCurseEnchant()){
				((MeleeWeapon) treasureItem).enchant(null);
			}
		}
		//not true ID
		treasureItem.levelKnown = treasureItem.cursedKnown = true;
		level.drop(treasureItem, treasurePos).type = Heap.Type.CHEST;

		entrance().set(Door.Type.REGULAR);

	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false; //no grass to obstruct vision
	}

	@Override
	public boolean canConnect(Point p) {
		Point c = center();
		return (Math.abs(c.x - p.x) <= 2 || Math.abs(c.y - p.y) <= 2) && super.canConnect(p);
	}
}
