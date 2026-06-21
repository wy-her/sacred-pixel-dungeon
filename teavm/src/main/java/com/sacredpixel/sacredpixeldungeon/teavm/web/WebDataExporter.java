/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebDataExporter - Serializes game data to compact format for URL export
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.watabou.utils.Bundle;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Exports game data to compact format for URL sharing.
 *
 * VERSION 0x09 Layout:
 *   [Global Section - 116 bytes fixed]
 *   0:       Version (0x09)
 *   1-24:    Global Badges bitmap (24B)
 *   25-74:   Catalog bitmap (50B)
 *   75-99:   Bestiary bitmap (25B)
 *   100-115: Document bitmap (16B)
 *
 *   [Rankings Section - variable]
 *   116:     Rankings count (1B)
 *   117+:    Optimized JSON (key shortening + class name shortening + backpack trimming)
 *
 * Optimizations applied:
 *   1. JSON key shortening: __className -> _c, level -> l, etc.
 *   2. Class name shortening: com.sacredpixel.sacredpixeldungeon. -> ~.
 *   3. Backpack trimming: Only QuickSlot items kept, others removed
 */
public class WebDataExporter {

    public static final byte VERSION = 0x09;

    private static final int BADGES_SIZE = 24;      // 192 badges
    private static final int CATALOG_SIZE = 50;     // 400 items
    private static final int BESTIARY_SIZE = 25;    // 200 monsters
    private static final int DOCUMENT_SIZE = 16;    // 64 pages × 2 bits
    private static final int GLOBAL_SIZE = 1 + BADGES_SIZE + CATALOG_SIZE + BESTIARY_SIZE + DOCUMENT_SIZE; // 116 bytes

    // Original: 11 (Top 10 + 1)
    // Changed to 6 (Top 5 + 1) for shorter URL
    private static final int MAX_RANKINGS = 6;

    // Class name prefix for shortening
    private static final String CLASS_PREFIX = "com.sacredpixel.sacredpixeldungeon.";
    private static final String CLASS_SHORT = "~.";

    // Key shortening map (original -> shortened)
    private static final Map<String, String> KEY_MAP = new HashMap<>();
    static {
        KEY_MAP.put("__className", "_c");
        KEY_MAP.put("quantity", "q");
        KEY_MAP.put("level", "l");
        KEY_MAP.put("cursed", "cu");
        KEY_MAP.put("cursedKnown", "ck");
        KEY_MAP.put("levelKnown", "lk");
        KEY_MAP.put("enchantment", "en");
        KEY_MAP.put("glyph", "gl");
        KEY_MAP.put("augment", "au");
        KEY_MAP.put("seal", "se");
        KEY_MAP.put("curseInfusionBonus", "cib");
        KEY_MAP.put("masteryPotionBonus", "mpb");
        KEY_MAP.put("enchantHardened", "eh");
        KEY_MAP.put("glyphHardened", "gh");
        KEY_MAP.put("usesLeftToID", "uid");
        KEY_MAP.put("availableUsesToID", "aid");
        KEY_MAP.put("weapon", "w");
        KEY_MAP.put("armor", "a");
        KEY_MAP.put("artifact", "ar");
        KEY_MAP.put("misc", "mi");
        KEY_MAP.put("ring", "ri");
        KEY_MAP.put("backpack", "bp");
        KEY_MAP.put("items", "it");
        KEY_MAP.put("talents", "ta");
        KEY_MAP.put("hero", "h");
        KEY_MAP.put("stats", "st");
        KEY_MAP.put("badges", "ba");
        KEY_MAP.put("challenges", "ch");
        KEY_MAP.put("game_version", "gv");
        KEY_MAP.put("seed", "sd");
        KEY_MAP.put("custom_seed", "cs");
        KEY_MAP.put("quickslot", "qs");
        KEY_MAP.put("heroClass", "hc");
        KEY_MAP.put("class", "cl");  // Hero's heroClass enum stored with key "class"
        KEY_MAP.put("tier", "ti");
        KEY_MAP.put("STR", "str");
        KEY_MAP.put("lvl", "lv");
        KEY_MAP.put("exp", "xp");
        KEY_MAP.put("HP", "hp");
        KEY_MAP.put("HT", "ht");
        KEY_MAP.put("attackSkill", "atk");
        KEY_MAP.put("defenseSkill", "def");
        KEY_MAP.put("keptThoughLostInvent", "kl");
        KEY_MAP.put("customNoteID", "cn");
        // Record-level fields (injected)
        KEY_MAP.put("__cause", "_ca");
        KEY_MAP.put("__win", "_w");
        KEY_MAP.put("__ascending", "_as");
        KEY_MAP.put("__armorTier", "_at");
        KEY_MAP.put("__depth", "_d");
        KEY_MAP.put("__date", "_dt");
        KEY_MAP.put("__version", "_v");
        KEY_MAP.put("__customSeed", "_cs");
        KEY_MAP.put("__score", "_sc");
    }

