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

package com.sacredpixel.sacredpixeldungeon.teavm;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.sacredpixel.sacredpixeldungeon.CloudSave;
import com.sacredpixel.sacredpixeldungeon.InterstitialAd;
import com.sacredpixel.sacredpixeldungeon.Leaderboard;
import com.sacredpixel.sacredpixeldungeon.Promotion;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.services.news.News;
import com.sacredpixel.sacredpixeldungeon.services.news.NewsImpl;
import com.sacredpixel.sacredpixeldungeon.services.updates.UpdateImpl;
import com.sacredpixel.sacredpixeldungeon.services.updates.Updates;
import com.sacredpixel.sacredpixeldungeon.scenes.DataScene;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.teavm.web.WebDataServiceImpl;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.FileUtils;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

public class TeaVMLauncher {

    public static void main(String[] args) {
        try {
            log("TeaVMLauncher: starting");

            Bundle.compressionSupported = false;
            log("TeaVMLauncher: compression disabled");

            // Game.version must be in x.x.x format for RankingsScene version display
            Game.version = "4.1.4";
            Game.versionCode = 912;
            log("TeaVMLauncher: version set");

            if (UpdateImpl.supportsUpdates()) {
                Updates.service = UpdateImpl.getUpdateService();
            }
            // News service disabled - no longer fetching from ShatteredPixel.com
            // if (NewsImpl.supportsNews()) {
            //     News.service = NewsImpl.getNewsService();
            // }
            log("TeaVMLauncher: services configured");

            FileUtils.setDefaultFileProperties(Files.FileType.Internal, "");
            log("TeaVMLauncher: file utils configured");

            // Set JVM default locale from browser language for correct initial language detection
            String browserLang = getBrowserLanguage();
            log("TeaVMLauncher: browser language = " + browserLang);
            if (browserLang != null && !browserLang.isEmpty()) {
                String[] parts = browserLang.split("-");
                if (parts.length >= 2) {
                    java.util.Locale.setDefault(new java.util.Locale(parts[0], parts[1]));
                } else {
                    java.util.Locale.setDefault(new java.util.Locale(parts[0]));
                }
            }

            // Set up web data service for DataScene
            DataScene.setDataService(new WebDataServiceImpl());
            log("TeaVMLauncher: web data service configured");

            // Set up Appsintoss leaderboard implementation if running in Appsintoss
            if (TeaVMLeaderboard.isAppsintoss()) {
                Leaderboard.impl = new Leaderboard.LeaderboardImpl() {
                    @Override
                    public void openLeaderboard() {
                        TeaVMLeaderboard.openLeaderboard();
                    }

                    @Override
                    public void submitScore(int score) {
                        TeaVMLeaderboard.submitScore(score);
                    }
                };
                log("TeaVMLauncher: Appsintoss leaderboard configured");

                // Set up Appsintoss cloud save implementation
                if (TeaVMCloudSave.isAvailable()) {
                    CloudSave.impl = new CloudSave.CloudSaveImpl() {
                        @Override
                        public void load(CloudSave.CloudSaveCallback callback) {
                            TeaVMCloudSave.load(new TeaVMCloudSave.CloudSaveCallback() {
                                @Override
                                public void onComplete(boolean success, String data) {
                                    callback.onComplete(success, data);
                                }
                            });
                        }

                        @Override
                        public void save(String jsonData, CloudSave.CloudSaveCallback callback) {
                            TeaVMCloudSave.save(jsonData, new TeaVMCloudSave.CloudSaveCallback() {
                                @Override
                                public void onComplete(boolean success, String data) {
                                    callback.onComplete(success, data);
                                }
                            });
                        }

                        @Override
                        public boolean isAvailable() {
                            return TeaVMCloudSave.isAvailable();
                        }
                    };
                    log("TeaVMLauncher: Appsintoss cloud save configured");
                }

                // Set up Appsintoss interstitial ad implementation
                // 초기화 시점 체크 제거 - 항상 impl 설정하고 show() 내에서 동적 체크
                InterstitialAd.impl = new InterstitialAd.InterstitialAdImpl() {
                    private Runnable pendingOnComplete = null;

                    @Override
                    public void show(Runnable onComplete) {
                        // 동적으로 isAvailable() 체크 (초기화 타이밍 이슈 회피)
                        if (TeaVMInterstitialAd.isAvailable()) {
                            // Store callback for polling-based invocation
                            pendingOnComplete = onComplete;
                            TeaVMInterstitialAd.show(new TeaVMInterstitialAd.AdCallback() {
                                @Override
                                public void onComplete() {
                                    // This callback might not be invoked reliably from JS
                                    // checkCallback() polling handles it instead
                                }
                            });
                        } else {
                            // 광고 API 없음 - 즉시 콜백 호출
                            log("TeaVMLauncher: Interstitial ad not available, skipping");
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        }
                    }

                    @Override
                    public boolean isAvailable() {
                        return TeaVMInterstitialAd.isAvailable();
                    }

                    @Override
                    public void preload() {
                        if (TeaVMInterstitialAd.isAvailable()) {
                            TeaVMInterstitialAd.preload();
                        }
                    }

                    @Override
                    public boolean isPreloaded() {
                        return TeaVMInterstitialAd.isPreloaded();
                    }

                    @Override
                    public void checkCallback() {
                        // Poll for ad completion flag and invoke our callback directly
                        // DO NOT call TeaVMInterstitialAd.checkAndInvokeCallback() - it clears the flag
                        // before we can check it, causing our callback to never execute
                        if (pendingOnComplete != null && TeaVMInterstitialAd.isAdComplete()) {
                            Runnable cb = pendingOnComplete;
                            pendingOnComplete = null;
                            TeaVMInterstitialAd.clearComplete();
                            cb.run();
                        }
                    }

                    @Override
                    public void block() {
                        // Clear pending callback to prevent late-loaded ads from triggering
                        pendingOnComplete = null;
                        TeaVMInterstitialAd.clearComplete();
                    }

                    @Override
                    public double getAdCompletedTimestamp() {
                        return TeaVMInterstitialAd.getAdCompletedTimestamp();
                    }

                    @Override
                    public void clearAdCompletedTimestamp() {
                        TeaVMInterstitialAd.clearAdCompletedTimestamp();
                    }
                };
                log("TeaVMLauncher: Appsintoss interstitial ad configured");

                // Set up Appsintoss promotion implementation
                Promotion.impl = new Promotion.PromotionImpl() {
                    @Override
                    public void grantReward(String promotionCode, int amount, Promotion.RewardCallback callback) {
                        boolean result = TeaVMPromotion.grantReward(promotionCode, amount);
                        if (callback != null) {
                            if (result) {
                                callback.onResult(true, "Reward granted");
                            } else {
                                callback.onResult(false, "Promotion API not available");
                            }
                        }
                    }

                    @Override
                    public boolean isAvailable() {
                        return TeaVMPromotion.isAvailable();
                    }
                };
                log("TeaVMLauncher: Appsintoss promotion configured");
            }

            final SacredPixelDungeon game = new SacredPixelDungeon(new TeaVMPlatformSupport());
            log("TeaVMLauncher: game instance created");

            //Register font atlas invalidation hook for GL context loss recovery.
            //Font atlas textures (CanvasFontData) are not in TextureCache, so
            //TextureCache.reload() does not cover them. This callback invalidates
            //all font atlas GL textures and forces a full canvas re-render + re-upload.
            Game.onGLContextRestore = new Callback() {
                @Override
                public void call() {
                    log("TeaVMLauncher: GL context restored, invalidating font atlases");
                    FreeTypeFontGenerator.invalidateAllFontAtlases();
                }
            };

            // Wrap in error-recovering listener
            ApplicationListener wrappedGame = new ApplicationListener() {
                private int consecutiveErrors = 0;
                private boolean recovering = false;

                @Override
                public void create() {
                    try {
                        log("TeaVMLauncher: Game.create() starting");
                        game.create();
                        log("TeaVMLauncher: Game.create() completed");
                    } catch (Throwable t) {
                        log("TeaVMLauncher: Game.create() FAILED: " + t);
                        showError("create() failed: " + t);
                        throw new RuntimeException(t);
                    }
                }

                @Override
                public void render() {
                    try {
                        if (recovering) {
                            recovering = false;
                            //Force full GPU state reload on recovery.
                            //This covers cases where render() threw an Error (not Exception)
                            //that Game.render()'s own catch didn't handle, or where the
                            //error happened before Game's context-check code ran.
                            Game.glReloadPending = true;
                            Game.webglContextLost = true;
                            game.resume();
                        }
                        game.render();
                        consecutiveErrors = 0;
                    } catch (Throwable t) {
                        consecutiveErrors++;
                        String chain = getErrorChain(t);
                        log("TeaVMLauncher: render() error #" + consecutiveErrors + ": " + chain);
                        if (consecutiveErrors <= 10) {
                            recovering = true;
                            log("TeaVMLauncher: attempting recovery...");
                        } else {
                            showError("render() failed: " + chain);
                            throw new RuntimeException(t);
                        }
                    }
                }

                @Override
                public void resize(int w, int h) {
                    try {
                        game.resize(w, h);
                    } catch (Throwable t) {
                        log("TeaVMLauncher: resize() error: " + t);
                        recovering = true;
                    }
                }

                @Override
                public void pause() {
                    try {
                        game.pause();
                    } catch (Throwable t) {
                        log("TeaVMLauncher: pause() error (save may have failed): " + t);
                    }
                }

                @Override
                public void resume() {
                    // Check if WebGL context was actually lost (set by JS event handler)
                    if (jsCheckContextLost()) {
                        Game.webglContextLost = true;
                    }
                    game.resume();
                }

                @Override
                public void dispose() { game.dispose(); }
            };

            WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
            config.width = 0;   // 0 = use all available space
            config.height = 0;
            config.antialiasing = false;
            config.stencil = false;
            config.alpha = false;
            //premultipliedAlpha and preserveDrawingBuffer are set to false by default
            //in WebGL context creation, which is optimal for performance.

            new WebApplication(wrappedGame, config);

            // Register JS-callable save function for lifecycle events (visibilitychange, beforeunload, auto-save)
            registerSaveHook(wrappedGame);

            // Register JS-callable music pause/resume for tab visibility changes
            registerMusicHooks();

        } catch (Throwable t) {
            log("TeaVMLauncher FATAL: " + t);
            showError("TeaVMLauncher failed: " + t);
            throw new RuntimeException(t);
        }
    }

