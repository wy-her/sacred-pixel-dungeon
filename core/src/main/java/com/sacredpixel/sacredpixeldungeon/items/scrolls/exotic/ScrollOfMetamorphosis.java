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

package com.sacredpixel.sacredpixeldungeon.items.scrolls.exotic;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.effects.Speck;
import com.sacredpixel.sacredpixeldungeon.effects.Transmuting;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.InventoryScroll;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.TalentButton;
import com.sacredpixel.sacredpixeldungeon.ui.TalentsPane;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.sacredpixel.sacredpixeldungeon.windows.WndOptions;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.watabou.utils.Signal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class ScrollOfMetamorphosis extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_METAMORPH;

		talentFactor = 2f;
	}

	protected static boolean identifiedByUse = false;
	
	@Override
	public void doRead() {
		if (!isKnown()) {
			identify();
			curItem = detach(curUser.belongings.backpack);
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}
		GameScene.show(new WndMetamorphChoose());
	}

	public static void onMetamorph( Talent oldTalent, Talent newTalent ){
		if (curItem instanceof ScrollOfMetamorphosis) {
			((ScrollOfMetamorphosis) curItem).readAnimation();
			Sample.INSTANCE.play(Assets.Sounds.READ);
		}
		curUser.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
		Transmuting.show(curUser, oldTalent, newTalent);

		if (Dungeon.hero.hasTalent(newTalent)) {
			Talent.onTalentUpgraded(Dungeon.hero, newTalent);
		}
	}

	private void confirmCancelation( Window chooseWindow, boolean byID ) {
		GameScene.show( new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				byID ? Messages.get(InventoryScroll.class, "warning") : Messages.get(ScrollOfMetamorphosis.class, "cancel_warn"),
				Messages.get(InventoryScroll.class, "yes"),
				Messages.get(InventoryScroll.class, "no") ) {
			@Override
			protected void onSelect( int index ) {
				switch (index) {
					case 0:
						curUser.spendAndNext( TIME_TO_READ );
						identifiedByUse = false;
						chooseWindow.hide();
						break;
					case 1:
						//do nothing
						break;
				}
			}
			public void onBackPressed() {}
		} );
	}

	public static class WndMetamorphChoose extends Window {

		public static WndMetamorphChoose INSTANCE;

		TalentsPane pane;

		public WndMetamorphChoose(){
			super();

			INSTANCE = this;

			float top = 0;

			IconTitle title = new IconTitle( curItem );
			title.color( TITLE_COLOR );
			title.setRect(0, 0, 149, 0);
			add(title);

			top = title.bottom() + 2;

			RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(ScrollOfMetamorphosis.class, "choose_desc"), 6);
			text.maxWidth(149);
			text.setPos(0, top);
			add(text);

			top = text.bottom() + 2;

			ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
			Talent.initClassTalents(Dungeon.hero.heroClass, talents, Dungeon.hero.metamorphedTalents);

			for (LinkedHashMap<Talent, Integer> tier : talents){
				for (Talent talent : tier.keySet()){
					tier.put(talent, Dungeon.hero.pointsInTalent(talent));
				}
			}

			pane = new TalentsPane(TalentButton.Mode.METAMORPH_CHOOSE, talents);
			add(pane);
			pane.setPos(0, top);
			pane.setSize(149, pane.content().height());
			resize((int)pane.width(), (int)pane.bottom());
			pane.setPos(0, top);
		}

		@Override
		public void hide() {
			super.hide();
			INSTANCE = null;
		}

		@Override
		public void onBackPressed() {

			if (identifiedByUse){
				((ScrollOfMetamorphosis)curItem).confirmCancelation(this, true);
			} else {
				super.onBackPressed();
			}
		}

		@Override
		public void offset(int xOffset, int yOffset) {
			super.offset(xOffset, yOffset);
			pane.setPos(pane.left(), pane.top()); //triggers layout
		}
	}

	public static class WndMetamorphReplace extends Window implements Signal.Listener<KeyEvent> {

		public static WndMetamorphReplace INSTANCE;

		public Talent replacing;
		public int tier;
		LinkedHashMap<Talent, Integer> replaceOptions;

		// Keyboard navigation
		private TalentsPane.TalentTierPane optionsPane;
		private int focusIndex = -1;

		//for window restoring
		public WndMetamorphReplace(){
			super();

			if (INSTANCE != null){
				replacing = INSTANCE.replacing;
				tier = INSTANCE.tier;
				replaceOptions = INSTANCE.replaceOptions;
				INSTANCE = this;
				setup(replacing, tier, replaceOptions);
			} else {
				hide();
			}
		}

		public WndMetamorphReplace(Talent replacing, int tier){
			super();

			if (!identifiedByUse && curItem instanceof ScrollOfMetamorphosis) {
				curItem.detach(curUser.belongings.backpack);
			}
			identifiedByUse = false;

			INSTANCE = this;

			this.replacing = replacing;
			this.tier = tier;

			LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
			Set<Talent> curTalentsAtTier = Dungeon.hero.talents.get(tier-1).keySet();

			for (HeroClass cls : HeroClass.values()){

				ArrayList<LinkedHashMap<Talent, Integer>> clsTalents = new ArrayList<>();
				Talent.initClassTalents(cls, clsTalents);

				Set<Talent> clsTalentsAtTier = clsTalents.get(tier-1).keySet();
				boolean replacingIsInSet = false;
				for (Talent talent : clsTalentsAtTier.toArray(new Talent[0])){
					if (talent == replacing){
						replacingIsInSet = true;
						break;
					} else {
						if (curTalentsAtTier.contains(talent)){
							clsTalentsAtTier.remove(talent);
						}
					}
				}
				if (!replacingIsInSet && !clsTalentsAtTier.isEmpty()) {
					options.put(Random.element(clsTalentsAtTier), Dungeon.hero.pointsInTalent(replacing));
				}
			}

			replaceOptions = options;
			setup(replacing, tier, options);
		}

		private void setup(Talent replacing, int tier, LinkedHashMap<Talent, Integer> replaceOptions){
			float top = 0;

			IconTitle title = new IconTitle( curItem );
			title.color( TITLE_COLOR );
			title.setRect(0, 0, 149, 0);
			add(title);

			top = title.bottom() + 2;

			RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(ScrollOfMetamorphosis.class, "replace_desc"), 6);
			text.maxWidth(149);
			text.setPos(0, top);
			add(text);

			top = text.bottom() + 2;

			optionsPane = new TalentsPane.TalentTierPane(replaceOptions, tier, TalentButton.Mode.METAMORPH_REPLACE);
			add(optionsPane);
			optionsPane.title.text(" ");
			optionsPane.setPos(0, top);

			// Calculate width based on number of talents (TalentButton.WIDTH = 20)
			int numTalents = optionsPane.buttons.size();
			int minWidth = Math.max(120, numTalents * TalentButton.WIDTH + (numTalents + 1) * 4);

			optionsPane.setSize(minWidth, optionsPane.height());
			resize(minWidth, (int)optionsPane.bottom());

			// Setup keyboard navigation
			KeyEvent.addKeyListener(this);
			// No default focus - user must press arrow key to start navigating
		}

		@Override
		public boolean onSignal(KeyEvent event) {
			if (!event.pressed || optionsPane == null || optionsPane.buttons.isEmpty()) return false;

			GameAction action = KeyBindings.getActionForKey(event);

			// ESC to trigger back (confirmation dialog)
			if (action == SPDAction.BACK) {
				onBackPressed();
				return true;
			}
			// Left/Right for talent navigation
			else if (action == SPDAction.W) {
				moveTalentFocus(-1);
				return true;
			} else if (action == SPDAction.E) {
				moveTalentFocus(1);
				return true;
			}
			// Enter to select talent
			else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
					|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
				if (focusIndex >= 0 && focusIndex < optionsPane.buttons.size()) {
					TalentButton btn = optionsPane.buttons.get(focusIndex);
					if (btn.active) {
						Sample.INSTANCE.play(Assets.Sounds.CLICK);
						btn.click();
					}
				}
				return true;
			}
			return false;
		}

		private void moveTalentFocus(int direction) {
			if (optionsPane.buttons.isEmpty()) return;

			clearHighlight();

			// If no current focus, start at first or last based on direction
			if (focusIndex == -1) {
				focusIndex = (direction > 0) ? 0 : optionsPane.buttons.size() - 1;
			} else {
				focusIndex += direction;
				if (focusIndex < 0) focusIndex = optionsPane.buttons.size() - 1;
				if (focusIndex >= optionsPane.buttons.size()) focusIndex = 0;
			}

			applyHighlight();
		}

		private void clearHighlight() {
			if (focusIndex >= 0 && focusIndex < optionsPane.buttons.size()) {
				TalentButton btn = optionsPane.buttons.get(focusIndex);
				btn.setFocused(false);
			}
		}

		private void applyHighlight() {
			if (focusIndex >= 0 && focusIndex < optionsPane.buttons.size()) {
				TalentButton btn = optionsPane.buttons.get(focusIndex);
				btn.setFocused(true);
			}
		}

		@Override
		public void destroy() {
			KeyEvent.removeKeyListener(this);
			super.destroy();
		}

		@Override
		public void hide() {
			super.hide();
			if (INSTANCE == this) {
				INSTANCE = null;
			}
		}

		@Override
		public void onBackPressed() {
			if (curItem instanceof ScrollOfMetamorphosis) {
				((ScrollOfMetamorphosis) curItem).confirmCancelation(this, false);
			} else {
				super.onBackPressed();
			}
		}
	}
}
