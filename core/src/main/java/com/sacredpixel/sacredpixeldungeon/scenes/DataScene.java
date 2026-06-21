/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
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

package com.sacredpixel.sacredpixeldungeon.scenes;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.journal.Bestiary;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.journal.Journal;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.sacredpixel.sacredpixeldungeon.windows.WndMessage;
import com.sacredpixel.sacredpixeldungeon.windows.WndOptions;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

/**
 * Data synchronization scene.
 * Allows exporting/importing game data via URL for cross-device sync.
 */
public class DataScene extends PixelScene {

    private static final int WIDTH_P = 149;
    private static final int WIDTH_L = 251;
    private static final int GAP = 2;
    private static final int BTN_HEIGHT = 16;

    private RedButton exportBtn;
    private RedButton deleteBtn;

    // Platform-specific data service (set by teavm module)
    private static DataService dataService;

    // Keyboard navigation
    private java.util.ArrayList<RedButton> focusableButtons = new java.util.ArrayList<>();
    private int focusIndex = -1;
    private Signal.Listener<KeyEvent> keyListener;

    // Flag to track if import dialog has been shown (prevents re-showing after cancel)
    private static boolean importDialogShown = false;

    /**
     * Interface for platform-specific data operations.
     * TeaVM module provides the implementation.
     */
    public interface DataService {
        /** Exports all data and returns share URL */
        String exportData();

        /** Copies URL to clipboard */
        boolean copyToClipboard(String url);

        /** Checks if URL import data is present */
        boolean hasImportData();

        /** Gets import preview info */
        String getImportPreview(String url);

        /** Applies import with merge policy */
        boolean applyImport(String url, boolean overwrite);

        /** Gets current URL fragment */
        String getUrlFragment();

        /** Clears URL fragment */
        void clearUrlFragment();

        /** Reloads page to refresh game data after import (web only) */
        default void reloadPage() {
            // No-op for non-web platforms
        }

        /** Checks if seed URL parameters are present */
        default boolean hasSeedParams() {
            return false;
        }

        /** Gets seed code from URL (e.g., "ABC-DEF-GHI") */
        default String getSeedParam() {
            return null;
        }

        /** Gets hero class from URL (e.g., "warrior") */
        default String getClassParam() {
            return null;
        }

        /** Gets challenges from URL */
        default int getChallengesParam() {
            return 0;
        }

        /** Clears seed parameters from URL */
        default void clearSeedParams() {
            // No-op for non-web platforms
        }

        /** Clears all browser data (localStorage, IndexedDB, etc.) */
        default void clearAllBrowserData() {
            // No-op for non-web platforms
        }
    }

    /**
     * Sets the platform-specific data service.
     * Called by TeaVM launcher.
     */
    public static void setDataService(DataService service) {
        dataService = service;
    }

    /**
     * Gets the current data service.
     * Used by TitleScene to check for URL imports.
     */
    public static DataService getDataService() {
        return dataService;
    }

