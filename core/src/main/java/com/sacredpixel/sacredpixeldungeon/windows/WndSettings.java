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

package com.sacredpixel.sacredpixeldungeon.windows;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.messages.Languages;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.scenes.SettingsScene;
import com.sacredpixel.sacredpixeldungeon.services.news.News;
import com.sacredpixel.sacredpixeldungeon.services.updates.Updates;
import com.sacredpixel.sacredpixeldungeon.ui.CheckBox;
import com.sacredpixel.sacredpixeldungeon.ui.GameLog;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.OptionSlider;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Toolbar;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class WndSettings extends WndTabbed {

	private static final int WIDTH_P	    = 149;
	private static final int WIDTH_L	    = 251;

	private static final int SLIDER_HEIGHT	= 28;
	private static final int BTN_HEIGHT	    = 16;
	private static final float GAP          = 1;
	private static final int TEXT_SIZE      = 7;

	private DisplayTab  display;
	private UITab       ui;
	private InputTab    input;
	private AudioTab    audio;
	private LangsTab    langs;

	public static int last_index = 0;

	// Track currently active tab for focus management
	private int activeTabIndex = 0;

	public WndSettings() {
		super();

		float height;

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		display = new DisplayTab();
		display.setSize(width, 0);
		height = display.height();
		add( display );

		add( new IconTab(Icons.get(Icons.DISPLAY)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				display.visible = display.active = value;
				if (value) {
					last_index = 0;
					activeTabIndex = 0;
					clearFocus();
					rebuildFocusableButtons();
				}
			}
		});

		ui = new UITab();
		ui.setSize(width, 0);
		height = Math.max(height, ui.height());
		add( ui );

		add( new IconTab(Icons.get(Icons.PREFS)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				ui.visible = ui.active = value;
				if (value) {
					last_index = 1;
					activeTabIndex = 1;
					clearFocus();
					rebuildFocusableButtons();
				}
			}
		});

		input = new InputTab();
		input.setSize(width, 0);
		height = Math.max(height, input.height());

		if (DeviceCompat.hasHardKeyboard() || ControllerHandler.isControllerConnected()) {
			add( input );
			Image icon;
			if (ControllerHandler.controllerActive || !DeviceCompat.hasHardKeyboard()){
				icon = Icons.get(Icons.CONTROLLER);
			} else {
				icon = Icons.get(Icons.KEYBOARD);
			}
			add(new IconTab(icon) {
				@Override
				protected void select(boolean value) {
					super.select(value);
					input.visible = input.active = value;
					if (value) {
						last_index = 2;
						activeTabIndex = 2;
						clearFocus();
						rebuildFocusableButtons();
					}
				}
			});
		}

		audio = new AudioTab();
		audio.setSize(width, 0);
		height = Math.max(height, audio.height());
		add( audio );

		add( new IconTab(Icons.get(Icons.AUDIO)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				audio.visible = audio.active = value;
				if (value) {
					last_index = 3;
					activeTabIndex = 3;
					clearFocus();
					rebuildFocusableButtons();
				}
			}
		});

		langs = new LangsTab();
		langs.setSize(width, 0);
		height = Math.max(height, langs.height());
		add( langs );


		IconTab langsTab = new IconTab(Icons.get(Icons.LANGS)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				langs.visible = langs.active = value;
				if (value) {
					last_index = 4;
					activeTabIndex = 4;
					clearFocus();
					rebuildFocusableButtons();
				}
			}

			@Override
			protected void createChildren() {
				super.createChildren();
				switch(Messages.lang().status()){
					case X_UNFINISH:
						icon.hardlight(1.5f, 0, 0);
						break;
					case __UNREVIEW:
						icon.hardlight(1.5f, 0.75f, 0f);
						break;
				}
			}

		};
		add( langsTab );

		resize(width, (int)Math.ceil(height));

		layoutTabs();

		if (tabs.size() == 4 && last_index >= 2){
			//input tab isn't visible
			select(last_index-1);
		} else {
			select(last_index);
		}

		// Build initial focus list based on active tab
		rebuildFocusableButtons();
	}

	private void rebuildFocusableButtons() {
		focusableButtons.clear();
		focusIndex = -1;

		switch (activeTabIndex) {
			case 0: // Display tab
				if (display.chkFullscreen != null && display.chkFullscreen.active) {
					addFocusableButton(display.chkFullscreen);
				}
				if (display.chkLandscape != null) {
					addFocusableButton(display.chkLandscape);
				}
				// OptionSliders - keyboard navigation with left/right arrows to adjust values
				if (display.optBrightness != null) {
					addFocusable(display.optBrightness);
				}
				if (display.optVisGrid != null) {
					addFocusable(display.optVisGrid);
				}
				if (display.optFollowIntensity != null) {
					addFocusable(display.optFollowIntensity);
				}
				if (display.optScreenShake != null) {
					addFocusable(display.optScreenShake);
				}
				break;
			case 1: // UI tab
				// OptionSliders for UI settings
				if (ui.optUIMode != null) {
					addFocusable(ui.optUIMode);
				}
				if (ui.optUIScale != null) {
					addFocusable(ui.optUIScale);
				}
				if (ui.btnToolbarSettings != null) {
					addFocusableButton(ui.btnToolbarSettings);
				}
				if (ui.chkFlipTags != null) {
					addFocusableButton(ui.chkFlipTags);
				}
				break;
			case 2: // Input tab
				if (input.btnKeyBindings != null) {
					addFocusableButton(input.btnKeyBindings);
				}
				if (input.btnControllerBindings != null) {
					addFocusableButton(input.btnControllerBindings);
				}
				break;
			case 3: // Audio tab
				// OptionSliders for volume control
				if (audio.optMusic != null) {
					addFocusable(audio.optMusic);
				}
				if (audio.chkMusicMute != null) {
					addFocusableButton(audio.chkMusicMute);
				}
				if (audio.optSFX != null) {
					addFocusable(audio.optSFX);
				}
				if (audio.chkMuteSFX != null) {
					addFocusableButton(audio.chkMuteSFX);
				}
				if (audio.chkIgnoreSilent != null) {
					addFocusableButton(audio.chkIgnoreSilent);
				}
				if (audio.chkMusicBG != null) {
					addFocusableButton(audio.chkMusicBG);
				}
				break;
			case 4: // Languages tab
				if (langs.lanBtns != null) {
					for (RedButton btn : langs.lanBtns) {
						addFocusableButton(btn);
					}
				}
				if (langs.btnCredits != null) {
					addFocusableButton(langs.btnCredits);
				}
				break;
		}
	}

	@Override
	public void hide() {
		super.hide();
		//resets generators because there's no need to retain chars for languages not selected
		//skip if a scene switch is already in progress to avoid double reset
		//also skip if we're in SettingsScene - it will handle navigation itself
		//also skip if hero is dead to avoid re-triggering death sequence and guide popups
		if (!Game.switchingScene() && !(Game.scene() instanceof SettingsScene)
				&& !(Dungeon.hero != null && !Dungeon.hero.isAlive())) {
			SacredPixelDungeon.seamlessResetScene(new Game.SceneChangeCallback() {
				@Override
				public void beforeCreate() {
					Game.platform.resetGenerators();
				}
				@Override
				public void afterCreate() {
					//do nothing
				}
			});
		}
	}

	private static class DisplayTab extends Component {

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
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			String fullscreenText = Messages.get(this, "fullscreen");
			if (DeviceCompat.isAndroid()){
				fullscreenText = Messages.get(this, "hide_navigation");
			} else if (DeviceCompat.isiOS()){
				fullscreenText = Messages.get(this, "hide_gesture");
			}
			chkFullscreen = new CheckBox( fullscreenText, TEXT_SIZE ) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.fullscreen(checked());
				}
			};
			if (Game.platform.supportsFullScreen()){
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

			title.setPos((width - title.width())/2, bottom + 2);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2;

			bottom = sep1.y + 1;

			chkFullscreen.setRect(0, bottom + GAP, width, BTN_HEIGHT);
			bottom = chkFullscreen.bottom();

			if (chkLandscape != null) {
				chkLandscape.setRect(0, bottom + GAP, width, BTN_HEIGHT);
				bottom = chkLandscape.bottom();
			}

			sep2.size(width, 1);
			sep2.y = bottom + GAP;
			bottom = sep2.y + 1;

			if (width > 200){
				optBrightness.setRect(0, bottom + GAP, width/2-GAP/2, SLIDER_HEIGHT);
				optVisGrid.setRect(optBrightness.right() + GAP, optBrightness.top(), width/2-GAP/2, SLIDER_HEIGHT);

				optFollowIntensity.setRect(0, optVisGrid.bottom() + GAP, width/2-GAP/2, SLIDER_HEIGHT);
				optScreenShake.setRect(optFollowIntensity.right() + GAP, optFollowIntensity.top(), width/2-GAP/2, SLIDER_HEIGHT);
			} else {
				optBrightness.setRect(0, bottom + GAP, width, SLIDER_HEIGHT);
				optVisGrid.setRect(0, optBrightness.bottom() + GAP, width, SLIDER_HEIGHT);

				optFollowIntensity.setRect(0, optVisGrid.bottom() + GAP, width, SLIDER_HEIGHT);
				optScreenShake.setRect(0, optFollowIntensity.bottom() + GAP, width, SLIDER_HEIGHT);
			}

			height = optScreenShake.bottom();
		}

	}

	private static class UITab extends Component {

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
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			//add slider for UI size only if device has enough space to support it
			float wMin = Game.width / PixelScene.MIN_WIDTH_FULL;
			float hMin = Game.height / PixelScene.MIN_HEIGHT_FULL;
			if (Math.min(wMin, hMin) >= 2*Game.density){
				optUIMode = new OptionSlider(
						Messages.get(this, "ui_mode"),
						Messages.get(this, "mobile"),
						Messages.get(this, "full"),
						0,
						2
				) {
					@Override
					protected void onChange() {
						SPDSettings.interfaceSize(getSelectedValue());
						SacredPixelDungeon.seamlessResetScene();
					}
				};
				optUIMode.setSelectedValue(SPDSettings.interfaceSize());
				add(optUIMode);
			}

			if ((int)Math.ceil(2* Game.density) < PixelScene.maxDefaultZoom) {
				optUIScale = new OptionSlider(Messages.get(this, "scale"),
						(int)Math.ceil(2* Game.density)+ "X",
						PixelScene.maxDefaultZoom + "X",
						(int)Math.ceil(2* Game.density),
						PixelScene.maxDefaultZoom ) {
					@Override
					protected void onChange() {
						if (getSelectedValue() != SPDSettings.scale()) {
							SPDSettings.scale(getSelectedValue());
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
				btnToolbarSettings = new RedButton(Messages.get(this, "toolbar_settings"), TEXT_SIZE){
					@Override
					protected void onClick() {
						SacredPixelDungeon.scene().addToFront(new Window(){

							RenderedTextBlock barDesc;
							RedButton btnSplit; RedButton btnGrouped; RedButton btnCentered;
							CheckBox chkQuickSwapper;
							RenderedTextBlock swapperDesc;
							CheckBox chkFlipToolbar;
							CheckBox chkFlipTags;

							{
								barDesc = PixelScene.renderTextBlock(Messages.get(WndSettings.UITab.this, "mode"), 7);
								add(barDesc);

								btnSplit = new RedButton(Messages.get(WndSettings.UITab.this, "split"), TEXT_SIZE) {
									@Override
									protected void onClick() {
										textColor(TITLE_COLOR);
										btnGrouped.textColor(WHITE);
										btnCentered.textColor(WHITE);
										SPDSettings.toolbarMode(Toolbar.Mode.SPLIT.name());
										Toolbar.updateLayout();
									}
								};
								if (SPDSettings.toolbarMode().equals(Toolbar.Mode.SPLIT.name())) {
									btnSplit.textColor(TITLE_COLOR);
								}
								add(btnSplit);

								btnGrouped = new RedButton(Messages.get(WndSettings.UITab.this, "group"), TEXT_SIZE) {
									@Override
									protected void onClick() {
										btnSplit.textColor(WHITE);
										textColor(TITLE_COLOR);
										btnCentered.textColor(WHITE);
										SPDSettings.toolbarMode(Toolbar.Mode.GROUP.name());
										Toolbar.updateLayout();
									}
								};
								if (SPDSettings.toolbarMode().equals(Toolbar.Mode.GROUP.name())) {
									btnGrouped.textColor(TITLE_COLOR);
								}
								add(btnGrouped);

								btnCentered = new RedButton(Messages.get(WndSettings.UITab.this, "center"), TEXT_SIZE) {
									@Override
									protected void onClick() {
										btnSplit.textColor(WHITE);
										btnGrouped.textColor(WHITE);
										textColor(TITLE_COLOR);
										SPDSettings.toolbarMode(Toolbar.Mode.CENTER.name());
										Toolbar.updateLayout();
									}
								};
								if (SPDSettings.toolbarMode().equals(Toolbar.Mode.CENTER.name())) {
									btnCentered.textColor(TITLE_COLOR);
								}
								add(btnCentered);

								chkQuickSwapper = new CheckBox(Messages.get(WndSettings.UITab.this, "quickslot_swapper"), TEXT_SIZE) {
									@Override
									protected void onClick() {
										super.onClick();
										SPDSettings.quickSwapper(checked());
										Toolbar.updateLayout();
									}
								};
								chkQuickSwapper.checked(SPDSettings.quickSwapper());
								add(chkQuickSwapper);

								swapperDesc = PixelScene.renderTextBlock(Messages.get(WndSettings.UITab.this, "swapper_desc"), 5);
								swapperDesc.hardlight(0x888888);
								add(swapperDesc);

								chkFlipToolbar = new CheckBox(Messages.get(WndSettings.UITab.this, "flip_toolbar"), TEXT_SIZE) {
									@Override
									protected void onClick() {
										super.onClick();
										SPDSettings.flipToolbar(checked());
										Toolbar.updateLayout();
									}
								};
								chkFlipToolbar.checked(SPDSettings.flipToolbar());
								add(chkFlipToolbar);

								chkFlipTags = new CheckBox(Messages.get(WndSettings.UITab.this, "flip_indicators"), TEXT_SIZE){
									@Override
									protected void onClick() {
										super.onClick();
										SPDSettings.flipTags(checked());
										GameScene.layoutTags();
									}
								};
								chkFlipTags.checked(SPDSettings.flipTags());
								add(chkFlipTags);

								//layout
								resize(WIDTH_P, 0);

								barDesc.setPos((width - barDesc.width()) / 2f, GAP);
								PixelScene.align(barDesc);

								int btnWidth = (int) (width - 2 * GAP) / 3;
								btnSplit.setRect(0, barDesc.bottom() + 2*GAP, btnWidth, BTN_HEIGHT);
								btnGrouped.setRect(btnSplit.right() + GAP, btnSplit.top(), btnWidth, BTN_HEIGHT);
								btnCentered.setRect(btnGrouped.right() + GAP, btnSplit.top(), btnWidth, BTN_HEIGHT);

								chkQuickSwapper.setRect(0, btnGrouped.bottom() + GAP, width, BTN_HEIGHT);

								swapperDesc.maxWidth(width);
								swapperDesc.setPos(0, chkQuickSwapper.bottom()+1);

								if (width > 200) {
									chkFlipToolbar.setRect(0, swapperDesc.bottom() + GAP, width / 2 - 1, BTN_HEIGHT);
									chkFlipTags.setRect(chkFlipToolbar.right() + GAP, chkFlipToolbar.top(), width / 2 - 1, BTN_HEIGHT);
								} else {
									chkFlipToolbar.setRect(0, swapperDesc.bottom() + GAP, width, BTN_HEIGHT);
									chkFlipTags.setRect(0, chkFlipToolbar.bottom() + GAP, width, BTN_HEIGHT);
								}

								resize(WIDTH_P, (int)chkFlipTags.bottom());

								// Register focusable buttons
								addFocusableButton(btnSplit);
								addFocusableButton(btnGrouped);
								addFocusableButton(btnCentered);
								addFocusableButton(chkQuickSwapper);
								addFocusableButton(chkFlipToolbar);
								addFocusableButton(chkFlipTags);
							}
						});
					}
				};
				add(btnToolbarSettings);

			} else {

				chkFlipTags = new CheckBox(Messages.get(this, "flip_indicators"), TEXT_SIZE) {
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.flipTags(checked());
						GameScene.layoutTags();
					}
				};
				chkFlipTags.checked(SPDSettings.flipTags());
				add(chkFlipTags);

			}

		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + 2);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2;

			height = sep1.y + 1;

			boolean hasSliders = (optUIMode != null || optUIScale != null);

			if (optUIMode != null && optUIScale != null && width > 200){
				optUIMode.setRect(0, height + GAP, width/2-1, SLIDER_HEIGHT);
				optUIScale.setRect(width/2+1, height + GAP, width/2-1, SLIDER_HEIGHT);
				height = optUIScale.bottom();
			} else {
				if (optUIMode != null) {
					optUIMode.setRect(0, height + GAP, width, SLIDER_HEIGHT);
					height = optUIMode.bottom();
				}

				if (optUIScale != null) {
					optUIScale.setRect(0, height + GAP, width, SLIDER_HEIGHT);
					height = optUIScale.bottom();
				}
			}

			// When no sliders: hide sep1, position sep2 at same y as other tabs' sep1
			// When sliders present: show both separators normally
			sep1.visible = false;

			sep2.size(width, 1);
			if (hasSliders) {
				sep2.y = height + GAP;
			} else {
				sep2.y = title.bottom() + 2;
			}
			height = sep2.y + 1;

			if (btnToolbarSettings != null) {
				btnToolbarSettings.setRect(0, height + GAP, width, BTN_HEIGHT);
				height = btnToolbarSettings.bottom();
			} else {
				chkFlipTags.setRect(0, height + GAP, width, BTN_HEIGHT);
				height = chkFlipTags.bottom();
			}

			height = height + GAP;
		}

	}

	private static class InputTab extends Component{

		RenderedTextBlock title;
		ColorBlock sep1;

		RedButton btnKeyBindings;
		RedButton btnControllerBindings;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			if (DeviceCompat.hasHardKeyboard()){

				btnKeyBindings = new RedButton(Messages.get(this, "key_bindings"), TEXT_SIZE){
					@Override
					protected void onClick() {
						super.onClick();
						SacredPixelDungeon.scene().addToFront(new WndKeyBindings(false));
					}
				};

				add(btnKeyBindings);
			}

			if (ControllerHandler.isControllerConnected()){
				btnControllerBindings = new RedButton(Messages.get(this, "controller_bindings"), TEXT_SIZE){
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
			title.setPos((width - title.width())/2, y + 2);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2;

			height = sep1.y+1;

			if (width > 200 && btnKeyBindings != null && btnControllerBindings != null){
				btnKeyBindings.setRect(0, height + GAP, width/2-1, BTN_HEIGHT);
				btnControllerBindings.setRect(width/2+1, height + GAP, width/2-1, BTN_HEIGHT);
				height = btnControllerBindings.bottom();
			} else {
				if (btnKeyBindings != null) {
					btnKeyBindings.setRect(0, height + GAP, width, BTN_HEIGHT);
					height = btnKeyBindings.bottom();
				}

				if (btnControllerBindings != null) {
					btnControllerBindings.setRect(0, height + GAP, width, BTN_HEIGHT);
					height = btnControllerBindings.bottom();
				}
			}
		}
	}

	private static class AudioTab extends Component {

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
			title.hardlight(TITLE_COLOR);
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

			chkMusicMute = new CheckBox(Messages.get(this, "music_mute"), TEXT_SIZE){
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
					if (Random.Int(100) == 0){
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

			chkMuteSFX = new CheckBox( Messages.get(this, "sfx_mute"), TEXT_SIZE ) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.soundFx(!checked());
					Sample.INSTANCE.play( Assets.Sounds.CLICK );
				}
			};
			chkMuteSFX.checked(!SPDSettings.soundFx());
			add( chkMuteSFX );

			if (DeviceCompat.isiOS()){

				sep3 = new ColorBlock(1, 1, 0xFF000000);
				add(sep3);
				sep3.visible = false;

				chkIgnoreSilent = new CheckBox( Messages.get(this, "ignore_silent"), TEXT_SIZE ){
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.ignoreSilentMode(checked());
					}
				};
				chkIgnoreSilent.checked(SPDSettings.ignoreSilentMode());
				add(chkIgnoreSilent);

			} else if (DeviceCompat.isDesktop()){

				sep3 = new ColorBlock(1, 1, 0xFF000000);
				add(sep3);
				sep3.visible = false;

				chkMusicBG = new CheckBox( Messages.get(this, "music_bg"), TEXT_SIZE ){
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
			title.setPos((width - title.width())/2, y + 2);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2;

			if (width > 200) {
				optMusic.setRect(0, sep1.y + 1 + GAP, width/2-1, SLIDER_HEIGHT);
				chkMusicMute.setRect(0, optMusic.bottom() + GAP, width/2-1, BTN_HEIGHT);

				sep2.size(width, 1);
				sep2.y = sep1.y; //just have them overlap

				optSFX.setRect(optMusic.right()+2, sep2.y + 1 + GAP, width/2-1, SLIDER_HEIGHT);
				chkMuteSFX.setRect(chkMusicMute.right()+2, optSFX.bottom() + GAP, width/2-1, BTN_HEIGHT);

			} else {
				optMusic.setRect(0, sep1.y + 1 + GAP, width, SLIDER_HEIGHT);
				chkMusicMute.setRect(0, optMusic.bottom() + GAP, width, BTN_HEIGHT);

				sep2.size(width, 1);
				sep2.y = chkMusicMute.bottom() + GAP;

				optSFX.setRect(0, sep2.y + 1 + GAP, width, SLIDER_HEIGHT);
				chkMuteSFX.setRect(0, optSFX.bottom() + GAP, width, BTN_HEIGHT);
			}

			height = chkMuteSFX.bottom();

			if (chkIgnoreSilent != null){
				sep3.size(width, 1);
				sep3.y = chkMuteSFX.bottom() + GAP;

				chkIgnoreSilent.setRect(0, sep3.y + 1 + GAP, width, BTN_HEIGHT);
				height = chkIgnoreSilent.bottom();
			} else if (chkMusicBG != null){
				sep3.size(width, 1);
				sep3.y = chkMuteSFX.bottom() + GAP;

				chkMusicBG.setRect(0, sep3.y + 1 + GAP, width, BTN_HEIGHT);
				height = chkMusicBG.bottom();
			}
		}

	}

	private static class LangsTab extends Component{

		final static int COLS_P = 3;
		final static int COLS_L = 6;

		final static int BTN_HEIGHT = 11;

		RenderedTextBlock title;
		ColorBlock sep1;
		RedButton[] lanBtns;
		ColorBlock sep3;
		RenderedTextBlock txtTranifex;
		RedButton btnCredits;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 8);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);
			sep1.visible = false;

			//Custom language order: English, Spanish, Korean, Japanese, Chinese SC/TC,
			//Latin-based, Russian-group, Southeast Asian, special languages
			final ArrayList<Languages> langs = new ArrayList<>();
			langs.add(Languages.ENGLISH);
			langs.add(Languages.SPANISH);
			langs.add(Languages.KOREAN);
			langs.add(Languages.JAPANESE);
			langs.add(Languages.CHI_SMPL);
			langs.add(Languages.CHI_TRAD);
			//Latin-based
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
			//Russian-group
			langs.add(Languages.RUSSIAN);
			langs.add(Languages.UKRANIAN);
			langs.add(Languages.BELARUSIAN);
			langs.add(Languages.GREEK);
			//Southeast Asian
			langs.add(Languages.VIETNAMESE);
			langs.add(Languages.INDONESIAN);
			//Special
			langs.add(Languages.ESPERANTO);

			final Languages currLang = Messages.lang();

			lanBtns = new RedButton[langs.size()];
			for (int i = 0; i < langs.size(); i++){
				final int langIndex = i;
				RedButton btn = new RedButton(Messages.titleCase(langs.get(i).nativeName()), 6){
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
								//do nothing
							}
						});
					}
				};
				if (currLang == langs.get(i)){
					btn.textColor(TITLE_COLOR);
				}
				lanBtns[i] = btn;
				add(btn);
			}

			sep3 = new ColorBlock(1, 1, 0xFF000000);
			add(sep3);
			sep3.visible = false;

			txtTranifex = PixelScene.renderTextBlock(5);
			txtTranifex.text(Messages.get(this, "transifex"));
			add(txtTranifex);

			if (currLang != Languages.ENGLISH) {
				String credText = Messages.titleCase(Messages.get(this, "credits"));
				btnCredits = new RedButton(credText, 6) {
					@Override
					protected void onClick() {
						super.onClick();
						String[] reviewers = currLang.reviewers();
						String[] translators = currLang.translators();

						int totalCredits = 2*reviewers.length + translators.length;
						int totalTokens = 2*totalCredits; //for spaces

						//additional space for titles, and newline chars
						if (reviewers.length > 0) totalTokens+=6;
						totalTokens +=4;

						String[] entries = new String[totalTokens];
						int index = 0;
						if (reviewers.length > 0){
							entries[0] = "_";
							entries[1] = Messages.titleCase(Messages.get(LangsTab.this, "reviewers"));
							entries[2] = "_";
							entries[3] = "\n";
							index = 4;
							for (int i = 0; i < reviewers.length; i++){
								entries[index] = reviewers[i];
								if (i < reviewers.length-1) entries[index] += ", ";
								entries[index+1] = " ";
								index += 2;
							}
							entries[index] = "\n";
							entries[index+1] = "\n";
							index += 2;
						}

						entries[index] = "_";
						entries[index+1] = Messages.titleCase(Messages.get(LangsTab.this, "translators"));
						entries[index+2] = "_";
						entries[index+3] = "\n";
						index += 4;

						//reviewers are also shown as translators
						for (int i = 0; i < reviewers.length; i++){
							entries[index] = reviewers[i];
							if (i < reviewers.length-1 || translators.length > 0) entries[index] += ", ";
							entries[index+1] = " ";
							index += 2;
						}

						for (int i = 0; i < translators.length; i++){
							entries[index] = translators[i];
							if (i < translators.length-1) entries[index] += ", ";
							entries[index+1] = " ";
							index += 2;
						}

						Window credits = new Window(0, 0, Chrome.get(Chrome.Type.TOAST));

						int w = PixelScene.landscape() ? 120 : 80;
						if (totalCredits >= 25) w *= 1.5f;

						RenderedTextBlock title = PixelScene.renderTextBlock(8);
						title.text(Messages.titleCase(Messages.get(LangsTab.this, "credits")), w);
						title.hardlight(TITLE_COLOR);
						title.setPos((w - title.width()) / 2, 0);
						credits.add(title);

						RenderedTextBlock text = PixelScene.renderTextBlock(6);
						text.maxWidth(w);
						text.tokens(entries);

						text.setPos(0, title.bottom() + 4);
						credits.add(text);

						credits.resize(w, (int) text.bottom() + 2);
						SacredPixelDungeon.scene().addToFront(credits);
					}
				};
				add(btnCredits);
			}

		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + 2);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2;

			y = sep1.y + 1 + GAP;
			int x = 0;

			int cols = PixelScene.landscape() ? COLS_L : COLS_P;
			int colIdx = 0;
			for (RedButton btn : lanBtns){
				//distribute width evenly across columns, absorbing rounding remainder
				int colStart = (int)(width * colIdx / (float)cols);
				int colEnd = (int)(width * (colIdx+1) / (float)cols);
				int bw = colEnd - colStart - 1;
				btn.setRect(x, y, bw, BTN_HEIGHT);
				btn.setPos(x, y);
				x += bw + 1;
				colIdx++;
				if (colIdx >= cols){
					colIdx = 0;
					x = 0;
					y += BTN_HEIGHT+1;
				}
			}
			if (x > 0){
				y += BTN_HEIGHT+1;
			}

			sep3.size(width, 1);
			sep3.y = y;
			y += 4*GAP;

			txtTranifex.setPos(0, y);
			txtTranifex.maxWidth((int)width);

			if (btnCredits != null){
				float creditsY = txtTranifex.bottom() + 4*GAP;
				btnCredits.setSize(width, BTN_HEIGHT);
				btnCredits.setPos(0, creditsY);

				height = btnCredits.bottom();
			} else {
				height = txtTranifex.bottom();
			}

		}
	}
}
