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

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.InterstitialAd;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.HeroSprite;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.utils.DateCompat;

public class WndRegionComplete extends Window {

	private static final int WIDTH = 149;
	private static final int GAP = 4;
	private static final int BTN_HEIGHT = 16;

	// Ad load timeout - if ad not loaded within this time, skip it
	private static final float AD_LOAD_TIMEOUT = 3.0f;

	private Runnable onClose;
	private RedButton btnNextStage;
	private float adWaitTime = 0f;
	private boolean waitingForAd = false;

	public WndRegionComplete(Runnable onClose) {
		super();

		this.onClose = onClose;

		Hero hero = Dungeon.hero;
		float pos = 0;

		IconTitle title = new IconTitle();
		title.icon(HeroSprite.avatar(hero.heroClass, hero.tier()));
		String heroName = hero.name();
		String heroClassName = hero.className();
		if (heroName != null && heroName.equals(heroClassName))
			title.label(Messages.get(this, "title", hero.lvl, heroClassName).toUpperCase(java.util.Locale.ENGLISH));
		else
			title.label(((heroName != null ? heroName : "") + "\n" + Messages.get(this, "title", hero.lvl, heroClassName != null ? heroClassName : "")).toUpperCase(java.util.Locale.ENGLISH));
		title.color(TITLE_COLOR);
		title.setRect(0, 0, WIDTH, 0);
		add(title);

		pos = title.bottom() + 2*GAP;

		// Stats
		int strBonus = hero.STR() - hero.STR;
		if (strBonus > 0)       pos = statSlot(Messages.get(this, "str"), hero.STR + " + " + strBonus, pos);
		else if (strBonus < 0)  pos = statSlot(Messages.get(this, "str"), hero.STR + " - " + -strBonus, pos);
		else                    pos = statSlot(Messages.get(this, "str"), Integer.toString(hero.STR()), pos);
		pos = statSlot(Messages.get(this, "duration"), DateCompat.formatNumber((int)Statistics.duration, Messages.locale()), pos);
		pos = statSlot(Messages.get(this, "depth"), DateCompat.formatNumber(Statistics.deepestFloor, Messages.locale()), pos);

		pos += GAP;

		pos = statSlot(Messages.get(this, "enemies"), DateCompat.formatNumber(Statistics.enemiesSlain, Messages.locale()), pos);
		pos = statSlot(Messages.get(this, "gold"), DateCompat.formatNumber(Statistics.goldCollected, Messages.locale()), pos);
		pos = statSlot(Messages.get(this, "food"), DateCompat.formatNumber(Statistics.foodEaten, Messages.locale()), pos);
		pos = statSlot(Messages.get(this, "alchemy"), DateCompat.formatNumber(Statistics.itemsCrafted, Messages.locale()), pos);

		pos += GAP;

		btnNextStage = new RedButton(Messages.get(this, "next_stage")) {
			@Override
			protected void onClick() {
				super.onClick();

				// Check if ad is available but not yet preloaded
				if (InterstitialAd.isAvailable() && !InterstitialAd.isPreloaded()) {
					// Start waiting for ad to load
					waitingForAd = true;
					adWaitTime = 0f;
					text(Messages.get(WndRegionComplete.class, "loading_ad"));
					enable(false);
				} else {
					// Ad already loaded or not available - proceed immediately
					enable(false);
					triggerClose();
				}
			}
		};
		btnNextStage.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(btnNextStage);

		// Add button to focusable list for keyboard navigation
		addFocusableButton(btnNextStage);

		resize(WIDTH, (int)btnNextStage.bottom() + 1);
	}

	private void triggerClose() {
		hide();
		if (onClose != null) {
			onClose.run();
		}
	}

	@Override
	public void update() {
		super.update();

		if (waitingForAd) {
			adWaitTime += Game.elapsed;

			if (InterstitialAd.isPreloaded()) {
				// Ad loaded - proceed
				waitingForAd = false;
				triggerClose();
			} else if (adWaitTime >= AD_LOAD_TIMEOUT) {
				// Timeout - block ad and proceed without it
				waitingForAd = false;
				InterstitialAd.block();
				triggerClose();
			}
		}
	}

	@Override
	public void onBackPressed() {
		// Do nothing - window can only be closed by clicking the "Next Stage" button
	}

	@Override
	public void onConfirm() {
		// Only activate focused button, don't close on Enter when nothing is focused
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			activateFocused();
		}
		// Don't call onBackPressed() when nothing is focused
	}

	private float statSlot(String label, String value, float pos) {
		int size = 7;
		RenderedTextBlock txt;
		do {
			txt = PixelScene.renderTextBlock(label, size);
			size--;
		} while (txt.width() >= WIDTH * 0.67f);
		txt.setPos(0, pos + (6 - txt.height())/2);
		PixelScene.align(txt);
		add(txt);

		size = 7;
		do {
			txt = PixelScene.renderTextBlock(value, size);
			size--;
		} while (txt.width() >= WIDTH * 0.33f);
		txt.setPos(WIDTH * 0.67f, pos + (6 - txt.height())/2);
		PixelScene.align(txt);
		add(txt);

		return pos + GAP + txt.height();
	}
}
