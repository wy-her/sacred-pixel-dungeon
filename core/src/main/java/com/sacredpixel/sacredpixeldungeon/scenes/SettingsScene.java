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

import com.badlogic.gdx.Input;
import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.messages.Languages;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.services.news.News;
import com.sacredpixel.sacredpixeldungeon.services.updates.Updates;
import com.sacredpixel.sacredpixeldungeon.ui.CheckBox;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.Focusable;
import com.sacredpixel.sacredpixeldungeon.ui.GameLog;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.OptionSlider;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.ui.Toolbar;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.sacredpixel.sacredpixeldungeon.windows.WndKeyBindings;
import com.watabou.input.ControllerHandler;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Settings scene that embeds settings content directly (like JournalScene).
 * Content is part of the scene, not a floating window.
 */
public class SettingsScene extends PixelScene {

	public static final int WIDTH_P = 149;
	public static final int WIDTH_L = 251;

	private static final int SLIDER_HEIGHT = 28;
	private static final int BTN_HEIGHT = 16;
	private static final float GAP = 1;
	private static final int TEXT_SIZE = 7;

	private static int lastIDX = 0;
	private static int lastFocusIndex = -1; // Preserve focus across scene reset
	private static String lastModifiedItem = null; // Track which item triggered scene reset

	private Signal.Listener<KeyEvent> keyListener;
	private ArrayList<StyledButton> tabButtons = new ArrayList<>();

	// Keyboard focus navigation for settings items (mirroring Window's focus system)
	private ArrayList<Focusable> focusableItems = new ArrayList<>();
	private int focusIndex = -1;

	// Content tabs
	private DisplayTab displayTab;
	private UITab uiTab;
	private InputTab inputTab;
	private AudioTab audioTab;
	private LangsTab langsTab;

	@Override
	public void create() {
		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;

		RectF insets = getCommonInsets();

		ColorBlock BG = new ColorBlock(w, h, 0xFF000000);
		// BG added later

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;

		float top = 20;

		IconTitle title = new IconTitle(Icons.PREFS.get(), Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (top - title.height()) / 2f
		);
		align(title);
		add(title);

		NinePatch panel = Chrome.get(Chrome.Type.TOAST);

		int pw = (landscape() ? WIDTH_L : WIDTH_P) + panel.marginHor();
		int ph = h - 50 + panel.marginVer();

		panel.size(pw, ph);
		panel.x = insets.left + (w - pw) / 2f;
		panel.y = insets.top + top;
		add(panel);

		float contentX = panel.x + panel.marginLeft();
		float contentY = panel.y + panel.marginTop();
		float contentW = panel.width() - panel.marginHor();
		float contentH = panel.height() - panel.marginVer();

		// Create content tabs (only one visible at a time)
		displayTab = new DisplayTab();
		displayTab.setRect(contentX, contentY, contentW, contentH);
		add(displayTab);

		uiTab = new UITab();
		uiTab.setRect(contentX, contentY, contentW, contentH);
		add(uiTab);

		// Input tab only if keyboard/controller available
		if (DeviceCompat.hasHardKeyboard() || ControllerHandler.isControllerConnected()) {
			inputTab = new InputTab();
			inputTab.setRect(contentX, contentY, contentW, contentH);
			add(inputTab);
		}

		audioTab = new AudioTab();
		audioTab.setRect(contentX, contentY, contentW, contentH);
		add(audioTab);

		langsTab = new LangsTab();
		langsTab.setRect(contentX, contentY, contentW, contentH);
		add(langsTab);

		// Tab buttons at the bottom of the panel
		int numTabs = inputTab != null ? 5 : 4;
		float tabWidth = pw / (float) numTabs + 1.5f;
		float tabY = panel.y + ph - 3;

		StyledButton btnDisplay = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "") {
			@Override
			protected void onClick() {
				selectTab(0);
				super.onClick();
			}
			@Override
			protected String hoverText() {
				return Messages.get(DisplayTab.class, "title");
			}
		};
		btnDisplay.icon(Icons.DISPLAY.get());
		btnDisplay.setRect(panel.x, tabY, tabWidth, lastIDX == 0 ? 25 : 20);
		align(btnDisplay);
		addToBack(btnDisplay);
		tabButtons.add(btnDisplay);