    /**
     * Exports all game data to a compact byte array.
     * @return Byte array ready for compression
     */
    public static byte[] exportAll() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // VERSION
            baos.write(VERSION);

            // Global Badges (24 bytes)
            baos.write(exportGlobalBadges());

            // Catalog (50 bytes)
            baos.write(exportCatalog());

            // Bestiary (25 bytes)
            baos.write(exportBestiary());

            // Document (16 bytes)
            baos.write(exportDocument());

            // Rankings count
            Rankings.INSTANCE.load();
            List<Rankings.Record> records = Rankings.INSTANCE.records;
            int count = Math.min(records.size(), MAX_RANKINGS);
            baos.write((byte) count);

            // Rankings as optimized JSON array
            StringBuilder jsonArray = new StringBuilder("[");
            for (int i = 0; i < count; i++) {
                Rankings.Record r = records.get(i);

                // Load gameData if not loaded
                if (r.gameData == null) {
                    Rankings.INSTANCE.loadGameData(r);
                }

                if (i > 0) jsonArray.append(",");

                if (r.gameData != null) {
                    String json = r.gameData.toString();

                    // Inject Record-level fields that are not in gameData
                    json = injectRecordFields(json, r);

                    // Apply optimizations
                    json = optimizeJson(json);
                    jsonArray.append(json);
                } else {
                    jsonArray.append("null");
                }
            }
            jsonArray.append("]");

            byte[] jsonBytes = jsonArray.toString().getBytes(StandardCharsets.UTF_8);

