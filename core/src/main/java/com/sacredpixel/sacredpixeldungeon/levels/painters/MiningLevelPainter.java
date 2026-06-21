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

package com.sacredpixel.sacredpixeldungeon.levels.painters;

import com.sacredpixel.sacredpixeldungeon.items.Heap;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.quest.DarkGold;
import com.sacredpixel.sacredpixeldungeon.levels.Level;
import com.sacredpixel.sacredpixeldungeon.levels.Terrain;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.Room;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.quest.MineSecretRoom;
import com.watabou.utils.Graph;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

public class MiningLevelPainter extends CavesPainter {

	@Override
	protected int padding(Level level) {
		return 3;
	}

	private int goldToAdd = 0;

	public RegularPainter setGold(int amount){
		goldToAdd = amount;
		return this;
	}

	@Override
	protected void generateGold(Level level, ArrayList<Room> rooms) {
		//we start by counting all the gold purposefully made by rooms
		for (int i = 0; i < level.length(); i++){
			if (level.map[i] == Terrain.WALL_DECO) {
				goldToAdd--;
			}
		}
		for (Heap h : level.heaps.valueList()){
			for (Item i : h.items){
				if (i instanceof DarkGold) goldToAdd -= i.quantity();
			}
		}

		int[] map = level.map;
		int maxAttempts = 50;  // 무한 루프 방지
		int attempts = 0;
		do {
			int goldBefore = goldToAdd;
			Random.shuffle(rooms);
			for (Room r : rooms) {

				if (r instanceof MineSecretRoom) continue;

				ArrayList<Integer> goldPosCandidates = new ArrayList<>();
				for (Point p : r.getPoints()){
					int i = level.pointToCell(p);

					if (level.insideMap(i) && goldToAdd > 0 && map[i] == Terrain.WALL){

						for (int j : PathFinder.NEIGHBOURS4){
							if (level.insideMap(i+j) && map[i+j] != Terrain.WALL){
								goldPosCandidates.add(i);
								break;
							}
						}
					}
				}

				if (goldToAdd > 0 && !goldPosCandidates.isEmpty()){
					int pos = Random.element(goldPosCandidates);

					map[pos] = Terrain.WALL_DECO;
					goldToAdd--;

					if (goldToAdd > 0){
						int i = PathFinder.NEIGHBOURS4[Random.Int(4)];
						if (level.insideMap(pos+i) && map[pos+i] == Terrain.WALL){
							map[pos+i] = Terrain.WALL_DECO;
							goldToAdd--;
						}
						if (Random.Int(2) == 0){
							i = PathFinder.NEIGHBOURS4[Random.Int(4)];
							if (level.insideMap(pos+i) && map[pos+i] == Terrain.WALL){
								map[pos+i] = Terrain.WALL_DECO;
								goldToAdd--;
							}
						}
					}

				}

			}
			// 한 바퀴 돌았는데 금이 하나도 배치되지 않았다면, 더 이상 벽에 배치 불가
			if (goldToAdd == goldBefore) {
				attempts++;
				if (attempts >= maxAttempts) {
					// 벽에 배치할 수 없는 남은 금은 바닥에 DarkGold로 배치
					while (goldToAdd > 0) {
						for (Room r : rooms) {
							if (r instanceof MineSecretRoom) continue;
							for (Point p : r.getPoints()) {
								int cell = level.pointToCell(p);
								if (goldToAdd > 0 && level.insideMap(cell)
										&& (map[cell] == Terrain.EMPTY || map[cell] == Terrain.EMPTY_SP
										|| map[cell] == Terrain.GRASS || map[cell] == Terrain.HIGH_GRASS)
										&& level.heaps.get(cell) == null) {
									level.drop(new DarkGold(), cell);
									goldToAdd--;
									if (goldToAdd <= 0) break;
								}
							}
							if (goldToAdd <= 0) break;
						}
						// 모든 방을 순회해도 배치 못하면 탈출 (안전장치)
						break;
					}
					break;
				}
			} else {
				attempts = 0;  // 금이 배치되었으면 카운터 리셋
			}
		} while (goldToAdd > 0);

	}

	@Override
	protected void paintDoors(Level l, ArrayList<Room> rooms) {
		HashMap<Room, Room> roomMerges = new HashMap<>();

		float hiddenDoorChance = 0.90f;

		//wall doors will still be wall
		//hidden doors become wall tiles a bit later in painting
		//everything else usually becomes empty, but can be wall sometimes
		for (Room r : rooms) {
			for (Room n : r.connected.keySet()) {

				Room.Door d = r.connected.get(n);
				int door = d.x + d.y * l.width();

				if (d.type == Room.Door.Type.WALL || d.type == Room.Door.Type.HIDDEN){
					l.map[door] = Terrain.WALL;
				} else {
					//some of these are randomly hidden, using the same rules as regular levels
					if (Random.Float() < hiddenDoorChance) {
						d.type = Room.Door.Type.HIDDEN;
						Graph.buildDistanceMap(rooms, r);
						if (n.distance == Integer.MAX_VALUE){
							l.map[door] = Terrain.EMPTY;
							d.type = Room.Door.Type.EMPTY;
						} else {
							l.map[door] = Terrain.WALL;
						}
					} else {
						l.map[door] = Terrain.EMPTY;
						d.type = Room.Door.Type.EMPTY;
					}

				}

				//if the door is empty, always merge the rooms
				if (l.map[door] == Terrain.EMPTY){
					if (roomMerges.get(r) == n || roomMerges.get(n) == r){
						continue;
					} else if (mergeRooms(l, r, n, r.connected.get(n), Terrain.EMPTY)) {
						roomMerges.put(r, n);
						roomMerges.put(n, r);
					}
				}

			}
		}
	}

	@Override
	protected void decorate(Level level, ArrayList<Room> rooms) {
		super.decorate(level, rooms);

		//no chasms allowed, replace with ground!
		for (int i = 0; i < level.length(); i++){
			if (level.map[i] == Terrain.CHASM){
				level.map[i] = Terrain.EMPTY;
			}
		}
	}
}
