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

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Challenges;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.GamesInProgress;
import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.journal.Journal;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.ui.ActionIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.CheckBox;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.Button;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.OptionSlider;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.utils.DungeonSeed;
import com.sacredpixel.sacredpixeldungeon.windows.WndChallenges;
import com.sacredpixel.sacredpixeldungeon.windows.WndHeroInfo;
import com.sacredpixel.sacredpixeldungeon.windows.WndKeyBindings;
import com.sacredpixel.sacredpixeldungeon.windows.WndMessage;
import com.sacredpixel.sacredpixeldungeon.windows.WndOptions;
import com.sacredpixel.sacredpixeldungeon.windows.WndTextInput;
import com.sacredpixel.sacredpixeldungeon.windows.WndTitledMessage;
import com.sacredpixel.sacredpixeldungeon.windows.WndVictoryCongrats;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.utils.Signal;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

import com.watabou.utils.DateCompat;

import java.util.ArrayList;

public class HeroSelectScene extends PixelScene {

	private Image background;
	private Image fadeLeft, fadeRight;
	private IconButton btnFade; //only on landscape

	//fading UI elements
	private RenderedTextBlock title;
	private ArrayList<StyledButton> heroBtns = new ArrayList<>();
	private RenderedTextBlock heroName; //only on landscape
	private RenderedTextBlock heroDesc; //only on landscape
	private StyledButton startBtn;
	private IconButton infoButton;
	private IconButton btnOptions;
	private GameOptions optionsPane;
	private PointerArea optionsPaneBlocker;
	private IconButton btnExit;

	private RectF insets;

	private static boolean heroWasRandomized = true;
	private static boolean chalWasRandomized = false;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private int heroFocusIndex = -1;

	// Multi-layer navigation system
	private enum NavigationLayer {
		HERO_BUTTONS,       // Layer 0: HeroBtn row
		ACTION_BUTTONS,     // Layer 1: Start/Options/Info buttons
		OPTIONS_PANEL       // Layer 2: Inside options panel
	}
	private NavigationLayer currentLayer = NavigationLayer.HERO_BUTTONS;
	private ArrayList<Button> actionButtons = new ArrayList<>();
	private int actionButtonFocusIndex = -1;
	private int optionsPaneFocusIndex = -1;

	@Override
	public void create() {
		super.create();
		SPDSettings.customSeed("");

		Dungeon.hero = null;

		Badges.loadGlobal();
		Journal.loadGlobal();

		insets = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK).scale(1f/defaultZoom);

		float w = (Camera.main.width - insets.left - insets.right);
		float h = (Camera.main.height - insets.top - insets.bottom);

		background = new Image(TextureCache.createSolid(0xFF2d2f31), 0, 0, 800, 450){
			@Override
			public void update() {
				if (GamesInProgress.selectedClass != null) {
					if (rm > 1f) {
						rm -= Game.elapsed;
						gm = bm = rm;
					} else {
						rm = gm = bm = 1;
					}
				}
			}
		};
		background.scale.set(Camera.main.height/background.height);

		background.x = (Camera.main.width - background.width())/2f;
		background.y = (Camera.main.height - background.height())/2f;
		PixelScene.align(background);
		add(background);

		fadeLeft = new Image(TextureCache.createGradient(0xFF000000, 0xFF000000, 0x00000000));
		fadeLeft.x = background.x-2;
		fadeLeft.scale.set(3, background.height());
		add(fadeLeft);

		fadeRight = new Image(fadeLeft);
		fadeRight.x = background.x + background.width() + 2;
		fadeRight.y = background.y + background.height();
		fadeRight.angle = 180;
		add(fadeRight);

		title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
		title.hardlight(Window.TITLE_COLOR);
		PixelScene.align(title);
		add(title);

		startBtn = new StyledButton(Chrome.Type.GREY_BUTTON_TR, ""){
			@Override
			protected void onClick() {
				super.onClick();

				if (hasOpenWindows()) return;
				if (optionsPane != null && optionsPane.visible) return;
				if (GamesInProgress.selectedClass == null) return;

				Dungeon.hero = null;
				SPDSettings.customSeed("");
				Dungeon.initSeed();

				ActionIndicator.clearAction();
				InterlevelScene.mode = InterlevelScene.Mode.DESCEND;

				Game.switchScene( InterlevelScene.class );
			}
		};
		startBtn.icon(Icons.get(Icons.ENTER));
		startBtn.setSize(80, 21);
		startBtn.textColor(Window.TITLE_COLOR);
		add(startBtn);
		startBtn.visible = startBtn.active = false;

