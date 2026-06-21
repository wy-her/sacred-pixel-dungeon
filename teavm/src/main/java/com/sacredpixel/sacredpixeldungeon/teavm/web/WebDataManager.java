/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebDataManager - High-level API for data export/import
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import org.teavm.jso.JSBody;

/**
 * High-level manager for web data synchronization.
 * Provides simple API for DataScene and other UI components.
 */
public class WebDataManager {

    /**
     * Export result container.
     */
    public static class ExportResult {
        public boolean success;
        public String url;
        public String error;
        public int rawSize;
        public int compressedSize;
        public int urlLength;
    }

    /**
     * Import preview container.
     */
    public static class ImportPreview {
        public boolean valid;
        public String error;

        // Imported data counts
        public int badgeCount;
        public int rankingCount;
        public int highestScore;
        public int catalogCount;
        public int bestiaryCount;
        public int documentCount;
        public int guideCount;
        public int alchemyCount;
        public int loreCount;

        // Local data counts (for comparison)
        public int localBadgeCount;
        public int localRankingCount;
        public int localHighestScore;
        public int localCatalogCount;
        public int localBestiaryCount;
        public int localLoreCount;
        public int localGuideCount;
        public int localAlchemyCount;

        // Differences
        public int newBadges;
        public int newCatalogItems;
        public int newBestiaryEntries;
        public int newLorePages;
        public int newGuidePages;
        public int newAlchemyPages;

        // Raw parsed data for merge
        WebDataImporter.ImportedData importedData;
    }

    /**
     * Exports all game data and returns share URL.
     * @return Export result with URL or error
     */
    public static ExportResult exportData() {
        ExportResult result = new ExportResult();

        try {
            log("Export: Starting export...");

            // Check pako availability
            if (!WebCompressor.isPakoAvailable()) {
                result.success = false;
                result.error = "Compression library not loaded";
                log("Export: FAILED - pako not available");
                return result;
            }
            log("Export: pako available, version=" + WebCompressor.getPakoVersion());

            // Export raw data
            log("Export: Exporting raw data...");
            byte[] rawData = WebDataExporter.exportAll();
            if (rawData == null) {
                result.success = false;
                result.error = "Export data is null";
                log("Export: FAILED - raw data is null");
                return result;
            }
            result.rawSize = rawData.length;
            log("Export: Raw data size=" + rawData.length);

            // Compress
            log("Export: Compressing...");
            byte[] compressed = WebCompressor.deflate(rawData);
            if (compressed == null) {
                result.success = false;
                result.error = "Compression failed";
                log("Export: FAILED - compression returned null");
                return result;
            }
            result.compressedSize = compressed.length;
            log("Export: Compressed size=" + compressed.length);

            // Create URL
            log("Export: Creating URL...");
            result.url = WebUrlCodec.createShareUrl(compressed);
            if (result.url == null) {
                result.success = false;
                result.error = "URL creation failed";
                log("Export: FAILED - URL is null");
                return result;
            }
            result.urlLength = result.url.length();
            log("Export: SUCCESS - URL length=" + result.url.length());

            result.success = true;
            return result;

        } catch (Exception e) {
            result.success = false;
            result.error = "Export failed: " + e.getMessage();
            log("Export: EXCEPTION - " + e.getClass().getName() + ": " + e.getMessage());
            return result;
        }
    }

    @JSBody(params = {"msg"}, script = "console.log('[WebDataManager] ' + msg);")
    private static native void log(String msg);