		StyledButton btnUI = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "") {
			@Override
			protected void onClick() {
				selectTab(1);
				super.onClick();
			}
			@Override
			protected String hoverText() {
				return Messages.get(UITab.class, "title");
			}
		};
		btnUI.icon(Icons.PREFS.get());
		btnUI.setRect(btnDisplay.right() - 2, tabY, tabWidth, lastIDX == 1 ? 25 : 20);
		align(btnUI);
		addToBack(btnUI);
		tabButtons.add(btnUI);

		int tabIdx = 2;

		if (inputTab != null) {
			Image inputIcon;
			if (ControllerHandler.controllerActive || !DeviceCompat.hasHardKeyboard()) {
				inputIcon = Icons.CONTROLLER.get();
			} else {
				inputIcon = Icons.KEYBOARD.get();
			}
			StyledButton btnInput = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "") {
				@Override
				protected void onClick() {
					selectTab(2);
					super.onClick();
				}
				@Override
				protected String hoverText() {
					return Messages.get(InputTab.class, "title");
				}
			};
			btnInput.icon(inputIcon);
			btnInput.setRect(btnUI.right() - 2, tabY, tabWidth, lastIDX == 2 ? 25 : 20);
			align(btnInput);
			addToBack(btnInput);
			tabButtons.add(btnInput);
			tabIdx = 3;
		}

		final int audioIdx = tabIdx;
		StyledButton btnAudio = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "") {
			@Override
			protected void onClick() {
				selectTab(audioIdx);
				super.onClick();
			}
			@Override
			protected String hoverText() {
				return Messages.get(AudioTab.class, "title");
			}
		};
		btnAudio.icon(Icons.AUDIO.get());
		btnAudio.setRect(tabButtons.get(tabButtons.size() - 1).right() - 2, tabY, tabWidth, lastIDX == audioIdx ? 25 : 20);
		align(btnAudio);
		addToBack(btnAudio);
		tabButtons.add(btnAudio);

		final int langsIdx = audioIdx + 1;
		StyledButton btnLangs = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "") {
			@Override
			protected void onClick() {
				selectTab(langsIdx);
				super.onClick();
			}
			@Override
			protected String hoverText() {
				return Messages.get(LangsTab.class, "title");
			}
		};
		btnLangs.icon(Icons.LANGS.get());
		switch (Messages.lang().status()) {
			case X_UNFINISH:
				btnLangs.icon().hardlight(1.5f, 0, 0);
				break;
			case __UNREVIEW:
				btnLangs.icon().hardlight(1.5f, 0.75f, 0f);
				break;
		}
		btnLangs.setRect(btnAudio.right() - 2, tabY, tabWidth, lastIDX == langsIdx ? 25 : 20);
		align(btnLangs);
		addToBack(btnLangs);
		tabButtons.add(btnLangs);

		addToBack(BG);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos(insets.left + w - btnExit.width(), insets.top);
		add(btnExit);

		// Select initial tab
		selectTab(lastIDX);

		// Setup keyboard navigation
		setupKeyboardNavigation();

		fadeIn();
	}

	private void selectTab(int idx) {
		// Adjust index if input tab doesn't exist
		int adjustedIdx = idx;
		if (inputTab == null && idx >= 2) {
			adjustedIdx = idx; // No adjustment needed, just handle mapping
		}

		lastIDX = idx;

		// Hide all tabs
		displayTab.visible = displayTab.active = false;
		uiTab.visible = uiTab.active = false;
		if (inputTab != null) inputTab.visible = inputTab.active = false;
		audioTab.visible = audioTab.active = false;
		langsTab.visible = langsTab.active = false;

		// Show selected tab
		if (inputTab != null) {
			switch (idx) {
				case 0:
					displayTab.visible = displayTab.active = true;
					break;
				case 1:
					uiTab.visible = uiTab.active = true;
					break;
				case 2:
					inputTab.visible = inputTab.active = true;
					break;
				case 3:
					audioTab.visible = audioTab.active = true;
					break;
				case 4:
					langsTab.visible = langsTab.active = true;
					break;
			}
		} else {
			switch (idx) {
				case 0:
					displayTab.visible = displayTab.active = true;
					break;
				case 1:
					uiTab.visible = uiTab.active = true;
					break;
				case 2:
					audioTab.visible = audioTab.active = true;
					break;
				case 3:
					langsTab.visible = langsTab.active = true;
					break;
			}
		}

		// Update tab button appearance
		for (int i = 0; i < tabButtons.size(); i++) {
			StyledButton btn = tabButtons.get(i);
			if (i == idx) {
				btn.setSize(btn.width(), 25);
				btn.icon().resetColor();
			} else {
				btn.setSize(btn.width(), 20);
				btn.icon().brightness(0.6f);
			}
		}

		// Rebuild focusable items for the new tab
		rebuildFocusableItems();
	}

	private void rebuildFocusableItems() {
		// Clear previous focus
		if (focusIndex >= 0 && focusIndex < focusableItems.size()) {
			focusableItems.get(focusIndex).restoreFocusState();
		}
		focusableItems.clear();
		focusIndex = -1;

		// Add focusable items based on active tab
		if (inputTab != null) {
			switch (lastIDX) {
				case 0: // Display tab
					if (displayTab.chkFullscreen != null && displayTab.chkFullscreen.active)
						focusableItems.add(displayTab.chkFullscreen);
					if (displayTab.chkLandscape != null)
						focusableItems.add(displayTab.chkLandscape);
					if (displayTab.optBrightness != null)
						focusableItems.add(displayTab.optBrightness);
					if (displayTab.optVisGrid != null)
						focusableItems.add(displayTab.optVisGrid);
					if (displayTab.optFollowIntensity != null)
						focusableItems.add(displayTab.optFollowIntensity);
					if (displayTab.optScreenShake != null)
						focusableItems.add(displayTab.optScreenShake);
					break;
				case 1: // UI tab
					if (uiTab.optUIMode != null)
						focusableItems.add(uiTab.optUIMode);
					if (uiTab.optUIScale != null)
						focusableItems.add(uiTab.optUIScale);
					if (uiTab.btnToolbarSettings != null)
						focusableItems.add(uiTab.btnToolbarSettings);
					if (uiTab.chkFlipTags != null)
						focusableItems.add(uiTab.chkFlipTags);
					break;
				case 2: // Input tab
					if (inputTab.btnKeyBindings != null)
						focusableItems.add(inputTab.btnKeyBindings);
					if (inputTab.btnControllerBindings != null)
						focusableItems.add(inputTab.btnControllerBindings);
					break;
				case 3: // Audio tab
					if (audioTab.optMusic != null)
						focusableItems.add(audioTab.optMusic);
					if (audioTab.chkMusicMute != null)
						focusableItems.add(audioTab.chkMusicMute);
					if (audioTab.optSFX != null)
						focusableItems.add(audioTab.optSFX);
					if (audioTab.chkMuteSFX != null)
						focusableItems.add(audioTab.chkMuteSFX);
					if (audioTab.chkIgnoreSilent != null)
						focusableItems.add(audioTab.chkIgnoreSilent);
					if (audioTab.chkMusicBG != null)
						focusableItems.add(audioTab.chkMusicBG);
					break;
				case 4: // Languages tab
					if (langsTab.lanBtns != null) {
						for (RedButton btn : langsTab.lanBtns) {
							focusableItems.add(btn);
						}
					}
					if (langsTab.btnCredits != null)
						focusableItems.add(langsTab.btnCredits);
					break;
			}
		} else {
			// No input tab
			switch (lastIDX) {
				case 0: // Display tab
					if (displayTab.chkFullscreen != null && displayTab.chkFullscreen.active)
						focusableItems.add(displayTab.chkFullscreen);
					if (displayTab.chkLandscape != null)
						focusableItems.add(displayTab.chkLandscape);
					if (displayTab.optBrightness != null)
						focusableItems.add(displayTab.optBrightness);
					if (displayTab.optVisGrid != null)
						focusableItems.add(displayTab.optVisGrid);
					if (displayTab.optFollowIntensity != null)
						focusableItems.add(displayTab.optFollowIntensity);
					if (displayTab.optScreenShake != null)
						focusableItems.add(displayTab.optScreenShake);
					break;
				case 1: // UI tab
					if (uiTab.optUIMode != null)
						focusableItems.add(uiTab.optUIMode);
					if (uiTab.optUIScale != null)
						focusableItems.add(uiTab.optUIScale);
					if (uiTab.btnToolbarSettings != null)
						focusableItems.add(uiTab.btnToolbarSettings);
					if (uiTab.chkFlipTags != null)
						focusableItems.add(uiTab.chkFlipTags);
					break;
				case 2: // Audio tab (index 2 when no input tab)
					if (audioTab.optMusic != null)
						focusableItems.add(audioTab.optMusic);
					if (audioTab.chkMusicMute != null)
						focusableItems.add(audioTab.chkMusicMute);
					if (audioTab.optSFX != null)
						focusableItems.add(audioTab.optSFX);
					if (audioTab.chkMuteSFX != null)
						focusableItems.add(audioTab.chkMuteSFX);
					if (audioTab.chkIgnoreSilent != null)
						focusableItems.add(audioTab.chkIgnoreSilent);
					if (audioTab.chkMusicBG != null)
						focusableItems.add(audioTab.chkMusicBG);
					break;
				case 3: // Languages tab (index 3 when no input tab)
					if (langsTab.lanBtns != null) {
						for (RedButton btn : langsTab.lanBtns) {
							focusableItems.add(btn);
						}
					}
					if (langsTab.btnCredits != null)
						focusableItems.add(langsTab.btnCredits);
					break;
			}
		}

		// Restore focus if saved from seamlessResetScene
		if (lastModifiedItem != null) {
			// Find the modified item and restore focus to it
			Focusable targetItem = null;
			if ("optUIMode".equals(lastModifiedItem) && uiTab.optUIMode != null) {
				targetItem = uiTab.optUIMode;
			} else if ("optUIScale".equals(lastModifiedItem) && uiTab.optUIScale != null) {
				targetItem = uiTab.optUIScale;
			}

			if (targetItem != null) {
				for (int i = 0; i < focusableItems.size(); i++) {
					if (focusableItems.get(i) == targetItem) {
						focusIndex = i;
						focusableItems.get(focusIndex).saveFocusState();
						focusableItems.get(focusIndex).setFocused(true);
						break;
					}
				}
			}
			lastModifiedItem = null; // Clear after restoring
		} else if (lastFocusIndex >= 0 && lastFocusIndex < focusableItems.size()) {
			focusIndex = lastFocusIndex;
			focusableItems.get(focusIndex).saveFocusState();
			focusableItems.get(focusIndex).setFocused(true);
			lastFocusIndex = -1; // Clear saved focus after restoring
		}
	}

	private void moveFocus(int direction) {
		if (focusableItems.isEmpty()) return;

		// Restore focus state on previously focused element
		if (focusIndex >= 0 && focusIndex < focusableItems.size()) {
			focusableItems.get(focusIndex).restoreFocusState();
		}

		// Move index
		int startIndex;
		if (focusIndex == -1) {
			startIndex = direction > 0 ? 0 : focusableItems.size() - 1;
		} else {
			startIndex = focusIndex + direction;
			if (startIndex < 0) startIndex = focusableItems.size() - 1;
			if (startIndex >= focusableItems.size()) startIndex = 0;
		}

		// Skip disabled elements
		focusIndex = startIndex;
		int checked = 0;
		while (!focusableItems.get(focusIndex).isActive() && checked < focusableItems.size()) {
			focusIndex += direction;
			if (focusIndex < 0) focusIndex = focusableItems.size() - 1;
			if (focusIndex >= focusableItems.size()) focusIndex = 0;
			checked++;
		}

		// Save focus state then highlight new element
		focusableItems.get(focusIndex).saveFocusState();
		focusableItems.get(focusIndex).setFocused(true);
	}

	private void activateFocused() {
		if (focusIndex >= 0 && focusIndex < focusableItems.size()) {
			Focusable focusable = focusableItems.get(focusIndex);
			if (focusable.isActive()) {
				Sample.INSTANCE.play(Assets.Sounds.CLICK);
				focusable.click();
			}
		}
	}

	private void setupKeyboardNavigation() {
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

				// Block all keys (except BACK) if a window is open
				for (Object v : members) {
					if (v instanceof Window) {
						GameAction action = KeyBindings.getActionForKey(event);
						if (action == SPDAction.BACK) {
							return false; // Let child window handle ESC
						}
						return true; // Block all other keys
					}
				}

				GameAction action = KeyBindings.getActionForKey(event);

				if (action == SPDAction.BACK) {
					onBackPressed();
					return true;
				}

				// Tab/Space to cycle tabs
				if (action == SPDAction.CYCLE) {
					int nextIdx = (lastIDX + 1) % tabButtons.size();
					selectTab(nextIdx);
					return true;
				}

				// Arrow keys for focus navigation (N/S/W/E = up/down/left/right)
				if (action == SPDAction.N || action == SPDAction.NW) {
					moveFocus(-1);
					return true;
				} else if (action == SPDAction.S || action == SPDAction.SE) {
					moveFocus(1);
					return true;
				}

				// Enter to activate focused item
				if (event.code == Input.Keys.ENTER
						|| event.code == Input.Keys.NUMPAD_ENTER) {
					if (focusIndex >= 0 && focusIndex < focusableItems.size()) {
						activateFocused();
						return true;
					}
				}

				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}

		// Reset generators for language changes
		Game.platform.resetGenerators();

		super.destroy();
	}

	@Override
	protected void onBackPressed() {
		SacredPixelDungeon.switchNoFade(TitleScene.class);
	}

	// ========== Display Tab ==========
	private class DisplayTab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		CheckBox chkFullscreen;
		CheckBox chkLandscape;
		ColorBlock sep2;
		OptionSlider optBrightness;
		OptionSlider optVisGrid;
		OptionSlider optFollowIntensity;
		OptionSlider optScreenShake;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			String fullscreenText = Messages.get(this, "fullscreen");
			if (DeviceCompat.isAndroid()) {
				fullscreenText = Messages.get(this, "hide_navigation");
			} else if (DeviceCompat.isiOS()) {
				fullscreenText = Messages.get(this, "hide_gesture");
			}
			chkFullscreen = new CheckBox(fullscreenText, TEXT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.fullscreen(checked());
				}
			};
			if (Game.platform.supportsFullScreen()) {
				chkFullscreen.checked(SPDSettings.fullscreen());
			} else {
				chkFullscreen.checked(true);
				chkFullscreen.enable(false);
			}
			add(chkFullscreen);

			if (DeviceCompat.isAndroid()) {
				chkLandscape = new CheckBox(Messages.get(this, "landscape"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.landscape(checked());
					}
				};
				chkLandscape.checked(SPDSettings.landscape());
				add(chkLandscape);
			}

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);
			sep2.visible = false;

			optBrightness = new OptionSlider(Messages.get(this, "brightness"),
					Messages.get(this, "dark"), Messages.get(this, "bright"), -1, 1) {
				@Override
				protected void onChange() {
					SPDSettings.brightness(getSelectedValue());
				}
			};
			optBrightness.setSelectedValue(SPDSettings.brightness());
			add(optBrightness);

			optVisGrid = new OptionSlider(Messages.get(this, "visual_grid"),
					Messages.get(this, "off"), Messages.get(this, "high"), -1, 2) {
				@Override
				protected void onChange() {
					SPDSettings.visualGrid(getSelectedValue());
				}
			};
			optVisGrid.setSelectedValue(SPDSettings.visualGrid());
			add(optVisGrid);

			optFollowIntensity = new OptionSlider(Messages.get(this, "camera_follow"),
					Messages.get(this, "low"), Messages.get(this, "high"), 1, 4) {
				@Override
				protected void onChange() {
					SPDSettings.cameraFollow(getSelectedValue());
				}
			};
			optFollowIntensity.setSelectedValue(SPDSettings.cameraFollow());
			add(optFollowIntensity);

			optScreenShake = new OptionSlider(Messages.get(this, "screenshake"),
					Messages.get(this, "off"), Messages.get(this, "high"), 0, 4) {
				@Override
				protected void onChange() {
					SPDSettings.screenShake(getSelectedValue());
				}
			};
			optScreenShake.setSelectedValue(SPDSettings.screenShake());
			add(optScreenShake);
		}

		@Override
		protected void layout() {
			float bottom = y;

			title.setPos((width - title.width()) / 2 + x, bottom + 2);
			sep1.size(width, 1);
			sep1.x = x;
			sep1.y = title.bottom() + 2;

			bottom = sep1.y + 1;

			chkFullscreen.setRect(x, bottom + GAP, width, BTN_HEIGHT);
			bottom = chkFullscreen.bottom();

			if (chkLandscape != null) {
				chkLandscape.setRect(x, bottom + GAP, width, BTN_HEIGHT);
				bottom = chkLandscape.bottom();
			}

			sep2.size(width, 1);
			sep2.x = x;
			sep2.y = bottom + GAP;
			bottom = sep2.y + 1;

			if (width > 200) {
				optBrightness.setRect(x, bottom + GAP, width / 2 - GAP / 2, SLIDER_HEIGHT);
				optVisGrid.setRect(optBrightness.right() + GAP, optBrightness.top(), width / 2 - GAP / 2, SLIDER_HEIGHT);

				optFollowIntensity.setRect(x, optVisGrid.bottom() + GAP, width / 2 - GAP / 2, SLIDER_HEIGHT);
				optScreenShake.setRect(optFollowIntensity.right() + GAP, optFollowIntensity.top(), width / 2 - GAP / 2, SLIDER_HEIGHT);
			} else {
				optBrightness.setRect(x, bottom + GAP, width, SLIDER_HEIGHT);
				optVisGrid.setRect(x, optBrightness.bottom() + GAP, width, SLIDER_HEIGHT);

				optFollowIntensity.setRect(x, optVisGrid.bottom() + GAP, width, SLIDER_HEIGHT);
				optScreenShake.setRect(x, optFollowIntensity.bottom() + GAP, width, SLIDER_HEIGHT);
			}
		}
	}

	// ========== UI Tab ==========
	private class UITab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		OptionSlider optUIMode;
		OptionSlider optUIScale;
		RedButton btnToolbarSettings;
		CheckBox chkFlipTags;
		ColorBlock sep2;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			float wMin = Game.width / PixelScene.MIN_WIDTH_FULL;
			float hMin = Game.height / PixelScene.MIN_HEIGHT_FULL;
			if (Math.min(wMin, hMin) >= 2 * Game.density) {
				optUIMode = new OptionSlider(
						Messages.get(this, "ui_mode"),
						Messages.get(this, "mobile"),
						Messages.get(this, "full"),
						0, 2) {
					@Override
					protected void onChange() {
						SPDSettings.interfaceSize(getSelectedValue());
						lastModifiedItem = "optUIMode"; // Track which slider was modified
						SacredPixelDungeon.seamlessResetScene();
					}
				};
				optUIMode.setSelectedValue(SPDSettings.interfaceSize());
				add(optUIMode);
			}

			if ((int) Math.ceil(2 * Game.density) < PixelScene.maxDefaultZoom) {
				optUIScale = new OptionSlider(Messages.get(this, "scale"),
						(int) Math.ceil(2 * Game.density) + "X",
						PixelScene.maxDefaultZoom + "X",
						(int) Math.ceil(2 * Game.density),
						PixelScene.maxDefaultZoom) {
					@Override
					protected void onChange() {
						if (getSelectedValue() != SPDSettings.scale()) {
							SPDSettings.scale(getSelectedValue());
							lastModifiedItem = "optUIScale"; // Track which slider was modified
							SacredPixelDungeon.seamlessResetScene();
						}
					}
				};
				optUIScale.setSelectedValue(SPDSettings.scale() == 0 ? 2 : PixelScene.defaultZoom);
				add(optUIScale);
			}

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);
			sep2.visible = false;

			if (SPDSettings.interfaceSize() == 0) {
				btnToolbarSettings = new RedButton(Messages.get(this, "toolbar_settings"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						SacredPixelDungeon.scene().addToFront(new WndToolbarSettings());
					}
				};
				add(btnToolbarSettings);
			} else {
				chkFlipTags = new CheckBox(Messages.get(this, "flip_indicators"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.flipTags(checked());
					}
				};
				chkFlipTags.checked(SPDSettings.flipTags());
				add(chkFlipTags);
			}
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width()) / 2 + x, y + 2);
			sep1.size(width, 1);
			sep1.x = x;
			sep1.y = title.bottom() + 2;

			float bottom = sep1.y + 1;

			boolean hasSliders = (optUIMode != null || optUIScale != null);

			if (optUIMode != null && optUIScale != null && width > 200) {
				optUIMode.setRect(x, bottom + GAP, width / 2 - 1, SLIDER_HEIGHT);
				optUIScale.setRect(x + width / 2 + 1, bottom + GAP, width / 2 - 1, SLIDER_HEIGHT);
				bottom = optUIScale.bottom();
			} else {
				if (optUIMode != null) {
					optUIMode.setRect(x, bottom + GAP, width, SLIDER_HEIGHT);
					bottom = optUIMode.bottom();
				}

				if (optUIScale != null) {
					optUIScale.setRect(x, bottom + GAP, width, SLIDER_HEIGHT);
					bottom = optUIScale.bottom();
				}
			}

			sep1.visible = false;

			sep2.size(width, 1);
			sep2.x = x;
			if (hasSliders) {
				sep2.y = bottom + GAP;
			} else {
				sep2.y = title.bottom() + 2;
			}
			bottom = sep2.y + 1;

			if (btnToolbarSettings != null) {
				btnToolbarSettings.setRect(x, bottom + GAP, width, BTN_HEIGHT);
			} else if (chkFlipTags != null) {
				chkFlipTags.setRect(x, bottom + GAP, width, BTN_HEIGHT);
			}
		}
	}

	// ========== Input Tab ==========
	private class InputTab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		RedButton btnKeyBindings;
		RedButton btnControllerBindings;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			if (DeviceCompat.hasHardKeyboard()) {
				btnKeyBindings = new RedButton(Messages.get(this, "key_bindings"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						super.onClick();
						SacredPixelDungeon.scene().addToFront(new WndKeyBindings(false));
					}
				};
				add(btnKeyBindings);
			}

			if (ControllerHandler.isControllerConnected()) {
				btnControllerBindings = new RedButton(Messages.get(this, "controller_bindings"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						super.onClick();
						SacredPixelDungeon.scene().addToFront(new WndKeyBindings(true));
					}
				};
				add(btnControllerBindings);
			}
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width()) / 2 + x, y + 2);
			sep1.size(width, 1);
			sep1.x = x;
			sep1.y = title.bottom() + 2;

			float bottom = sep1.y + 1;

			if (width > 200 && btnKeyBindings != null && btnControllerBindings != null) {
				btnKeyBindings.setRect(x, bottom + GAP, width / 2 - 1, BTN_HEIGHT);
				btnControllerBindings.setRect(x + width / 2 + 1, bottom + GAP, width / 2 - 1, BTN_HEIGHT);
			} else {
				if (btnKeyBindings != null) {
					btnKeyBindings.setRect(x, bottom + GAP, width, BTN_HEIGHT);
					bottom = btnKeyBindings.bottom();
				}

				if (btnControllerBindings != null) {
					btnControllerBindings.setRect(x, bottom + GAP, width, BTN_HEIGHT);
				}
			}
		}
	}

	// ========== Audio Tab ==========
	private class AudioTab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		OptionSlider optMusic;
		CheckBox chkMusicMute;
		ColorBlock sep2;
		OptionSlider optSFX;
		CheckBox chkMuteSFX;
		ColorBlock sep3;
		CheckBox chkIgnoreSilent;
		CheckBox chkMusicBG;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			optMusic = new OptionSlider(Messages.get(this, "music_vol"), "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					SPDSettings.musicVol(getSelectedValue());
				}
			};
			optMusic.setSelectedValue(SPDSettings.musicVol());
			add(optMusic);

			chkMusicMute = new CheckBox(Messages.get(this, "music_mute"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.music(!checked());
				}
			};
			chkMusicMute.checked(!SPDSettings.music());
			add(chkMusicMute);

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);
			sep2.visible = false;

			optSFX = new OptionSlider(Messages.get(this, "sfx_vol"), "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					SPDSettings.SFXVol(getSelectedValue());
					if (Random.Int(100) == 0) {
						Sample.INSTANCE.play(Assets.Sounds.MIMIC);
					} else {
						Sample.INSTANCE.play(Random.oneOf(Assets.Sounds.GOLD,
								Assets.Sounds.HIT,
								Assets.Sounds.ITEM,
								Assets.Sounds.SHATTER,
								Assets.Sounds.EVOKE,
								Assets.Sounds.SECRET));
					}
				}
			};
			optSFX.setSelectedValue(SPDSettings.SFXVol());
			add(optSFX);

			chkMuteSFX = new CheckBox(Messages.get(this, "sfx_mute"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.soundFx(!checked());
					Sample.INSTANCE.play(Assets.Sounds.CLICK);
				}
			};
			chkMuteSFX.checked(!SPDSettings.soundFx());
			add(chkMuteSFX);

			if (DeviceCompat.isiOS()) {
				sep3 = new ColorBlock(1, 1, 0xFF000000);
				add(sep3);
				sep3.visible = false;

				chkIgnoreSilent = new CheckBox(Messages.get(this, "ignore_silent"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.ignoreSilentMode(checked());
					}
				};
				chkIgnoreSilent.checked(SPDSettings.ignoreSilentMode());
				add(chkIgnoreSilent);
			} else if (DeviceCompat.isDesktop()) {
				sep3 = new ColorBlock(1, 1, 0xFF000000);
				add(sep3);
				sep3.visible = false;

				chkMusicBG = new CheckBox(Messages.get(this, "music_bg"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.playMusicInBackground(checked());
					}
				};
				chkMusicBG.checked(SPDSettings.playMusicInBackground());
				add(chkMusicBG);
			}
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width()) / 2 + x, y + 2);
			sep1.size(width, 1);
			sep1.x = x;
			sep1.y = title.bottom() + 2;

			if (width > 200) {
				optMusic.setRect(x, sep1.y + 1 + GAP, width / 2 - 1, SLIDER_HEIGHT);
				chkMusicMute.setRect(x, optMusic.bottom() + GAP, width / 2 - 1, BTN_HEIGHT);

				sep2.size(width, 1);
				sep2.x = x;
				sep2.y = sep1.y;

				optSFX.setRect(optMusic.right() + 2, sep2.y + 1 + GAP, width / 2 - 1, SLIDER_HEIGHT);
				chkMuteSFX.setRect(chkMusicMute.right() + 2, optSFX.bottom() + GAP, width / 2 - 1, BTN_HEIGHT);
			} else {
				optMusic.setRect(x, sep1.y + 1 + GAP, width, SLIDER_HEIGHT);
				chkMusicMute.setRect(x, optMusic.bottom() + GAP, width, BTN_HEIGHT);

				sep2.size(width, 1);
				sep2.x = x;
				sep2.y = chkMusicMute.bottom() + GAP;

				optSFX.setRect(x, sep2.y + 1 + GAP, width, SLIDER_HEIGHT);
				chkMuteSFX.setRect(x, optSFX.bottom() + GAP, width, BTN_HEIGHT);
			}

			if (chkIgnoreSilent != null) {
				sep3.size(width, 1);
				sep3.x = x;
				sep3.y = chkMuteSFX.bottom() + GAP;

				chkIgnoreSilent.setRect(x, sep3.y + 1 + GAP, width, BTN_HEIGHT);
			} else if (chkMusicBG != null) {
				sep3.size(width, 1);
				sep3.x = x;
				sep3.y = chkMuteSFX.bottom() + GAP;

				chkMusicBG.setRect(x, sep3.y + 1 + GAP, width, BTN_HEIGHT);
			}
		}
	}

	// ========== Languages Tab ==========
	private class LangsTab extends Component {

		final static int COLS_P = 3;
		final static int COLS_L = 6;
		final static int LANG_BTN_HEIGHT = 11;

		RenderedTextBlock title;
		ColorBlock sep1;
		RedButton[] lanBtns;
		ColorBlock sep3;
		RenderedTextBlock txtTransifex;
		RedButton btnCredits;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			final ArrayList<Languages> langs = new ArrayList<>();
			langs.add(Languages.ENGLISH);
			langs.add(Languages.SPANISH);
			langs.add(Languages.KOREAN);
			langs.add(Languages.JAPANESE);
			langs.add(Languages.CHI_SMPL);
			langs.add(Languages.CHI_TRAD);
			langs.add(Languages.FRENCH);
			langs.add(Languages.GERMAN);
			langs.add(Languages.PORTUGUESE);
			langs.add(Languages.ITALIAN);
			langs.add(Languages.DUTCH);
			langs.add(Languages.SWEDISH);
			langs.add(Languages.POLISH);
			langs.add(Languages.CZECH);
			langs.add(Languages.HUNGARIAN);
			langs.add(Languages.TURKISH);
			langs.add(Languages.RUSSIAN);
			langs.add(Languages.UKRANIAN);
			langs.add(Languages.BELARUSIAN);
			langs.add(Languages.GREEK);
			langs.add(Languages.VIETNAMESE);
			langs.add(Languages.INDONESIAN);
			langs.add(Languages.ESPERANTO);

			final Languages currLang = Messages.lang();

			lanBtns = new RedButton[langs.size()];
			for (int i = 0; i < langs.size(); i++) {
				final int langIndex = i;
				RedButton btn = new RedButton(Messages.titleCase(langs.get(i).nativeName()), 6) {
					@Override
					protected void onClick() {
						super.onClick();
						Messages.setup(langs.get(langIndex));
						SacredPixelDungeon.seamlessResetScene(new Game.SceneChangeCallback() {
							@Override
							public void beforeCreate() {
								SPDSettings.language(langs.get(langIndex));
								GameLog.wipe();
								Game.platform.resetGenerators();
							}

							@Override
							public void afterCreate() {
							}
						});
					}
				};
				if (currLang == langs.get(i)) {
					btn.textColor(Window.TITLE_COLOR);
				}
				lanBtns[i] = btn;
				add(btn);
			}

			sep3 = new ColorBlock(1, 1, 0xFF000000);
			add(sep3);
			sep3.visible = false;

			txtTransifex = PixelScene.renderTextBlock(5);
			txtTransifex.text(Messages.get(this, "transifex"));
			add(txtTransifex);

			if (currLang != Languages.ENGLISH) {
				String credText = Messages.titleCase(Messages.get(this, "credits"));
				btnCredits = new RedButton(credText, 6) {
					@Override
					protected void onClick() {
						super.onClick();
						SacredPixelDungeon.scene().addToFront(new WndLangCredits(currLang));
					}
				};
				add(btnCredits);
			}
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width()) / 2 + x, y + 2);
			sep1.size(width, 1);
			sep1.x = x;
			sep1.y = title.bottom() + 2;

			float posY = sep1.y + 1 + GAP;
			float posX = x;

			int cols = PixelScene.landscape() ? COLS_L : COLS_P;
			int colIdx = 0;
			for (RedButton btn : lanBtns) {
				int colStart = (int) (width * colIdx / (float) cols);
				int colEnd = (int) (width * (colIdx + 1) / (float) cols);
				int bw = colEnd - colStart - 1;
				btn.setRect(posX, posY, bw, LANG_BTN_HEIGHT);
				posX += bw + 1;
				colIdx++;
				if (colIdx >= cols) {
					colIdx = 0;
					posX = x;
					posY += LANG_BTN_HEIGHT + 1;
				}
			}
			if (posX > x) {
				posY += LANG_BTN_HEIGHT + 1;
			}

			sep3.size(width, 1);
			sep3.x = x;
			sep3.y = posY;
			posY += 4 * GAP;

			txtTransifex.setPos(x, posY);
			txtTransifex.maxWidth((int) width);

			if (btnCredits != null) {
				float creditsY = txtTransifex.bottom() + 4 * GAP;
				btnCredits.setSize(width, BTN_HEIGHT);
				btnCredits.setPos(x, creditsY);
			}
		}
	}

	// ========== Helper Windows ==========
	private static class WndToolbarSettings extends Window {

		private static final int WIDTH = 149;

		private RedButton btnSplit;
		private RedButton btnGrouped;
		private RedButton btnCentered;

		public WndToolbarSettings() {
			super();

			RenderedTextBlock barDesc = PixelScene.renderTextBlock(Messages.get(UITab.class, "mode"), 7);
			add(barDesc);

			btnSplit = new RedButton(Messages.get(UITab.class, "split"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					resetModeButtonColors();
					textColor(Window.TITLE_COLOR);
					SPDSettings.toolbarMode(Toolbar.Mode.SPLIT.name());
					Toolbar.updateLayout();
				}
			};
			if (SPDSettings.toolbarMode().equals(Toolbar.Mode.SPLIT.name())) {
				btnSplit.textColor(Window.TITLE_COLOR);
			}
			add(btnSplit);
			addFocusableButton(btnSplit);

			btnGrouped = new RedButton(Messages.get(UITab.class, "group"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					resetModeButtonColors();
					textColor(Window.TITLE_COLOR);
					SPDSettings.toolbarMode(Toolbar.Mode.GROUP.name());
					Toolbar.updateLayout();
				}
			};
			if (SPDSettings.toolbarMode().equals(Toolbar.Mode.GROUP.name())) {
				btnGrouped.textColor(Window.TITLE_COLOR);
			}
			add(btnGrouped);
			addFocusableButton(btnGrouped);

			btnCentered = new RedButton(Messages.get(UITab.class, "center"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					resetModeButtonColors();
					textColor(Window.TITLE_COLOR);
					SPDSettings.toolbarMode(Toolbar.Mode.CENTER.name());
					Toolbar.updateLayout();
				}
			};
			if (SPDSettings.toolbarMode().equals(Toolbar.Mode.CENTER.name())) {
				btnCentered.textColor(Window.TITLE_COLOR);
			}
			add(btnCentered);
			addFocusableButton(btnCentered);

			CheckBox chkQuickSwapper = new CheckBox(Messages.get(UITab.class, "quickslot_swapper"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.quickSwapper(checked());
					Toolbar.updateLayout();
				}
			};
			chkQuickSwapper.checked(SPDSettings.quickSwapper());
			add(chkQuickSwapper);
			addFocusableButton(chkQuickSwapper);

			RenderedTextBlock swapperDesc = PixelScene.renderTextBlock(Messages.get(UITab.class, "swapper_desc"), 5);
			swapperDesc.hardlight(0x888888);
			add(swapperDesc);

			CheckBox chkFlipToolbar = new CheckBox(Messages.get(UITab.class, "flip_toolbar"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.flipToolbar(checked());
					Toolbar.updateLayout();
				}
			};
			chkFlipToolbar.checked(SPDSettings.flipToolbar());
			add(chkFlipToolbar);
			addFocusableButton(chkFlipToolbar);

			CheckBox chkFlipTags = new CheckBox(Messages.get(UITab.class, "flip_indicators"), TEXT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.flipTags(checked());
				}
			};
			chkFlipTags.checked(SPDSettings.flipTags());
			add(chkFlipTags);
			addFocusableButton(chkFlipTags);

			// Layout
			barDesc.setPos((WIDTH - barDesc.width()) / 2f, GAP);
			PixelScene.align(barDesc);

			int btnWidth = (int) (WIDTH - 2 * GAP) / 3;
			btnSplit.setRect(0, barDesc.bottom() + 2 * GAP, btnWidth, BTN_HEIGHT);
			btnGrouped.setRect(btnSplit.right() + GAP, btnSplit.top(), btnWidth, BTN_HEIGHT);
			btnCentered.setRect(btnGrouped.right() + GAP, btnSplit.top(), btnWidth, BTN_HEIGHT);

			chkQuickSwapper.setRect(0, btnGrouped.bottom() + GAP, WIDTH, BTN_HEIGHT);

			swapperDesc.maxWidth(WIDTH);
			swapperDesc.setPos(0, chkQuickSwapper.bottom() + 1);

			if (WIDTH > 200) {
				chkFlipToolbar.setRect(0, swapperDesc.bottom() + GAP, WIDTH / 2 - 1, BTN_HEIGHT);
				chkFlipTags.setRect(chkFlipToolbar.right() + GAP, chkFlipToolbar.top(), WIDTH / 2 - 1, BTN_HEIGHT);
			} else {
				chkFlipToolbar.setRect(0, swapperDesc.bottom() + GAP, WIDTH, BTN_HEIGHT);
				chkFlipTags.setRect(0, chkFlipToolbar.bottom() + GAP, WIDTH, BTN_HEIGHT);
			}

			resize(WIDTH, (int) chkFlipTags.bottom());
		}

		private void resetModeButtonColors() {
			btnSplit.textColor(0xFFFFFF);
			btnGrouped.textColor(0xFFFFFF);
			btnCentered.textColor(0xFFFFFF);
		}
	}

	private static class WndLangCredits extends Window {

		public WndLangCredits(Languages lang) {
			super();

			String[] reviewers = lang.reviewers();
			String[] translators = lang.translators();

			int totalCredits = 2 * reviewers.length + translators.length;
			int totalTokens = 2 * totalCredits;

			if (reviewers.length > 0) totalTokens += 6;
			totalTokens += 4;

			String[] entries = new String[totalTokens];
			int index = 0;
			if (reviewers.length > 0) {
				entries[0] = "_";
				entries[1] = Messages.titleCase(Messages.get(LangsTab.class, "reviewers"));
				entries[2] = "_";
				entries[3] = "\n";
				index = 4;
				for (int i = 0; i < reviewers.length; i++) {
					entries[index] = reviewers[i];
					if (i < reviewers.length - 1) entries[index] += ", ";
					entries[index + 1] = " ";
					index += 2;
				}
				entries[index] = "\n";
				entries[index + 1] = "\n";
				index += 2;
			}

			entries[index] = "_";
			entries[index + 1] = Messages.titleCase(Messages.get(LangsTab.class, "translators"));
			entries[index + 2] = "_";
			entries[index + 3] = "\n";
			index += 4;

			for (int i = 0; i < reviewers.length; i++) {
				entries[index] = reviewers[i];
				if (i < reviewers.length - 1 || translators.length > 0) entries[index] += ", ";
				entries[index + 1] = " ";
				index += 2;
			}

			for (int i = 0; i < translators.length; i++) {
				entries[index] = translators[i];
				if (i < translators.length - 1) entries[index] += ", ";
				entries[index + 1] = " ";
				index += 2;
			}

			int w = PixelScene.landscape() ? 120 : 80;
			if (totalCredits >= 25) w *= 1.5f;

			RenderedTextBlock title = PixelScene.renderTextBlock(8);
			title.text(Messages.titleCase(Messages.get(LangsTab.class, "credits")), w);
			title.hardlight(Window.TITLE_COLOR);
			title.setPos((w - title.width()) / 2, 0);
			add(title);

			RenderedTextBlock text = PixelScene.renderTextBlock(6);
			text.maxWidth(w);
			text.tokens(entries);
			text.setPos(0, title.bottom() + 4);
			add(text);

			resize(w, (int) text.bottom() + 2);
		}
	}
}
