/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sacredpixel.sacredpixeldungeon;

import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.journal.Journal;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Platform-independent cloud save interface.
 * Appsintoss platform provides Firebase implementation.
 *
 * Saves the same data as "데이터 내보내기" feature:
 * - Badges (업적)
 * - Rankings (랭킹 기록)
 * - Catalog (아이템 도감)
 * - Bestiary (몬스터 도감)
 * - Documents (Lore, Guide, Alchemy)
 */
public class CloudSave {

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

    // Platform-specific implementation (set by platform launcher)
    public static CloudSaveImpl impl = null;

    // Data format version (v2: added document read state, rankings gameData)
    private static final int VERSION = 2;

    // Max rankings to save (same as WebDataExporter)
    private static final int MAX_RANKINGS = 6;

    // Flag to prevent recursive saves during restore
    private static boolean isRestoring = false;

    /**
     * Check if we're currently restoring from cloud.
     * Used by Badges.unlock() to skip cloud save during restore.
     */
    public static boolean isRestoring() {
        return isRestoring;
    }

    /**
     * Platform-specific implementation interface.
     */
    public interface CloudSaveImpl {
        void load(CloudSaveCallback callback);
        void save(String jsonData, CloudSaveCallback callback);
        boolean isAvailable();
    }

    /**
     * Callback for async operations.
     */
    public interface CloudSaveCallback {
        void onComplete(boolean success, String data);
    }

    /**
     * Check if cloud save is available on this platform.
     */
    public static boolean isAvailable() {
        return impl != null && impl.isAvailable();
    }

