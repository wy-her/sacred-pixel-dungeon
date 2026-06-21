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

package com.sacredpixel.sacredpixeldungeon;

import com.sacredpixel.sacredpixeldungeon.CloudSave;
import com.sacredpixel.sacredpixeldungeon.Leaderboard;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Belongings;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.items.Generator;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.bags.Bag;
import com.sacredpixel.sacredpixeldungeon.items.potions.Potion;
import com.sacredpixel.sacredpixeldungeon.items.quest.CorpseDust;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.Scroll;
import com.sacredpixel.sacredpixeldungeon.items.trinkets.Trinket;
import com.sacredpixel.sacredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.sacredpixel.sacredpixeldungeon.journal.Notes;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.ui.QuickSlotButton;
import com.sacredpixel.sacredpixeldungeon.ui.Toolbar;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import com.watabou.utils.DateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Rankings {
	
	INSTANCE;
	
	// Original: Top 10 + 1 structure
	// public static final int TABLE_SIZE	= 11;
	// Changed to Top 5 + 1 for shorter URL export
	public static final int TABLE_SIZE	= 6;

	private static String generateRandomId() {
		long hi = com.watabou.utils.Random.Long(Long.MAX_VALUE);
		long lo = com.watabou.utils.Random.Long(Long.MAX_VALUE);
		return Long.toHexString(hi) + Long.toHexString(lo);
	}
	
	public static final String RANKINGS_FILE = "rankings.dat";
	
	public ArrayList<Record> records;
	public int lastRecord;
	public int totalNumber;
	public int wonNumber;

	//The number of runs which are only present locally, not in the cloud
	public int localTotal;
	public int localWon;

	public void submit( boolean win, Object cause ) {

		load();
		
		Record rec = new Record();

		//we trim version to just the numbers, ignoring alpha/beta, etc.
		Pattern p = Pattern.compile("\\d+\\.\\d+\\.\\d+");
		Matcher m = p.matcher(SacredPixelDungeon.version);
		if (m.find()) {
			rec.version = "v" + m.group(0);
		} else {
			rec.version = "";
		}

		rec.date = DateCompat.formatDateUTC(Game.realTime);

		rec.cause = cause instanceof Class ? (Class)cause : cause.getClass();
		rec.win		= win;
		rec.heroClass	= Dungeon.hero.heroClass;
		rec.armorTier	= Dungeon.hero.tier();
		rec.herolevel	= Dungeon.hero.lvl;
		if (Statistics.highestAscent == 0){
			rec.depth = Statistics.deepestFloor;
			rec.ascending = false;
		} else {
			rec.depth = Statistics.highestAscent;
			rec.ascending = true;
		}
		rec.score       = calculateScore();
		rec.customSeed  = Dungeon.customSeedText;

		// Copy score breakdown from Statistics to Record (for URL export)
		rec.progressScore   = Statistics.progressScore;
		rec.treasureScore   = Statistics.treasureScore;
		rec.exploreScore    = Statistics.exploreScore;
		rec.totalBossScore  = Statistics.totalBossScore;
		rec.totalQuestScore = Statistics.totalQuestScore;
		rec.winMultiplier   = Statistics.winMultiplier;
		rec.chalMultiplier  = Statistics.chalMultiplier;
		rec.scoreBreakdownKnown = true;

		// Copy additional context for Replay and URL export
		rec.challenges      = Dungeon.challenges;
		rec.dungeonSeed     = Dungeon.seed;
		rec.duration        = Statistics.duration;

		// Copy statistics for URL export (v0x06)
		rec.heroSTR         = Dungeon.hero.STR;
		rec.strBonus        = Dungeon.hero.STR() - Dungeon.hero.STR;
		rec.enemiesSlain    = Statistics.enemiesSlain;
		rec.goldCollected   = Statistics.goldCollected;
		rec.foodEaten       = Statistics.foodEaten;
		rec.itemsCrafted    = Statistics.itemsCrafted;

		Badges.validateHighScore( rec.score );

		INSTANCE.saveGameData(rec);

		rec.gameID = generateRandomId();

		records.add( rec );
		
		Collections.sort( records, scoreComparator );
		
		lastRecord = records.indexOf( rec );
		int size = records.size();
		while (size > TABLE_SIZE) {

			if (lastRecord == size - 1) {
				records.remove( size - 2 );
				lastRecord--;
			} else {
				records.remove( size - 1 );
			}

			size = records.size();
		}

		if (rec.customSeed.isEmpty()) {
			totalNumber++;
			if (win) {
				wonNumber++;
			}
		}

		Badges.validateGamesPlayed();

		// Submit score to Appsintoss leaderboard (only for non-custom-seed runs)
		if (Leaderboard.isAvailable() && rec.customSeed.isEmpty() && rec.score > 0) {
			Leaderboard.submit(rec.score);
		}

		save();

		// Trigger cloud save on WIN only (Appsintoss) - reduces Firebase writes
		if (win && CloudSave.isAvailable()) {
			CloudSave.saveToCloud();
		}
	}

	private int score( boolean win ) {
		return (Statistics.goldCollected + Dungeon.hero.lvl * (win ? 26 : Dungeon.depth ) * 100) * (win ? 2 : 1);
	}

	//assumes a ranking is loaded, or game is ending
	public int calculateScore(){

		if (Dungeon.initialVersion > SacredPixelDungeon.v1_2_3){
			Statistics.progressScore = Dungeon.hero.lvl * Statistics.deepestFloor * 65;
			Statistics.progressScore = Math.min(Statistics.progressScore, 50_000);

			if (Statistics.heldItemValue == 0) {
				for (Item i : Dungeon.hero.belongings) {
					Statistics.heldItemValue += i.value();
					if (i instanceof CorpseDust && Statistics.deepestFloor >= 10){
						// in case player kept the corpse dust, for a necromancer run
						//we also override the score here, ignoring penalties
						Statistics.questScores[1] = 2000;
					}
				}
			}
			Statistics.treasureScore = Statistics.goldCollected + Statistics.heldItemValue;
			Statistics.treasureScore = Math.min(Statistics.treasureScore, 20_000);

			Statistics.exploreScore = 0;
			int scorePerFloor = Statistics.floorsExplored.size * 50;
			for (float percentExplored : Statistics.floorsExplored.valueList()){
				Statistics.exploreScore += Math.round(percentExplored*scorePerFloor);
			}

			Statistics.totalBossScore = 0;
			for (int i : Statistics.bossScores){
				if (i > 0) Statistics.totalBossScore += i;
			}

			Statistics.totalQuestScore = 0;
			for (int i : Statistics.questScores){
				if (i > 0) Statistics.totalQuestScore += i;
			}

			Statistics.winMultiplier = 1f;
			if (Statistics.gameWon)         Statistics.winMultiplier += 1f;
			if (Statistics.ascended)        Statistics.winMultiplier += 0.5f;

		//pre v1.3.0 runs have different score calculations
		//only progress and treasure score, and they are each up to 50% bigger
		//win multiplier is a simple 2x if run was a win, challenge multi is the same as 1.3.0
		} else {
			Statistics.progressScore = Dungeon.hero.lvl * Statistics.deepestFloor * 100;
			Statistics.treasureScore = Math.min(Statistics.goldCollected, 30_000);

			Statistics.exploreScore = Statistics.totalBossScore = Statistics.totalQuestScore = 0;

			Statistics.winMultiplier = Statistics.gameWon ? 2 : 1;

		}

		Statistics.chalMultiplier = (float)Math.pow(1.25, Challenges.activeChallenges());
		Statistics.chalMultiplier = Math.round(Statistics.chalMultiplier*20f)/20f;

		Statistics.totalScore = Statistics.progressScore + Statistics.treasureScore + Statistics.exploreScore
					+ Statistics.totalBossScore + Statistics.totalQuestScore;

		Statistics.totalScore *= Statistics.winMultiplier * Statistics.chalMultiplier;

		return Statistics.totalScore;
	}

	public static final String HERO         = "hero";
	public static final String STATS        = "stats";
	public static final String BADGES       = "badges";
	public static final String HANDLERS     = "handlers";
	public static final String CHALLENGES   = "challenges";
	public static final String GAME_VERSION = "game_version";
	public static final String SEED         = "seed";
	public static final String CUSTOM_SEED	= "custom_seed";

	public void saveGameData(Record rec){
		if (Dungeon.hero == null){
			rec.gameData = null;
			return;
		}

		rec.gameData = new Bundle();

		Belongings belongings = Dungeon.hero.belongings;

		//save the hero and belongings
		ArrayList<Item> allItems = (ArrayList<Item>) belongings.backpack.items.clone();
		//remove items that won't show up in the rankings screen
		for (Item item : belongings.backpack.items.toArray( new Item[0])) {
			if (item instanceof Bag){
				for (Item bagItem : ((Bag) item).items.toArray( new Item[0])){
					if (Dungeon.quickslot.contains(bagItem)
							&& !Dungeon.quickslot.contains(item)){
						belongings.backpack.items.add(bagItem);
					}
				}
			}
			if (!(item instanceof Trinket) && !Dungeon.quickslot.contains(item)) {
				belongings.backpack.items.remove(item);
			}
		}

		//remove all buffs (ones tied to equipment will be re-applied)
		for(Buff b : Dungeon.hero.buffs()){
			//except Duelist's melee weapon charge buff
			if (!(b instanceof MeleeWeapon.Charger)) {
				Dungeon.hero.remove(b);
			}
		}

		rec.gameData.put( HERO, Dungeon.hero );

		//save stats
		Bundle stats = new Bundle();
		Statistics.storeInBundle(stats);
		rec.gameData.put( STATS, stats);

		//save badges
		Bundle badges = new Bundle();
		Badges.saveLocal(badges);
		rec.gameData.put( BADGES, badges);

		//save handler information
		Bundle handler = new Bundle();
		Scroll.saveSelectively(handler, belongings.backpack.items);
		Potion.saveSelectively(handler, belongings.backpack.items);
		//include potentially worn rings
		if (belongings.misc != null)        belongings.backpack.items.add(belongings.misc);
		if (belongings.ring != null)        belongings.backpack.items.add(belongings.ring);
		Ring.saveSelectively(handler, belongings.backpack.items);
		rec.gameData.put( HANDLERS, handler);

		//restore items now that we're done saving
		belongings.backpack.items = allItems;
		
		//save challenges
		rec.gameData.put( CHALLENGES, Dungeon.challenges );

		rec.gameData.put( GAME_VERSION, Dungeon.initialVersion );

		rec.gameData.put( SEED, Dungeon.seed );
		rec.gameData.put( CUSTOM_SEED, Dungeon.customSeedText );
	}

	public void loadGameData(Record rec){
		Bundle data = rec.gameData;

		Actor.clear();
		Dungeon.hero = null;
		Dungeon.level = null;
		Generator.fullReset();
		Notes.reset();
		Dungeon.quickslot.reset();
		QuickSlotButton.reset();
		Toolbar.swappedQuickslots = false;

		if (data == null) return;

		Bundle handler = data.getBundle(HANDLERS);
		Scroll.restore(handler);
		Potion.restore(handler);
		Ring.restore(handler);

		Badges.loadLocal(data.getBundle(BADGES));

		Dungeon.hero = (Hero)data.get(HERO);
		Dungeon.hero.belongings.identify();

		Statistics.restoreFromBundle(data.getBundle(STATS));
		
		Dungeon.challenges = data.getInt(CHALLENGES);

		Dungeon.initialVersion = data.getInt(GAME_VERSION);

		if (Dungeon.initialVersion <= SacredPixelDungeon.v1_2_3){
			Statistics.gameWon = rec.win;
		}
		rec.score = calculateScore();

		// Load seed from gameData FIRST (before score breakdown migration)
		// This ensures Dungeon.seed has the correct value for rec.dungeonSeed assignment
		if (rec.gameData.contains(SEED)){
			Dungeon.seed = rec.gameData.getLong(SEED);
			Dungeon.customSeedText = rec.gameData.getString(CUSTOM_SEED);
		} else {
			Dungeon.seed = -1;
			Dungeon.customSeedText = "";
		}

		// Migrate score breakdown from Statistics to Record if not already present
		// This handles legacy data that has gameData but no score breakdown fields
		if (!rec.hasScoreBreakdown()) {
			rec.progressScore   = Statistics.progressScore;
			rec.treasureScore   = Statistics.treasureScore;
			rec.exploreScore    = Statistics.exploreScore;
			rec.totalBossScore  = Statistics.totalBossScore;
			rec.totalQuestScore = Statistics.totalQuestScore;
			rec.winMultiplier   = Statistics.winMultiplier;
			rec.chalMultiplier  = Statistics.chalMultiplier;
			rec.challenges      = Dungeon.challenges;
			rec.dungeonSeed     = Dungeon.seed;
			rec.duration        = Statistics.duration;
			rec.scoreBreakdownKnown = true;
		}
	}
	
	private static final String RECORDS	= "records";
	private static final String LATEST	= "latest";
	private static final String TOTAL	= "total";
	private static final String WON     = "won";

	public void save() {
		Bundle bundle = new Bundle();
		bundle.put( RECORDS, records );
		bundle.put( LATEST, lastRecord );
		bundle.put( TOTAL, totalNumber );
		bundle.put( WON, wonNumber );

		try {
			FileUtils.bundleToFile( RANKINGS_FILE, bundle);
		} catch (IOException e) {
			SacredPixelDungeon.reportException(e);
		}

	}
	
	public void load() {
		
		if (records != null) {
			return;
		}
		
		records = new ArrayList<>();
		
		try {
			Bundle bundle = FileUtils.bundleFromFile( RANKINGS_FILE );
			
			for (Bundlable record : bundle.getCollection( RECORDS )) {
				records.add( (Record)record );
			}
			lastRecord = bundle.getInt( LATEST );
			
			totalNumber = bundle.getInt( TOTAL );
			if (totalNumber == 0) {
				totalNumber = records.size();
			}

			wonNumber = bundle.getInt( WON );
			if (wonNumber == 0) {
				for (Record rec : records) {
					if (rec.win) {
						wonNumber++;
					}
				}
			}

		} catch (IOException e) {
		}
	}
	
	public static class Record implements Bundlable {

		private static final String CAUSE   = "cause";
		private static final String WIN		= "win";
		private static final String SCORE	= "score";
		private static final String CLASS	= "class";
		private static final String TIER	= "tier";
		private static final String LEVEL	= "level";
		private static final String DEPTH	= "depth";
		private static final String ASCEND	= "ascending";
		private static final String DATA	= "gameData";
		private static final String ID      = "gameID";
		private static final String SEED    = "custom_seed";

		private static final String DATE    = "date";
		private static final String VERSION = "version";

		// Score breakdown fields for URL export (added in web version)
		private static final String PROGRESS_SCORE  = "prog_score";
		private static final String TREASURE_SCORE  = "tres_score";
		private static final String EXPLORE_SCORE   = "expl_score";
		private static final String BOSS_SCORE      = "boss_score";
		private static final String QUEST_SCORE     = "quest_score";
		private static final String WIN_MULT        = "win_mult";
		private static final String CHAL_MULT       = "chal_mult";
		private static final String CHALLENGES_REC  = "challenges_rec";
		private static final String DUNGEON_SEED    = "dungeon_seed";
		private static final String DURATION_REC    = "duration_rec";

		// Statistics fields for URL export (v0x06)
		private static final String HERO_STR        = "hero_str";
		private static final String STR_BONUS       = "str_bonus";
		private static final String ENEMIES_SLAIN   = "enemies_slain";
		private static final String GOLD_COLLECTED  = "gold_collected";
		private static final String FOOD_EATEN      = "food_eaten";
		private static final String ITEMS_CRAFTED   = "items_crafted";

		// Imported extended data (for records without gameData)
		private static final String IMPORTED_BADGES  = "imp_badges";
		private static final String IMPORTED_TALENTS = "imp_talents";
		private static final String IMPORTED_ITEMS   = "imp_items";

		public Class cause;
		public boolean win;

		public HeroClass heroClass;
		public int armorTier;
		public int herolevel;
		public int depth;
		public boolean ascending;

		public Bundle gameData;
		public String gameID;

		//Note this is for summary purposes, visible score should be re-calculated from game data
		public int score;

		// Score breakdown fields for URL export (new)
		public int progressScore;
		public int treasureScore;
		public int exploreScore;
		public int totalBossScore;
		public int totalQuestScore;
		public float winMultiplier = 1f;
		public float chalMultiplier = 1f;
		public boolean scoreBreakdownKnown = false; // true if score breakdown was recorded

		// Additional context for URL export and Replay
		public int challenges;
		public long dungeonSeed;
		public float duration; // game turn time (from Statistics.duration)

		// Statistics fields for URL export (v0x06)
		public int heroSTR;
		public int strBonus;
		public int enemiesSlain;
		public int goldCollected;
		public int foodEaten;
		public int itemsCrafted;

		// Extended data for imported records (when gameData is null)
		// These fields allow WndRanking to display tabs for imported records
		public byte[] importedBadges;      // 24 bytes per-run badges bitmap
		public int[][] importedTalents;    // [16][2] - [talent_ordinal, level]
		public int[][] importedItems;      // [12][2] - [item_id, level]

		public String customSeed;

		public String date;
		public String version;

		// Check if score breakdown is available (for UI display)
		public boolean hasScoreBreakdown() {
			// scoreBreakdownKnown is set for both new records and imported records
			return scoreBreakdownKnown;
		}

		public String desc(){
			if (win){
				if (ascending){
					return Messages.get(this, "ascended");
				} else {
					return Messages.get(this, "won");
				}
			} else if (cause == null) {
				return Messages.get(this, "something");
			} else {
				String result = Messages.get(cause, "rankings_desc", (Messages.get(cause, "name")));
				if (result.contains(Messages.NO_TEXT_FOUND)){
					return Messages.get(this, "something");
				} else {
					return result;
				}
			}
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {

			if (bundle.contains( CAUSE )) {
				cause = bundle.getClass( CAUSE );
			} else {
				cause = null;
			}

			win		    = bundle.getBoolean( WIN );
			score	    = bundle.getInt( SCORE );
			customSeed  = bundle.getString( SEED );

			heroClass	= bundle.getEnum( CLASS, HeroClass.class );
			armorTier	= bundle.getInt( TIER );
			herolevel   = bundle.getInt( LEVEL );
			depth       = bundle.getInt( DEPTH );
			ascending   = bundle.getBoolean( ASCEND );

			if (bundle.contains( DATE )){
				date = bundle.getString( DATE );
				version = bundle.getString( VERSION );
			} else {
				date = version = null;
			}

			if (bundle.contains(DATA))  gameData = bundle.getBundle(DATA);
			if (bundle.contains(ID))   gameID = bundle.getString(ID);

			if (gameID == null) gameID = generateRandomId();

			// Load score breakdown fields (new - backward compatible)
			if (bundle.contains(PROGRESS_SCORE)) {
				progressScore   = bundle.getInt(PROGRESS_SCORE);
				treasureScore   = bundle.getInt(TREASURE_SCORE);
				exploreScore    = bundle.getInt(EXPLORE_SCORE);
				totalBossScore  = bundle.getInt(BOSS_SCORE);
				totalQuestScore = bundle.getInt(QUEST_SCORE);
				winMultiplier   = bundle.getFloat(WIN_MULT);
				chalMultiplier  = bundle.getFloat(CHAL_MULT);
				scoreBreakdownKnown = true;
			} else {
				// Legacy data: score breakdown not available
				progressScore = treasureScore = exploreScore = 0;
				totalBossScore = totalQuestScore = 0;
				winMultiplier = 1f;
				chalMultiplier = 1f;
				scoreBreakdownKnown = false;
			}

			// Load additional context fields
			if (bundle.contains(CHALLENGES_REC)) {
				challenges = bundle.getInt(CHALLENGES_REC);
			}
			if (bundle.contains(DUNGEON_SEED)) {
				dungeonSeed = bundle.getLong(DUNGEON_SEED);
			}
			if (bundle.contains(DURATION_REC)) {
				duration = bundle.getFloat(DURATION_REC);
			}

			// Load statistics fields (v0x06)
			heroSTR = bundle.getInt(HERO_STR);
			strBonus = bundle.getInt(STR_BONUS);
			enemiesSlain = bundle.getInt(ENEMIES_SLAIN);
			goldCollected = bundle.getInt(GOLD_COLLECTED);
			foodEaten = bundle.getInt(FOOD_EATEN);
			itemsCrafted = bundle.getInt(ITEMS_CRAFTED);

			// Load imported extended data
			if (bundle.contains(IMPORTED_BADGES)) {
				// Convert int[] back to byte[]
				int[] badgesAsInt = bundle.getIntArray(IMPORTED_BADGES);
				if (badgesAsInt != null) {
					importedBadges = new byte[badgesAsInt.length];
					for (int i = 0; i < badgesAsInt.length; i++) {
						importedBadges[i] = (byte) badgesAsInt[i];
					}
				}
			}
			if (bundle.contains(IMPORTED_TALENTS)) {
				int[] flatTalents = bundle.getIntArray(IMPORTED_TALENTS);
				if (flatTalents != null) {
					importedTalents = new int[16][2];
					for (int i = 0; i < 16 && i * 2 + 1 < flatTalents.length; i++) {
						importedTalents[i][0] = flatTalents[i * 2];
						importedTalents[i][1] = flatTalents[i * 2 + 1];
					}
				}
			}
			if (bundle.contains(IMPORTED_ITEMS)) {
				int[] flatItems = bundle.getIntArray(IMPORTED_ITEMS);
				if (flatItems != null) {
					importedItems = new int[12][2];
					for (int i = 0; i < 12 && i * 2 + 1 < flatItems.length; i++) {
						importedItems[i][0] = flatItems[i * 2];
						importedItems[i][1] = flatItems[i * 2 + 1];
					}
				}
			}

		}
		
		@Override
		public void storeInBundle( Bundle bundle ) {

			if (cause != null) bundle.put( CAUSE, cause );

			bundle.put( WIN, win );
			bundle.put( SCORE, score );
			bundle.put( SEED, customSeed );

			bundle.put( CLASS, heroClass );
			bundle.put( TIER, armorTier );
			bundle.put( LEVEL, herolevel );
			bundle.put( DEPTH, depth );
			bundle.put( ASCEND, ascending );

			bundle.put( DATE, date );
			bundle.put( VERSION, version );

			if (gameData != null) bundle.put( DATA, gameData );
			bundle.put( ID, gameID );

			// Save score breakdown fields (new)
			bundle.put( PROGRESS_SCORE, progressScore );
			bundle.put( TREASURE_SCORE, treasureScore );
			bundle.put( EXPLORE_SCORE, exploreScore );
			bundle.put( BOSS_SCORE, totalBossScore );
			bundle.put( QUEST_SCORE, totalQuestScore );
			bundle.put( WIN_MULT, winMultiplier );
			bundle.put( CHAL_MULT, chalMultiplier );

			// Save additional context
			bundle.put( CHALLENGES_REC, challenges );
			bundle.put( DUNGEON_SEED, dungeonSeed );
			bundle.put( DURATION_REC, duration );

			// Save statistics fields (v0x06)
			bundle.put( HERO_STR, heroSTR );
			bundle.put( STR_BONUS, strBonus );
			bundle.put( ENEMIES_SLAIN, enemiesSlain );
			bundle.put( GOLD_COLLECTED, goldCollected );
			bundle.put( FOOD_EATEN, foodEaten );
			bundle.put( ITEMS_CRAFTED, itemsCrafted );

			// Save imported extended data (for records without gameData)
			if (importedBadges != null) {
				// Convert byte[] to int[] for Bundle compatibility
				int[] badgesAsInt = new int[importedBadges.length];
				for (int i = 0; i < importedBadges.length; i++) {
					badgesAsInt[i] = importedBadges[i] & 0xFF;
				}
				bundle.put( IMPORTED_BADGES, badgesAsInt );
			}
			if (importedTalents != null) {
				int[] flatTalents = new int[32];
				for (int i = 0; i < 16 && i < importedTalents.length; i++) {
					flatTalents[i * 2] = importedTalents[i][0];
					flatTalents[i * 2 + 1] = importedTalents[i][1];
				}
				bundle.put( IMPORTED_TALENTS, flatTalents );
			}
			if (importedItems != null) {
				int[] flatItems = new int[24];
				for (int i = 0; i < 12 && i < importedItems.length; i++) {
					flatItems[i * 2] = importedItems[i][0];
					flatItems[i * 2 + 1] = importedItems[i][1];
				}
				bundle.put( IMPORTED_ITEMS, flatItems );
			}
		}
	}

	public static final Comparator<Record> scoreComparator = new Comparator<Rankings.Record>() {
		@Override
		public int compare( Record lhs, Record rhs ) {
			//this covers custom seeded runs and dailies
			if (rhs.customSeed.isEmpty() && !lhs.customSeed.isEmpty()){
				return +1;
			} else if (lhs.customSeed.isEmpty() && !rhs.customSeed.isEmpty()){
				return -1;
			}

			int result = (int)Math.signum( rhs.score - lhs.score );
			if (result == 0) {
				return (int)Math.signum( rhs.gameID.hashCode() - lhs.gameID.hashCode());
			} else {
				return result;
			}
		}
	};
}
