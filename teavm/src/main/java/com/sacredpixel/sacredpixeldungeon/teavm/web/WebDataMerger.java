/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebDataMerger - Merges imported data with local data
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.journal.Journal;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Merges imported game data with local data.
 * Supports VERSION 0x09 format.
 */
public class WebDataMerger {

    /**
     * Merge policy enum.
     */
    public enum MergePolicy {
        MERGE_UNION,
        OVERWRITE
    }

    /**
     * Merge result for UI feedback.
     */
    public static class MergeResult {
        public int newBadges;
        public int newRankings;
        public int newCatalogItems;
        public int newBestiaryEntries;
        public int newDocumentPages;

        public boolean hasChanges() {
            return newBadges > 0 || newRankings > 0 || newCatalogItems > 0
                    || newBestiaryEntries > 0 || newDocumentPages > 0;
        }
    }

    /**
     * Merges imported data using specified policy.
     */
    public static MergeResult merge(WebDataImporter.ImportedData imported, MergePolicy policy) {
        MergeResult result = new MergeResult();

        // Restore gameData from JSON to Records
        if (imported.gameDataJsons != null) {
            restoreGameDataFromJson(imported);
        }

        if (policy == MergePolicy.OVERWRITE) {
            result.newBadges = applyBadges(imported.badges, true);
            result.newRankings = applyRankings(imported.rankings, true);
            result.newCatalogItems = applyCatalog(imported.catalog, true);
            result.newBestiaryEntries = applyBestiary(imported.bestiary, true);
            result.newDocumentPages = applyDocument(imported.document, true);
        } else {
            result.newBadges = mergeBadges(imported.badges);
            result.newRankings = mergeRankings(imported.rankings);
            result.newCatalogItems = mergeCatalog(imported.catalog);
            result.newBestiaryEntries = mergeBestiary(imported.bestiary);
            result.newDocumentPages = mergeDocument(imported.document);
        }

        Journal.saveGlobal(true);

        return result;
    }

    /**
     * Restores gameData Bundle from JSON strings.
     */
    private static void restoreGameDataFromJson(WebDataImporter.ImportedData imported) {
        List<Rankings.Record> records = imported.rankings;
        List<String> jsons = imported.gameDataJsons;

        if (records == null || jsons == null) return;

        for (int i = 0; i < records.size() && i < jsons.size(); i++) {
            String json = jsons.get(i);
            if (json != null && !json.isEmpty() && !json.equals("null")) {
                try {
                    Bundle gameData = Bundle.read(json);
                    records.get(i).gameData = gameData;
                } catch (Exception e) {
                    // Failed to restore gameData - ignore
                }
            }
        }
    }

    /**
     * Merges badges using OR operation.
     */
    private static int mergeBadges(byte[] importedBitmap) {
        if (importedBitmap == null) return 0;

        Badges.loadGlobal();
        int newCount = 0;

        Badges.Badge[] allBadges = Badges.Badge.values();
        for (int i = 0; i < allBadges.length && i < 192; i++) {
            if (WebDataImporter.isBitSet(importedBitmap, i)) {
                if (!Badges.isUnlocked(allBadges[i])) {
                    Badges.unlock(allBadges[i]);
                    newCount++;
                }
            }
        }

        if (newCount > 0) {
            Badges.saveGlobal();
        }

        return newCount;
    }

    private static int applyBadges(byte[] importedBitmap, boolean overwrite) {
        if (overwrite) {
            // Use clearGlobal() instead of reset() to actually clear global badges.
            // reset() only clears local badges and doesn't affect global because
            // loadGlobal() is a no-op when global is already initialized.
            Badges.clearGlobal();
        }
        return mergeBadges(importedBitmap);
    }

