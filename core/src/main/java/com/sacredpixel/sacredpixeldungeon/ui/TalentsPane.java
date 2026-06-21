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

package com.sacredpixel.sacredpixeldungeon.ui;

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.sacredpixel.sacredpixeldungeon.effects.Speck;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Signal;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TalentsPane extends ScrollPane {

	ArrayList<TalentTierPane> panes = new ArrayList<>();
	ArrayList<ColorBlock> separators = new ArrayList<>();

	ColorBlock sep;
	ColorBlock blocker;
	RenderedTextBlock blockText;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private int focusTierIndex = 0;
	private int focusTalentIndex = -1;

	public TalentsPane( TalentButton.Mode mode ) {
		this( mode, Dungeon.hero.talents );
	}

	public TalentsPane( TalentButton.Mode mode, ArrayList<LinkedHashMap<Talent, Integer>> talents ) {
		super(new Component());

		Ratmogrify.useRatroicEnergy = Dungeon.hero != null && Dungeon.hero.armorAbility instanceof Ratmogrify;

		int tiersAvailable = 1;

		if (mode == TalentButton.Mode.INFO){
			if (!Badges.isUnlocked(Badges.Badge.LEVEL_REACHED_2) && !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2)){
				tiersAvailable = 2;
			} else if (!Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4)){
				tiersAvailable = 3;
			} else {
				tiersAvailable = Talent.MAX_TALENT_TIERS;
			}
		} else {
			while (tiersAvailable < Talent.MAX_TALENT_TIERS
					&& Dungeon.hero.lvl+1 >= Talent.tierLevelThresholds[tiersAvailable+1]){
				tiersAvailable++;
			}
			if (tiersAvailable > 2 && Dungeon.hero.subClass == HeroSubClass.NONE){
				tiersAvailable = 2;
			} else if (tiersAvailable > 3 && Dungeon.hero.armorAbility == null){
				tiersAvailable = 3;
			}
		}

		tiersAvailable = Math.min(tiersAvailable, talents.size());

		for (int i = 0; i < Math.min(tiersAvailable, talents.size()); i++){
			if (talents.get(i).isEmpty()) continue;

			TalentTierPane pane = new TalentTierPane(talents.get(i), i+1, mode);
			panes.add(pane);
			content.add(pane);

			ColorBlock sep = new ColorBlock(0, 1, 0xFF000000);
			separators.add(sep);
			content.add(sep);
		}

		sep = new ColorBlock(0, 1, 0xFF000000);
		content.add(sep);

		blocker = new ColorBlock(0, 0, 0xFF222222);
		content.add(blocker);

		if (mode != TalentButton.Mode.INFO) {
			if (tiersAvailable == 1) {
				blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier2"), 6);
				content.add(blockText);
			} else if (tiersAvailable == 2) {
				blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier3"), 6);
				content.add(blockText);
			} else if (tiersAvailable == 3) {
				blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier4"), 6);
				content.add(blockText);
			} else {
				blockText = null;
			}
		} else {
			blockText = null;
		}

		for (int i = panes.size()-1; i >= 0; i--){
			content.bringToFront(panes.get(i));
		}

		// Setup keyboard navigation
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				// Don't process events if not visible or not active
				if (!visible || !active || !event.pressed || panes.isEmpty()) return false;

				// Check entire parent chain for visibility/activity
				// This is important for tabbed windows where the tab content might be hidden
				com.watabou.noosa.Group p = parent;
				while (p != null) {
					if (!p.visible || !p.active) return false;
					if (p instanceof com.watabou.noosa.Group) {
						p = ((com.watabou.noosa.Group) p).parent;
					} else {
						break;
					}
				}

				GameAction action = KeyBindings.getActionForKey(event);

				// Up/Down for tier navigation
				if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE) {
					moveTierFocus(-1);
					return true;
				} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE) {
					moveTierFocus(1);
					return true;
				}
				// Left/Right for talent navigation within tier
				else if (action == SPDAction.W) {
					moveTalentFocus(-1);
					return true;
				} else if (action == SPDAction.E) {
					moveTalentFocus(1);
					return true;
				}
				// Enter to select/upgrade talent
				else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
						|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
					if (focusTierIndex >= 0 && focusTierIndex < panes.size() &&
							focusTalentIndex >= 0) {
						TalentTierPane tier = panes.get(focusTierIndex);
						if (focusTalentIndex < tier.buttons.size()) {
							TalentButton btn = tier.buttons.get(focusTalentIndex);
							if (btn.active) {
								Sample.INSTANCE.play(Assets.Sounds.CLICK);
								btn.click();
							}
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
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		super.destroy();
	}

	public void moveTierFocus(int direction) {
		// Clear previous highlight
		clearTalentHighlight();

		// Move tier index
		focusTierIndex += direction;
		if (focusTierIndex < 0) focusTierIndex = panes.size() - 1;
		if (focusTierIndex >= panes.size()) focusTierIndex = 0;

		// Reset talent index to first talent in new tier
		focusTalentIndex = 0;

		// Apply highlight
		applyTalentHighlight();

		// Scroll to make the tier visible
		scrollToTier(focusTierIndex);
	}

	public void moveTalentFocus(int direction) {
		if (panes.isEmpty() || focusTierIndex >= panes.size()) return;

		TalentTierPane tier = panes.get(focusTierIndex);
		if (tier.buttons.isEmpty()) return;

		// Clear previous highlight
		clearTalentHighlight();

		// Move talent index
		if (focusTalentIndex == -1) {
			focusTalentIndex = direction > 0 ? 0 : tier.buttons.size() - 1;
		} else {
			focusTalentIndex += direction;
			if (focusTalentIndex < 0) focusTalentIndex = tier.buttons.size() - 1;
			if (focusTalentIndex >= tier.buttons.size()) focusTalentIndex = 0;
		}

		// Apply highlight
		applyTalentHighlight();
	}

	private void clearTalentHighlight() {
		if (focusTierIndex >= 0 && focusTierIndex < panes.size() &&
				focusTalentIndex >= 0) {
			TalentTierPane tier = panes.get(focusTierIndex);
			if (focusTalentIndex < tier.buttons.size()) {
				TalentButton btn = tier.buttons.get(focusTalentIndex);
				btn.icon.resetColor();
				btn.bg.resetColor();
			}
		}
	}

	private void applyTalentHighlight() {
		if (focusTierIndex >= 0 && focusTierIndex < panes.size() &&
				focusTalentIndex >= 0) {
			TalentTierPane tier = panes.get(focusTierIndex);
			if (focusTalentIndex < tier.buttons.size()) {
				TalentButton btn = tier.buttons.get(focusTalentIndex);
				btn.icon.brightness(1.3f);
				btn.bg.brightness(1.3f);
			}
		}
	}

	private void scrollToTier(int tierIndex) {
		if (tierIndex >= 0 && tierIndex < panes.size()) {
			TalentTierPane tier = panes.get(tierIndex);
			// Scroll so the tier is visible
			float targetY = tier.top() - 5;
			if (targetY < 0) targetY = 0;
			scrollTo(0, targetY);
		}
	}

	// Talent focus state save/restore methods for WndHero tab switching
	public int[] saveTalentFocusState() {
		return new int[] { focusTierIndex, focusTalentIndex };
	}

	public void restoreTalentFocusState(int[] state) {
		if (state == null || state.length < 2) return;
		focusTierIndex = state[0];
		focusTalentIndex = state[1];
		applyTalentHighlight();
	}

	public void clearFocus() {
		clearTalentHighlight();
		focusTierIndex = 0;
		focusTalentIndex = -1;
	}

	public void initializeFocus() {
		// Start focus on T1's first talent
		focusTierIndex = 0;
		focusTalentIndex = 0;
		applyTalentHighlight();
	}

	public boolean hasFocus() {
		return focusTalentIndex >= 0;
	}

	public void activateFocused() {
		if (focusTierIndex >= 0 && focusTierIndex < panes.size() &&
				focusTalentIndex >= 0) {
			TalentTierPane tier = panes.get(focusTierIndex);
			if (focusTalentIndex < tier.buttons.size()) {
				TalentButton btn = tier.buttons.get(focusTalentIndex);
				if (btn.active) {
					Sample.INSTANCE.play(Assets.Sounds.CLICK);
					btn.click();
				}
			}
		}
	}

	@Override
	public void onClick(float x, float y) {
		// Forward clicks to TalentButtons
		for (int paneIdx = 0; paneIdx < panes.size(); paneIdx++) {
			TalentTierPane pane = panes.get(paneIdx);

			for (int btnIdx = 0; btnIdx < pane.buttons.size(); btnIdx++) {
				TalentButton btn = pane.buttons.get(btnIdx);
				if (btn.inside(x, y)) {
					btn.click();
					return;
				}
			}
		}
	}

	@Override
	protected void layout() {
		super.layout();

		float top = 0;
		for (int i = 0; i < panes.size(); i++){
			top += 2;
			// Use 0 for x position since panes are inside content, which is at (0,0)
			panes.get(i).setRect(0, top, width, 0);
			top = panes.get(i).bottom();

			separators.get(i).visible = false;

			top += 3;

		}

		float bottom;
		if (blockText != null) {
			blockText.maxWidth((int) width);
			blockText.align(RenderedTextBlock.CENTER_ALIGN);
			float minBlockerH = blockText.height() + 10;
			bottom = Math.max(height, top + minBlockerH);

			blocker.x = 0;
			blocker.y = top;
			blocker.size(width, bottom - top);

			blockText.setPos((width - blockText.width()) / 2f, blocker.y + (bottom - blocker.y - blockText.height()) / 2);
		} else {
			bottom = Math.max(height, top);

			blocker.visible = false;
		}

		content.setSize(width, bottom);
	}

	public static class TalentTierPane extends Component {

		private int tier;

		public RenderedTextBlock title;
		public ArrayList<TalentButton> buttons;

		ArrayList<Image> stars = new ArrayList<>();

		public TalentTierPane(LinkedHashMap<Talent, Integer> talents, int tier, TalentButton.Mode mode){
			super();

			this.tier = tier;

			title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(TalentsPane.class, "tier", tier)), 8);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			if (mode == TalentButton.Mode.UPGRADE) {
				setupStars();
			}

			buttons = new ArrayList<>();
			for (Talent talent : talents.keySet()){
				TalentButton btn = new TalentButton(tier, talent, talents.get(talent), mode){
					@Override
					public void upgradeTalent() {
						super.upgradeTalent();
						if (parent != null) {
							setupStars();
							TalentTierPane.this.layout();
						}
					}
				};
				buttons.add(btn);
				add(btn);
			}

		}

		private void setupStars(){
			if (!stars.isEmpty()){
				for (Image im : stars){
					im.killAndErase();
				}
				stars.clear();
			}

			int totStars = Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier] + Dungeon.hero.bonusTalentPoints(tier);
			int openStars = Dungeon.hero.talentPointsAvailable(tier);
			int usedStars = Dungeon.hero.talentPointsSpent(tier);
			for (int i = 0; i < totStars; i++){
				Image im = new Speck().image(Speck.STAR);
				stars.add(im);
				add(im);
				if (i >= openStars && i < (openStars + usedStars)){
					im.tint(0.75f, 0.75f, 0.75f, 0.9f);
				} else if (i >= (openStars + usedStars)){
					im.tint(0f, 0f, 0f, 0.9f);
				}
			}
		}

		@Override
		protected void layout() {
			super.layout();

			int regStars = Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier];

			float titleWidth = title.width();
			titleWidth += 2 + Math.min(stars.size(), regStars)*6;
			title.setPos(x + (width - titleWidth)/2f, y);

			float left = title.right() + 2;

			float starTop = title.top();
			if (regStars < stars.size()) starTop -= 2;

			for (Image star : stars){
				star.x = left;
				star.y = starTop;
				PixelScene.align(star);
				left += 6;
				regStars--;
				if (regStars == 0){
					starTop += 6;
					left = title.right() + 2;
				}
			}

			float gap = (width - buttons.size()*TalentButton.WIDTH)/(buttons.size()+1);
			left = x + gap;
			for (TalentButton btn : buttons){
				btn.setPos(left, title.bottom() + 4);
				PixelScene.align(btn);
				left += btn.width() + gap;
			}

			height = buttons.get(0).bottom() - y;

		}

	}
}
