/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebDataImporter - Deserializes game data from compact byte array
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.watabou.utils.Bundle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports game data from compact byte array format.
 * Supports VERSION 0x09 (optimized JSON with key/classname shortening).
 */
public class WebDataImporter {

    private static final int BADGES_SIZE = 24;
    private static final int CATALOG_SIZE = 50;
    private static final int BESTIARY_SIZE = 25;
    private static final int DOCUMENT_SIZE = 16;
    private static final int GLOBAL_SIZE = 1 + BADGES_SIZE + CATALOG_SIZE + BESTIARY_SIZE + DOCUMENT_SIZE; // 116 bytes

    // Class name prefix for expansion
    private static final String CLASS_PREFIX = "com.sacredpixel.sacredpixeldungeon.";
    private static final String CLASS_SHORT = "~.";

    // Allowed class prefixes for security (whitelist)
    private static final String[] ALLOWED_CLASS_PREFIXES = {
        "com.sacredpixel.sacredpixeldungeon.",
        "com.watabou."
    };

    private static boolean isAllowedClass(String className) {
        if (className == null || className.isEmpty()) return false;
        for (String prefix : ALLOWED_CLASS_PREFIXES) {
            if (className.startsWith(prefix)) return true;
        }
        System.err.println("[Security] Blocked class: " + className);
        return false;
    }

    // Reverse key map (shortened -> original)
    private static final Map<String, String> REVERSE_KEY_MAP = new HashMap<>();
    static {
        REVERSE_KEY_MAP.put("_c", "__className");
        REVERSE_KEY_MAP.put("q", "quantity");
        REVERSE_KEY_MAP.put("l", "level");
        REVERSE_KEY_MAP.put("cu", "cursed");
        REVERSE_KEY_MAP.put("ck", "cursedKnown");
        REVERSE_KEY_MAP.put("lk", "levelKnown");
        REVERSE_KEY_MAP.put("en", "enchantment");
        REVERSE_KEY_MAP.put("gl", "glyph");
        REVERSE_KEY_MAP.put("au", "augment");
        REVERSE_KEY_MAP.put("se", "seal");
        REVERSE_KEY_MAP.put("cib", "curseInfusionBonus");
        REVERSE_KEY_MAP.put("mpb", "masteryPotionBonus");
        REVERSE_KEY_MAP.put("eh", "enchantHardened");
        REVERSE_KEY_MAP.put("gh", "glyphHardened");
        REVERSE_KEY_MAP.put("uid", "usesLeftToID");
        REVERSE_KEY_MAP.put("aid", "availableUsesToID");
        REVERSE_KEY_MAP.put("w", "weapon");
        REVERSE_KEY_MAP.put("a", "armor");
        REVERSE_KEY_MAP.put("ar", "artifact");
        REVERSE_KEY_MAP.put("mi", "misc");
        REVERSE_KEY_MAP.put("ri", "ring");
        REVERSE_KEY_MAP.put("bp", "backpack");
        REVERSE_KEY_MAP.put("it", "items");
        REVERSE_KEY_MAP.put("ta", "talents");
        REVERSE_KEY_MAP.put("h", "hero");
        REVERSE_KEY_MAP.put("st", "stats");
        REVERSE_KEY_MAP.put("ba", "badges");
        REVERSE_KEY_MAP.put("ch", "challenges");
        REVERSE_KEY_MAP.put("gv", "game_version");
        REVERSE_KEY_MAP.put("sd", "seed");
        REVERSE_KEY_MAP.put("cs", "custom_seed");
        REVERSE_KEY_MAP.put("qs", "quickslot");
        REVERSE_KEY_MAP.put("hc", "heroClass");
        REVERSE_KEY_MAP.put("cl", "class");  // Hero's heroClass enum stored with key "class"
        REVERSE_KEY_MAP.put("ti", "tier");
        REVERSE_KEY_MAP.put("str", "STR");
        REVERSE_KEY_MAP.put("lv", "lvl");
        REVERSE_KEY_MAP.put("xp", "exp");
        REVERSE_KEY_MAP.put("hp", "HP");
        REVERSE_KEY_MAP.put("ht", "HT");
        REVERSE_KEY_MAP.put("atk", "attackSkill");
        REVERSE_KEY_MAP.put("def", "defenseSkill");
        REVERSE_KEY_MAP.put("kl", "keptThoughLostInvent");
        REVERSE_KEY_MAP.put("cn", "customNoteID");
        // Record-level fields (injected)
        REVERSE_KEY_MAP.put("_ca", "__cause");
        REVERSE_KEY_MAP.put("_w", "__win");
        REVERSE_KEY_MAP.put("_as", "__ascending");
        REVERSE_KEY_MAP.put("_at", "__armorTier");
        REVERSE_KEY_MAP.put("_d", "__depth");
        REVERSE_KEY_MAP.put("_dt", "__date");
        REVERSE_KEY_MAP.put("_v", "__version");
        REVERSE_KEY_MAP.put("_cs", "__customSeed");
        REVERSE_KEY_MAP.put("_sc", "__score");
    }