    @Override
    public void create() {
        super.create();

        // Load global data
        Badges.loadGlobal();
        Journal.loadGlobal();
        Rankings.INSTANCE.load();

        Music.INSTANCE.playTracks(
                new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
                new float[]{1, 1},
                false);

        uiCamera.visible = false;

        int w = Camera.main.width;
        int h = Camera.main.height;

        RectF insets = getCommonInsets();

        ColorBlock BG = new ColorBlock(w, h, 0xFF000000);
        add(BG);

        w -= insets.left + insets.right;
        h -= insets.top + insets.bottom;

        float top = 20;

        // Title
        IconTitle title = new IconTitle(Icons.CHANGES.get(), Messages.get(this, "title"));
        title.setSize(200, 0);
        title.setPos(
                insets.left + (w - title.reqWidth()) / 2f,
                insets.top + (top - title.height()) / 2f
        );
        align(title);
        add(title);

        // Exit button
        ExitButton btnExit = new ExitButton();
        btnExit.setPos(insets.left + w - btnExit.width(), insets.top);
        add(btnExit);

        // Content panel
        NinePatch panel = Chrome.get(Chrome.Type.TOAST);

        int pw = (landscape() ? WIDTH_L : WIDTH_P) + panel.marginHor();
        int ph = h - 50 + panel.marginVer();

        panel.size(pw, ph);
        panel.x = insets.left + (w - pw) / 2f;
        panel.y = insets.top + top;
        add(panel);

        float panelInnerX = panel.x + panel.marginLeft();
        float panelInnerY = panel.y + panel.marginTop();
        float panelInnerW = panel.width() - panel.marginHor();
        float panelInnerH = panel.height() - panel.marginVer();

        float yPos = panelInnerY + 5;

        // Summary info - individual stat slots with GAP spacing (2pt between items)
        yPos = createSummaryStats(panelInnerX + 5, yPos, (int) panelInnerW - 10);

        yPos += 4; // 4pt gap before export button

        // Export button (full width, red)
        exportBtn = new RedButton(Messages.get(this, "export")) {
            @Override
            protected void onClick() {
                doExport();
            }
        };
        exportBtn.setRect(panelInnerX + 5, yPos, panelInnerW - 10, BTN_HEIGHT);
        add(exportBtn);

        yPos += 18; // 16 (button height) + 2pt gap

        // Delete all data button
        deleteBtn = new RedButton(Messages.get(this, "delete_all")) {
            @Override
            protected void onClick() {
                showDeleteConfirmDialog();
            }
        };
        deleteBtn.setRect(panelInnerX + 5, yPos, panelInnerW - 10, BTN_HEIGHT);
        add(deleteBtn);

        // Check for auto-import from URL (only show once per session)
        if (dataService != null && dataService.hasImportData() && !importDialogShown) {
            importDialogShown = true;
            Game.runOnRenderThread(() -> {
                showAutoImportDialog();
            });
        }

        // Setup keyboard navigation
        focusableButtons.clear();
        focusableButtons.add(exportBtn);
        focusableButtons.add(deleteBtn);

        keyListener = new Signal.Listener<KeyEvent>() {
            @Override
            public boolean onSignal(KeyEvent event) {
                if (!event.pressed) return false;

                // Don't handle keys if not on DataScene
                if (!(Game.scene() instanceof DataScene)) return false;

                // Don't handle keys if a window is open
                for (Object v : members) {
                    if (v instanceof Window) return false;
                }

                GameAction action = KeyBindings.getActionForKey(event);

                if (action == SPDAction.N || action == SPDAction.NW
                        || action == SPDAction.W || action == SPDAction.SW) {
                    moveFocus(-1);
                    return true;
                } else if (action == SPDAction.S || action == SPDAction.SE
                        || action == SPDAction.E || action == SPDAction.NE) {
                    moveFocus(1);
                    return true;
                } else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
                        || event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
                    if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
                        RedButton btn = focusableButtons.get(focusIndex);
                        if (btn.active) {
                            com.watabou.noosa.audio.Sample.INSTANCE.play(Assets.Sounds.CLICK);
                            btn.click();
                        }
                    }
                    return true;
                }
                return false;
            }
        };
        KeyEvent.addKeyListener(keyListener);

        fadeIn();
    }

    private void moveFocus(int direction) {
        if (focusableButtons.isEmpty()) return;

        // Clear focus on previously focused button
        if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
            focusableButtons.get(focusIndex).setFocused(false);
        }

        // Move index
        if (focusIndex == -1) {
            focusIndex = direction > 0 ? 0 : focusableButtons.size() - 1;
        } else {
            focusIndex += direction;
            if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
            if (focusIndex >= focusableButtons.size()) focusIndex = 0;
        }

        // Set focus on new button
        focusableButtons.get(focusIndex).setFocused(true);
    }

    @Override
    public void destroy() {
        if (keyListener != null) {
            KeyEvent.removeKeyListener(keyListener);
            keyListener = null;
        }
        super.destroy();
    }

    private float createSummaryStats(float x, float startY, int maxWidth) {
        float pos = startY;

        // 1. Rankings count
        int rankCount = Rankings.INSTANCE.records.size();
        int highScore = 0;
        for (Rankings.Record r : Rankings.INSTANCE.records) {
            if (r.score > highScore) highScore = r.score;
        }
        pos = statSlot(x, pos, maxWidth, Messages.get(this, "rankings", rankCount, highScore));

        // 2. Badge count
        int badgeCount = 0;
        int totalBadges = Badges.Badge.values().length;
        for (Badges.Badge b : Badges.Badge.values()) {
            if (Badges.isUnlocked(b)) badgeCount++;
        }
        pos = statSlot(x, pos, maxWidth, Messages.get(this, "badges", badgeCount, totalBadges));

        // 3. Catalog count
        int catalogCount = 0;
        int totalCatalog = 0;
        for (Catalog cat : Catalog.values()) {
            catalogCount += cat.totalSeen();
            totalCatalog += cat.items().size();
        }
        pos = statSlot(x, pos, maxWidth, Messages.get(this, "catalog", catalogCount, totalCatalog));

        // 4. Bestiary count
        int bestiaryCount = 0;
        int totalBestiary = 0;
        for (Bestiary cat : Bestiary.values()) {
            bestiaryCount += cat.totalSeen();
            totalBestiary += cat.entities().size();
        }
        pos = statSlot(x, pos, maxWidth, Messages.get(this, "bestiary", bestiaryCount, totalBestiary));

        // 5. Lore documents count
        int loreFound = 0;
        int loreTotal = 0;
        Document[] loreDocs = {Document.SEWERS_GUARD, Document.PRISON_WARDEN,
                               Document.CAVES_EXPLORER, Document.CITY_WARLOCK, Document.HALLS_KING};
        for (Document doc : loreDocs) {
            loreTotal += doc.pageNames().size();
            for (String page : doc.pageNames()) {
                if (doc.isPageFound(page)) loreFound++;
            }
        }
        pos = statSlot(x, pos, maxWidth, Messages.get(this, "lore", loreFound, loreTotal));

        // 6. Guide pages count
        int guideFound = 0;
        int guideTotal = Document.ADVENTURERS_GUIDE.pageNames().size();
        for (String page : Document.ADVENTURERS_GUIDE.pageNames()) {
            if (Document.ADVENTURERS_GUIDE.isPageFound(page)) guideFound++;
        }
        pos = statSlot(x, pos, maxWidth, Messages.get(this, "guide", guideFound, guideTotal));

        // 7. Alchemy pages count
        int alchemyFound = 0;
        int alchemyTotal = Document.ALCHEMY_GUIDE.pageNames().size();
        for (String page : Document.ALCHEMY_GUIDE.pageNames()) {
            if (Document.ALCHEMY_GUIDE.isPageFound(page)) alchemyFound++;
        }
        pos = statSlot(x, pos, maxWidth, Messages.get(this, "alchemy", alchemyFound, alchemyTotal));

        return pos;
    }

    private float statSlot(float x, float pos, int maxWidth, String text) {
        RenderedTextBlock txt = PixelScene.renderTextBlock(text, 7);
        txt.maxWidth(maxWidth);
        txt.setPos(x, pos);
        add(txt);
        return pos + GAP + txt.height();
    }

    private void doExport() {
        if (dataService == null) {
            showMessage(Messages.get(this, "not_supported"));
            return;
        }

        String url = dataService.exportData();
        if (url == null) {
            showMessage(Messages.get(this, "export_failed"));
            return;
        }

        // Copy to clipboard
        boolean copied = dataService.copyToClipboard(url);

        if (copied) {
            // Show export success with yellow import hint
            showExportSuccessMessage();
        } else {
            showMessage(Messages.get(this, "export_clipboard_failed"));
        }
    }

    private void showDeleteConfirmDialog() {
        Game.scene().addToFront(new WndOptions(
                Icons.WARNING.get(),
                Messages.get(this, "delete_title"),
                Messages.get(this, "delete_warn"),
                Messages.get(this, "delete_yes"),
                Messages.get(this, "delete_no")
        ) {
            @Override
            protected void onSelect(int index) {
                if (index == 0) {
                    // Show second confirmation
                    Game.scene().addToFront(new WndOptions(
                            Icons.WARNING.get(),
                            Messages.get(DataScene.class, "delete_title"),
                            Messages.get(DataScene.class, "delete_final_warn"),
                            Messages.get(DataScene.class, "delete_final_yes"),
                            Messages.get(DataScene.class, "delete_no")
                    ) {
                        @Override
                        protected void onSelect(int index) {
                            if (index == 0) {
                                doDeleteAllData();
                            }
                        }
                    });
                }
            }
        });
    }

    private void doDeleteAllData() {
        if (dataService == null) {
            showMessage(Messages.get(this, "not_supported"));
            return;
        }

        dataService.clearAllBrowserData();
        // Page will reload after clearing data
    }

    private void processImport(String url) {
        if (dataService == null) return;

        String preview = dataService.getImportPreview(url);
        if (preview == null) {
            showMessage(Messages.get(this, "import_invalid"));
            return;
        }

        // Show preview with merge options
        Game.scene().addToFront(new WndImportPreview(preview, url));
    }

    private void showAutoImportDialog() {
        if (dataService == null) return;

        try {
            String fragment = dataService.getUrlFragment();
            if (fragment == null || fragment.isEmpty()) return;

            // Support both old (#d=CODE) and new (#CODE) formats
            if (!fragment.startsWith("#")) return;
            // Need at least some data after the hash
            if (fragment.length() < 10) return;

            String preview = dataService.getImportPreview(fragment);
            if (preview == null) {
                dataService.clearUrlFragment();
                showMessage(Messages.get(this, "import_invalid"));
                return;
            }

            Game.scene().addToFront(new WndImportPreview(preview, fragment));
        } catch (Exception e) {
            // Catch any errors during import preview to prevent black screen
            System.err.println("[DataScene] Import preview error: " + e.getMessage());
            e.printStackTrace();
            if (dataService != null) {
                dataService.clearUrlFragment();
            }
            showMessage(Messages.get(this, "import_invalid"));
        }
    }

    private void showMessage(String msg) {
        Game.scene().addToFront(new WndMessage(msg));
    }

    private void showExportSuccessMessage() {
        // Custom window with white success message and yellow import hint
        Game.scene().addToFront(new Window() {
            {
                int width = 149;
                int margin = 4;

                String successText = Messages.get(DataScene.class, "export_success");
                String hintText = Messages.get(DataScene.class, "import_hint");

                RenderedTextBlock successInfo = PixelScene.renderTextBlock(successText, 6);
                successInfo.maxWidth(width - margin * 2);
                successInfo.setPos(margin, margin);
                add(successInfo);

                RenderedTextBlock hintInfo = PixelScene.renderTextBlock(hintText, 6);
                hintInfo.maxWidth(width - margin * 2);
                hintInfo.hardlight(0xFFFF44); // Yellow color
                hintInfo.setPos(margin, successInfo.bottom() + 6);
                add(hintInfo);

                resize(
                    (int)Math.max(successInfo.width(), hintInfo.width()) + margin * 2,
                    (int)hintInfo.bottom() + margin);
            }
        });
    }

    @Override
    protected void onBackPressed() {
        SacredPixelDungeon.switchNoFade(TitleScene.class);
    }

    /**
     * Import preview window with merge options.
     */
    private class WndImportPreview extends Window {

        private java.util.ArrayList<RedButton> focusableButtons = new java.util.ArrayList<>();
        private int focusIndex = -1;
        private Signal.Listener<KeyEvent> keyListener;

        public WndImportPreview(String preview, String url) {
            super();

            int width = Math.min(149, (int)(PixelScene.uiCamera.width * 0.9f));

            // Title with data sync icon
            IconTitle titlebar = new IconTitle(Icons.CHANGES.get(),
                    Messages.get(DataScene.class, "import_preview_title"));
            titlebar.setRect(0, 0, width, 0);
            add(titlebar);

            // Preview text - render each line separately with 2pt spacing
            float previewY = titlebar.bottom() + 4;
            String[] lines = preview.split("\n");
            for (String line : lines) {
                if (!line.isEmpty()) {
                    RenderedTextBlock txt = PixelScene.renderTextBlock(line, 7);
                    txt.maxWidth(width - 10);
                    txt.setPos(0, previewY);
                    add(txt);
                    previewY += 2 + txt.height(); // 2pt gap between preview lines
                }
            }

            float btnY = previewY + 4; // 4pt gap before buttons
            float BTN_GAP = 2;
            float MARGIN = 5;
            float btnWidth = width - MARGIN * 2;
            float btnHeight = 16;

            // Merge button (recommended)
            RedButton mergeBtn = new RedButton(Messages.get(DataScene.class, "merge")) {
                @Override
                protected void onClick() {
                    if (dataService != null) {
                        dataService.applyImport(url, false);
                        dataService.clearUrlFragment();
                        // Reload page to refresh all game data
                        dataService.reloadPage();
                    }
                    hide();
                    showMessage(Messages.get(DataScene.class, "import_success"));
                }
            };
            mergeBtn.setRect(MARGIN, btnY, btnWidth, btnHeight);
            add(mergeBtn);
            focusableButtons.add(mergeBtn);
            btnY += btnHeight + BTN_GAP;

            // Overwrite button
            RedButton overwriteBtn = new RedButton(Messages.get(DataScene.class, "overwrite")) {
                @Override
                protected void onClick() {
                    if (dataService != null) {
                        dataService.applyImport(url, true);
                        dataService.clearUrlFragment();
                        // Reload page to refresh all game data
                        dataService.reloadPage();
                    }
                    hide();
                    showMessage(Messages.get(DataScene.class, "import_success"));
                }
            };
            overwriteBtn.setRect(MARGIN, btnY, btnWidth, btnHeight);
            add(overwriteBtn);
            focusableButtons.add(overwriteBtn);
            btnY += btnHeight + BTN_GAP;

            // Cancel button
            RedButton cancelBtn = new RedButton(Messages.get(DataScene.class, "cancel")) {
                @Override
                protected void onClick() {
                    if (dataService != null) {
                        dataService.clearUrlFragment();
                    }
                    hide();
                }
            };
            cancelBtn.setRect(MARGIN, btnY, btnWidth, btnHeight);
            add(cancelBtn);
            focusableButtons.add(cancelBtn);

            resize(width, (int)(btnY + btnHeight + MARGIN));

            // Setup keyboard navigation
            keyListener = new Signal.Listener<KeyEvent>() {
                @Override
                public boolean onSignal(KeyEvent event) {
                    if (!event.pressed) return false;

                    GameAction action = KeyBindings.getActionForKey(event);

                    if (action == SPDAction.N || action == SPDAction.NW
                            || action == SPDAction.W || action == SPDAction.SW) {
                        moveFocus(-1);
                        return true;
                    } else if (action == SPDAction.S || action == SPDAction.SE
                            || action == SPDAction.E || action == SPDAction.NE) {
                        moveFocus(1);
                        return true;
                    } else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
                            || event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
                        if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
                            RedButton btn = focusableButtons.get(focusIndex);
                            if (btn.active) {
                                Sample.INSTANCE.play(Assets.Sounds.CLICK);
                                btn.click();
                            }
                        }
                        return true;
                    }
                    return false;
                }
            };
            KeyEvent.addKeyListener(keyListener);
        }

        @Override
        public void onBackPressed() {
            // Clear URL fragment when closing via outside click or ESC key
            if (dataService != null) {
                dataService.clearUrlFragment();
            }
            super.onBackPressed();
        }

        protected void moveFocus(int direction) {
            if (focusableButtons.isEmpty()) return;

            // Clear previous focus
            if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
                focusableButtons.get(focusIndex).setFocused(false);
            }

            // Move index
            if (focusIndex == -1) {
                focusIndex = direction > 0 ? 0 : focusableButtons.size() - 1;
            } else {
                focusIndex += direction;
                if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
                if (focusIndex >= focusableButtons.size()) focusIndex = 0;
            }

            // Set focus on new button
            focusableButtons.get(focusIndex).setFocused(true);
        }

        @Override
        public void destroy() {
            if (keyListener != null) {
                KeyEvent.removeKeyListener(keyListener);
                keyListener = null;
            }
            super.destroy();
        }
    }
}