    /**
     * Load data from cloud and merge with local data.
     * Call this on game start (e.g., in TitleScene).
     */
    public static void loadAndMerge(Runnable onComplete) {
        if (!isAvailable()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        impl.load(new CloudSaveCallback() {
            @Override
            public void onComplete(boolean success, String data) {
                if (success && data != null) {
                    try {
                        mergeCloudData(data);
                    } catch (Exception e) {
                        System.err.println("[CloudSave] Merge failed: " + e.getMessage());
                    }
                }
                if (onComplete != null) onComplete.run();
            }
        });
    }

    /**
     * Save current data to cloud.
     * Call this when badges are unlocked, game is completed, etc.
     */
    public static void saveToCloud() {
        if (!isAvailable()) {
            return;
        }

        try {
            String jsonData = exportToJson();
            impl.save(jsonData, new CloudSaveCallback() {
                @Override
                public void onComplete(boolean success, String data) {
                    if (!success) {
                        System.err.println("[CloudSave] Save failed");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[CloudSave] Export failed: " + e.getMessage());
        }
    }

    /**
     * Export current game data to JSON string.
     */
    private static String exportToJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Version
        sb.append("\"version\":").append(VERSION).append(",");

        // Badges
        Badges.loadGlobal();
        sb.append("\"badges\":[");
        boolean first = true;
        for (Badges.Badge badge : Badges.Badge.values()) {
            if (Badges.isUnlocked(badge)) {
                if (!first) sb.append(",");
                sb.append("\"").append(badge.name()).append("\"");
                first = false;
            }
        }
        sb.append("],");

        // Rankings (top MAX_RANKINGS with gameData, same as WebDataExporter)
        Rankings.INSTANCE.load();
        sb.append("\"rankings\":[");
        first = true;
        int count = 0;
        for (Rankings.Record record : Rankings.INSTANCE.records) {
            if (count >= MAX_RANKINGS) break;
            if (!first) sb.append(",");
            sb.append("{");
            sb.append("\"score\":").append(record.score).append(",");
            sb.append("\"heroClass\":\"").append(record.heroClass.name()).append("\",");
            sb.append("\"armorTier\":").append(record.armorTier).append(",");
            sb.append("\"herolevel\":").append(record.herolevel).append(",");
            sb.append("\"depth\":").append(record.depth).append(",");
            sb.append("\"win\":").append(record.win).append(",");
            sb.append("\"ascending\":").append(record.ascending).append(",");
            sb.append("\"challenges\":").append(record.challenges).append(",");
            sb.append("\"seed\":\"").append(record.customSeed != null ? record.customSeed : "").append("\",");
            sb.append("\"date\":\"").append(record.date != null ? record.date : "").append("\",");
            sb.append("\"gameID\":\"").append(record.gameID != null ? record.gameID : "").append("\"");
            // Include gameData if available
            if (record.gameData == null) {
                Rankings.INSTANCE.loadGameData(record);
            }
            if (record.gameData != null) {
                String gameDataJson = record.gameData.toString();
                // Escape for JSON string
                gameDataJson = gameDataJson.replace("\\", "\\\\").replace("\"", "\\\"");
                sb.append(",\"gameData\":\"").append(gameDataJson).append("\"");
            }
            sb.append("}");
            first = false;
            count++;
        }
        sb.append("],");

        // Catalog
        sb.append("\"catalog\":{");
        first = true;
        for (Catalog cat : Catalog.values()) {
            if (!first) sb.append(",");
            sb.append("\"").append(cat.name()).append("\":[");
            boolean firstItem = true;
            for (Class<?> cls : cat.items()) {
                if (Catalog.isSeen(cls)) {
                    if (!firstItem) sb.append(",");
                    sb.append("\"").append(cls.getName()).append("\"");
                    firstItem = false;
                }
            }
            sb.append("]");
            first = false;
        }
        sb.append("},");

        // Bestiary
        sb.append("\"bestiary\":{");
        first = true;
        for (Bestiary cat : Bestiary.values()) {
            if (!first) sb.append(",");
            sb.append("\"").append(cat.name()).append("\":[");
            boolean firstItem = true;
            for (Class<?> cls : cat.entities()) {
                if (Bestiary.isSeen(cls)) {
                    if (!firstItem) sb.append(",");
                    sb.append("\"").append(cls.getName()).append("\"");
                    firstItem = false;
                }
            }
            sb.append("]");
            first = false;
        }
        sb.append("},");

        // Documents (Lore, Guide, Alchemy) - with found/read state
        Journal.loadGlobal();
        sb.append("\"documents\":{");
        Document[] docs = {
            Document.ADVENTURERS_GUIDE,
            Document.ALCHEMY_GUIDE,
            Document.INTROS,
            Document.SEWERS_GUARD,
            Document.PRISON_WARDEN,
            Document.CAVES_EXPLORER,
            Document.CITY_WARLOCK,
            Document.HALLS_KING
        };
        first = true;
        for (Document doc : docs) {
            if (!first) sb.append(",");
            sb.append("\"").append(doc.name()).append("\":{");
            sb.append("\"found\":[");
            boolean firstPage = true;
            for (String page : doc.pageNames()) {
                if (doc.isPageFound(page) && !doc.isPageRead(page)) {
                    if (!firstPage) sb.append(",");
                    sb.append("\"").append(page).append("\"");
                    firstPage = false;
                }
            }
            sb.append("],\"read\":[");
            firstPage = true;
            for (String page : doc.pageNames()) {
                if (doc.isPageRead(page)) {
                    if (!firstPage) sb.append(",");
                    sb.append("\"").append(page).append("\"");
                    firstPage = false;
                }
            }
            sb.append("]}");
            first = false;
        }
        sb.append("}");

        sb.append("}");
        return sb.toString();
    }

    /**
     * Merge cloud data with local data (union merge - keep more).
     */
    private static void mergeCloudData(String jsonData) {
        // Set flag to prevent recursive saves during restore
        isRestoring = true;

        try {
            // Simple JSON parsing (no external library)
            // This is a simplified parser - in production you might want a proper JSON library

            // Parse badges
            int badgesStart = jsonData.indexOf("\"badges\":[");
            if (badgesStart >= 0) {
                int badgesEnd = jsonData.indexOf("]", badgesStart);
                String badgesStr = jsonData.substring(badgesStart + 10, badgesEnd);
                mergeBadges(badgesStr);
            }

            // Parse rankings
            int rankingsStart = jsonData.indexOf("\"rankings\":[");
            if (rankingsStart >= 0) {
                int rankingsEnd = findMatchingBracket(jsonData, rankingsStart + 11);
                String rankingsStr = jsonData.substring(rankingsStart + 12, rankingsEnd);
                mergeRankings(rankingsStr);
            }

            // Parse catalog
            int catalogStart = jsonData.indexOf("\"catalog\":{");
            if (catalogStart >= 0) {
                int catalogEnd = findMatchingBrace(jsonData, catalogStart + 10);
                String catalogStr = jsonData.substring(catalogStart + 11, catalogEnd);
                mergeCatalog(catalogStr);
            }

            // Parse bestiary
            int bestiaryStart = jsonData.indexOf("\"bestiary\":{");
            if (bestiaryStart >= 0) {
                int bestiaryEnd = findMatchingBrace(jsonData, bestiaryStart + 11);
                String bestiaryStr = jsonData.substring(bestiaryStart + 12, bestiaryEnd);
                mergeBestiary(bestiaryStr);
            }

            // Parse documents
            int documentsStart = jsonData.indexOf("\"documents\":{");
            if (documentsStart >= 0) {
                int documentsEnd = findMatchingBrace(jsonData, documentsStart + 12);
                String documentsStr = jsonData.substring(documentsStart + 13, documentsEnd);
                mergeDocuments(documentsStr);
            }

            // Save merged data locally
            Badges.saveGlobal();
            Journal.saveGlobal();
        } finally {
            isRestoring = false;
        }
    }

    private static void mergeBadges(String badgesStr) {
        Badges.loadGlobal();
        // Parse badge names from JSON array string
        String[] parts = badgesStr.replace("\"", "").split(",");
        for (String part : parts) {
            String badgeName = part.trim();
            if (!badgeName.isEmpty()) {
                try {
                    Badges.Badge badge = Badges.Badge.valueOf(badgeName);
                    if (!Badges.isUnlocked(badge)) {
                        Badges.unlock(badge);
                    }
                } catch (IllegalArgumentException e) {
                    // Unknown badge, skip
                }
            }
        }
        Badges.saveGlobal();
    }

    private static void mergeCatalog(String catalogStr) {
        // Parse catalog: {"WEAPONS":["class1","class2"],"ARMOR":["class3"]}
        // Extract class names and mark them as seen
        int pos = 0;
        while (pos < catalogStr.length()) {
            // Find next array start
            int arrayStart = catalogStr.indexOf('[', pos);
            if (arrayStart < 0) break;

            int arrayEnd = catalogStr.indexOf(']', arrayStart);
            if (arrayEnd < 0) break;

            String arrayContent = catalogStr.substring(arrayStart + 1, arrayEnd);
            // Parse class names from array
            String[] classNames = arrayContent.replace("\"", "").split(",");
            for (String className : classNames) {
                className = className.trim();
                if (isAllowedClass(className)) {
                    try {
                        Class<?> cls = Class.forName(className);
                        if (!Catalog.isSeen(cls)) {
                            Catalog.setSeen(cls);
                        }
                    } catch (ClassNotFoundException e) {
                        // Unknown class, skip
                    }
                }
            }
            pos = arrayEnd + 1;
        }
    }

    private static void mergeBestiary(String bestiaryStr) {
        // Parse bestiary: {"SEWERS":["class1","class2"],"PRISON":["class3"]}
        // Extract class names and mark them as seen
        int pos = 0;
        while (pos < bestiaryStr.length()) {
            // Find next array start
            int arrayStart = bestiaryStr.indexOf('[', pos);
            if (arrayStart < 0) break;

            int arrayEnd = bestiaryStr.indexOf(']', arrayStart);
            if (arrayEnd < 0) break;

            String arrayContent = bestiaryStr.substring(arrayStart + 1, arrayEnd);
            // Parse class names from array
            String[] classNames = arrayContent.replace("\"", "").split(",");
            for (String className : classNames) {
                className = className.trim();
                if (isAllowedClass(className)) {
                    try {
                        Class<?> cls = Class.forName(className);
                        if (!Bestiary.isSeen(cls)) {
                            Bestiary.setSeen(cls);
                        }
                    } catch (ClassNotFoundException e) {
                        // Unknown class, skip
                    }
                }
            }
            pos = arrayEnd + 1;
        }
    }

    private static void mergeDocuments(String documentsStr) {
        // Parse documents v2: {"ADVENTURERS_GUIDE":{"found":["page1"],"read":["page2"]},...}
        // Also supports v1: {"ADVENTURERS_GUIDE":["page1","page2"],...}
        Journal.loadGlobal();

        Document[] docs = {
            Document.ADVENTURERS_GUIDE,
            Document.ALCHEMY_GUIDE,
            Document.INTROS,
            Document.SEWERS_GUARD,
            Document.PRISON_WARDEN,
            Document.CAVES_EXPLORER,
            Document.CITY_WARLOCK,
            Document.HALLS_KING
        };

        for (Document doc : docs) {
            String docName = doc.name();

            // Try v2 format first: {"found":[...],"read":[...]}
            String docKeyV2 = "\"" + docName + "\":{";
            int docStartV2 = documentsStr.indexOf(docKeyV2);

            if (docStartV2 >= 0) {
                // v2 format with found/read arrays
                int objStart = docStartV2 + docKeyV2.length() - 1;
                int objEnd = findMatchingBrace(documentsStr, objStart);
                String docContent = documentsStr.substring(objStart + 1, objEnd);

                // Parse "found" array
                parseAndApplyPages(doc, docContent, "found", false);
                // Parse "read" array
                parseAndApplyPages(doc, docContent, "read", true);
            } else {
                // Try v1 format: ["page1","page2"]
                String docKeyV1 = "\"" + docName + "\":[";
                int docStartV1 = documentsStr.indexOf(docKeyV1);
                if (docStartV1 >= 0) {
                    int arrayStart = docStartV1 + docKeyV1.length() - 1;
                    int arrayEnd = documentsStr.indexOf(']', arrayStart);
                    if (arrayEnd >= 0) {
                        String arrayContent = documentsStr.substring(arrayStart + 1, arrayEnd);
                        String[] pageNames = arrayContent.replace("\"", "").split(",");
                        for (String pageName : pageNames) {
                            pageName = pageName.trim();
                            if (!pageName.isEmpty() && !doc.isPageFound(pageName)) {
                                doc.findPage(pageName);
                            }
                        }
                    }
                }
            }
        }

        Journal.saveGlobal();
    }

    private static void parseAndApplyPages(Document doc, String content, String arrayName, boolean markAsRead) {
        String arrayKey = "\"" + arrayName + "\":[";
        int arrayStart = content.indexOf(arrayKey);
        if (arrayStart < 0) return;

        int start = arrayStart + arrayKey.length();
        int end = content.indexOf(']', start);
        if (end < 0) return;

        String arrayContent = content.substring(start, end);
        String[] pageNames = arrayContent.replace("\"", "").split(",");
        for (String pageName : pageNames) {
            pageName = pageName.trim();
            if (pageName.isEmpty()) continue;

            if (!doc.isPageFound(pageName)) {
                doc.findPage(pageName);
            }
            if (markAsRead && !doc.isPageRead(pageName)) {
                doc.readPage(pageName);
            }
        }
    }

    private static int findMatchingBrace(String str, int start) {
        int depth = 1;
        for (int i = start + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return str.length() - 1;
    }

    private static int findMatchingBraceWithStrings(String str, int start) {
        // Like findMatchingBrace but skips over string contents (handles escaped quotes)
        int depth = 1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return str.length() - 1;
    }

    private static int findMatchingBracket(String str, int start) {
        int depth = 1;
        for (int i = start + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return str.length() - 1;
    }

    private static void mergeRankings(String rankingsStr) {
        // Parse rankings array: [{...},{...},...]
        Rankings.INSTANCE.load();

        // Build map of existing records by key
        java.util.Map<String, Rankings.Record> existingMap = new java.util.HashMap<>();
        for (Rankings.Record r : Rankings.INSTANCE.records) {
            existingMap.put(makeRecordKey(r.score, r.heroClass, r.depth, r.win, r.herolevel), r);
        }

        // Parse cloud records and merge
        List<Rankings.Record> cloudRecords = parseRankingsJson(rankingsStr);
        for (Rankings.Record cloud : cloudRecords) {
            String key = makeRecordKey(cloud.score, cloud.heroClass, cloud.depth, cloud.win, cloud.herolevel);
            Rankings.Record existing = existingMap.get(key);

            if (existing == null) {
                // New record, add it
                Rankings.INSTANCE.records.add(cloud);
                existingMap.put(key, cloud);
            } else if (cloud.gameData != null && existing.gameData == null) {
                // Cloud has gameData, local doesn't - update local with cloud's gameData
                existing.gameData = cloud.gameData;
                existing.ascending = cloud.ascending;
                existing.date = cloud.date != null ? cloud.date : existing.date;
                existing.gameID = cloud.gameID != null ? cloud.gameID : existing.gameID;
            }
        }

        // Sort by score descending and keep top TABLE_SIZE records
        Collections.sort(Rankings.INSTANCE.records, (a, b) -> b.score - a.score);
        while (Rankings.INSTANCE.records.size() > Rankings.TABLE_SIZE) {
            Rankings.INSTANCE.records.remove(Rankings.INSTANCE.records.size() - 1);
        }

        // Save merged rankings and gameData
        Rankings.INSTANCE.save();
        for (Rankings.Record r : Rankings.INSTANCE.records) {
            if (r.gameData != null) {
                Rankings.INSTANCE.saveGameData(r);
            }
        }
    }

    private static String makeRecordKey(int score, HeroClass heroClass, int depth, boolean win, int herolevel) {
        return score + "_" + (heroClass != null ? heroClass.name() : "null") + "_" + depth + "_" + win + "_" + herolevel;
    }

    private static List<Rankings.Record> parseRankingsJson(String rankingsStr) {
        List<Rankings.Record> records = new ArrayList<>();

        // Parse JSON array of objects: {score:123,heroClass:"WARRIOR",...,gameData:"escaped json"}
        int pos = 0;
        while (pos < rankingsStr.length()) {
            int objStart = rankingsStr.indexOf('{', pos);
            if (objStart < 0) break;

            // Use findMatchingBraceWithStrings to handle escaped strings in gameData
            int objEnd = findMatchingBraceWithStrings(rankingsStr, objStart);
            if (objEnd < 0) break;

            String objStr = rankingsStr.substring(objStart + 1, objEnd);
            Rankings.Record rec = parseRecordJson(objStr);
            if (rec != null) {
                records.add(rec);
            }
            pos = objEnd + 1;
        }

        return records;
    }

    private static Rankings.Record parseRecordJson(String objStr) {
        try {
            Rankings.Record rec = new Rankings.Record();

            // Parse score
            rec.score = parseIntField(objStr, "score");

            // Parse heroClass
            String heroClassName = parseStringField(objStr, "heroClass");
            if (heroClassName != null && !heroClassName.isEmpty()) {
                try {
                    rec.heroClass = HeroClass.valueOf(heroClassName);
                } catch (IllegalArgumentException e) {
                    rec.heroClass = HeroClass.WARRIOR; // default
                }
            }

            // Parse other fields
            rec.armorTier = parseIntField(objStr, "armorTier");
            rec.herolevel = parseIntField(objStr, "herolevel");
            rec.depth = parseIntField(objStr, "depth");
            rec.win = parseBooleanField(objStr, "win");
            rec.ascending = parseBooleanField(objStr, "ascending");
            rec.challenges = parseIntField(objStr, "challenges");
            rec.customSeed = parseStringField(objStr, "seed");
            rec.date = parseStringField(objStr, "date");
            rec.gameID = parseStringField(objStr, "gameID");

            // Parse gameData (escaped JSON string)
            String gameDataStr = parseEscapedJsonField(objStr, "gameData");
            if (gameDataStr != null && !gameDataStr.isEmpty()) {
                try {
                    rec.gameData = Bundle.read(gameDataStr);
                } catch (Exception e) {
                    // Failed to parse gameData, leave as null
                }
            }

            return rec;
        } catch (Exception e) {
            return null;
        }
    }

    private static String parseEscapedJsonField(String objStr, String fieldName) {
        String pattern = "\"" + fieldName + "\":\"";
        int idx = objStr.indexOf(pattern);
        if (idx < 0) return null;

        int valueStart = idx + pattern.length();
        // Find the closing quote, handling escaped quotes
        int valueEnd = valueStart;
        boolean escaped = false;
        while (valueEnd < objStr.length()) {
            char c = objStr.charAt(valueEnd);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            }
            valueEnd++;
        }
        if (valueEnd >= objStr.length()) return null;

        String escaped_str = objStr.substring(valueStart, valueEnd);
        // Unescape
        return escaped_str.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static int parseIntField(String objStr, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int idx = objStr.indexOf(pattern);
        if (idx < 0) return 0;

        int valueStart = idx + pattern.length();
        int valueEnd = valueStart;
        while (valueEnd < objStr.length()) {
            char c = objStr.charAt(valueEnd);
            if (c == ',' || c == '}' || c == ' ') break;
            valueEnd++;
        }
        try {
            return Integer.parseInt(objStr.substring(valueStart, valueEnd).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean parseBooleanField(String objStr, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int idx = objStr.indexOf(pattern);
        if (idx < 0) return false;

        int valueStart = idx + pattern.length();
        return objStr.substring(valueStart).trim().startsWith("true");
    }

    private static String parseStringField(String objStr, String fieldName) {
        String pattern = "\"" + fieldName + "\":\"";
        int idx = objStr.indexOf(pattern);
        if (idx < 0) return "";

        int valueStart = idx + pattern.length();
        int valueEnd = objStr.indexOf("\"", valueStart);
        if (valueEnd < 0) return "";

        return objStr.substring(valueStart, valueEnd);
    }
}