    /**
     * Parsed import data container.
     */
    public static class ImportedData {
        public byte version;
        public byte[] badges;           // 24 bytes global badges bitmap
        public byte[] catalog;          // 50 bytes bitmap
        public byte[] bestiary;         // 25 bytes bitmap
        public byte[] document;         // 16 bytes (2 bits per page)
        public List<Rankings.Record> rankings;
        public List<String> gameDataJsons;  // Restored gameData JSON strings

        // Summary for preview
        public int badgeCount;
        public int rankingCount;
        public int highestScore;
        public int catalogCount;
        public int bestiaryCount;
        public int documentCount;
        public int guideCount;
        public int alchemyCount;
        public int loreCount;
    }

    /**
     * Parses byte array to ImportedData.
     * @param data Raw byte array
     * @return Parsed data or null if invalid
     */
    public static ImportedData parse(byte[] data) {
        if (data == null || data.length < GLOBAL_SIZE + 1) {
            return null;
        }

        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        ImportedData result = new ImportedData();

        // VERSION
        result.version = buf.get();

        if (result.version != 0x09) {
            return null;
        }

        // Global Badges (24 bytes)
        result.badges = new byte[BADGES_SIZE];
        buf.get(result.badges);
        result.badgeCount = countBits(result.badges);

        // Catalog (50 bytes)
        result.catalog = new byte[CATALOG_SIZE];
        buf.get(result.catalog);
        result.catalogCount = countBits(result.catalog);

        // Bestiary (25 bytes)
        result.bestiary = new byte[BESTIARY_SIZE];
        buf.get(result.bestiary);
        result.bestiaryCount = countBits(result.bestiary);

        // Document (16 bytes)
        result.document = new byte[DOCUMENT_SIZE];
        buf.get(result.document);
        result.documentCount = countDocumentPages(result.document);
        result.guideCount = countDocumentPagesInRange(result.document, 0, 13);
        result.alchemyCount = countDocumentPagesInRange(result.document, 14, 22);
        result.loreCount = countDocumentPagesInRange(result.document, 30, 59);

        // Rankings count
        int rankingCount = buf.get() & 0xFF;
        result.rankingCount = rankingCount;

        // JSON length (2 bytes LE)
        if (buf.remaining() < 2) {
            return result;
        }
        int jsonLen = (buf.get() & 0xFF) | ((buf.get() & 0xFF) << 8);

        if (buf.remaining() < jsonLen) {
            return result;
        }

        // Read JSON bytes
        byte[] jsonBytes = new byte[jsonLen];
        buf.get(jsonBytes);
        String jsonArray = new String(jsonBytes, StandardCharsets.UTF_8);

        // Restore optimized JSON
        jsonArray = restoreJson(jsonArray);

        // Parse JSON array to individual gameData strings
        result.rankings = new ArrayList<>();
        result.gameDataJsons = new ArrayList<>();
        parseJsonArray(jsonArray, result);

        return result;
    }

    /**
     * Restores optimized JSON to original format.
     * 1. Expand shortened keys
     * 2. Expand shortened class names
     */
    private static String restoreJson(String json) {
        // Expand class names first (before key expansion to avoid conflicts)
        json = json.replace(CLASS_SHORT, CLASS_PREFIX);

        // Expand shortened keys (reverse order by key length to avoid partial matches)
        // Sort by shortened key length descending
        List<Map.Entry<String, String>> entries = new ArrayList<>(REVERSE_KEY_MAP.entrySet());
        entries.sort((a, b) -> b.getKey().length() - a.getKey().length());

        for (Map.Entry<String, String> entry : entries) {
            // Replace "shortKey": with "originalKey":
            json = json.replace("\"" + entry.getKey() + "\":", "\"" + entry.getValue() + "\":");
        }

        return json;
    }

