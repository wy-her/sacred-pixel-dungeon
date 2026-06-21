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
import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.CloudSave;
import com.sacredpixel.sacredpixeldungeon.Challenges;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.GamesInProgress;
import com.sacredpixel.sacredpixeldungeon.Leaderboard;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.effects.BannerSprites;
import com.sacredpixel.sacredpixeldungeon.scenes.InterlevelScene;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialManager;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.effects.Fireball;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.services.news.News;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.Archs;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.windows.WndOptions;
import com.sacredpixel.sacredpixeldungeon.windows.WndSettings;
import com.sacredpixel.sacredpixeldungeon.windows.WndVictoryCongrats;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.utils.Signal;
import com.watabou.utils.ColorMath;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.RectF;

import java.util.Date;

public class TitleScene extends PixelScene {

	private Image title;
	private Image titleGlow;

	private StyledButton btnTutorial;
	private StyledButton btnPlay;
	private StyledButton btnRankings;
	private StyledButton btnJournal;
	private StyledButton btnData;
	private StyledButton btnSettings;
	private StyledButton btnAbout;
	private StyledButton btnTestZone;

	private BitmapText version;
	private ExitButton btnExit;

	private Signal.Listener<PointerEvent> bgmListener;
	private Signal.Listener<KeyEvent> keyListener;

	// Keyboard navigation
	private java.util.ArrayList<StyledButton> focusableButtons = new java.util.ArrayList<>();
	private int focusIndex = -1;

