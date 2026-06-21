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

package com.sacredpixel.sacredpixeldungeon.scenes;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.InterstitialAd;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.GamesInProgress;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.Actor;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Imp;
import com.sacredpixel.sacredpixeldungeon.effects.ShadowBox;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.LostBackpack;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.SkeletonKey;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.journal.Notes;
import com.sacredpixel.sacredpixeldungeon.levels.Level;
import com.sacredpixel.sacredpixeldungeon.levels.Terrain;
import com.sacredpixel.sacredpixeldungeon.levels.TestLevel;
import com.sacredpixel.sacredpixeldungeon.levels.features.Chasm;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialManager;
import com.sacredpixel.sacredpixeldungeon.levels.features.LevelTransition;
import com.sacredpixel.sacredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.ui.ActionIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.GameLog;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.windows.WndError;
import com.sacredpixel.sacredpixeldungeon.windows.WndOptions;
import com.sacredpixel.sacredpixeldungeon.windows.WndRegionComplete;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.BArray;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class InterlevelScene extends PixelScene {
	
	//slow fade on entering a new region or floor 26
	//FADE_IN (0.9s) + FADE_OUT (0.9s) = 1.8s total
	private static final float SLOW_FADE = 0.9f;
	//norm fade when loading, falling, returning, or descending to a new floor
	//FADE_IN (0.7s) + FADE_OUT (0.7s) = 1.4s total
	private static final float NORM_FADE = 0.7f;
	//fast fade when ascending, or descending to a floor you've been on
	//FADE_IN (0.5s) + FADE_OUT (0.5s) = 1.0s total
	//currently unused, reserved for potential future use
	private static final float FAST_FADE = 0.5f;

	//background fade duration (in/out) for story sequences
	private static final float BG_FADE_DURATION = 0.5f;
	//story text fade duration (in/out)
	private static final float STORY_FADE_DURATION = 0.8f;

	//minimum time the scene must be displayed before transitioning
	private static final float MINIMUM_DISPLAY_TIME = 0.5f;

	private static float fadeTime;
	
	public enum Mode {
		DESCEND, ASCEND, CONTINUE, RESURRECT, RETURN, FALL, RESET, NONE
	}
	public static Mode mode;

	public static LevelTransition curTransition = null;
	public static int returnDepth;
	public static int returnBranch;
	public static int returnPos;

	public static boolean fallIntoPit;
	public static boolean testLevel = false;
	public static boolean tutorialLevel = false;

	private enum Phase {
		FADE_IN, STATIC, FADE_OUT, STAGE_CLEAR
	}
	private Phase phase;
	private float timeLeft;

	public Image background;

	private RenderedTextBlock loadingText;

	private RenderedTextBlock storyMessage;
	private ShadowBox storyBG;
	private StyledButton btnContinue;
	
	private static Thread thread;
	private static Exception error = null;
	private float waitingTime;

	private boolean stageClearPending = false;
	private boolean stageClearWindowCreated = false;
	private ColorBlock stageclearBg;
	private boolean isNewFloor = false;
	private float displayTimeElapsed = 0f;
	private boolean waitingForMinTime = false;
	private boolean storyFadingOut = false;
	private boolean proceedCalled = false;  // Prevents duplicate proceedAfterLoading() calls
	private float storyFadeOutTime = 0f;
	private float btnContinueEnabledTime = 0f;  // Time since continue button was enabled
	private static final float BTN_INPUT_DELAY = 0.5f;  // Delay before accepting input on continue button

	// Whether this floor is a story floor (6,11,16,21) with deferred story
	private boolean isStoryFloor = false;
	private int storyRegion = 0;
	private boolean exitAfterFadeIn = false;  // For non-story floors after region complete
	private boolean interstitialAdShown = false;  // Prevents duplicate ad/story display
	private boolean storyCreatedForFadeIn = false;  // Story elements created, waiting for fade-in

	// Interstitial ad callback timeout (prevents infinite black screen)
	private boolean waitingForAdCallback = false;
	private float adCallbackWaitTime = 0f;
	// Timeout after ad completion is detected (via timestamp) but callback didn't arrive
	private static final float AD_POST_COMPLETE_TIMEOUT = 3f;  // 3 seconds after ad ends

	// For floor 1: show Dungeon intro first, then Sewers intro
	private boolean hasPendingSecondStory = false;
	private int pendingSecondRegion = 0;

	// Background fade states for story sequences
	private boolean bgFadingIn = false;      // Background fading in before story
	private boolean bgFadingOut = false;     // Background fading out after story (to GameScene)
	private boolean bgTransitioning = false; // Background crossfading (floor 1: ENTRANCE→SEWERS)
	private float bgFadeTime = 0f;           // Tracks background fade progress

	public static int lastRegion = -1;

	private RectF insets;

	{
		inGameScene = true;
	}
	
	@Override
	public void create() {
		super.create();

		String loadingAsset;
		int loadingDepth;
		fadeTime = NORM_FADE;

		// Tutorial and Test levels: show entrance.jpg with slow fade, no story
		boolean isTutorialOrTestLevel = tutorialLevel || testLevel;

		long seed = Dungeon.seed;
		switch (mode){
			default:
				loadingDepth = Dungeon.depth;
				break;
			case CONTINUE:
				loadingDepth = GamesInProgress.check(GamesInProgress.curSlot).depth;
				seed = GamesInProgress.check(GamesInProgress.curSlot).seed;
				break;
			case DESCEND:
				if (Dungeon.hero == null){
					loadingDepth = 1;
					fadeTime = SLOW_FADE;
					isNewFloor = true;
				} else {
					if (curTransition != null)  loadingDepth = curTransition.destDepth;
					else                        loadingDepth = Dungeon.depth+1;
					if (Statistics.deepestFloor >= loadingDepth) {
						isNewFloor = false;
					} else {
						isNewFloor = true;
					}
					//floors 6,11,16,21,26 skip FADE_IN (WndRegionComplete shown first)
					//so SLOW_FADE is not applied to any region complete floors
				}
				break;
			case FALL:
				loadingDepth = Dungeon.depth+1;
				break;
			case ASCEND:
				if (curTransition != null)  loadingDepth = curTransition.destDepth;
				else                        loadingDepth = Dungeon.depth-1;
				break;
			case RETURN:
				loadingDepth = returnDepth;
				break;
		}

		//flush the texture cache whenever moving between regions, helps reduce memory load
		int region = (int)Math.ceil(loadingDepth / 5f);
		if (region != lastRegion){
			TextureCache.clear();
			lastRegion = region;
		}

		int loadingCenter = 400;

		//for portrait users, each run the splashes change what details they focus on
		Random.pushGenerator(seed+lastRegion);
			// Tutorial/Test levels and Floor 1 (new game) use ENTRANCE background
			if (isTutorialOrTestLevel || (Dungeon.hero == null && loadingDepth == 1)) {
				loadingAsset = Assets.Splashes.ENTRANCE;
				loadingCenter = 400; // center focus
				fadeTime = SLOW_FADE;
			} else switch (lastRegion){
				case 1:
					loadingAsset = Assets.Splashes.SEWERS;
					switch (Random.Int(2)){
						case 0: loadingCenter = 180; break; //focus on rats and left side
						case 1: loadingCenter = 485; break; //focus on center pipe and door
					}
					break;
				case 2:
					loadingAsset = Assets.Splashes.PRISON;
					switch (Random.Int(3)){
						case 0: loadingCenter = 190; break; //focus on left skeleton
						case 1: loadingCenter = 402; break; //focus on center arch
					}
					break;
				case 3:
					loadingAsset = Assets.Splashes.CAVES;
					switch (Random.Int(3)){
						case 0: loadingCenter = 340; break; //focus on center gnoll groups
						case 1: loadingCenter = 625; break; //focus on right gnoll
					}
					break;
				case 4:
					loadingAsset = Assets.Splashes.CITY;
					switch (Random.Int(3)){
						case 0: loadingCenter = 275; break; //focus on left bookcases
						case 1: loadingCenter = 485; break; //focus on center pathway
					}
					break;
				case 5: default:
					loadingAsset = Assets.Splashes.HALLS;
					switch (Random.Int(3)){
						case 0: loadingCenter = 145; break; //focus on left arches
						case 1: loadingCenter = 400; break; //focus on ripper demon
					}
					break;
			}
		Random.popGenerator();
		
		// Debug mode no longer skips fade time to ensure story and minimum display time work properly

		insets = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK).scale(1f/defaultZoom);

		int w = (int)(Camera.main.width - insets.left - insets.right);
		int h = (int)(Camera.main.height - insets.top - insets.bottom);

		background = new Image(loadingAsset);
		background.scale.set(Camera.main.height/background.height);

		if (Camera.main.width >= background.width()){
			background.x = (Camera.main.width - background.width())/2f;
		} else {
			background.x = Camera.main.width/2f - loadingCenter*background.scale.x;
			background.x = GameMath.gate(Camera.main.width - background.width(), background.x, 0);
		}
		background.y = (Camera.main.height - background.height())/2f;
		PixelScene.align(background);
		add(background);

		Image fadeLeft, fadeRight;
		fadeLeft = new Image(TextureCache.createGradient(0xFF000000, 0xFF000000, 0x00000000));
		fadeLeft.x = background.x-2;
		fadeLeft.scale.set(3, background.height());
		fadeLeft.visible = background.x > 0;
		add(fadeLeft);

		fadeRight = new Image(fadeLeft);
		fadeRight.x = background.x + background.width() + 2;
		fadeRight.y = background.y + background.height();
		fadeRight.angle = 180;
		fadeRight.visible = fadeLeft.visible;
		add(fadeRight);

		Image im = new Image(TextureCache.createGradient(0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000)){
			@Override
			public void update() {
				super.update();
				if (lastRegion == 6)                aa = 1;
				else if (phase == Phase.FADE_IN)    aa = Math.max( 0, 2*(timeLeft - (fadeTime - BG_FADE_DURATION)));
				else if (phase == Phase.FADE_OUT)   aa = Math.max( 0, 2*(BG_FADE_DURATION - timeLeft));
				//else                                aa = 0;
			}
		};
		im.angle = 90;
		im.x = insets.left + w;
		im.scale.x = h/5f;
		im.scale.y = w;
		add(im);

		String text = Messages.get(Mode.class, mode.name());
		
		loadingText = PixelScene.renderTextBlock( text, 8 );
		loadingText.setPos(
				insets.left + w - loadingText.width() - 12,
				insets.top + h - loadingText.height() - 6
		);
		align(loadingText);
		add(loadingText);

		// Tutorial/Test levels skip story completely
		if (!isTutorialOrTestLevel && mode == Mode.DESCEND && lastRegion <= 5){
			if (Dungeon.hero == null || (loadingDepth > Statistics.deepestFloor && loadingDepth % 5 == 1)){
				// Flag this as a story floor; actual story elements will be created
				// after WndRegionComplete + countdown on story floors (6,11,16,21),
				// or immediately for floor 1 (which has no region complete).
				isStoryFloor = true;
				storyRegion = region;

				if (Dungeon.hero == null) {
					// Floor 1: show "Dungeon" intro (region 0) first, then "Sewers" intro (region 1)
					createStoryElements(0);  // Dungeon intro
					hasPendingSecondStory = true;
					pendingSecondRegion = region;  // Sewers intro (region 1)
				}
				// Other story floors: defer creation until after ad countdown
			}
		}

		//Skip fade-in for WndRegionComplete floors (6,11,16,21,26)
		boolean isRegionCompleteFloor = isNewFloor && mode == Mode.DESCEND && Dungeon.hero != null
				&& (loadingDepth == 6 || loadingDepth == 11 || loadingDepth == 16 || loadingDepth == 21 || loadingDepth == 26);
		if (isRegionCompleteFloor) {
			phase = Phase.STATIC;
			if (background != null) background.alpha(0);
			if (loadingText != null) loadingText.alpha(0);
		} else {
			phase = Phase.FADE_IN;
		}
		timeLeft = fadeTime;
		
		if (thread == null) {
			thread = new Thread() {
				@Override
				public void run() {
					
					try {

						Actor.fixTime();

						switch (mode) {
							case DESCEND:
								descend();
								break;
							case ASCEND:
								ascend();
								break;
							case CONTINUE:
								restore();
								break;
							case RESURRECT:
								resurrect();
								break;
							case RETURN:
								returnTo();
								break;
							case FALL:
								fall();
								break;
							case RESET:
								reset();
								break;
						}
						
					} catch (Exception e) {
						
						error = e;
						
					}

					synchronized (thread) {
						if (phase == Phase.STATIC && error == null) {
							afterLoading();
						}
					}
				}
			};
			thread.start();
		}
		waitingTime = 0f;
	}

	private int dots = 0;
	private boolean textFadingIn = false;

	@Override
	public void update() {
		super.update();

		// Poll for interstitial ad completion callback
		InterstitialAd.checkCallback();

		// Timeout safety net for interstitial ad callback
		// Prevents infinite black screen if JS callback fails to reach Java
		// Uses timestamp-based detection: if ad completed but callback didn't arrive within timeout
		if (waitingForAdCallback) {
			// Check if ad has completed (via JavaScript timestamp)
			double adCompletedTimestamp = InterstitialAd.getAdCompletedTimestamp();
			if (adCompletedTimestamp > 0) {
				// Ad completed - start counting from completion time
				double currentTime = System.currentTimeMillis();
				double elapsedSinceCompletion = (currentTime - adCompletedTimestamp) / 1000.0;

				if (elapsedSinceCompletion >= AD_POST_COMPLETE_TIMEOUT) {
					// Timeout passed since ad completed but callback never arrived
					waitingForAdCallback = false;
					InterstitialAd.clearAdCompletedTimestamp();
					Game.reportException(new RuntimeException(
						"Interstitial ad callback timeout " + AD_POST_COMPLETE_TIMEOUT + "s after ad completion - forcing proceed"));
					proceedToStory();
				}
			}
		}

		//STAGE_CLEAR phase: create WndRegionComplete (no fade-in)
		if (phase == Phase.STAGE_CLEAR && !stageClearWindowCreated) {
			stageClearWindowCreated = true;
			try {
				WndRegionComplete wnd = new WndRegionComplete(() -> {
					startPostRegionComplete();
				});
				add(wnd);
			} catch (Exception e) {
				Game.reportException(e);
				startPostRegionComplete();
			}
		}

		if (btnContinue == null || !btnContinue.isActive()) {
			waitingTime += Game.elapsed;
		}

		// Track total display time for minimum display time check
		displayTimeElapsed += Game.elapsed;

		if (mode != Mode.FALL && dots != Math.ceil(waitingTime / ((2*fadeTime)/3f))) {
			String text = Messages.get(Mode.class, mode.name());
			dots = (int)Math.ceil(waitingTime / ((2*fadeTime)/3f))%3;
			switch (dots){
				case 1: default:
					loadingText.text(text + ".");
					break;
				case 2:
					loadingText.text(text + "..");
					break;
				case 0:
					loadingText.text(text + "...");
					break;
			}
		}

		int w = (int)(Camera.main.width - insets.left - insets.right);
		int h = (int)(Camera.main.height - insets.top - insets.bottom);
		
		switch (phase) {
		
		case FADE_IN:
			float fadeInAlpha = Math.max(0, fadeTime - (timeLeft-BG_FADE_DURATION));
			loadingText.alpha(fadeInAlpha);
			// Only fade in background for non-story floors after region complete
			if (exitAfterFadeIn) {
				if (background != null) background.alpha(Math.min(1, fadeInAlpha));
			}
			if ((timeLeft -= Game.elapsed) <= 0) {
				if (storyCreatedForFadeIn) {
					// Story floors 6,11,16,21: story elements already created
					// Enable continue button like floor 1's afterLoading() does
					storyCreatedForFadeIn = false;
					if (btnContinue != null) {
						btnContinue.visible = true;
						btnContinue.enable(true);
						btnContinueEnabledTime = 0f;  // Reset timer for input delay
						btnContinue.alpha(0);  // Will fade in during STATIC phase
					}
					if (storyBG != null) storyBG.visible = true;
					if (storyMessage != null) storyMessage.visible = true;
					textFadingIn = true;
					phase = Phase.STATIC;
				} else if (exitAfterFadeIn) {
					// After region complete fade-in (non-story floor), go to game
					exitAfterFadeIn = false;
					phase = Phase.FADE_OUT;
					timeLeft = fadeTime;
				} else if (thread != null) {
					synchronized (thread) {
						if (!thread.isAlive() && error == null) {
							afterLoading();
						} else {
							phase = Phase.STATIC;
						}
					}
				} else {
					// Thread is null (shouldn't happen normally), just fade out
					phase = Phase.FADE_OUT;
					timeLeft = fadeTime;
				}
			}
			break;
			
		case FADE_OUT:
			background.acc.set(0);
			background.speed.set(0);

			loadingText.alpha( Math.min(1, timeLeft+BG_FADE_DURATION) );

			if (btnContinue != null && btnContinue.visible){
				btnContinue.alpha((timeLeft/fadeTime));
				storyMessage.alpha(btnContinue.alpha());
				storyBG.alpha(btnContinue.alpha()*0.8f);
			}
			
			if ((timeLeft -= Game.elapsed) <= 0) {
				Game.switchScene( GameScene.class );
				KeyEvent.clearListeners(); //removes potential listener for continue
				thread = null;
				error = null;
			}
			break;
			
		case STATIC:

			// Cap elapsed time to prevent instant jumps after ad (Game.elapsed can be huge)
			float cappedElapsed = Math.min(Game.elapsed, 0.05f);

			// Track time since continue button was enabled for input delay
			if (btnContinue != null && btnContinue.active) {
				btnContinueEnabledTime += cappedElapsed;
			}

			// === BACKGROUND FADE-IN (before story) ===
			if (bgFadingIn && background != null) {
				bgFadeTime += cappedElapsed;
				float bgAlpha = Math.min(1, bgFadeTime / BG_FADE_DURATION);
				background.alpha(bgAlpha);
				if (loadingText != null) loadingText.alpha(bgAlpha);

				if (bgAlpha >= 1) {
					bgFadingIn = false;
					bgFadeTime = 0f;
					// Now start story fade-in
					textFadingIn = true;
				}
			}

			// === STORY FADE-IN ===
			if (btnContinue != null && textFadingIn && !storyFadingOut && !bgFadingIn) {
				float newAlpha = Math.min(1, btnContinue.alpha() + cappedElapsed / STORY_FADE_DURATION);

				btnContinue.alpha(newAlpha);
				storyMessage.alpha(newAlpha);
				storyBG.alpha(newAlpha * 0.8f);

				if (newAlpha >= 1){
					textFadingIn = false;
				}
			}

			// === STORY FADE-OUT ===
			if (storyFadingOut && btnContinue != null && !bgFadingOut && !bgTransitioning) {
				storyFadeOutTime += cappedElapsed;
				float alpha = Math.max(0, 1f - storyFadeOutTime / STORY_FADE_DURATION);
				btnContinue.alpha(alpha);
				storyMessage.alpha(alpha);
				storyBG.alpha(alpha * 0.8f);

				if (storyFadeOutTime >= STORY_FADE_DURATION) {
					storyFadingOut = false;
					storyFadeOutTime = 0f;
					// Remove old story elements
					btnContinue.destroy();
					storyMessage.destroy();
					storyBG.destroy();
					btnContinue = null;
					storyMessage = null;
					storyBG = null;

					if (hasPendingSecondStory) {
						// Floor 1: transition background from ENTRANCE to SEWERS
						bgTransitioning = true;
						bgFadeTime = 0f;
					} else {
						// Last story: fade out background then go to GameScene
						bgFadingOut = true;
						bgFadeTime = 0f;
					}
				}
			}

			// === BACKGROUND TRANSITION (Floor 1: ENTRANCE → SEWERS) ===
			if (bgTransitioning && background != null) {
				bgFadeTime += cappedElapsed;
				float progress = bgFadeTime / BG_FADE_DURATION;

				if (progress < 1) {
					// Fade out current background
					background.alpha(1 - progress);
					if (loadingText != null) loadingText.alpha(1 - progress);
				} else if (progress < 2) {
					// Swap texture at midpoint and fade in
					if (progress >= 1 && bgFadeTime - cappedElapsed < BG_FADE_DURATION) {
						// Just crossed the midpoint - swap texture
						if (pendingSecondRegion == 1) {
							background.texture(TextureCache.get(Assets.Splashes.SEWERS));
						}
					}
					float fadeInProgress = progress - 1;
					background.alpha(fadeInProgress);
					if (loadingText != null) loadingText.alpha(fadeInProgress);
				} else {
					// Transition complete
					bgTransitioning = false;
					bgFadeTime = 0f;
					hasPendingSecondStory = false;

					// Create and show second story
					createStoryElements(pendingSecondRegion);
					btnContinue.visible = true;
					btnContinue.enable(true);
					btnContinueEnabledTime = 0f;  // Reset timer for input delay
					btnContinue.alpha(0);
					if (storyBG != null) storyBG.visible = true;
					if (storyMessage != null) storyMessage.visible = true;

					// Start background fade-in for second story
					background.alpha(1); // Background already faded in during transition
					textFadingIn = true;  // Go directly to story fade-in
				}
			}

			// === BACKGROUND FADE-OUT (to GameScene) ===
			if (bgFadingOut && background != null) {
				bgFadeTime += cappedElapsed;
				float bgAlpha = Math.max(0, 1 - bgFadeTime / BG_FADE_DURATION);
				background.alpha(bgAlpha);
				if (loadingText != null) loadingText.alpha(bgAlpha);

				if (bgAlpha <= 0) {
					bgFadingOut = false;
					bgFadeTime = 0f;
					// Background fade complete, go directly to GameScene
					Game.switchScene(GameScene.class);
					KeyEvent.clearListeners();
					thread = null;
					error = null;
				}
			}

			// Check if we were waiting for minimum display time and can now proceed
			if (waitingForMinTime && displayTimeElapsed >= MINIMUM_DISPLAY_TIME) {
				waitingForMinTime = false;
				proceedAfterLoading();
			}

			if (error != null) {
				String errorMsg;
				if (error instanceof FileNotFoundException)     errorMsg = Messages.get(this, "file_not_found");
				else if (error instanceof IOException)          errorMsg = Messages.get(this, "io_error");
				else if (error.getMessage() != null &&
						error.getMessage().equals("old save")) errorMsg = Messages.get(this, "io_error");

				else {
					//On HTML5/GWT, JavaScriptExceptions (e.g. "c is null") can occur
					//during level generation due to WebGL context loss or serialization issues.
					//Log the error but handle gracefully instead of crashing.
					Game.reportException(new RuntimeException("error occurred while moving between floors. " +
							"Seed:" + Dungeon.seed + " depth:" + Dungeon.depth, error));
					errorMsg = Messages.get(this, "io_error");
				}

				add( new WndError( errorMsg ) {
					public void onBackPressed() {
						super.onBackPressed();
						Game.switchScene( StartScene.class );
					}
				} );
				thread = null;
				error = null;
			} else if (thread != null && (int)waitingTime == 10){
				waitingTime = 11f;
				String s = "";
				for (StackTraceElement t : thread.getStackTrace()){
					s += "\n";
					s += t.toString();
				}
				//we care about reporting game logic exceptions, not slow IO
				if (!s.contains("FileUtils.bundleToFile")){
					SacredPixelDungeon.reportException(
							new RuntimeException("waited more than 10 seconds on levelgen. " +
									"Seed:" + Dungeon.seed + " depth:" + Dungeon.depth + " trace:" +
									s));
				}
			}
			break;
		}

		if (mode == Mode.FALL) {
			loadingText.setPos(
					//the randomization is effectively -2 to +2
					// we don't use the generator stack as levelgen may be occurring
					// and we don't want to accidentally use a seeded generator
					(w + insets.left - loadingText.width() - 4) + 4*(Random.Float(false)-0.5f),
					(h + insets.top - loadingText.height() - 6) + 4*(Random.Float(false)-0.5f)
			);
			align(loadingText);
		}
	}

	private void createStoryElements(int region) {
		int w = (int)(Camera.main.width - insets.left - insets.right);
		int h = (int)(Camera.main.height - insets.top - insets.bottom);

		storyMessage = PixelScene.renderTextBlock(Document.INTROS.pageBody(region), 7);
		storyMessage.maxWidth( PixelScene.landscape() ? 180 : 125);
		storyMessage.setPos(insets.left+(w-storyMessage.width())/2f, insets.top+(h-storyMessage.height())/2f);
		storyMessage.alpha(0);  // Start at 0, fade in during STATIC

		storyBG = new ShadowBox();
		storyBG.boxRect(storyMessage.left()-10, storyMessage.top()-10, storyMessage.width()+20, storyMessage.height()+20);
		storyBG.alpha(0);  // Start at 0, fade in during STATIC

		add(storyBG);
		add(storyMessage);

		btnContinue = new StyledButton(Chrome.Type.TOAST_TR, Messages.get(InterlevelScene.class, "continue"), 8){
			@Override
			protected void onClick() {
				btnContinue.enable(false);
				Document.INTROS.readPage(region);
				startStoryFadeOut();
			}
		};
		btnContinue.icon(Icons.STAIRS.get());
		btnContinue.setSize(btnContinue.reqWidth()+10, 22);
		btnContinue.visible = false;
		btnContinue.enable(false);

		Signal.Listener<KeyEvent> keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent keyEvent) {
				// Require minimum delay after button is enabled to prevent accidental dismissal
				if (!keyEvent.pressed && btnContinue.active && btnContinueEnabledTime >= BTN_INPUT_DELAY){
					btnContinue.enable(false);
					Document.INTROS.readPage(region);
					KeyEvent.removeKeyListener(this);
					startStoryFadeOut();
					return true;
				}
				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);

		btnContinue.setPos(insets.left + (w - btnContinue.width())/2f, storyMessage.bottom()+10);
		add(btnContinue);
		btnContinue.alpha(0);
	}

	private void afterLoading(){
		if (btnContinue != null){
			// Floor 1 story: story already created, enable continue button
			// Make all story elements visible for fade-in
			btnContinue.visible = true;
			btnContinue.enable(true);
			btnContinueEnabledTime = 0f;  // Reset timer for input delay
			btnContinue.alpha(0);  // Start at 0, will fade in during STATIC phase
			if (storyBG != null) storyBG.visible = true;
			if (storyMessage != null) storyMessage.visible = true;
			textFadingIn = true;   // Enable fade-in animation for story elements
			phase = Phase.STATIC;
		} else if (isStoryFloor && isNewFloor && mode == Mode.DESCEND && Dungeon.hero != null) {
			// Story floors 6,11,16,21: show WndRegionComplete first, then story
			proceedAfterLoading();
		} else {
			// Non-story floors: ensure minimum display time then fade out
			if (displayTimeElapsed < MINIMUM_DISPLAY_TIME) {
				waitingForMinTime = true;
				phase = Phase.STATIC;
			} else {
				proceedAfterLoading();
			}
		}

	}

	private void startStoryFadeOut() {
		storyFadingOut = true;
		storyFadeOutTime = 0f;
	}

	private void proceedAfterLoading() {
		if (proceedCalled) return;
		proceedCalled = true;

		// Only proceed with stage clear on new floors when descending
		if (!isNewFloor || mode != Mode.DESCEND) {
			phase = Phase.FADE_OUT;
			timeLeft = fadeTime;
			return;
		}

		// Show WndRegionComplete on region complete floors: 6, 11, 16, 21, 26 (first visit only)
		// No gray overlay - just hide interlevel elements until WndRegionComplete closes
		if (Dungeon.depth == 6 || Dungeon.depth == 11 || Dungeon.depth == 16 || Dungeon.depth == 21 || Dungeon.depth == 26) {
			if (Dungeon.hero != null) {
				stageClearPending = true;
				stageClearWindowCreated = false;
				//Hide interlevel elements (no gray overlay)
				if (loadingText != null) loadingText.visible = false;
				if (background != null) background.visible = false;
				// Defer WndRegionComplete creation to update() in STAGE_CLEAR phase
				phase = Phase.STAGE_CLEAR;
			} else {
				phase = Phase.FADE_OUT;
				timeLeft = fadeTime;
			}
			return;
		}

		phase = Phase.FADE_OUT;
		timeLeft = fadeTime;
	}

	// Called after WndRegionComplete closes (floors 6,11,16,21,26)
	// Creates story elements and starts fade-in for story floors
	// For non-story floors (26), keeps black background and transitions to GameScene
	private void startPostRegionComplete() {
		// Prevent duplicate calls (e.g., from ad callback race conditions)
		if (interstitialAdShown) return;
		interstitialAdShown = true;

		stageClearPending = false;

		// Show interstitial ad for floors 6, 11, 16, 21 (not 26)
		// Ad is preloaded when boss is killed on floors 5, 10, 15, 20
		int depth = Dungeon.depth;
		if (depth == 6 || depth == 11 || depth == 16 || depth == 21) {
			final InterlevelScene scene = this;
			// Start timeout tracking for ad callback
			waitingForAdCallback = true;
			adCallbackWaitTime = 0f;
			InterstitialAd.show(() -> {
				// Ad completed (or skipped/timed out) - proceed to story
				// Must run on render thread since callback comes from JavaScript
				Game.runOnRenderThread(new com.watabou.utils.Callback() {
					@Override
					public void call() {
						if (waitingForAdCallback) {
							waitingForAdCallback = false;
							scene.proceedToStory();
						}
					}
				});
			});
		} else {
			// Floor 26 or other - no ad, proceed directly
			proceedToStory();
		}
	}

	// Called after interstitial ad completes (or immediately if no ad)
	private void proceedToStory() {
		if (isStoryFloor) {
			// Restore background visibility but start with alpha 0 for smooth fade-in
			if (background != null) {
				background.visible = true;
				background.alpha(0);
			}
			if (loadingText != null) {
				loadingText.visible = true;
				loadingText.alpha(0);
			}

			// Create story elements (same as floor 1 in create())
			createStoryElements(storyRegion);

			// Follow exact same pattern as floor 1's afterLoading()
			if (btnContinue != null) {
				btnContinue.visible = true;
				btnContinue.enable(true);
				btnContinueEnabledTime = 0f;  // Reset timer for input delay
				btnContinue.alpha(0);  // Start at 0, fade-in via STATIC phase
			}
			if (storyBG != null) storyBG.visible = true;
			if (storyMessage != null) storyMessage.visible = true;

			// Start with background fade-in, then story fade-in
			bgFadingIn = true;
			bgFadeTime = 0f;
			textFadingIn = false;  // Will be set true after bgFadingIn completes
			phase = Phase.STATIC;
		} else {
			// Non-story floor (26) - keep black background, use SLOW_FADE for dramatic effect
			// Background stays hidden (black) during FADE_OUT
			phase = Phase.FADE_OUT;
			timeLeft = SLOW_FADE;
		}
	}

	private void descend() throws IOException {

		SacredPixelDungeon.preloadGameAssets();

		// Tutorial level - simple room for new players
		if (tutorialLevel) {
			tutorialLevel = false;
			TutorialManager.reset(); // Reset tutorial state for fresh start
			TutorialManager.enterTutorial(); // Isolate journal state for tutorial
			Dungeon.hero = null;
			Mob.clearHeldAllies();
			Dungeon.initSeed();
			Dungeon.init();
			GameLog.wipe();
			ActionIndicator.clearAction();
			Dungeon.depth = 1;
			Level level = new com.sacredpixel.sacredpixeldungeon.levels.TutorialLevel();
			level.create();
			Dungeon.switchLevel(level, level.entrance());
			return;
		}

		// Restore journal state if we were in tutorial mode
		TutorialManager.exitTutorial();

		// Test levels always force a fresh start regardless of hero state
		if (testLevel) {
			testLevel = false;
			Dungeon.hero = null;
			Mob.clearHeldAllies();
			Dungeon.initSeed();
			Dungeon.init();
			GameLog.wipe();
			ActionIndicator.clearAction();
			Dungeon.hero.HP = Dungeon.hero.HT = 999;
			Dungeon.depth = 1; // Test Level at depth 1
			Statistics.deepestFloor = 19; // Keep high for exits to depth 20/25
			Level level = new com.sacredpixel.sacredpixeldungeon.levels.TestLevel();
			level.create();
			Dungeon.switchLevel( level, -1 );
			return;
		}

		if (Dungeon.hero == null) {
			Mob.clearHeldAllies();
			// initSeed() is already called by HeroSelectScene (new game) or WndRanking (replay)
			// Calling it again here would overwrite the seed set by those scenes
			// Only call initSeed() if no custom seed was set (fallback for edge cases)
			if (SPDSettings.customSeed().isEmpty()) {
				Dungeon.initSeed();
			}

			Dungeon.init();
			GameLog.wipe();

			//When debugging, we may start a game at a later depth to quickly test something
			// if this happens, the games quickly generates all prior levels on branch 0 first,
			// which ensures levelgen consistency with a regular game that was played to that depth.
			if (DeviceCompat.isDebug()){
				int trueDepth = Dungeon.depth;
				int trueBranch = Dungeon.branch;
				for (int i = 1; i < trueDepth + (trueBranch == 0 ? 0 : 1); i++){
					if (!Dungeon.levelHasBeenGenerated(i, 0)){
						Dungeon.depth = i;
						Dungeon.branch = 0;
						Dungeon.level = Dungeon.newLevel();
						Dungeon.saveLevel(GamesInProgress.curSlot);
					}
				}
				Dungeon.depth = trueDepth;
				Dungeon.branch = trueBranch;
			}

			Level level = Dungeon.newLevel();
			Dungeon.switchLevel( level, -1 );
		} else {
			if (curTransition.destBranch != Dungeon.branch && Dungeon.depth >= 16 && Dungeon.depth <= 20) {
				//FIXME avoids holding allies when entering city quest area, this is very sloppy though
				// perhaps holding allies could be a property of the transition?
			} else {
				Mob.holdAllies(Dungeon.level);
			}
			Dungeon.saveAll();

			Level level;
			int prevDepth = Dungeon.depth;
			Dungeon.depth = curTransition.destDepth;
			Dungeon.branch = curTransition.destBranch;

			// Special case: Test Level → Floor 5 (Goo boss)
			// Reset statistics so Goo fight is fresh (test ad preload → floor 6 → WndRegionComplete → ad → story)
			// Only applies when coming from actual TestLevel
			if (Dungeon.depth == 5 && Dungeon.level instanceof TestLevel) {
				Statistics.deepestFloor = 4;  // So floor 5 is "new"
				// No bosses killed yet
				Statistics.bossScores[0] = 0;     // Goo not killed
				Statistics.bossScores[1] = 0;     // Tengu not killed
				Statistics.bossScores[2] = 0;     // DM-300 not killed
				Statistics.bossScores[3] = 0;     // Dwarf King not killed
				Statistics.bossScores[4] = 0;     // Yog-Dzewa not killed
			}

			if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
				level = Dungeon.loadLevel( GamesInProgress.curSlot );
			} else {
				level = Dungeon.newLevel();
			}

			LevelTransition destTransition = level.getTransition(curTransition.destType);
			curTransition = null;
			Dungeon.switchLevel( level, destTransition.cell() );
		}

	}

	//TODO atm falling always just increments depth by 1, do we eventually want to roll it into the transition system?
	private void fall() throws IOException {
		
		Mob.holdAllies( Dungeon.level );
		
		Buff.affect( Dungeon.hero, Chasm.Falling.class );
		Dungeon.saveAll();

		Level level;
		Dungeon.depth++;
		if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
			level = Dungeon.loadLevel( GamesInProgress.curSlot );
		} else {
			level = Dungeon.newLevel();
		}
		Dungeon.switchLevel( level, level.fallCell( fallIntoPit ));
	}

	private void ascend() throws IOException {
		if (curTransition.destBranch != Dungeon.branch && Dungeon.depth >= 16 && Dungeon.depth <= 20) {
			//FIXME avoids holding allies when entering city quest area, this is very sloppy though
			// perhaps holding allies could be a property of the transition?
		} else {
			Mob.holdAllies(Dungeon.level);
		}
		Dungeon.saveAll();

		Level level;
		Dungeon.depth = curTransition.destDepth;
		Dungeon.branch = curTransition.destBranch;

		if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
			level = Dungeon.loadLevel( GamesInProgress.curSlot );
		} else {
			level = Dungeon.newLevel();
		}

		LevelTransition destTransition = level.getTransition(curTransition.destType);
		curTransition = null;
		Dungeon.switchLevel( level, destTransition.cell() );
	}
	
	private void returnTo() throws IOException {
		Mob.holdAllies( Dungeon.level );
		Dungeon.saveAll();

		Level level;
		Dungeon.depth = returnDepth;
		Dungeon.branch = returnBranch;
		if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
			level = Dungeon.loadLevel( GamesInProgress.curSlot );
		} else {
			level = Dungeon.newLevel();
		}

		Dungeon.switchLevel( level, returnPos );
	}
	
	private void restore() throws IOException {
		
		Mob.clearHeldAllies();

		GameLog.wipe();

		Dungeon.loadGame( GamesInProgress.curSlot );
		if (Dungeon.depth == -1) {
			Dungeon.depth = Statistics.deepestFloor;
			Dungeon.switchLevel( Dungeon.loadLevel( GamesInProgress.curSlot ), -1 );
		} else {
			Level level = Dungeon.loadLevel( GamesInProgress.curSlot );
			Dungeon.switchLevel( level, Dungeon.hero.pos );
		}
	}
	
	private void resurrect() {
		
		Mob.holdAllies( Dungeon.level );

		Level level;
		if (Dungeon.level.locked) {
			ArrayList<Item> preservedItems = Dungeon.level.getItemsToPreserveFromSealedResurrect();

			Dungeon.hero.resurrect();
			level = Dungeon.newLevel();
			Dungeon.hero.pos = level.randomRespawnCell(Dungeon.hero);
			if (Dungeon.hero.pos == -1) Dungeon.hero.pos = level.entrance();

			for (Item i : preservedItems){
				int pos = level.randomRespawnCell(null);
				if (pos == -1) pos = level.entrance();
				level.drop(i, pos);
			}
			int pos = level.randomRespawnCell(null);
			if (pos == -1) pos = level.entrance();
			level.drop(new LostBackpack(), pos);

			//need to reset key replacement tracking as well
			if (Dungeon.hero.buff(SkeletonKey.KeyReplacementTracker.class) != null){
				Dungeon.hero.buff(SkeletonKey.KeyReplacementTracker.class).clearDepth();
			}

		} else {
			level = Dungeon.level;
			BArray.setFalse(level.heroFOV);
			BArray.setFalse(level.visited);
			BArray.setFalse(level.mapped);
			int invPos = Dungeon.hero.pos;
			int tries = 0;
			do {
				Dungeon.hero.pos = level.randomRespawnCell(Dungeon.hero);
				tries++;

			//prevents spawning on traps or plants, prefers farther locations first
			} while (level.traps.get(Dungeon.hero.pos) != null
					|| (level.plants.get(Dungeon.hero.pos) != null && tries < 500)
					|| level.trueDistance(invPos, Dungeon.hero.pos) <= 30 - (tries/10));

			//directly trample grass
			if (level.map[Dungeon.hero.pos] == Terrain.HIGH_GRASS || level.map[Dungeon.hero.pos] == Terrain.FURROWED_GRASS){
				level.map[Dungeon.hero.pos] = Terrain.GRASS;
			}
			Dungeon.hero.resurrect();
			level.drop(new LostBackpack(), invPos);
		}

		Notes.add(Notes.Landmark.LOST_PACK);

		Dungeon.switchLevel( level, Dungeon.hero.pos );
	}

	private void reset() throws IOException {
		
		Mob.holdAllies( Dungeon.level );

		SpecialRoom.resetPitRoom(Dungeon.depth+1);

		Level level = Dungeon.newLevel();
		Dungeon.switchLevel( level, level.entrance() );
	}
	
	@Override
	protected void onBackPressed() {
		//Do nothing
	}
}
