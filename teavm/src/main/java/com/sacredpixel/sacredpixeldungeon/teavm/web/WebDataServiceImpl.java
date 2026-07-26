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
import com.sacredpixel.sacredpixeldungeon.windows.WndJournal;
import com.sacredpixel.sacredpixeldungeon.windows.WndRanking;

/**
 * TeaVM implementation of DataScene.DataService.
 * Bridges DataScene UI with WebDataManager.
 */
public class WebDataServiceImpl implements DataScene.DataService {

    // Dynamic total calculations (same as DataScene)
    private static int getTotalBadges() {
        return Badges.Badge.values().length;
    }

    private static int getTotalEquipment() {
        int total = 0;
        for (Catalog cat : Catalog.equipmentCatalogs) {
            total += cat.items().size();
        }
        return total;
    }

    private static int getTotalConsumables() {
        int total = 0;
        for (Catalog cat : Catalog.consumableCatalogs) {
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
        // Includes INTROS + region lore documents
        Document[] loreDocs = {Document.INTROS, Document.SEWERS_GUARD, Document.PRISON_WARDEN,
                Document.CAVES_EXPLORER, Document.CITY_WARLOCK, Document.HALLS_KING};
        for (Document doc : loreDocs) {
            total += doc.pageNames().size();
        }
        return total;
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

        // Use journal key references for localization consistency
        // Use \n\n as delimiter between stats for WndImportPreview to split and add GAP

        // 1. Best score (references DataScene.best_score key)
        sb.append(Messages.get(DataScene.class, "best_score", preview.highestScore));
        sb.append("\n\n");

        // 2. Badges (references WndJournal.BadgesTab key)
        String badgesTitle = Messages.get(WndJournal.BadgesTab.class, "title");
        sb.append(badgesTitle).append(": ").append(preview.badgeCount).append("/").append(getTotalBadges());
        if (preview.newBadges > 0) {
            sb.append(" (+").append(preview.newBadges).append(")");
        }
        sb.append("\n\n");

        // 3. Equipment (references WndJournal.CatalogTab key)
        String equipTitle = Messages.get(WndJournal.CatalogTab.class, "title_equipment");
        sb.append(equipTitle).append(": ").append(preview.equipmentCount).append("/").append(getTotalEquipment());
        if (preview.newEquipmentItems > 0) {
            sb.append(" (+").append(preview.newEquipmentItems).append(")");
        }
        sb.append("\n\n");

        // 4. Consumables (references WndJournal.CatalogTab key)
        String consumTitle = Messages.get(WndJournal.CatalogTab.class, "title_consumables");
        sb.append(consumTitle).append(": ").append(preview.consumablesCount).append("/").append(getTotalConsumables());
        if (preview.newConsumablesItems > 0) {
            sb.append(" (+").append(preview.newConsumablesItems).append(")");
        }
        sb.append("\n\n");

        // 5. Bestiary (references WndJournal.CatalogTab key)
        String bestiaryTitle = Messages.get(WndJournal.CatalogTab.class, "title_bestiary");
        sb.append(bestiaryTitle).append(": ").append(preview.bestiaryCount).append("/").append(getTotalBestiary());
        if (preview.newBestiaryEntries > 0) {
            sb.append(" (+").append(preview.newBestiaryEntries).append(")");
        }
        sb.append("\n\n");

        // 6. Lore (references WndJournal.CatalogTab key, includes INTROS + region lore)
        String loreTitle = Messages.get(WndJournal.CatalogTab.class, "title_lore");
        sb.append(loreTitle).append(": ").append(preview.loreCount).append("/").append(getTotalLore());
        if (preview.newLorePages > 0) {
            sb.append(" (+").append(preview.newLorePages).append(")");
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
