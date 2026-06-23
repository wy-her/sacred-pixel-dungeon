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
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.scenes.TitleScene;
import com.sacredpixel.sacredpixeldungeon.sprites.HeroSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialManager;
import com.sacredpixel.sacredpixeldungeon.Promotion;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.ControllerHandler;
import com.watabou.input.KeyBindings;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

/**
 * Tutorial hint windows with icon support.
 */
public class WndTutorial extends Window {

	private static final int WIDTH_P = 149;
	private static final int WIDTH_L = 251;
	private static final int BTN_HEIGHT = 16;
	private static final int MARGIN = 2;
	private static final int ICON_SIZE = 16;

	private Runnable onClose;

	public WndTutorial(Image icon, String title, String message) {
		this(icon, title, message, null);
	}

	public WndTutorial(Image icon, String title, String message, Runnable onClose) {
		super();

		this.onClose = onClose;

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float top = MARGIN;

		// Icon (if provided)
		if (icon != null) {
			icon.x = MARGIN;
			icon.y = MARGIN;
			add(icon);
			top = icon.y + icon.height() + MARGIN;
		}

		// Title (size 8, matching WndOptions)
		RenderedTextBlock titleBlock = PixelScene.renderTextBlock(title, 8);
		titleBlock.hardlight(Window.TITLE_COLOR);
		titleBlock.maxWidth(width - MARGIN * 2);
		if (icon != null) {
			// Title next to icon
			titleBlock.setPos(icon.x + icon.width() + MARGIN, MARGIN + (icon.height() - titleBlock.height()) / 2f);
			top = Math.max(top, icon.y + icon.height() + MARGIN);
		} else {
			titleBlock.setPos((width - titleBlock.width()) / 2f, MARGIN);
			top = titleBlock.bottom() + MARGIN;
		}
		PixelScene.align(titleBlock);
		add(titleBlock);

		// Message (size 6, matching WndOptions)
		RenderedTextBlock msgBlock = PixelScene.renderTextBlock(message, 6);
		msgBlock.maxWidth(width - MARGIN * 2);
		msgBlock.setPos(MARGIN, top + MARGIN);
		add(msgBlock);

		float pos = msgBlock.bottom() + 2 * MARGIN;

		// OK Button
		RedButton btnOK = new RedButton(Messages.get(this, "ok")) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnOK.setRect(MARGIN, pos, width - MARGIN * 2, BTN_HEIGHT);
		add(btnOK);
		addFocusableButton(btnOK);

		resize(width, (int) btnOK.bottom() + MARGIN);
	}

	@Override
	public void hide() {
		super.hide();
		if (onClose != null) {
			onClose.run();
		}
	}

	// Prevent closing via ESC key or clicking outside the window
	// Tutorial windows should only be closed by clicking the OK button
	@Override
	public void onBackPressed() {
		// Do nothing - force user to click OK button
	}

