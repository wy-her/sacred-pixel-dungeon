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

package com.sacredpixel.sacredpixeldungeon.levels.rooms.quest;

import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Imp;
import com.sacredpixel.sacredpixeldungeon.levels.Level;
import com.sacredpixel.sacredpixeldungeon.levels.Terrain;
import com.sacredpixel.sacredpixeldungeon.levels.painters.Painter;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.watabou.utils.Point;

public class AmbitiousImpRoom extends SpecialRoom {

	@Override
	public int maxWidth() { return 9; }
	public int minWidth() { return 9; }
	public int maxHeight() { return 9; }
	public int minHeight() { return 9; }

	@Override
	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );      // Regular tiles (city theme) for 7x7 inner area

		Point c = center();

		// Carpet only inside pillars (center 3x3 area) - pillars are at ±2, so carpet is at ±1
		Painter.fill( level, c.x-1, c.y-1, 3, 3, Terrain.EMPTY_SP );

		// Pillars at center ±2
		Painter.set(level, c.x-2, c.y-2, Terrain.STATUE);
		Painter.set(level, c.x+2, c.y-2, Terrain.STATUE);
		Painter.set(level, c.x-2, c.y+2, Terrain.STATUE);
		Painter.set(level, c.x+2, c.y+2, Terrain.STATUE);

		Door entrance = entrance();
		Imp npc = new Imp();
		npc.pos = level.pointToCell(c);  // Place Imp at exact center
		level.mobs.add( npc );

		Painter.drawInside(level, this, entrance, 1, Terrain.EMPTY);
		entrance.set( Door.Type.REGULAR );
	}

	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		return Point.distance(p, center()) >= 2;
	}

	@Override
	public boolean canPlaceItem(Point p, Level l) {
		return false;
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}
}