		infoButton = new IconButton(Icons.get(Icons.INFO)){
			@Override
			protected void onClick() {
				super.onClick();
				if (hasOpenWindows()) return;
				if (optionsPane != null && optionsPane.visible) return;
				HeroClass cls = GamesInProgress.selectedClass;
				if (cls != null) {
					Window info = new WndHeroInfo(GamesInProgress.selectedClass);
					SacredPixelDungeon.scene().addToFront(info);
				}
			}

			@Override
			protected String hoverText() {
				return Messages.titleCase(Messages.get(WndKeyBindings.class, "hero_info"));
			}
		};
		infoButton.visible = infoButton.active = false;
		infoButton.setSize(20, 21);
		add(infoButton);

		for (HeroClass cl : HeroClass.values()){
			HeroBtn button = new HeroBtn(cl);
			add(button);
			heroBtns.add(button);
		}

		// Blocker for options panel - closes panel when clicking outside
		optionsPaneBlocker = new PointerArea(0, 0, Camera.main.width, Camera.main.height) {
			@Override
			protected void onClick(PointerEvent event) {
				if (optionsPane != null && optionsPane.visible) {
					// Check if click is outside options panel bounds
					float x = event.current.x;
					float y = event.current.y;
					if (x < optionsPane.left() || x > optionsPane.right() ||
						y < optionsPane.top() || y > optionsPane.bottom()) {
						// Also check if click is on btnOptions (toggle button)
						if (btnOptions != null &&
							x >= btnOptions.left() && x <= btnOptions.right() &&
							y >= btnOptions.top() && y <= btnOptions.bottom()) {
							// Let btnOptions handle it
							return;
						}
						// Close the options panel
						optionsPane.visible = optionsPane.active = false;
						optionsPaneBlocker.active = false;
					}
				}
			}
		};
		optionsPaneBlocker.active = false;
		add(optionsPaneBlocker);

		optionsPane = new GameOptions();
		optionsPane.visible = optionsPane.active = false;
		optionsPane.layout();
		add(optionsPane);

		btnOptions = new IconButton(Icons.get(Icons.PREFS)){
			@Override
			protected void onClick() {
				super.onClick();
				if (hasOpenWindows()) return;
				optionsPane.visible = !optionsPane.visible;
				optionsPane.active = !optionsPane.active;
				optionsPaneBlocker.active = optionsPane.visible;
				// Change layer when opening/closing options panel via mouse click
				if (optionsPane.visible) {
					clearActionButtonFocus();
					currentLayer = NavigationLayer.OPTIONS_PANEL;
					optionsPaneFocusIndex = -1;
				} else {
					currentLayer = NavigationLayer.ACTION_BUTTONS;
					actionButtonFocusIndex = 0;
				}
			}

			@Override
			protected void onPointerDown() {
				super.onPointerDown();
			}

			@Override
			protected void onPointerUp() {
				updateOptionsColor();
			}

			@Override
			protected String hoverText() {
				return Messages.get(HeroSelectScene.class, "options");
			}
		};
		updateOptionsColor();
		btnOptions.visible = false;

		if(!SPDSettings.intro()){
			add(btnOptions);
		}

		// Build action buttons array for Layer 1 navigation
		// Order: Options (left), Start (center), Info (right)
		actionButtons.clear();
		actionButtons.add(btnOptions);
		actionButtons.add(startBtn);
		actionButtons.add(infoButton);