            // Write JSON length (2 bytes LE) and JSON
            baos.write(jsonBytes.length & 0xFF);
            baos.write((jsonBytes.length >> 8) & 0xFF);
            baos.write(jsonBytes);

        } catch (Exception e) {
            // Export error - return partial result
        }

        return baos.toByteArray();
    }

    /**
     * Injects Record-level fields into the gameData JSON.
     * These fields are stored in Rankings.Record but not in gameData Bundle.
     */
    private static String injectRecordFields(String json, Rankings.Record r) {
        // Build fields to inject
        StringBuilder inject = new StringBuilder();

        // cause class name (for death message)
        if (r.cause != null) {
            inject.append("\"__cause\":\"").append(r.cause.getName()).append("\",");
        }

        // win/ascending flags
        inject.append("\"__win\":").append(r.win).append(",");
        inject.append("\"__ascending\":").append(r.ascending).append(",");

        // armorTier
        inject.append("\"__armorTier\":").append(r.armorTier).append(",");

        // depth
        inject.append("\"__depth\":").append(r.depth).append(",");

        // date and version
        if (r.date != null) {
            inject.append("\"__date\":\"").append(r.date).append("\",");
        }
        if (r.version != null) {
            inject.append("\"__version\":\"").append(r.version).append("\",");
        }

        // customSeed
        if (r.customSeed != null && !r.customSeed.isEmpty()) {
            inject.append("\"__customSeed\":\"").append(r.customSeed).append("\",");
        }

        // score
        inject.append("\"__score\":").append(r.score).append(",");

        // Insert after opening brace
        if (json.startsWith("{") && inject.length() > 0) {
            json = "{" + inject.toString() + json.substring(1);
        }

        return json;
    }

    /**
     * Applies all optimizations to JSON string.
     * 1. Shorten keys
     * 2. Shorten class names
     * 3. Trim backpack (remove non-quickslot items)
     */
    private static String optimizeJson(String json) {
        // Option 3: Trim backpack - remove items without quickslot key
        json = trimBackpack(json);

        // Option 2: Shorten class names
        json = json.replace(CLASS_PREFIX, CLASS_SHORT);

        // Option 1: Shorten keys
        for (Map.Entry<String, String> entry : KEY_MAP.entrySet()) {
            // Replace "key": with "shortKey":
            json = json.replace("\"" + entry.getKey() + "\":", "\"" + entry.getValue() + "\":");
        }

        return json;
    }

    /**
     * Trims backpack items, keeping only those with quickslot key.
     * This is a simplified implementation using string manipulation.
     */
    private static String trimBackpack(String json) {
        // Find backpack items array and filter
        // This is a heuristic approach - for robustness, should use JSON parser
        try {
            int bpStart = json.indexOf("\"backpack\":");
            if (bpStart < 0) return json;

            int itemsStart = json.indexOf("\"items\":[", bpStart);
            if (itemsStart < 0) return json;

            int arrayStart = json.indexOf("[", itemsStart);
            if (arrayStart < 0) return json;

            // Find matching ]
            int depth = 1;
            int arrayEnd = arrayStart + 1;
            while (depth > 0 && arrayEnd < json.length()) {
                char c = json.charAt(arrayEnd);
                if (c == '[') depth++;
                else if (c == ']') depth--;
                arrayEnd++;
            }

            String itemsArray = json.substring(arrayStart, arrayEnd);

            // Parse items and filter those with quickslot
            String filteredItems = filterQuickslotItems(itemsArray);

            // Replace original items array with filtered
            json = json.substring(0, arrayStart) + filteredItems + json.substring(arrayEnd);

        } catch (Exception e) {
            // Backpack trimming failed - return original
        }

        return json;
    }

    /**
     * Filters items array to keep only those with "quickslot" key.
     */
    private static String filterQuickslotItems(String itemsArray) {
        if (itemsArray.equals("[]")) return "[]";

        StringBuilder result = new StringBuilder("[");
        boolean first = true;

        // Simple parsing: split by },{ pattern
        int depth = 0;
        int itemStart = 1; // Skip opening [

        for (int i = 1; i < itemsArray.length() - 1; i++) {
            char c = itemsArray.charAt(i);
            if (c == '{') {
                if (depth == 0) itemStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    String item = itemsArray.substring(itemStart, i + 1);
                    // Check if this item has quickslot key
                    if (item.contains("\"quickslot\":") || item.contains("\"quickslot\" :")) {
                        if (!first) result.append(",");
                        result.append(item);
                        first = false;
                    }
                }
            }
        }

        result.append("]");
        return result.toString();
    }

    /**
     * Exports global badges as a 24-byte bitmap.
     */
    private static byte[] exportGlobalBadges() {
        byte[] bitmap = new byte[BADGES_SIZE];
        Badges.loadGlobal();

        Badges.Badge[] allBadges = Badges.Badge.values();
        for (int i = 0; i < allBadges.length && i < 192; i++) {
            if (Badges.isUnlocked(allBadges[i])) {
                bitmap[i / 8] |= (1 << (i % 8));
            }
        }

        return bitmap;
    }

    /**
     * Exports catalog as a 50-byte bitmap.
     */
    private static byte[] exportCatalog() {
        byte[] bitmap = new byte[CATALOG_SIZE];

        int bitIndex = 0;
        for (Catalog cat : Catalog.values()) {
            for (Class<?> itemClass : cat.items()) {
                if (bitIndex < 400) {
                    if (Catalog.isSeen(itemClass)) {
                        bitmap[bitIndex / 8] |= (1 << (bitIndex % 8));
                    }
                    bitIndex++;
                }
            }
        }

        return bitmap;
    }

    /**
     * Exports bestiary as a 25-byte bitmap.
     */
    private static byte[] exportBestiary() {
        byte[] bitmap = new byte[BESTIARY_SIZE];

        int bitIndex = 0;
        for (Bestiary cat : Bestiary.values()) {
            for (Class<?> entityClass : cat.entities()) {
                if (bitIndex < 200) {
                    if (Bestiary.isSeen(entityClass)) {
                        bitmap[bitIndex / 8] |= (1 << (bitIndex % 8));
                    }
                    bitIndex++;
                }
            }
        }

        return bitmap;
    }

    /**
     * Exports document pages as a 16-byte bitmap (2 bits per page).
     */
    private static byte[] exportDocument() {
        byte[] bitmap = new byte[DOCUMENT_SIZE];

        int pageIndex = 0;
        for (Document doc : Document.values()) {
            for (String page : doc.pageNames()) {
                if (pageIndex < 64) {
                    int state = 0;
                    if (doc.isPageRead(page)) {
                        state = 2; // READ
                    } else if (doc.isPageFound(page)) {
                        state = 1; // FOUND
                    }
                    // 2 bits per page: pageIndex * 2 bits
                    int byteIdx = (pageIndex * 2) / 8;
                    int bitOffset = (pageIndex * 2) % 8;
                    bitmap[byteIdx] |= (state << bitOffset);
                    pageIndex++;
                }
            }
        }

        return bitmap;
    }
}
