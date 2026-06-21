/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebDataServiceImpl - TeaVM implementation of DataScene.DataService
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.DataScene;

/**
 * TeaVM implementation of DataScene.DataService.
 * Bridges DataScene UI with WebDataManager.
 */
public class WebDataServiceImpl implements DataScene.DataService {

    // Dynamic total calculations (same as DataScene)
    private static int getTotalBadges() {
        return Badges.Badge.values().length;
    }

    private static int getTotalCatalog() {
        int total = 0;
        for (Catalog cat : Catalog.values()) {
            total += cat.items().size();
        }
        return total;
    }

    private static int getTotalBestiary() {
        int total = 0;
        for (Bestiary cat : Bestiary.values()) {
            total += cat.entities().size();
        }
        return total;
    }

    private static int getTotalLore() {
        int total = 0;
        Document[] loreDocs = {Document.SEWERS_GUARD, Document.PRISON_WARDEN,
                Document.CAVES_EXPLORER, Document.CITY_WARLOCK, Document.HALLS_KING};
        for (Document doc : loreDocs) {
            total += doc.pageNames().size();
        }
        return total;
    }

    private static int getTotalGuide() {
        return Document.ADVENTURERS_GUIDE.pageNames().size();
    }

    private static int getTotalAlchemy() {
        return Document.ALCHEMY_GUIDE.pageNames().size();
    }

    @Override
    public String exportData() {
        WebDataManager.ExportResult result = WebDataManager.exportData();
        if (result.success) {
            return result.url;
        }
        return null;
    }

    @Override
    public boolean copyToClipboard(String url) {
        return WebDataManager.copyToClipboard(url);
    }

    @Override
    public boolean hasImportData() {
        return WebDataManager.hasImportData();
    }

    @Override
    public String getImportPreview(String url) {
        WebDataManager.ImportPreview preview = WebDataManager.previewImport(url);
        if (!preview.valid) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        // Use Messages.get for proper localization, same format as DataScene
        // 1. Rankings
        sb.append(Messages.get(DataScene.class, "rankings", preview.rankingCount, preview.highestScore));
        sb.append("\n");

        // 2. Badges
        sb.append(Messages.get(DataScene.class, "badges", preview.badgeCount, getTotalBadges()));
        if (preview.newBadges > 0) {
            sb.append(" (+").append(preview.newBadges).append(")");
        }
        sb.append("\n");

        // 3. Catalog
        sb.append(Messages.get(DataScene.class, "catalog", preview.catalogCount, getTotalCatalog()));
        if (preview.newCatalogItems > 0) {
            sb.append(" (+").append(preview.newCatalogItems).append(")");
        }
        sb.append("\n");

        // 4. Bestiary
        sb.append(Messages.get(DataScene.class, "bestiary", preview.bestiaryCount, getTotalBestiary()));
        if (preview.newBestiaryEntries > 0) {
            sb.append(" (+").append(preview.newBestiaryEntries).append(")");
        }
        sb.append("\n");

        // 5. Lore
        sb.append(Messages.get(DataScene.class, "lore", preview.loreCount, getTotalLore()));
        if (preview.newLorePages > 0) {
            sb.append(" (+").append(preview.newLorePages).append(")");
        }
        sb.append("\n");

        // 6. Guide
        sb.append(Messages.get(DataScene.class, "guide", preview.guideCount, getTotalGuide()));
        if (preview.newGuidePages > 0) {
            sb.append(" (+").append(preview.newGuidePages).append(")");
        }
        sb.append("\n");

        // 7. Alchemy
        sb.append(Messages.get(DataScene.class, "alchemy", preview.alchemyCount, getTotalAlchemy()));
        if (preview.newAlchemyPages > 0) {
            sb.append(" (+").append(preview.newAlchemyPages).append(")");
        }

        return sb.toString();
    }

    @Override
    public boolean applyImport(String url, boolean overwrite) {
        WebDataManager.ImportPreview preview = WebDataManager.previewImport(url);
        if (!preview.valid) {
            return false;
        }

        WebDataMerger.MergePolicy policy = overwrite
                ? WebDataMerger.MergePolicy.OVERWRITE
                : WebDataMerger.MergePolicy.MERGE_UNION;

        WebDataMerger.MergeResult result = WebDataManager.applyImport(preview, policy);
        return result.hasChanges() || overwrite;
    }

    @Override
    public String getUrlFragment() {
        return WebUrlCodec.getUrlFragment();
    }

    @Override
    public void clearUrlFragment() {
        WebUrlCodec.clearDataFragment();
    }

    @Override
    public void reloadPage() {
        WebUrlCodec.reloadPage();
    }

    @Override
    public boolean hasSeedParams() {
        return WebUrlCodec.hasSeedParams();
    }

    @Override
    public String getSeedParam() {
        return WebUrlCodec.getSeedParam();
    }

    @Override
    public String getClassParam() {
        return WebUrlCodec.getClassParam();
    }

    @Override
    public int getChallengesParam() {
        return WebUrlCodec.getChallengesParam();
    }

    @Override
    public void clearSeedParams() {
        WebUrlCodec.clearSeedParams();
    }

    @Override
    public void clearAllBrowserData() {
        WebDataManager.clearAllData();
    }
}
