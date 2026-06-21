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

package com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs;

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.AscensionChallenge;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Senior;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.sacredpixel.sacredpixeldungeon.items.Generator;
import com.sacredpixel.sacredpixeldungeon.items.quest.DwarfToken;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.sacredpixel.sacredpixeldungeon.journal.Notes;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.Room;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.quest.AmbitiousImpRoom;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ImpSprite;
import com.sacredpixel.sacredpixeldungeon.windows.WndImp;
import com.sacredpixel.sacredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Imp extends NPC {

	{
		spriteClass = ImpSprite.class;

		properties.add(Property.IMMOVABLE);
	}

	private boolean seenBefore = false;

	@Override
	public Notes.Landmark landmark() {
		return Notes.Landmark.IMP;
	}

	@Override
	protected boolean act() {
		if (Dungeon.hero.buff(AscensionChallenge.class) != null){
			die(null);
			return true;
		}
		if (!Quest.given && Dungeon.level.visited[pos]) {
			if (!seenBefore && Dungeon.level.heroFOV[pos]) {
				yell(Messages.get(this, "hey", Messages.titleCase(Dungeon.hero.name())));
				seenBefore = true;
			}
		} else {
			seenBefore = false;
		}

		return super.act();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//do nothing
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(Char c) {

		sprite.turnTo( pos, Dungeon.hero.pos );

		if (c != Dungeon.hero){
			return true;
		}

		if (Quest.given) {

			DwarfToken tokens = Dungeon.hero.belongings.getItem( DwarfToken.class );
			if (tokens != null && tokens.quantity() >= 1) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndImp( Imp.this, tokens ) );
					}
				});
			} else {
				tell( Messages.get(this, "seniors_2", Messages.titleCase(Dungeon.hero.name())) );
			}

		} else {
			tell( Messages.get(this, "intro") + "\n\n" +
					Messages.get(this, "seniors_1", Messages.titleCase(Dungeon.hero.name())) );
			Quest.given = true;
			Quest.completed = false;

			// Spawn 5 Senior Monks on this floor
			Quest.spawnSeniorMonks();
		}

		return true;
	}

	private void tell( String text ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndQuest( Imp.this, text ));
			}
		});
	}

	public void flee() {

		yell( Messages.get(this, "cya", Messages.titleCase(Dungeon.hero.name())) );

		destroy();
		sprite.die();
	}

	public static class Quest {

		public static boolean spawned;
		public static boolean given;
		public static boolean completed;
		public static int questDepth; // The floor where quest was given

		public static Ring reward;

		public static void reset() {
			spawned = false;
			given = false;
			completed = false;
			questDepth = 0;

			reward = null;
		}

		private static final String NODE		= "demon";

		private static final String SPAWNED		= "spawned";
		private static final String GIVEN		= "given";
		private static final String COMPLETED	= "completed";
		private static final String REWARD		= "reward";
		private static final String QUEST_DEPTH	= "quest_depth";

		public static void storeInBundle( Bundle bundle ) {

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			if (spawned) {
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REWARD, reward );
				node.put( QUEST_DEPTH, questDepth );
			}

			bundle.put( NODE, node );
		}

		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reward = (Ring)node.get( REWARD );
				questDepth = node.getInt( QUEST_DEPTH );
			}
		}

		public static ArrayList<Room> spawn( ArrayList<Room> rooms ) {
			if (!spawned && Dungeon.depth > 16 && Random.Int( 20 - Dungeon.depth ) == 0) {

				rooms.add(new AmbitiousImpRoom());
				spawned = true;
				given = false;

				do {
					reward = (Ring)Generator.random( Generator.Category.RING );
				} while (reward.cursed);
				// Base reward is +0 cursed ring, will be upgraded based on token count
				reward.cursed = true;
			}

			return rooms;
		}

		public static void spawnSeniorMonks() {
			questDepth = Dungeon.depth;

			ArrayList<Integer> candidates = new ArrayList<>();

			// Find valid spawn positions (passable cells not occupied)
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.passable[i]
						&& Dungeon.level.findMob(i) == null
						&& i != Dungeon.hero.pos
						&& Dungeon.level.distance(i, Dungeon.hero.pos) > 4) {
					candidates.add(i);
				}
			}

			// Spawn 5 Senior Monks
			int toSpawn = Math.min(5, candidates.size());
			for (int i = 0; i < toSpawn; i++) {
				int idx = Random.index(candidates);
				int pos = candidates.remove(idx);

				Senior senior = new Senior();
				senior.pos = pos;
				senior.state = senior.WANDERING;
				GameScene.add(senior);
				ScrollOfTeleportation.appear(senior, pos);
			}

			// Notify the player
			GLog.w(Messages.get(Imp.class, "seniors_spawned"));
		}

		public static boolean given(){
			return given;
		}

		public static void process( Mob mob ) {
			// Only process Senior monks on the quest floor
			if (spawned && given && !completed && Dungeon.depth == questDepth) {
				if (mob instanceof Senior) {
					Dungeon.level.drop( new DwarfToken(), mob.pos ).sprite.drop();
				}
			}
		}

		public static Ring getAdjustedReward(int tokenCount) {
			if (reward == null) return null;

			// Calculate upgrade level and curse status based on token count
			// 1 token: +0 cursed, 2 tokens: +1 cursed, 3 tokens: +2 cursed
			// 4 tokens: +3 cursed, 5+ tokens: +3 uncursed
			int upgradeLevel;
			boolean cursed;

			if (tokenCount >= 5) {
				upgradeLevel = 3;
				cursed = false;
			} else {
				upgradeLevel = tokenCount - 1; // 1->0, 2->1, 3->2, 4->3
				cursed = true;
			}

			reward.level(upgradeLevel);
			reward.cursed = cursed;

			return reward;
		}

		public static void complete() {
			reward = null;
			completed = true;

			Statistics.questScores[3] = 4000;
			Notes.remove( Notes.Landmark.IMP );
		}

		public static boolean isCompleted() {
			return spawned && completed;
		}
	}
}