    static String getErrorChain(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable cur = t;
        int depth = 0;
        while (cur != null && depth < 10) {
            if (depth > 0) sb.append(" <- Caused by: ");
            sb.append(cur.getClass().getName()).append(": ").append(cur.getMessage());
            cur = cur.getCause();
            depth++;
        }
        return sb.toString();
    }

    @JSFunctor
    public interface JsSaveCallback extends JSObject {
        void onSave();
    }

    private static final Object saveLock = new Object();
    private static boolean saveInProgress = false;

    private static void registerSaveHook(final ApplicationListener listener) {
        JsSaveCallback callback = new JsSaveCallback() {
            @Override
            public void onSave() {
                synchronized (saveLock) {
                    // Prevent re-entrant saves (e.g. visibilitychange + pagehide firing together)
                    if (saveInProgress) {
                        log("TeaVMLauncher: save hook skipped (already in progress)");
                        return;
                    }
                    try {
                        // Only save if we're actually in the game scene
                        if (!(Game.scene() instanceof GameScene)) {
                            log("TeaVMLauncher: save hook skipped (not in GameScene)");
                            return;
                        }
                        saveInProgress = true;
                        long startMs = System.currentTimeMillis();
                        log("TeaVMLauncher: save hook triggered");
                        listener.pause();
                        long elapsed = System.currentTimeMillis() - startMs;
                        log("TeaVMLauncher: save hook completed in " + elapsed + "ms");
                    } catch (Throwable t) {
                        log("TeaVMLauncher: save hook error: " + t);
                    } finally {
                        saveInProgress = false;
                    }
                }
            }
        };
        installJsSaveHook(callback);
    }