    /**
     * Merges rankings using Top-6 sort (Top 5 + 1).
     */
    private static int mergeRankings(List<Rankings.Record> importedRecords) {
        if (importedRecords == null || importedRecords.isEmpty()) return 0;

        Rankings.INSTANCE.load();
        List<Rankings.Record> local = Rankings.INSTANCE.records;

        Set<String> seenIds = new HashSet<>();
        List<Rankings.Record> merged = new ArrayList<>();

        // Add local records first
        for (Rankings.Record r : local) {
            if (r.gameID != null && seenIds.add(r.gameID)) {
                merged.add(r);
            }
        }

        int originalSize = merged.size();

        // Add imported records (deduped)
        for (Rankings.Record r : importedRecords) {
            if (r.gameID != null && seenIds.add(r.gameID)) {
                merged.add(r);
            }
        }

        // Sort using Rankings.scoreComparator (considers customSeed + score)
        Collections.sort(merged, Rankings.scoreComparator);

        // Keep top 6 (Top 5 + 1)
        ArrayList<Rankings.Record> finalList = new ArrayList<>();
        for (int i = 0; i < Math.min(merged.size(), 6); i++) {
            finalList.add(merged.get(i));
        }

        int newCount = Math.max(0, finalList.size() - originalSize);

        Rankings.INSTANCE.records = finalList;
        Rankings.INSTANCE.lastRecord = -1;  // Clear highlight after import (no "newest" record)
        Rankings.INSTANCE.save();

        return newCount;
    }

    private static int applyRankings(List<Rankings.Record> importedRecords, boolean overwrite) {
        if (overwrite) {
            Rankings.INSTANCE.records = new ArrayList<>();
        }
        return mergeRankings(importedRecords);
    }

    /**
     * Merges catalog using OR operation.
     */
    private static int mergeCatalog(byte[] importedBitmap) {
        if (importedBitmap == null) return 0;

        int newCount = 0;
        int bitIndex = 0;

        for (Catalog cat : Catalog.values()) {
            for (Class<?> itemClass : cat.items()) {
                if (bitIndex < 400) {
                    if (WebDataImporter.isBitSet(importedBitmap, bitIndex)) {
                        if (!Catalog.isSeen(itemClass)) {
                            Catalog.setSeen(itemClass);
                            newCount++;
                        }
                    }
                    bitIndex++;
                }
            }
        }

        return newCount;
    }

    private static int applyCatalog(byte[] importedBitmap, boolean overwrite) {
        if (overwrite) {
            Catalog.reset();
        }
        return mergeCatalog(importedBitmap);
    }

    /**
     * Merges bestiary using OR operation.
     */
    private static int mergeBestiary(byte[] importedBitmap) {
        if (importedBitmap == null) return 0;

        int newCount = 0;
        int bitIndex = 0;

        for (Bestiary cat : Bestiary.values()) {
            for (Class<?> entityClass : cat.entities()) {
                if (bitIndex < 200) {
                    if (WebDataImporter.isBitSet(importedBitmap, bitIndex)) {
                        if (!Bestiary.isSeen(entityClass)) {
                            Bestiary.setSeen(entityClass);
                            newCount++;
                        }
                    }
                    bitIndex++;
                }
            }
        }

        return newCount;
    }

    private static int applyBestiary(byte[] importedBitmap, boolean overwrite) {
        if (overwrite) {
            Bestiary.reset();
        }
        return mergeBestiary(importedBitmap);
    }

    /**
     * Merges document pages using MAX operation.
     */
    private static int mergeDocument(byte[] importedBitmap) {
        if (importedBitmap == null) return 0;

        int newCount = 0;
        int pageIndex = 0;

        for (Document doc : Document.values()) {
            for (String page : doc.pageNames()) {
                if (pageIndex < 64) {
                    int importedState = WebDataImporter.getDocumentPageState(importedBitmap, pageIndex);
                    if (importedState > 0) {
                        if (importedState >= 2 && !doc.isPageRead(page)) {
                            if (!doc.isPageFound(page)) {
                                doc.findPage(page);
                                newCount++;
                            }
                            doc.readPage(page);
                        } else if (importedState >= 1 && !doc.isPageFound(page)) {
                            doc.findPage(page);
                            newCount++;
                        }
                    }
                    pageIndex++;
                }
            }
        }

        return newCount;
    }

    private static int applyDocument(byte[] importedBitmap, boolean overwrite) {
        if (overwrite) {
            Document.reset();
        }
        return mergeDocument(importedBitmap);
    }
}