	@Override
	public void create() {

		super.create();

		// Restore journal state if returning from tutorial
		TutorialManager.exitTutorial();

		// Cloud save: auto-load on fresh install (no local badges = new device)
		if (CloudSave.isAvailable()) {
			Badges.loadGlobal();
			boolean hasLocalData = false;
			for (Badges.Badge b : Badges.Badge.values()) {
				if (Badges.isUnlocked(b)) {
					hasLocalData = true;
					break;
				}
			}
			if (!hasLocalData) {
				CloudSave.loadAndMerge(null);
			}
		}

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;

		RectF insets = getCommonInsets();

		// Title background image (covers full screen, center-cropped)
		Image bg = new Image(Assets.Splashes.TITLE_BG);
		float bgScale = Math.max((float)w / bg.width, (float)h / bg.height);
		bg.scale.set(bgScale);
		bg.x = (w - bg.width * bgScale) / 2f;
		bg.y = (h - bg.height * bgScale) / 2f;
		add(bg);
		// Dark overlay to keep UI readable
		ColorBlock overlay = new ColorBlock(w, h, 0xAA000000);
		add(overlay);

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;

		float buttonAreaWidth = PixelScene.MIN_WIDTH_P+29;

		// Always use portrait sprite for consistent logo size across orientations
		float logoTargetWidth = (PixelScene.MIN_WIDTH_P - 2) * 0.7f;
		title = BannerSprites.get(BannerSprites.Type.TITLE_PORT);
		add( title );

		float origW = title.width();
		float origH = title.height();
		float titleScale = logoTargetWidth / origW;
		title.scale.set(titleScale);
		float scaledW = origW * titleScale;
		float scaledH = origH * titleScale;

		// Keep original topRegion for button placement
		float topRegion = Math.max(scaledH - 6, h*0.45f);

		// Position logo closer to buttons (just above button area)
		title.x = insets.left + (w - scaledW) / 2f;
		title.y = insets.top + topRegion - scaledH - 5;

		// Glow overlay on title logo (additive blending for glow effect)
		titleGlow = BannerSprites.get(BannerSprites.Type.TITLE_GLOW_PORT);
		titleGlow.scale.set(titleScale);
		titleGlow.x = title.x;
		titleGlow.y = title.y;
		titleGlow.alpha(0f);
		titleGlow.brightness(2f);
		add(titleGlow);

		align(title);

		// Fireball torches on both sides of the logo
		Fireball leftFB = new Fireball();
		Fireball rightFB = new Fireball();
		float fbNativeH = 61;
		float fbNativeW = 61;
		float targetFBH = scaledH * 0.9f;
		float fbScale = targetFBH / fbNativeH;
		leftFB.scale.set(fbScale);
		rightFB.scale.set(fbScale);
		float fbW = fbNativeW * fbScale;
		float fbH = fbNativeH * fbScale;
		float fbY = title.y + (scaledH - fbH) / 2f;
		float fbGap = -6;
		leftFB.x = title.x - fbW - fbGap;
		leftFB.y = fbY;
		rightFB.x = title.x + scaledW + fbGap;
		rightFB.y = fbY;
		add(leftFB);
		add(rightFB);

		final Chrome.Type GREY_TR = Chrome.Type.GREY_BUTTON_TR;
		final int TITLE_BTN_SIZE = 8;

		btnTutorial = new StyledButton(GREY_TR, Messages.get(this, "tutorial"), TITLE_BTN_SIZE){
			@Override
			protected void onClick() {
				GamesInProgress.selectedClass = com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.WARRIOR;
				GamesInProgress.curSlot = GamesInProgress.MAX_SLOTS;
				try {
					Dungeon.deleteGame(GamesInProgress.MAX_SLOTS, true);
				} catch (Exception e) { /* ignore */ }
				Dungeon.hero = null;
				InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
				InterlevelScene.curTransition = null;
				InterlevelScene.tutorialLevel = true;
				Game.switchScene(InterlevelScene.class);
			}
		};
		btnTutorial.icon(new ItemSprite(ItemSpriteSheet.MASTERY));
		add(btnTutorial);

		btnPlay = new StyledButton(GREY_TR, Messages.get(this, "enter"), TITLE_BTN_SIZE){
			@Override
			protected void onClick() {
				if (GamesInProgress.checkAll().size() == 0){
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					SacredPixelDungeon.switchScene(HeroSelectScene.class);
				} else {
					SacredPixelDungeon.switchNoFade( StartScene.class );
				}
			}
			
			@Override
			protected boolean onLongClick() {
				//making it easier to start runs quickly while debugging
				if (DeviceCompat.isDebug()) {
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					SacredPixelDungeon.switchScene(HeroSelectScene.class);
					return true;
				}
				return super.onLongClick();
			}
		};
		btnPlay.icon(Icons.get(Icons.ENTER));
		add(btnPlay);

		btnRankings = new StyledButton(GREY_TR, Messages.get(this, "rankings"), TITLE_BTN_SIZE){
			@Override
			protected void onClick() {
				SacredPixelDungeon.switchNoFade( RankingsScene.class );
			}
		};
		btnRankings.icon(Icons.get(Icons.RANKINGS));
		add(btnRankings);

		btnJournal = new StyledButton(GREY_TR, Messages.get(this, "journal"), TITLE_BTN_SIZE){
			@Override
			protected void onClick() {
				SacredPixelDungeon.switchNoFade( JournalScene.class );
			}
		};
		btnJournal.icon(Icons.get(Icons.JOURNAL));
		add(btnJournal);

		if (Leaderboard.isAvailable()) {
			// Appsintoss: Leaderboard button
			btnData = new StyledButton(GREY_TR, Messages.get(this, "leaderboard"), TITLE_BTN_SIZE){
				@Override
				protected void onClick() {
					Leaderboard.open();
				}
			};
			btnData.icon(Icons.get(Icons.CHANGES));
		} else {
			// Other platforms: Data export button (default)
			btnData = new StyledButton(GREY_TR, Messages.get(this, "data"), TITLE_BTN_SIZE){
				@Override
				protected void onClick() {
					SacredPixelDungeon.switchNoFade( DataScene.class );
				}
			};
			btnData.icon(Icons.get(Icons.CHANGES));
		}
		add(btnData);

		btnSettings = new SettingsButton(GREY_TR, Messages.get(this, "settings"), TITLE_BTN_SIZE);
		add(btnSettings);

		btnAbout = new StyledButton(GREY_TR, Messages.get(this, "about"), TITLE_BTN_SIZE){
			@Override
			protected void onClick() {
				SacredPixelDungeon.switchScene( AboutScene.class );
			}
		};
		Image sacredLogo = new Image("interfaces/sacred-logo.png");
		sacredLogo.scale.set(16f / Math.max(sacredLogo.width(), sacredLogo.height()));
		btnAbout.icon(sacredLogo);
		add(btnAbout);
		
		final int BTN_HEIGHT = 20;
		int GAP = (int)(h - topRegion - 5*BTN_HEIGHT)/5;  // 5 rows now (including tutorial)
		GAP /= 5;
		GAP = Math.max(GAP, 2);

		//Button area width is fixed, not extended by fireballs
		float btnAreaLeft = insets.left + (w - buttonAreaWidth) / 2f;
		float halfWidth = (buttonAreaWidth/2)-1;
		// Row 1: Play (full width)
		btnPlay.setRect(btnAreaLeft, insets.top + topRegion+GAP, buttonAreaWidth, BTN_HEIGHT);
		align(btnPlay);
		// Row 2: Tutorial (full width)
		btnTutorial.setRect(btnAreaLeft, btnPlay.bottom()+ GAP, buttonAreaWidth, BTN_HEIGHT);
		align(btnTutorial);
		// Row 3: Data (full width)
		btnData.setRect(btnAreaLeft, btnTutorial.bottom()+ GAP, buttonAreaWidth, BTN_HEIGHT);
		// Row 4: Rankings, Journal (half each)
		btnRankings.setRect(btnAreaLeft, btnData.bottom()+ GAP, halfWidth, BTN_HEIGHT);
		btnJournal.setRect(btnRankings.right()+2, btnRankings.top(), halfWidth, BTN_HEIGHT);
		// Row 5: Settings, About (half each)
		btnSettings.setRect(btnAreaLeft, btnRankings.bottom()+ GAP, halfWidth, BTN_HEIGHT);
		btnAbout.setRect(btnSettings.right()+2, btnSettings.top(), halfWidth, BTN_HEIGHT);

		// === TEST LEVEL BUTTON (Only visible when INDEV is in version string) ===
		if (DeviceCompat.isDebug() || Game.version.toUpperCase().contains("INDEV")) {
			btnTestZone = new StyledButton(GREY_TR, "Test Level"){
				@Override
				protected void onClick() {
					SacredPixelDungeon.scene().addToFront(new WndOptions(
						Messages.get(TitleScene.class, "test_zone"),
						Messages.get(TitleScene.class, "test_zone_desc"),
						HeroClass.WARRIOR.title(),
						HeroClass.MAGE.title(),
						HeroClass.ROGUE.title(),
						HeroClass.HUNTRESS.title(),
						HeroClass.DUELIST.title(),
						HeroClass.CLERIC.title()
					) {
						@Override
						protected void onSelect(int index) {
							HeroClass[] classes = {HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE, HeroClass.HUNTRESS, HeroClass.DUELIST, HeroClass.CLERIC};
							GamesInProgress.selectedClass = classes[index];
							GamesInProgress.curSlot = GamesInProgress.MAX_SLOTS;
							try {
								Dungeon.deleteGame(GamesInProgress.MAX_SLOTS, true);
							} catch (Exception e) { /* ignore */ }
							Dungeon.hero = null;
							InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
							InterlevelScene.curTransition = null;
							InterlevelScene.testLevel = true;
							Game.switchScene(InterlevelScene.class);
						}
					});
				}
			};
			btnTestZone.icon(Icons.get(Icons.WARNING));
			btnTestZone.textColor(0xFF4444);
			add(btnTestZone);
			btnTestZone.setRect(btnAreaLeft, btnAbout.bottom()+ GAP, buttonAreaWidth, BTN_HEIGHT);
		}

		version = new BitmapText( "v" + Game.version, pixelFont);
		version.measure();
		version.hardlight( 0x888888 );
		version.x = insets.left + w - version.width() - (DeviceCompat.isDesktop() ? 4 : 8);
		version.y = insets.top + h - version.height() - (DeviceCompat.isDesktop() ? 2 : 4);
		add( version );



		if (DeviceCompat.isDesktop()) {
			btnExit = new ExitButton();
			btnExit.setPos( w - btnExit.width(), 0 );
			add( btnExit );
		}

		Badges.loadGlobal();

		// Setup keyboard navigation (order matches button layout)
		// Row 1: btnPlay (full)
		// Row 2: btnTutorial (full)
		// Row 3: btnData (full)
		// Row 4: btnRankings, btnJournal (half each)
		// Row 5: btnSettings, btnAbout (half each)
		// Row 6: btnTestZone (debug only)
		focusableButtons.clear();
		focusableButtons.add(btnPlay);
		focusableButtons.add(btnTutorial);
		focusableButtons.add(btnData);
		focusableButtons.add(btnRankings);
		focusableButtons.add(btnJournal);
		focusableButtons.add(btnSettings);
		focusableButtons.add(btnAbout);
		if (btnTestZone != null) {
			focusableButtons.add(btnTestZone);
		}

		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

				// Don't handle keys if not on TitleScene
				if (!(Game.scene() instanceof TitleScene)) return false;

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
						StyledButton btn = focusableButtons.get(focusIndex);
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

		// Listen for any touch/click to start BGM (browser autoplay policy workaround)
		bgmListener = new Signal.Listener<PointerEvent>() {
			@Override
			public boolean onSignal(PointerEvent event) {
				if (event != null && !Music.INSTANCE.isPlaying()) {
					Music.INSTANCE.restartCurrentTracks();
				}
				return false; // don't consume, let UI elements handle it
			}
		};
		PointerEvent.addPointerListener(bgmListener);

		// Check for URL-based data import
		// If URL contains import data, redirect to DataScene to show import dialog
		checkForUrlImport();

		// Check for seed URL parameters (e.g., ?seed=ABC&class=warrior&challenges=15)
		checkForSeedUrl();

		fadeIn();
	}