    @JSBody(params = {"callback"}, script =
        "window._spdPauseForSave = function() { callback(); };")
    private static native void installJsSaveHook(JsSaveCallback callback);

    // Track if music was paused due to tab visibility change
    private static boolean musicPausedByVisibility = false;

    @JSFunctor
    public interface JsVoidCallback extends JSObject {
        void call();
    }

    private static void registerMusicHooks() {
        JsVoidCallback pauseCallback = new JsVoidCallback() {
            @Override
            public void call() {
                try {
                    if (Music.INSTANCE.isPlaying()) {
                        musicPausedByVisibility = true;
                        Music.INSTANCE.pause();
                        log("TeaVMLauncher: music paused (tab hidden)");
                    }
                } catch (Throwable t) {
                    log("TeaVMLauncher: music pause error: " + t);
                }
            }
        };

        JsVoidCallback resumeCallback = new JsVoidCallback() {
            @Override
            public void call() {
                try {
                    if (musicPausedByVisibility && Music.INSTANCE.paused()) {
                        musicPausedByVisibility = false;
                        Music.INSTANCE.resume();
                        log("TeaVMLauncher: music resumed (tab visible)");
                    }
                } catch (Throwable t) {
                    log("TeaVMLauncher: music resume error: " + t);
                }
            }
        };

        JsVoidCallback triggerMusicCallback = new JsVoidCallback() {
            @Override
            public void call() {
                try {
                    // Audio was just unlocked by user gesture.
                    // If music is enabled but not actually playing, restart the current tracks.
                    // We call restartCurrentTracks() which re-issues the play command
                    // now that the AudioContext is resumed.
                    if (Music.INSTANCE.isEnabled() && !Music.INSTANCE.isPlaying()) {
                        Music.INSTANCE.restartCurrentTracks();
                        log("TeaVMLauncher: music restarted after audio unlock");
                    }
                } catch (Throwable t) {
                    log("TeaVMLauncher: music trigger error: " + t);
                }
            }
        };

        installJsMusicHooks(pauseCallback, resumeCallback, triggerMusicCallback);
    }