		if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
			Dungeon.challenges = 0;
			SPDSettings.challenges(0);
			SPDSettings.customSeed("");
		}

		{
			background.visible = false;

			int btnWidth = HeroBtn.MIN_WIDTH;

			float curX = insets.left + (w - btnWidth * heroBtns.size()) / 2f;
			if (curX > 0) {
				btnWidth += Math.min(curX / (heroBtns.size() / 2f), 15);
				curX = insets.left + (w - btnWidth * heroBtns.size()) / 2f;
			}
			float curY = insets.top + h - HeroBtn.HEIGHT - 8;

			for (StyledButton button : heroBtns) {
				button.setRect(curX, curY, btnWidth, HeroBtn.HEIGHT + insets.bottom);
				curX += btnWidth;
			}

			//add a darkening bar along bottom
			if (insets.bottom > 0){
				SkinnedBlock bar = new SkinnedBlock(Camera.main.width, insets.bottom, TextureCache.createSolid(0xAA000000));
				bar.y = h + insets.top;
				add(bar);

				PointerArea blocker = new PointerArea(0, Camera.main.width - insets.bottom, Camera.main.width, insets.bottom);
				add(blocker);
			}

			title.setPos(insets.left + (w - title.width()) / 2f, insets.top + (h - HeroBtn.HEIGHT - title.height() - 20));

			btnOptions.setRect(heroBtns.get(0).left() + 16, Camera.main.height-HeroBtn.HEIGHT-27, 20, 21);
			optionsPane.setPos(heroBtns.get(0).left(), 0);
		}

		btnExit = new ExitButton(){
			@Override
			protected void onClick() {
				if (hasOpenWindows()) return;
				if (optionsPane != null && optionsPane.visible) {
					optionsPane.visible = optionsPane.active = false;
					optionsPaneBlocker.active = false;
					currentLayer = NavigationLayer.ACTION_BUTTONS;
					return;
				}
				super.onClick();
			}
			@Override
			protected void onPointerDown() {
				// Don't show click effect when options panel is open
				if (optionsPane != null && optionsPane.visible) {
					return;
				}
				super.onPointerDown();
			}
			@Override
			public GameAction keyAction() {
				// Don't respond to ESC key when options panel is open
				if (optionsPane != null && optionsPane.visible) {
					return null;
				}
				return super.keyAction();
			}
			@Override
			protected String hoverText() {
				// Don't show tooltip when options panel is open
				if (optionsPane != null && optionsPane.visible) {
					return null;
				}
				return super.hoverText();
			}
		};
		int ofs = 4;
		btnExit.setPos( Camera.main.width - btnExit.width() - ofs, ofs );
		add( btnExit );
		btnExit.visible = btnExit.active = true;

		PointerArea fadeResetter = new PointerArea(0, 0, Camera.main.width, Camera.main.height){
			@Override
			public boolean onSignal(PointerEvent event) {
				if (event != null && event.type == PointerEvent.Type.UP){
					resetFade();
				}
				return false;
			}
		};
		add(fadeResetter);
		resetFade();

		if (GamesInProgress.selectedClass != null){
			setSelectedHero(GamesInProgress.selectedClass);
		}

		if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		// Setup keyboard navigation with multi-layer system
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

				// Don't handle keys if a window is open
				for (Object v : members) {
					if (v instanceof Window) return false;
				}

				GameAction action = KeyBindings.getActionForKey(event);

				switch (currentLayer) {
					case HERO_BUTTONS:
						return handleHeroButtonsNavigation(action, event);
					case ACTION_BUTTONS:
						return handleActionButtonsNavigation(action, event);
					case OPTIONS_PANEL:
						return handleOptionsPanelNavigation(action, event);
				}
				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);

		// Initialize focus if a hero is already selected
		if (GamesInProgress.selectedClass != null) {
			HeroClass[] classes = HeroClass.values();
			for (int i = 0; i < classes.length; i++) {
				if (classes[i] == GamesInProgress.selectedClass) {
					heroFocusIndex = i;
					break;
				}
			}
		}

		fadeIn();

	}

	private void updateOptionsColor(){
		if (!SPDSettings.customSeed().isEmpty()){
			btnOptions.icon().hardlight(1f, 1.5f, 0.67f);
		} else if (SPDSettings.challenges() != 0){
			btnOptions.icon().hardlight(2f, 1.33f, 0.5f);
		} else {
			btnOptions.icon().resetColor();
		}
	}

	private void setSelectedHero(HeroClass cl){
		GamesInProgress.selectedClass = cl;
		GamesInProgress.randomizedClass = false;

		try {
			//loading these big jpgs fails sometimes, so we have a catch for it
			background.texture(cl.splashArt());
		} catch (Exception e){
			Game.reportException(e);
			background.texture(TextureCache.createSolid(0xFF2d2f31));
			background.frame(0, 0, 800, 450);
		}
		background.visible = true;
		background.hardlight(1.5f,1.5f,1.5f);

		float leftPortion = Math.max(100, (Camera.main.width - insets.left - insets.right)/3f);

		{
			title.visible = false;

			startBtn.visible = startBtn.active = true;
			startBtn.text(Messages.titleCase(cl.title()));
			startBtn.setSize(startBtn.reqWidth() + 8, 21);

			startBtn.setPos((Camera.main.width - startBtn.width())/2f, (Camera.main.height - insets.bottom - HeroBtn.HEIGHT - 14 - startBtn.height()));
			PixelScene.align(startBtn);

			infoButton.visible = infoButton.active = true;
			infoButton.setPos(startBtn.right(), startBtn.top());

			btnOptions.visible = btnOptions.active = !SPDSettings.intro();
			btnOptions.setPos(startBtn.left()-btnOptions.width(), startBtn.top());

			optionsPane.setPos(heroBtns.get(0).left(), startBtn.top() - optionsPane.height() - 2);
			align(optionsPane);
		}

		updateOptionsColor();
	}

	private float uiAlpha;

	@Override
	public void update() {
		super.update();
		if (SPDSettings.intro() && Rankings.INSTANCE.totalNumber > 0){
			SPDSettings.intro(false);
		}
		btnExit.visible = btnExit.active = true;
		//do not fade when a window is open
		for (Object v : members){
			if (v instanceof Window) resetFade();
		}
		if (GamesInProgress.selectedClass != null) {
			//fadeout disabled - keep UI always visible
			updateFade();
		}
	}

	private void updateFade(){
		float alpha = GameMath.gate(0f, uiAlpha, 1f);
		title.alpha(alpha);
		for (StyledButton b : heroBtns){
			b.enable(alpha != 0);
			b.alpha(alpha);
		}
		startBtn.enable(alpha != 0);
		startBtn.alpha(alpha);
		btnExit.enable(btnExit.visible && alpha != 0);
		btnExit.icon().alpha(alpha);
		optionsPane.active = optionsPane.visible && alpha != 0;
		optionsPane.alpha(alpha);
		btnOptions.enable(alpha != 0);
		btnOptions.icon().alpha(alpha);
		infoButton.enable(alpha != 0);
		infoButton.icon().alpha(alpha);

		fadeLeft.x = background.x-5;
		fadeRight.x = background.x + background.width() + 5;

		fadeLeft.visible = background.x > 0;
		fadeRight.visible = background.x + background.width() < Camera.main.width;
	}

	private void resetFade(){
		//starts fading after 4 seconds, fades over 4 seconds.
		uiAlpha = 2f;
		updateFade();
	}

	@Override
	protected void onBackPressed() {
		SacredPixelDungeon.switchScene(TitleScene.class);
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		super.destroy();
	}

	private void moveHeroFocus(int direction) {
		if (heroBtns.isEmpty()) return;

		// Move index
		if (heroFocusIndex == -1) {
			heroFocusIndex = direction > 0 ? 0 : heroBtns.size() - 1;
		} else {
			heroFocusIndex += direction;
			if (heroFocusIndex < 0) heroFocusIndex = heroBtns.size() - 1;
			if (heroFocusIndex >= heroBtns.size()) heroFocusIndex = 0;
		}

		// Allow focusing on all heroes (including locked ones)
		// Update visual focus for the hero button
		for (int i = 0; i < heroBtns.size(); i++) {
			HeroBtn btn = (HeroBtn) heroBtns.get(i);
			btn.setKeyboardFocused(i == heroFocusIndex);
		}

		// Only select unlocked heroes
		HeroClass[] classes = HeroClass.values();
		if (heroFocusIndex < classes.length && classes[heroFocusIndex].isUnlocked()) {
			setSelectedHero(classes[heroFocusIndex]);
		}
	}

	// ===== Layer 0: HERO_BUTTONS Navigation =====
	private boolean handleHeroButtonsNavigation(GameAction action, KeyEvent event) {
		// Left navigation
		if (action == SPDAction.W || action == SPDAction.NW || action == SPDAction.SW) {
			moveHeroFocus(-1);
			return true;
		}
		// Right navigation
		if (action == SPDAction.E || action == SPDAction.NE || action == SPDAction.SE) {
			moveHeroFocus(1);
			return true;
		}
		// Up/Down - do nothing (no layer transition, layer change via Enter/ESC only)
		if (action == SPDAction.N || action == SPDAction.S) {
			return true; // Consume but ignore
		}
		// Enter - handle based on focused hero state
		if (event.code == com.badlogic.gdx.Input.Keys.ENTER
				|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
			if (heroFocusIndex >= 0 && heroFocusIndex < heroBtns.size()) {
				HeroBtn focusedBtn = (HeroBtn) heroBtns.get(heroFocusIndex);

				if (!focusedBtn.cl.isUnlocked()) {
					// Locked hero - show unlock message
					com.watabou.noosa.audio.Sample.INSTANCE.play(com.sacredpixel.sacredpixeldungeon.Assets.Sounds.CLICK);
					focusedBtn.click();
				} else if (focusedBtn.cl == GamesInProgress.selectedClass && startBtn.visible) {
					// Focused hero is the selected hero - go to Layer 1
					currentLayer = NavigationLayer.ACTION_BUTTONS;
					actionButtonFocusIndex = 1; // startBtn
					updateActionButtonFocus();
				} else {
					// Focused hero is unlocked but not selected - select it
					com.watabou.noosa.audio.Sample.INSTANCE.play(com.sacredpixel.sacredpixeldungeon.Assets.Sounds.CLICK);
					focusedBtn.click();
				}
			}
			return true;
		}
		return false;
	}

	// ===== Layer 1: ACTION_BUTTONS Navigation =====
	private boolean handleActionButtonsNavigation(GameAction action, KeyEvent event) {
		// Left navigation
		if (action == SPDAction.W || action == SPDAction.NW || action == SPDAction.SW) {
			moveActionButtonFocus(-1);
			return true;
		}
		// Right navigation
		if (action == SPDAction.E || action == SPDAction.NE || action == SPDAction.SE) {
			moveActionButtonFocus(1);
			return true;
		}
		// Up/Down - do nothing (no layer transition, layer change via Enter/ESC only)
		if (action == SPDAction.N || action == SPDAction.S) {
			return true; // Consume but ignore
		}
		// ESC/Back - return to HERO_BUTTONS layer (hierarchical navigation)
		if (action == SPDAction.BACK) {
			clearActionButtonFocus();
			currentLayer = NavigationLayer.HERO_BUTTONS;
			return true;
		}
		// Enter - activate focused button
		if (event.code == com.badlogic.gdx.Input.Keys.ENTER
				|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
			if (actionButtonFocusIndex >= 0 && actionButtonFocusIndex < actionButtons.size()) {
				Button btn = actionButtons.get(actionButtonFocusIndex);
				if (btn.active && btn.visible) {
					com.watabou.noosa.audio.Sample.INSTANCE.play(com.sacredpixel.sacredpixeldungeon.Assets.Sounds.CLICK);

					// Special handling for options button - enter OPTIONS_PANEL layer
					if (btn == btnOptions) {
						if (!optionsPane.visible) {
							btnOptions.click(); // Toggle optionsPane visibility
						}
						clearActionButtonFocus(); // Clear Layer 1 focus
						currentLayer = NavigationLayer.OPTIONS_PANEL;
						optionsPaneFocusIndex = -1; // No initial focus - must navigate to focus
					} else {
						btn.click();
					}
				}
			}
			return true;
		}
		return false;
	}

	// ===== Layer 2: OPTIONS_PANEL Navigation =====
	private boolean handleOptionsPanelNavigation(GameAction action, KeyEvent event) {
		ArrayList<StyledButton> panelButtons = optionsPane.buttons;

		// Up/Left navigation
		if (action == SPDAction.N || action == SPDAction.W) {
			moveOptionsPanelFocus(-1);
			return true;
		}
		// Down/Right navigation
		if (action == SPDAction.S || action == SPDAction.E) {
			moveOptionsPanelFocus(1);
			return true;
		}
		// ESC/Back - return to ACTION_BUTTONS layer and close options panel
		if (action == SPDAction.BACK) {
			clearOptionsPanelFocus();
			// Close the options panel
			if (optionsPane.visible) {
				optionsPane.visible = false;
				optionsPane.active = false;
				optionsPaneBlocker.active = false;
			}
			currentLayer = NavigationLayer.ACTION_BUTTONS;
			actionButtonFocusIndex = 0; // Focus on options button
			updateActionButtonFocus();
			return true;
		}
		// Enter - activate focused option
		if (event.code == com.badlogic.gdx.Input.Keys.ENTER
				|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
			if (optionsPaneFocusIndex >= 0 && optionsPaneFocusIndex < panelButtons.size()) {
				StyledButton btn = panelButtons.get(optionsPaneFocusIndex);
				if (btn.active) {
					com.watabou.noosa.audio.Sample.INSTANCE.play(com.sacredpixel.sacredpixeldungeon.Assets.Sounds.CLICK);
					btn.click();
				}
			}
			return true;
		}
		return false;
	}

	// ===== Focus Management Helpers =====
	private void clearActionButtonFocus() {
		if (actionButtonFocusIndex >= 0 && actionButtonFocusIndex < actionButtons.size()) {
			Button btn = actionButtons.get(actionButtonFocusIndex);
			btn.setFocused(false);
			// Restore btnOptions color after unfocusing
			if (btn == btnOptions) {
				updateOptionsColor();
			}
		}
		actionButtonFocusIndex = -1;
	}

	private void updateActionButtonFocus() {
		if (actionButtonFocusIndex >= 0 && actionButtonFocusIndex < actionButtons.size()) {
			Button btn = actionButtons.get(actionButtonFocusIndex);
			if (btn.visible && btn.active) {
				btn.setFocused(true);
			}
		}
	}

	private void moveActionButtonFocus(int direction) {
		if (actionButtons.isEmpty()) return;

		// Clear current focus visual
		if (actionButtonFocusIndex >= 0 && actionButtonFocusIndex < actionButtons.size()) {
			Button btn = actionButtons.get(actionButtonFocusIndex);
			btn.setFocused(false);
			// Restore btnOptions color after unfocusing
			if (btn == btnOptions) {
				updateOptionsColor();
			}
		}

		// Initialize if needed
		if (actionButtonFocusIndex == -1) {
			actionButtonFocusIndex = 1; // Start with startBtn (center)
		}

		// Move index first, then check if valid
		int startIndex = actionButtonFocusIndex;
		int checked = 0;
		do {
			actionButtonFocusIndex += direction;
			if (actionButtonFocusIndex < 0) actionButtonFocusIndex = actionButtons.size() - 1;
			if (actionButtonFocusIndex >= actionButtons.size()) actionButtonFocusIndex = 0;
			checked++;

			// Check if current button is valid (visible and active)
			Button btn = actionButtons.get(actionButtonFocusIndex);
			if (btn.visible && btn.active) {
				break; // Found valid button
			}
		} while (checked < actionButtons.size());

		updateActionButtonFocus();
	}

	private void clearOptionsPanelFocus() {
		ArrayList<StyledButton> panelButtons = optionsPane.buttons;
		if (optionsPaneFocusIndex >= 0 && optionsPaneFocusIndex < panelButtons.size()) {
			panelButtons.get(optionsPaneFocusIndex).setFocused(false);
		}
		optionsPaneFocusIndex = -1;
	}

	private void updateOptionsPanelFocus() {
		ArrayList<StyledButton> panelButtons = optionsPane.buttons;
		if (optionsPaneFocusIndex >= 0 && optionsPaneFocusIndex < panelButtons.size()) {
			panelButtons.get(optionsPaneFocusIndex).setFocused(true);
		}
	}

	private void moveOptionsPanelFocus(int direction) {
		ArrayList<StyledButton> panelButtons = optionsPane.buttons;
		if (panelButtons.isEmpty()) return;

		// Clear visual focus of current button (without resetting index)
		if (optionsPaneFocusIndex >= 0 && optionsPaneFocusIndex < panelButtons.size()) {
			panelButtons.get(optionsPaneFocusIndex).setFocused(false);
		}

		// Initialize if needed (first navigation after entering Layer 2)
		if (optionsPaneFocusIndex == -1) {
			// Start from first button if going down/right, last button if going up/left
			optionsPaneFocusIndex = direction > 0 ? 0 : panelButtons.size() - 1;
		} else {
			// Move from current position
			optionsPaneFocusIndex += direction;
			if (optionsPaneFocusIndex < 0) optionsPaneFocusIndex = panelButtons.size() - 1;
			if (optionsPaneFocusIndex >= panelButtons.size()) optionsPaneFocusIndex = 0;
		}

		updateOptionsPanelFocus();
	}

	private class HeroBtn extends StyledButton {

		private HeroClass cl;
		private boolean keyboardFocused = false;

		private static final int MIN_WIDTH = 20;
		private static final int HEIGHT = 24;

		HeroBtn ( HeroClass cl ){
			super(Chrome.Type.GREY_BUTTON_TR, "");

			this.cl = cl;

			icon(new Image(cl.spritesheet(), 0, 90, 12, 15));

		}

		public void setKeyboardFocused(boolean focused) {
			this.keyboardFocused = focused;
			// Use StyledButton's setFocused for button background effect
			super.setFocused(focused);
			// For locked heroes, keep the icon dark even when focused
			if (focused && !cl.isUnlocked()) {
				icon.brightness(0.1f);
			}
		}

		@Override
		public void update() {
			super.update();
			// For keyboard focused locked hero, keep icon dark
			if (keyboardFocused && !cl.isUnlocked()) {
				icon.brightness(0.1f);
				return;
			}
			// Skip normal brightness update if keyboard focused (StyledButton handles it)
			if (keyboardFocused) {
				return;
			}
			if (cl != GamesInProgress.selectedClass){
				if (!cl.isUnlocked()){
					icon.brightness(0.1f);
				} else {
					icon.brightness(0.6f);
				}
			} else {
				icon.brightness(1f);
			}
		}

		@Override
		protected void onClick() {
			super.onClick();
			if (hasOpenWindows()) return;
			if (optionsPane != null && optionsPane.visible) return;

			if( !cl.isUnlocked() ){
				SacredPixelDungeon.scene().addToFront( new WndMessage(cl.unlockMsg()));
			} else if (GamesInProgress.selectedClass == cl) {
				Window w = new WndHeroInfo(cl);
				SacredPixelDungeon.scene().addToFront(w);
			} else {
				setSelectedHero(cl);
			}
		}

		@Override
		protected void layout() {
			super.layout();
			//if we're super tall (i.e. rendering into display inset) then put hero at the top
			if (height > 30) {
				icon.y = y + (HEIGHT - icon.height()) / 2f;
			}
		}
	}

	private class GameOptions extends Component {

		private static final int BTN_HEIGHT = 16;

		private NinePatch bg;

		ArrayList<StyledButton> buttons; // Package-private for keyboard navigation access
		private ArrayList<ColorBlock> spacers;

		protected StyledButton challengeButton;

		@Override
		protected void createChildren() {

			bg = Chrome.get(Chrome.Type.GREY_BUTTON_TR);
			add(bg);

			buttons = new ArrayList<>();
			spacers = new ArrayList<>();


			challengeButton = new StyledButton(Chrome.Type.BLANK, Messages.get(WndChallenges.class, "title"), 6){
				@Override
				protected void onClick() {
					if (hasOpenWindows()) return;
					if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
						SacredPixelDungeon.scene().addToFront( new WndTitledMessage(
								Icons.get(Icons.CHALLENGE_GREY),
								Messages.get(WndChallenges.class, "title"),
								Messages.get(HeroSelectScene.class, "challenges_nowin")
						));
						return;
					}

					SacredPixelDungeon.scene().addToFront(new WndChallenges(SPDSettings.challenges(), true) {
						public void onBackPressed() {
							super.onBackPressed();
							icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
							updateOptionsColor();
						}
					} );
				}
			};
			challengeButton.leftJustify = true;
			challengeButton.icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
			add(challengeButton);
			buttons.add(challengeButton);

			int unlockedCount = 0;
			for (HeroClass cls : HeroClass.values()){
				if (cls.isUnlocked()) unlockedCount++;
			}

			if (unlockedCount >= 2) {
				StyledButton randomButton = new StyledButton(Chrome.Type.BLANK, Messages.get(HeroSelectScene.class, "randomize"), 6) {
					@Override
					protected void onClick() {
						if (hasOpenWindows()) return;

						if (Badges.isUnlocked(Badges.Badge.VICTORY) || DeviceCompat.isDebug()){
							SacredPixelDungeon.scene().addToFront(new WndRandomize());
						} else {

							HeroClass randomCls;
							do {
								randomCls = Random.oneOf(HeroClass.values());
							} while (!randomCls.isUnlocked());
							setSelectedHero(randomCls);
							GamesInProgress.randomizedClass = true;
						}
					}
				};
				randomButton.leftJustify = true;
				randomButton.icon(Icons.SHUFFLE.get());
				buttons.add(randomButton);
				add(randomButton);
			}

			for (int i = 1; i < buttons.size(); i++){
				ColorBlock spc = new ColorBlock(1, 1, 0xFF000000);
				add(spc);
				spacers.add(spc);
			}
		}

		private class WndRandomize extends Window {

			CheckBox chkHero;
			CheckBox chkChals;
			OptionSlider optChals;

			public WndRandomize(){
				super();

				int width = 149;
				RenderedTextBlock rndTitle = PixelScene.renderTextBlock(Messages.get(HeroSelectScene.class, "randomize"), 8);
				rndTitle.hardlight(TITLE_COLOR);
				rndTitle.setPos((width - rndTitle.width()) / 2f, 2);
				PixelScene.align(rndTitle);
				add(rndTitle);

				float titleBottom = rndTitle.bottom() + 4;

				chkHero = new CheckBox(Messages.get(HeroSelectScene.class, "randomize_hero")){
					@Override
					public void checked(boolean value) {
						super.checked(value);
						heroWasRandomized = value;
					}
				};
				chkHero.setRect(0, titleBottom, width, BTN_HEIGHT);
				chkHero.checked(heroWasRandomized);
				add(chkHero);

				chkChals = new CheckBox(Messages.get(HeroSelectScene.class, "randomize_chals")){
					@Override
					public void checked(boolean value) {
						super.checked(value);
						optChals.enable(value);
						chalWasRandomized = value;
					}
				};
				chkChals.setRect(0, titleBottom + 20, width, BTN_HEIGHT);
				add(chkChals);

				int max = Challenges.MAX_CHALS;
				optChals = new OptionSlider(Messages.get(HeroSelectScene.class, "randomize_chals_title"), "0", Integer.toString(max), 0, max) {
					@Override
					protected void onChange() {
						//do nothing immediately
					}
				};
				optChals.enable(false);
				optChals.setSelectedValue(Challenges.activeChallenges(SPDSettings.challenges()));
				optChals.setRect(0, titleBottom + 38, width, 28);
				add(optChals);

				chkChals.checked(chalWasRandomized);

				RedButton btnCancel = new RedButton(Messages.get(HeroSelectScene.class, "randomize_cancel")){
					@Override
					protected void onClick() {
						super.onClick();
						hide();
					}
				};
				btnCancel.setRect(width/2 + 1, titleBottom + 70, width/2 - 1, BTN_HEIGHT);
				add(btnCancel);

				RedButton btnConfirm = new RedButton(Messages.get(HeroSelectScene.class, "randomize_confirm")){
					@Override
					protected void onClick() {
						super.onClick();
						hide();

						if (chkChals.checked()){
							int chals = optChals.getSelectedValue();
							ArrayList<Integer> chalMasks = new ArrayList<>();
							for (int i = 0; i < Challenges.MAX_CHALS; i++){
								chalMasks.add((int)Math.pow(2, i));
							}
							Random.shuffle(chalMasks);
							int mask = 0;
							for (int i = 0; i < chals; i++){
								mask += chalMasks.remove(0);
							}
							SPDSettings.challenges(mask);
							challengeButton.icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
							SacredPixelDungeon.scene().addToFront(new WndChallenges(mask, false));
						}

						if (chkHero.checked()){
							HeroClass randomCls;
							do {
								randomCls = Random.oneOf(HeroClass.values());
							} while (!randomCls.isUnlocked());
							setSelectedHero(randomCls);
							GamesInProgress.randomizedClass = true;
						} else {
							setSelectedHero(GamesInProgress.selectedClass);
						}
					}
				};
				btnConfirm.setRect(0, titleBottom + 70, width/2 - 1, BTN_HEIGHT);
				add(btnConfirm);

				resize(width, (int)btnConfirm.bottom());

				// Add buttons for keyboard navigation
				addFocusable(chkHero);
				addFocusable(chkChals);
				addFocusable(optChals);
				addFocusable(btnConfirm);
				addFocusable(btnCancel);
			}

		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;

			int width = 0;
			for (StyledButton btn : buttons){
				if (width < btn.reqWidth()) width = (int)btn.reqWidth();
			}
			width += bg.marginHor();

			int top = (int)y + bg.marginTop() - 1;
			int i = 0;
			for (StyledButton btn : buttons){
				btn.setRect(x+bg.marginLeft(), top, width - bg.marginHor(), BTN_HEIGHT);
				top = (int)btn.bottom();
				if (i < spacers.size()) {
					spacers.get(i).size(btn.width(), 1);
					spacers.get(i).x = btn.left();
					spacers.get(i).y = PixelScene.align(btn.bottom()-0.5f);
					i++;
				}
			}

			this.width = width;
			this.height = top+bg.marginBottom()-y-1;
			bg.size(this.width, this.height);

		}

		private void alpha( float value ){
			bg.alpha(value);

			for (StyledButton btn : buttons){
				btn.alpha(value);
			}

			for (ColorBlock spc : spacers){
				spc.alpha(value);
			}
		}
	}

}