    /**
     * Previews import from URL without applying changes.
     * @param url Full URL or fragment
     * @return Import preview with comparison data
     */
    public static ImportPreview previewImport(String url) {
        ImportPreview preview = new ImportPreview();

        try {
            log("previewImport: url=" + (url != null ? url.substring(0, Math.min(30, url.length())) + "..." : "null"));

            // Check pako availability
            if (!WebCompressor.isPakoAvailable()) {
                preview.valid = false;
                preview.error = "Compression library not loaded";
                log("previewImport: FAILED - pako not available");
                return preview;
            }

            // Extract encoded data from URL
            String encoded = WebUrlCodec.extractData(url);
            if (encoded == null || encoded.isEmpty()) {
                preview.valid = false;
                preview.error = "Invalid URL format";
                log("previewImport: FAILED - invalid URL format");
                return preview;
            }
            log("previewImport: encoded length=" + encoded.length());

            // Decode Base64
            byte[] compressed = WebUrlCodec.fromUrlSafeBase64(encoded);
            if (compressed == null) {
                preview.valid = false;
                preview.error = "Invalid Base64 encoding";
                log("previewImport: FAILED - base64 decode failed");
                return preview;
            }
            log("previewImport: compressed length=" + compressed.length);

            // Decompress
            byte[] rawData = WebCompressor.inflate(compressed);
            if (rawData == null) {
                preview.valid = false;
                preview.error = "Decompression failed";
                log("previewImport: FAILED - decompression failed");
                return preview;
            }
            log("previewImport: raw data length=" + rawData.length);

            // Parse
            WebDataImporter.ImportedData imported = WebDataImporter.parse(rawData);
            if (imported == null) {
                preview.valid = false;
                preview.error = "Invalid data format";
                log("previewImport: FAILED - parse failed (version mismatch or invalid data)");
                return preview;
            }
            log("previewImport: parse OK - badges=" + imported.badgeCount + ", rankings=" + imported.rankingCount);

            // Fill preview with imported data
            preview.importedData = imported;
            preview.badgeCount = imported.badgeCount;
            preview.rankingCount = imported.rankingCount;
            preview.highestScore = imported.highestScore;
            preview.catalogCount = imported.catalogCount;
            preview.bestiaryCount = imported.bestiaryCount;
            preview.documentCount = imported.documentCount;
            preview.guideCount = imported.guideCount;
            preview.alchemyCount = imported.alchemyCount;
            preview.loreCount = imported.loreCount;

            // Get local data for comparison
            preview.localBadgeCount = countLocalBadges();
            preview.localRankingCount = countLocalRankings();
            preview.localHighestScore = getLocalHighestScore();
            preview.localCatalogCount = countLocalCatalog();
            preview.localBestiaryCount = countLocalBestiary();
            preview.localLoreCount = countLocalLore();
            preview.localGuideCount = countLocalGuide();
            preview.localAlchemyCount = countLocalAlchemy();

            // Calculate differences
            preview.newBadges = Math.max(0, preview.badgeCount - preview.localBadgeCount);
            preview.newCatalogItems = Math.max(0, preview.catalogCount - preview.localCatalogCount);
            preview.newBestiaryEntries = Math.max(0, preview.bestiaryCount - preview.localBestiaryCount);
            preview.newLorePages = Math.max(0, preview.loreCount - preview.localLoreCount);
            preview.newGuidePages = Math.max(0, preview.guideCount - preview.localGuideCount);
            preview.newAlchemyPages = Math.max(0, preview.alchemyCount - preview.localAlchemyCount);

            preview.valid = true;
            log("previewImport: SUCCESS");
            return preview;

        } catch (Exception e) {
            preview.valid = false;
            preview.error = "Preview failed: " + e.getMessage();
            log("previewImport: EXCEPTION - " + e.getClass().getName() + ": " + e.getMessage());
            return preview;
        }
    }

    /**
     * Applies import with specified merge policy.
     * @param preview Previously generated preview
     * @param policy Merge policy
     * @return Merge result
     */
    public static WebDataMerger.MergeResult applyImport(
            ImportPreview preview,
            WebDataMerger.MergePolicy policy) {

        if (preview == null || !preview.valid || preview.importedData == null) {
            return new WebDataMerger.MergeResult();
        }

        return WebDataMerger.merge(preview.importedData, policy);
    }

    /**
     * Checks if current URL has import data.
     * @return true if hash fragment contains data
     */
    public static boolean hasImportData() {
        String fragment = WebUrlCodec.getUrlFragment();
        log("hasImportData: fragment=" + (fragment == null ? "null" : "'" + fragment + "' len=" + fragment.length()));
        if (fragment == null || fragment.isEmpty()) {
            log("hasImportData: false (null or empty)");
            return false;
        }
        // Check for legacy format: #d=CODE
        if (fragment.startsWith("#d=") && fragment.length() > 3) {
            log("hasImportData: true (legacy format)");
            return true;
        }
        // Check for new format: #CODE (at least 10 chars of base64)
        if (fragment.startsWith("#") && fragment.length() > 10) {
            log("hasImportData: true (new format)");
            return true;
        }
        log("hasImportData: false (no match)");
        return false;
    }

    /**
     * Gets import data from current URL.
     * @return Preview or null if no data
     */
    public static ImportPreview checkUrlForImport() {
        if (!hasImportData()) {
            return null;
        }
        return previewImport(WebUrlCodec.getUrlFragment());
    }

    /**
     * Copies URL to clipboard.
     * @param url URL to copy
     * @return true if successful
     */
    @JSBody(params = {"url"}, script =
            "try {" +
            "    if (navigator.clipboard && navigator.clipboard.writeText) {" +
            "        navigator.clipboard.writeText(url);" +
            "        return true;" +
            "    } else {" +
            "        var textarea = document.createElement('textarea');" +
            "        textarea.value = url;" +
            "        textarea.style.position = 'fixed';" +
            "        textarea.style.opacity = '0';" +
            "        document.body.appendChild(textarea);" +
            "        textarea.select();" +
            "        var success = document.execCommand('copy');" +
            "        document.body.removeChild(textarea);" +
            "        return success;" +
            "    }" +
            "} catch (e) {" +
            "    console.error('Clipboard copy failed:', e);" +
            "    return false;" +
            "}")
    public static native boolean copyToClipboard(String url);

    /**
     * Reads from clipboard.
     * Note: This may require user gesture and permissions.
     */
    @JSBody(script =
            "return new Promise(function(resolve) {" +
            "    if (navigator.clipboard && navigator.clipboard.readText) {" +
            "        navigator.clipboard.readText().then(resolve).catch(function() { resolve(''); });" +
            "    } else {" +
            "        resolve('');" +
            "    }" +
            "});")
    private static native void readFromClipboardAsync();