	/**
	 * Checks if the current URL contains import data.
	 * If found, redirects to DataScene to show the import dialog.
	 */
	private void checkForUrlImport() {
		try {
			DataScene.DataService dataService = DataScene.getDataService();
			if (dataService != null) {
				boolean hasData = dataService.hasImportData();
				if (hasData) {
					// Delay the redirect slightly to let TitleScene render first
					Game.runOnRenderThread(() -> {
						try {
							SacredPixelDungeon.switchNoFade(DataScene.class);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			}
		} catch (Exception e) {
			// Catch any errors to prevent TitleScene from breaking
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the URL contains seed parameters (e.g., ?seed=ABC&class=warrior&challenges=15).
	 * If found, shows a dialog to start a new game with those settings.
	 */
	private void checkForSeedUrl() {
		try {
			DataScene.DataService dataService = DataScene.getDataService();
			if (dataService != null && dataService.hasSeedParams()) {
				String seedCode = dataService.getSeedParam();
				String classParam = dataService.getClassParam();
				int challenges = dataService.getChallengesParam();

				// Parse hero class
				HeroClass heroClass = HeroClass.WARRIOR;  // Default
				if (classParam != null) {
					try {
						heroClass = HeroClass.valueOf(classParam.toUpperCase());
					} catch (IllegalArgumentException e) {
						// Invalid class param, use default
					}
				}

				final HeroClass finalHeroClass = heroClass;
				final String finalSeedCode = seedCode;
				final int finalChallenges = challenges;

				// Clear the URL parameters
				dataService.clearSeedParams();

				// Show confirmation dialog
				Game.runOnRenderThread(() -> {
					String message = Messages.get(TitleScene.class, "seed_url_found",
							finalSeedCode, finalHeroClass.title(),
							finalChallenges > 0 ? String.valueOf(Challenges.activeChallenges(finalChallenges)) : "0");

					Game.scene().addToFront(new WndOptions(
							Messages.get(TitleScene.class, "seed_url_title"),
							message,
							Messages.get(TitleScene.class, "seed_url_start"),
							Messages.get(TitleScene.class, "seed_url_cancel")
					) {
						@Override
						protected void onSelect(int index) {
							if (index == 0) {
								startGameWithSeed(finalSeedCode, finalHeroClass, finalChallenges);
							}
						}
					});
				});
			}
		} catch (Exception e) {
			System.err.println("[TitleScene] Error checking for seed URL: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Starts a new game with the specified seed, class, and challenges.
	 */
	private void startGameWithSeed(String seedCode, HeroClass heroClass, int challenges) {
		// Find an empty slot
		int slot = -1;
		for (int i = 1; i <= GamesInProgress.MAX_SLOTS; i++) {
			if (GamesInProgress.check(i) == null) {
				slot = i;
				break;
			}
		}

		if (slot == -1) {
			// No empty slot, show message
			Game.scene().addToFront(new com.sacredpixel.sacredpixeldungeon.windows.WndMessage(
					Messages.get(TitleScene.class, "seed_url_no_slot")));
			return;
		}

		// Set up the game
		GamesInProgress.selectedClass = heroClass;
		GamesInProgress.curSlot = slot;
		Dungeon.challenges = challenges;

		// Set custom seed
		if (seedCode != null && !seedCode.isEmpty()) {
			SPDSettings.customSeed(seedCode);
		}

		// Start the game
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		SacredPixelDungeon.switchScene(InterlevelScene.class);
	}



	private static class NewsButton extends StyledButton {

		public NewsButton(Chrome.Type type, String label ){
			super(type, label);
			if (SPDSettings.news()) News.checkForNews();
		}

		int unreadCount = -1;

		@Override
		public void update() {
			super.update();

			if (unreadCount == -1 && News.articlesAvailable()){
				long lastRead = SPDSettings.newsLastRead();
				if (lastRead == 0){
					if (News.articles().get(0) != null) {
						SPDSettings.newsLastRead(News.articles().get(0).date.getTime());
					}
				} else {
					unreadCount = News.unreadArticles(new Date(SPDSettings.newsLastRead()));
					if (unreadCount > 0) {
						unreadCount = Math.min(unreadCount, 9);
						text(text() + "(" + unreadCount + ")");
					}
				}
			}

			if (unreadCount > 0){
				textColor(ColorMath.interpolate( 0xFFFFFF, Window.SHPX_COLOR, 0.5f + (float)Math.sin(Game.timeTotal*5)/2f));
			}
		}

		@Override
		protected void onClick() {
			super.onClick();
			SacredPixelDungeon.switchNoFade( NewsScene.class );
		}
	}

	private static class SettingsButton extends StyledButton {

		public SettingsButton( Chrome.Type type, String label, int size ){
			super(type, label, size);
			icon(Icons.get(Icons.PREFS));
		}

		@Override
		protected void onClick() {
			super.onClick();
			SacredPixelDungeon.switchNoFade(SettingsScene.class);
		}
	}

	@Override
	public void update() {
		super.update();
		if (titleGlow != null) {
			// Glow cycle: 0.75f = 2x slower than original 1.5f
			titleGlow.alpha((float)(0.1f + 0.4f * Math.pow(Math.sin(Game.timeTotal * 0.75f), 2)));
		}
	}

	@Override
	public void destroy() {
		if (bgmListener != null) {
			PointerEvent.removePointerListener(bgmListener);
			bgmListener = null;
		}
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		super.destroy();
	}

	private void moveFocus(int direction) {
		if (focusableButtons.isEmpty()) return;

		// Clear focus on previously focused button
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			focusableButtons.get(focusIndex).setFocused(false);
		}

		// Move index
		int startIndex;
		if (focusIndex == -1) {
			startIndex = direction > 0 ? 0 : focusableButtons.size() - 1;
		} else {
			startIndex = focusIndex + direction;
			if (startIndex < 0) startIndex = focusableButtons.size() - 1;
			if (startIndex >= focusableButtons.size()) startIndex = 0;
		}

		// Skip inactive buttons
		focusIndex = startIndex;
		int checked = 0;
		while (!focusableButtons.get(focusIndex).active && checked < focusableButtons.size()) {
			focusIndex += direction > 0 ? 1 : -1;
			if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
			if (focusIndex >= focusableButtons.size()) focusIndex = 0;
			checked++;
		}

		// Set focus on new button (uses background brightness, not text color)
		focusableButtons.get(focusIndex).setFocused(true);
	}

}