    @JSBody(params = {"pauseCallback", "resumeCallback", "triggerCallback"}, script =
        "window._spdPauseMusic = function() { pauseCallback(); };" +
        "window._spdResumeMusic = function() { resumeCallback(); };" +
        "window._spdTriggerMusicAfterUnlock = function() { triggerCallback(); };")
    private static native void installJsMusicHooks(JsVoidCallback pauseCallback, JsVoidCallback resumeCallback, JsVoidCallback triggerCallback);

    @JSBody(script = "return navigator.language || navigator.userLanguage || 'en';")
    static native String getBrowserLanguage();

    // Debug logging disabled for production
    @JSBody(params = {"msg"}, script = "")
    static native void log(String msg);

    @JSBody(params = {"msg"}, script =
        "var div = document.getElementById('loading');" +
        "if (!div) {" +
        "  div = document.createElement('div');" +
        "  div.id = 'loading';" +
        "  div.style.cssText = 'position:fixed;top:0;left:0;width:100%;z-index:9999;background:black;padding:10px;';" +
        "  document.body.appendChild(div);" +
        "}" +
        "div.style.display = 'block';" +
        "var p = document.createElement('p');" +
        "p.style.cssText = 'color:red;font-size:11px;text-align:left;word-break:break-all;font-family:monospace;';" +
        "p.textContent = msg;" +
        "div.appendChild(p);")
    static native void showError(String msg);

    @JSBody(script =
        "var lost = window._spdContextLost || false;" +
        "window._spdContextLost = false;" +
        "return lost;")
    static native boolean jsCheckContextLost();

    @JSBody(script = "return !!window._spdAudioUnlocked;")
    static native boolean isAudioUnlocked();

    @JSBody(script = "return window.devicePixelRatio || 1;")
    static native float getDevicePixelRatio();

    @JSBody(script = "return window.innerWidth;")
    static native int getWindowWidth();

    @JSBody(script = "return window.innerHeight;")
    static native int getWindowHeight();
}