    // Local data counting helpers

    private static int countLocalBadges() {
        com.sacredpixel.sacredpixeldungeon.Badges.loadGlobal();
        int count = 0;
        for (com.sacredpixel.sacredpixeldungeon.Badges.Badge b :
                com.sacredpixel.sacredpixeldungeon.Badges.Badge.values()) {
            if (com.sacredpixel.sacredpixeldungeon.Badges.isUnlocked(b)) {
                count++;
            }
        }
        return count;
    }

    private static int countLocalRankings() {
        com.sacredpixel.sacredpixeldungeon.Rankings.INSTANCE.load();
        return com.sacredpixel.sacredpixeldungeon.Rankings.INSTANCE.records.size();
    }

    private static int getLocalHighestScore() {
        com.sacredpixel.sacredpixeldungeon.Rankings.INSTANCE.load();
        int highest = 0;
        for (com.sacredpixel.sacredpixeldungeon.Rankings.Record r :
                com.sacredpixel.sacredpixeldungeon.Rankings.INSTANCE.records) {
            if (r.score > highest) highest = r.score;
        }
        return highest;
    }

    private static int countLocalCatalog() {
        int count = 0;
        for (com.sacredpixel.sacredpixeldungeon.journal.Catalog cat :
                com.sacredpixel.sacredpixeldungeon.journal.Catalog.values()) {
            count += cat.totalSeen();
        }
        return count;
    }

    private static int countLocalBestiary() {
        int count = 0;
        for (com.sacredpixel.sacredpixeldungeon.journal.Bestiary cat :
                com.sacredpixel.sacredpixeldungeon.journal.Bestiary.values()) {
            count += cat.totalSeen();
        }
        return count;
    }

    private static int countLocalLore() {
        com.sacredpixel.sacredpixeldungeon.journal.Journal.loadGlobal();
        int count = 0;
        com.sacredpixel.sacredpixeldungeon.journal.Document[] loreDocs = {
            com.sacredpixel.sacredpixeldungeon.journal.Document.SEWERS_GUARD,
            com.sacredpixel.sacredpixeldungeon.journal.Document.PRISON_WARDEN,
            com.sacredpixel.sacredpixeldungeon.journal.Document.CAVES_EXPLORER,
            com.sacredpixel.sacredpixeldungeon.journal.Document.CITY_WARLOCK,
            com.sacredpixel.sacredpixeldungeon.journal.Document.HALLS_KING
        };
        for (com.sacredpixel.sacredpixeldungeon.journal.Document doc : loreDocs) {
            for (String page : doc.pageNames()) {
                if (doc.isPageFound(page)) count++;
            }
        }
        return count;
    }

    private static int countLocalGuide() {
        com.sacredpixel.sacredpixeldungeon.journal.Journal.loadGlobal();
        int count = 0;
        for (String page : com.sacredpixel.sacredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.pageNames()) {
            if (com.sacredpixel.sacredpixeldungeon.journal.Document.ADVENTURERS_GUIDE.isPageFound(page)) count++;
        }
        return count;
    }

    private static int countLocalAlchemy() {
        com.sacredpixel.sacredpixeldungeon.journal.Journal.loadGlobal();
        int count = 0;
        for (String page : com.sacredpixel.sacredpixeldungeon.journal.Document.ALCHEMY_GUIDE.pageNames()) {
            if (com.sacredpixel.sacredpixeldungeon.journal.Document.ALCHEMY_GUIDE.isPageFound(page)) count++;
        }
        return count;
    }

    /**
     * Clears only Sacred PD data (localStorage keys with 'spd_' prefix and IndexedDB 'spdDB').
     * Does not affect other websites' data.
     */
    @JSBody(script =
            "console.log('[WebDataManager] Clearing Sacred PD data...');" +
            "try {" +
            "    var keysToDelete = [];" +
            "    for (var i = 0; i < localStorage.length; i++) {" +
            "        var key = localStorage.key(i);" +
            "        if (key && key.indexOf('spd_') === 0) { keysToDelete.push(key); }" +
            "    }" +
            "    keysToDelete.forEach(function(key) { localStorage.removeItem(key); });" +
            "    console.log('[WebDataManager] Cleared ' + keysToDelete.length + ' localStorage keys');" +
            "    var dbName = 'spdDB';" +
            "    var deleteRequest = indexedDB.deleteDatabase(dbName);" +
            "    deleteRequest.onsuccess = function() { console.log('[WebDataManager] IndexedDB deleted'); window.location.reload(); };" +
            "    deleteRequest.onerror = function() { console.log('[WebDataManager] IndexedDB error'); window.location.reload(); };" +
            "    deleteRequest.onblocked = function() { console.log('[WebDataManager] IndexedDB blocked'); window.location.reload(); };" +
            "} catch (e) { console.error('[WebDataManager] Clear error:', e); window.location.reload(); }")
    public static native void clearAllData();
}