	// Helper method to get key binding string
	private static String getKeyString(com.watabou.input.GameAction action) {
		return KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(action, ControllerHandler.isControllerConnected()));
	}

	// Factory methods for each tutorial step

	// Step 4: Journal hint - Book icon (MASTERY)
	public static WndTutorial createJournalHint() {
		String key = getKeyString(SPDAction.JOURNAL);
		String msg = Messages.get(WndTutorial.class, "journal_msg", key);
		GLog.p(msg);
		return new WndTutorial(
				new ItemSprite(ItemSpriteSheet.MASTERY),
				Messages.get(WndTutorial.class, "journal_title"),
				msg
		);
	}

	// Step 6: Hero info hint - Warrior avatar icon (with cloth armor)
	public static WndTutorial createHeroInfoHint() {
		String key = getKeyString(SPDAction.HERO_INFO);
		String msg = Messages.get(WndTutorial.class, "hero_info_msg", key);
		GLog.p(msg);
		Image heroIcon = HeroSprite.avatar(HeroClass.WARRIOR, 1); // tier 1 = cloth armor
		return new WndTutorial(
				heroIcon,
				Messages.get(WndTutorial.class, "hero_info_title"),
				msg
		);
	}

	// Step 8: Examine hint - Magnifying glass icon
	public static WndTutorial createExamineHint() {
		String key = getKeyString(SPDAction.EXAMINE);
		String msg = Messages.get(WndTutorial.class, "examine_msg", key);
		GLog.p(msg);
		return new WndTutorial(
				Icons.get(Icons.MAGNIFY),
				Messages.get(WndTutorial.class, "examine_title"),
				msg
		);
	}

	// Step 10: Combat hint - Rat icon (static image)
	public static WndTutorial createCombatHint() {
		String key = getKeyString(SPDAction.TAG_ATTACK);
		String msg = Messages.get(WndTutorial.class, "combat_msg", key);
		GLog.p(msg);
		TutorialManager.stopFlashExamine();
		// Create static rat icon from first frame (16x15 pixels)
		Image ratIcon = new Image(TextureCache.get(Assets.Sprites.RAT));
		TextureFilm ratFrames = new TextureFilm(ratIcon.texture, 16, 15);
		ratIcon.frame(ratFrames.get(0));
		return new WndTutorial(
				ratIcon,
				Messages.get(WndTutorial.class, "combat_title"),
				msg
		);
	}

	// Step 12a: Liquid Flame potion hint - burn the barricade
	public static WndTutorial createLiquidFlameHint() {
		String msg = Messages.get(WndTutorial.class, "liquid_flame_msg");
		GLog.p(msg.replace("\n\n", " "));
		TutorialManager.stopFlashAttack();
		return new WndTutorial(
				new ItemSprite(ItemSpriteSheet.POTION_HOLDER),
				Messages.get(WndTutorial.class, "liquid_flame_title"),
				msg
		);
	}

	// Step 12b: Frost potion hint - extinguish the fire
	public static WndTutorial createFrostHint() {
		String msg = Messages.get(WndTutorial.class, "frost_msg");
		GLog.p(msg.replace("\n\n", " "));
		return new WndTutorial(
				new ItemSprite(ItemSpriteSheet.POTION_HOLDER),
				Messages.get(WndTutorial.class, "frost_title"),
				msg
		);
	}

	// Step 12c: Scroll hint
	public static WndTutorial createScrollHint() {
		String msg = Messages.get(WndTutorial.class, "scroll_msg");
		GLog.p(msg.replace("\n\n", " "));
		return new WndTutorial(
				new ItemSprite(ItemSpriteSheet.SCROLL_HOLDER),
				Messages.get(WndTutorial.class, "scroll_title"),
				msg
		);
	}

	// Step 14: Search hint - Magnifying glass icon
	public static WndTutorial createSearchHint() {
		String key = getKeyString(SPDAction.EXAMINE);
		String msg = Messages.get(WndTutorial.class, "search_msg", key);
		GLog.p(msg.replace("\n\n", "\n"));
		return new WndTutorial(
				Icons.get(Icons.MAGNIFY),
				Messages.get(WndTutorial.class, "search_title"),
				msg
		);
	}

	// Step 16a: Wait for snake hint - Wait button icon
	public static WndTutorial createWaitHint() {
		String key = getKeyString(SPDAction.WAIT);
		String msg = Messages.get(WndTutorial.class, "wait_msg", key);
		GLog.p(msg.replace("\n\n", "\n"));
		TutorialManager.stopFlashExamine();
		// Use the actual wait button icon from toolbar (176, 0, 16, 16)
		Image waitIcon = new Image(Assets.Interfaces.TOOLBAR);
		waitIcon.frame(176, 0, 16, 16);
		return new WndTutorial(
				waitIcon,
				Messages.get(WndTutorial.class, "wait_title"),
				msg
		);
	}

	// Step 16b: Surprise attack hint - Snake icon (static image)
	public static WndTutorial createSurpriseAttackHint() {
		String key = getKeyString(SPDAction.TAG_ATTACK);
		String msg = Messages.get(WndTutorial.class, "surprise_attack_msg", key);
		GLog.p(msg.replace("\n\n", "\n"));
		TutorialManager.stopFlashWait();
		// Create static snake icon from first frame (12x11 pixels)
		Image snakeIcon = new Image(TextureCache.get(Assets.Sprites.SNAKE));
		TextureFilm snakeFrames = new TextureFilm(snakeIcon.texture, 12, 11);
		snakeIcon.frame(snakeFrames.get(0));
		return new WndTutorial(
				snakeIcon,
				Messages.get(WndTutorial.class, "surprise_attack_title"),
				msg
		);
	}

	// Legacy: old surprise hint
	@Deprecated
	public static WndTutorial createSurpriseHint() {
		return createWaitHint();
	}

	// Step 18: Completion - Trophy icon (gold)
	public static WndTutorial createCompletion() {
		String msg = Messages.get(WndTutorial.class, "complete_msg");
		Image trophyIcon = Icons.get(Icons.CHALLENGE_COLOR);
		return new WndTutorial(
				trophyIcon,
				Messages.get(WndTutorial.class, "complete_title"),
				msg,
				() -> {
					// Grant promotion reward on Appsintoss (tutorial completion)
					if (Promotion.isAvailable()) {
						Promotion.grantTutorialReward((success, message) -> {
							if (success) {
								GLog.p(Messages.get(WndTutorial.class, "reward_granted"));
							}
						});
					}
					TutorialManager.reset();
					Game.switchScene(TitleScene.class);
				}
		);
	}

	// Legacy methods for compatibility (deprecated)
	@Deprecated
	public static WndTutorial createItemHint() {
		return createLiquidFlameHint();
	}

	@Deprecated
	public static WndTutorial createExamineTileHint() {
		return createExamineHint();
	}

	@Deprecated
	public static WndTutorial createPotionHint() {
		return createLiquidFlameHint();
	}

	@Deprecated
	public static WndTutorial createScrollUsedHint() {
		return createFrostHint();
	}

	@Deprecated
	public static WndTutorial createPotionUsedHint() {
		return createSearchHint();
	}

	@Deprecated
	public static WndTutorial createSurpriseDoorHint() {
		return createWaitHint();
	}
}