    /**
     * Parses JSON array string and populates ImportedData.
     */
    private static void parseJsonArray(String jsonArray, ImportedData result) {
        // Simple JSON array parsing
        if (!jsonArray.startsWith("[") || !jsonArray.endsWith("]")) {
            return;
        }

        // Remove outer brackets
        String content = jsonArray.substring(1, jsonArray.length() - 1).trim();
        if (content.isEmpty()) return;

        // Split by top-level commas (depth=0)
        List<String> elements = new ArrayList<>();
        int depth = 0;
        int start = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') depth--;
            else if (c == ',' && depth == 0) {
                elements.add(content.substring(start, i).trim());
                start = i + 1;
            }
        }
        elements.add(content.substring(start).trim());

        // Process each element
        for (int i = 0; i < elements.size(); i++) {
            String elem = elements.get(i);

            if (elem.equals("null") || elem.isEmpty()) {
                result.gameDataJsons.add(null);
                result.rankings.add(createEmptyRecord(i));
                continue;
            }

            result.gameDataJsons.add(elem);

            // Create Record from gameData JSON
            try {
                Bundle gameData = Bundle.read(elem);
                Rankings.Record record = createRecordFromGameData(gameData, i);
                result.rankings.add(record);

                if (record.score > result.highestScore) {
                    result.highestScore = record.score;
                }
            } catch (Exception e) {
                result.rankings.add(createEmptyRecord(i));
            }
        }
    }

    /**
     * Creates a Rankings.Record from gameData Bundle.
     */
    private static Rankings.Record createRecordFromGameData(Bundle gameData, int index) {
        Rankings.Record r = new Rankings.Record();
        r.gameData = gameData;
        r.gameID = "imported_" + System.currentTimeMillis() + "_" + index;

        // Extract injected Record-level fields first
        if (gameData.contains("__cause")) {
            String causeClassName = gameData.getString("__cause");
            if (isAllowedClass(causeClassName)) {
                try {
                    r.cause = Class.forName(causeClassName);
                } catch (ClassNotFoundException e) {
                    // Cause class not found - ignore
                }
            }
        }
        if (gameData.contains("__win")) {
            r.win = gameData.getBoolean("__win");
        }
        if (gameData.contains("__ascending")) {
            r.ascending = gameData.getBoolean("__ascending");
        }
        if (gameData.contains("__armorTier")) {
            r.armorTier = gameData.getInt("__armorTier");
        }
        if (gameData.contains("__depth")) {
            r.depth = gameData.getInt("__depth");
        }
        if (gameData.contains("__date")) {
            r.date = gameData.getString("__date");
        }
        if (gameData.contains("__version")) {
            r.version = gameData.getString("__version");
        }
        if (gameData.contains("__customSeed")) {
            r.customSeed = gameData.getString("__customSeed");
        }
        if (gameData.contains("__score")) {
            r.score = gameData.getInt("__score");
        }

        // Extract basic info from hero bundle
        if (gameData.contains("hero")) {
            Bundle hero = gameData.getBundle("hero");
            if (hero != null) {
                // HeroClass - stored as enum string in "class" field
                String heroClassStr = hero.getString("class");
                if (heroClassStr != null) {
                    try {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.valueOf(heroClassStr);
                    } catch (IllegalArgumentException e) {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.WARRIOR;
                    }
                } else {
                    // Fallback: try __className (legacy format)
                    String heroClassName = hero.getString("__className");
                    if (heroClassName != null && heroClassName.contains("Warrior")) {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.WARRIOR;
                    } else if (heroClassName != null && heroClassName.contains("Mage")) {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.MAGE;
                    } else if (heroClassName != null && heroClassName.contains("Rogue")) {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.ROGUE;
                    } else if (heroClassName != null && heroClassName.contains("Huntress")) {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.HUNTRESS;
                    } else if (heroClassName != null && heroClassName.contains("Duelist")) {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.DUELIST;
                    } else if (heroClassName != null && heroClassName.contains("Cleric")) {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.CLERIC;
                    } else {
                        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.WARRIOR;
                    }
                }

                r.herolevel = hero.getInt("lvl");
                r.heroSTR = hero.getInt("STR");
            }
        }

        // Extract stats
        if (gameData.contains("stats")) {
            Bundle stats = gameData.getBundle("stats");
            if (stats != null) {
                r.duration = stats.getFloat("duration");
                // Use __depth from injected fields if available, otherwise from stats
                if (r.depth == 0) {
                    r.depth = stats.getInt("deepestFloor");
                }
                r.enemiesSlain = stats.getInt("enemiesSlain");
                r.goldCollected = stats.getInt("goldCollected");
                r.foodEaten = stats.getInt("foodEaten");
                r.itemsCrafted = stats.getInt("alchemyPotions");
                r.progressScore = stats.getInt("progressScore");
                r.treasureScore = stats.getInt("treasureScore");
                r.exploreScore = stats.getInt("exploreScore");
                r.totalBossScore = stats.getInt("totalBossScore");
                r.totalQuestScore = stats.getInt("totalQuestScore");
                // Use __win from injected fields if available
                if (!gameData.contains("__win")) {
                    r.win = stats.getBoolean("gameWon");
                }
                if (!gameData.contains("__ascending")) {
                    r.ascending = stats.getBoolean("ascending");
                }
            }
        }

        // Calculate score if not already set from injected field
        if (r.score == 0) {
            r.score = r.progressScore + r.treasureScore + r.exploreScore + r.totalBossScore + r.totalQuestScore;
        }

        // Extract challenges and seed
        if (gameData.contains("challenges")) {
            r.challenges = gameData.getInt("challenges");
        }
        if (gameData.contains("seed")) {
            r.dungeonSeed = gameData.getLong("seed");
        }
        if (gameData.contains("custom_seed") && (r.customSeed == null || r.customSeed.isEmpty())) {
            r.customSeed = gameData.getString("custom_seed");
        }
        if (gameData.contains("game_version") && (r.version == null || r.version.isEmpty())) {
            r.version = "v" + gameData.getInt("game_version");
        }

        // Ensure customSeed is not null
        if (r.customSeed == null) {
            r.customSeed = "";
        }

        return r;
    }

    /**
     * Creates an empty placeholder record.
     */
    private static Rankings.Record createEmptyRecord(int index) {
        Rankings.Record r = new Rankings.Record();
        r.gameID = "empty_" + index;
        r.heroClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.WARRIOR;
        return r;
    }

    /**
     * Counts set bits in bitmap.
     */
    public static int countBits(byte[] bitmap) {
        int count = 0;
        for (byte b : bitmap) {
            count += Integer.bitCount(b & 0xFF);
        }
        return count;
    }

    /**
     * Checks if a specific bit is set.
     */
    public static boolean isBitSet(byte[] bitmap, int index) {
        if (index < 0 || index >= bitmap.length * 8) return false;
        return (bitmap[index / 8] & (1 << (index % 8))) != 0;
    }

    /**
     * Counts document pages with state > 0.
     */
    private static int countDocumentPages(byte[] bitmap) {
        int count = 0;
        for (int i = 0; i < 64; i++) {
            int state = getDocumentPageState(bitmap, i);
            if (state > 0) count++;
        }
        return count;
    }

    /**
     * Counts document pages in range with state > 0.
     */
    private static int countDocumentPagesInRange(byte[] bitmap, int start, int end) {
        int count = 0;
        for (int i = start; i <= end && i < 64; i++) {
            int state = getDocumentPageState(bitmap, i);
            if (state > 0) count++;
        }
        return count;
    }

    /**
     * Gets document page state (0=NONE, 1=FOUND, 2=READ).
     */
    public static int getDocumentPageState(byte[] bitmap, int pageIndex) {
        if (pageIndex < 0 || pageIndex >= 64) return 0;
        int byteIdx = (pageIndex * 2) / 8;
        int bitOffset = (pageIndex * 2) % 8;
        if (byteIdx >= bitmap.length) return 0;
        return (bitmap[byteIdx] >> bitOffset) & 0x03;
    }
}
