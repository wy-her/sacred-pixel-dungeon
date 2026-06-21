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
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.watabou.utils.DateCompat;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.HeroSprite;
import com.sacredpixel.sacredpixeldungeon.ui.BuffIcon;
import com.sacredpixel.sacredpixeldungeon.ui.BuffIndicator;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.ScrollPane;
import com.sacredpixel.sacredpixeldungeon.ui.StatusPane;
import com.sacredpixel.sacredpixeldungeon.ui.TalentButton;
import com.sacredpixel.sacredpixeldungeon.ui.TalentsPane;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialManager;
import com.sacredpixel.sacredpixeldungeon.tutorial.TutorialState;
import com.sacredpixel.sacredpixeldungeon.utils.DungeonSeed;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Locale;

public class WndHero extends WndTabbed {

	private static final int WIDTH		= 149;
	private static final int HEIGHT		= 120;

	private StatsTab stats;
	private TalentsTab talents;
	private BuffsTab buffs;

	public static int lastIdx = 0;

	// Focus state for talents tab persistence
	private int[] savedTalentFocusState = null;

	public WndHero() {
		
		super();
		
		resize( WIDTH, HEIGHT );
		
		stats = new StatsTab();
		add( stats );

		talents = new TalentsTab();
		add(talents);
		talents.setRect(0, 0, WIDTH, HEIGHT);

		buffs = new BuffsTab();
		add( buffs );
		buffs.setRect(0, 0, WIDTH, HEIGHT);
		buffs.setupList();
		
		add( new IconTab( Icons.get(Icons.RANKINGS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) {
					lastIdx = 0;
					if (!stats.visible) {
						stats.initialize();
					}
					// Restore talents focus if switching away
					if (talents.pane.hasFocus()) {
						savedTalentFocusState = talents.pane.saveTalentFocusState();
						talents.pane.clearFocus();
					}
					clearFocus();
				}
				stats.visible = stats.active = selected;
				if (selected) {
					rebuildFocusableButtons();
				}
			}
		} );
		add( new IconTab( Icons.get(Icons.TALENT) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) {
					lastIdx = 1;
					StatusPane.talentBlink = 0;
					// Restore focus state when returning to talents tab
					if (savedTalentFocusState != null) {
						talents.pane.restoreTalentFocusState(savedTalentFocusState);
						savedTalentFocusState = null;
					}
					clearFocus();
					rebuildFocusableButtons();
				} else {
					// Save focus state when leaving talents tab
					if (talents.pane.hasFocus()) {
						savedTalentFocusState = talents.pane.saveTalentFocusState();
						talents.pane.clearFocus();
					}
				}
				talents.visible = talents.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.BUFFS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) {
					lastIdx = 2;
					// Save talents focus if switching away
					if (talents.pane.hasFocus()) {
						savedTalentFocusState = talents.pane.saveTalentFocusState();
						talents.pane.clearFocus();
					}
					clearFocus();
					rebuildFocusableButtons();
					// Reset buffs focus when tab is selected
					buffs.resetFocus();
				} else {
					// Clear buffs focus when leaving tab
					buffs.resetFocus();
				}
				buffs.visible = buffs.active = selected;
			}
		} );

		layoutTabs();

		talents.setRect(0, 0, WIDTH, HEIGHT);
		talents.pane.scrollTo(0, talents.pane.content().height() - talents.pane.height());
		talents.layout();

		select( lastIdx );
	}

	@Override
	public void hide() {
		super.hide();
		// Trigger tutorial progression when hero info is closed
		if (TutorialManager.isTutorialLevel() && TutorialManager.getState() == TutorialState.HERO_INFO_HINT) {
			TutorialManager.onAction(TutorialManager.TutorialAction.HERO_INFO_CLOSED);
		}
	}

	@Override
	public boolean onSignal(KeyEvent event) {
		// Don't process keyboard events if window is not active (another window is on top)
		if (!active) return super.onSignal(event);
		if (event.pressed) {
			if (KeyBindings.getActionForKey( event ) == SPDAction.HERO_INFO) {
				onBackPressed();
				return true;
			}

			// Handle BuffsTab keyboard navigation
			if (lastIdx == 2 && buffs.visible) {
				com.watabou.input.GameAction action = KeyBindings.getActionForKey(event);
				boolean isUp = action == SPDAction.N || action == SPDAction.NW
						|| action == SPDAction.W || action == SPDAction.SW;
				boolean isDown = action == SPDAction.S || action == SPDAction.SE
						|| action == SPDAction.E || action == SPDAction.NE;
				boolean isEnter = event.code == com.badlogic.gdx.Input.Keys.ENTER
						|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER;

				if (isUp) {
					buffs.moveFocus(-1);
					return true;
				} else if (isDown) {
					buffs.moveFocus(1);
					return true;
				} else if (isEnter) {
					buffs.activateFocused();
					return true;
				}
			}
		}
		return super.onSignal(event);
	}

	@Override
	public void offset(int xOffset, int yOffset) {
		super.offset(xOffset, yOffset);
		talents.layout();
		buffs.layout();
	}

	private void rebuildFocusableButtons() {
		focusableButtons.clear();
		focusIndex = -1;

		// Add stats tab info button if visible
		if (stats != null && stats.visible && stats.infoButton != null) {
			addFocusableButton(stats.infoButton);
		}
	}

	private class StatsTab extends Group {

		private static final int GAP = 6;

		private float pos;
		IconButton infoButton;

		public StatsTab() {
			initialize();
		}

		public void initialize(){

			for (Gizmo g : members){
				if (g != null) g.destroy();
			}
			clear();
			
			Hero hero = Dungeon.hero;

			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar(hero) );
			if (hero.name().equals(hero.className()))
				title.label( Messages.get(this, "title", hero.lvl, hero.className() ).toUpperCase( Locale.ENGLISH ) );
			else
				title.label((hero.name() + "\n" + Messages.get(this, "title", hero.lvl, hero.className())).toUpperCase(Locale.ENGLISH));
			title.color(Window.TITLE_COLOR);
			title.setRect( 0, 0, WIDTH-16, 0 );
			add(title);

			infoButton = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					if (SacredPixelDungeon.scene() instanceof GameScene){
						GameScene.show(new WndHeroInfo(hero.heroClass));
					} else {
						SacredPixelDungeon.scene().addToFront(new WndHeroInfo(hero.heroClass));
					}
				}

				@Override
				protected String hoverText() {
					return Messages.titleCase(Messages.get(WndKeyBindings.class, "hero_info"));
				}

			};
			infoButton.setRect(title.right(), 0, 16, 16);
			add(infoButton);

			pos = title.bottom() + 2*GAP;

			int strBonus = hero.STR() - hero.STR;
			if (strBonus > 0)           statSlot( Messages.get(this, "str"), hero.STR + " + " + strBonus );
			else if (strBonus < 0)      statSlot( Messages.get(this, "str"), hero.STR + " - " + -strBonus );
			else                        statSlot( Messages.get(this, "str"), hero.STR() );
			if (hero.shielding() > 0)   statSlot( Messages.get(this, "health"), hero.HP + "+" + hero.shielding() + "/" + hero.HT );
			else                        statSlot( Messages.get(this, "health"), (hero.HP) + "/" + hero.HT );
			statSlot( Messages.get(this, "exp"), hero.exp + "/" + hero.maxExp() );

			pos += GAP;

			statSlot( Messages.get(this, "gold"), DateCompat.formatNumber(Statistics.goldCollected, Messages.locale()) );
			statSlot( Messages.get(this, "depth"), DateCompat.formatNumber(Statistics.deepestFloor, Messages.locale()) );
			if (!Dungeon.customSeedText.isEmpty()){
				statSlot( Messages.get(this, "custom_seed"), "_" + Dungeon.customSeedText + "_" );
			} else {
				statSlot( Messages.get(this, "dungeon_seed"), DungeonSeed.convertToCode(Dungeon.seed) );
			}

			pos += GAP;
		}

		private void statSlot( String label, String value ) {

			int size = 7;
			RenderedTextBlock txt;
			do {
				txt = PixelScene.renderTextBlock( label, size );
				size--;
			} while (txt.width() >= WIDTH * 0.67f);
			txt.setPos(0, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );

			size = 7;
			do {
				txt = PixelScene.renderTextBlock( value, size );
				size--;
			} while (txt.width() >= WIDTH * 0.33f);
			txt.setPos(WIDTH * 0.67f, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );
			
			pos += GAP + txt.height();
		}
		
		private void statSlot( String label, int value ) {
			statSlot( label, DateCompat.formatNumber( value, Messages.locale() ) );
		}
		
		public float height() {
			return pos;
		}
	}

	public class TalentsTab extends Component {

		TalentsPane pane;

		@Override
		protected void createChildren() {
			super.createChildren();
			pane = new TalentsPane(TalentButton.Mode.UPGRADE);
			add(pane);
		}

		@Override
		protected void layout() {
			super.layout();
			pane.setRect(x, y, width, height);
		}

	}
	
	private class BuffsTab extends Component {

		private static final int GAP = 2;

		private float pos;
		private ScrollPane buffList;
		private ArrayList<BuffSlot> slots = new ArrayList<>();
		private int focusIndex = -1;

		@Override
		protected void createChildren() {

			super.createChildren();

			buffList = new ScrollPane( new Component() ){
				@Override
				public void onClick( float x, float y ) {
					int size = slots.size();
					for (int i=0; i < size; i++) {
						if (slots.get( i ).onClick( x, y )) {
							break;
						}
					}
				}
			};
			add(buffList);
		}

		@Override
		protected void layout() {
			super.layout();
			buffList.setRect(0, 0, width, height);
		}

		private void setupList() {
			Component content = buffList.content();
			for (Buff buff : Dungeon.hero.buffs()) {
				if (buff.icon() != BuffIndicator.NONE) {
					BuffSlot slot = new BuffSlot(buff);
					slot.setRect(0, pos, WIDTH, slot.icon.height());
					content.add(slot);
					slots.add(slot);
					pos += GAP + slot.height();
				}
			}
			content.setSize(buffList.width(), pos);
			buffList.setSize(buffList.width(), buffList.height());
		}

		public void resetFocus() {
			if (focusIndex >= 0 && focusIndex < slots.size()) {
				slots.get(focusIndex).setFocused(false);
			}
			focusIndex = -1;
		}

		public void moveFocus(int direction) {
			if (slots.isEmpty()) return;

			// Clear previous focus
			if (focusIndex >= 0 && focusIndex < slots.size()) {
				slots.get(focusIndex).setFocused(false);
			}

			// Move index
			if (focusIndex == -1) {
				focusIndex = direction > 0 ? 0 : slots.size() - 1;
			} else {
				focusIndex += direction;
				if (focusIndex < 0) focusIndex = slots.size() - 1;
				if (focusIndex >= slots.size()) focusIndex = 0;
			}

			// Set focus on new slot
			BuffSlot slot = slots.get(focusIndex);
			slot.setFocused(true);

			// Scroll to make focused slot visible
			float slotTop = slot.top();
			float slotBottom = slot.bottom();
			float scrollTop = buffList.content().camera.scroll.y;
			float scrollBottom = scrollTop + buffList.height();

			if (slotTop < scrollTop) {
				buffList.scrollTo(0, slotTop);
			} else if (slotBottom > scrollBottom) {
				buffList.scrollTo(0, slotBottom - buffList.height());
			}
		}

		public void activateFocused() {
			if (focusIndex >= 0 && focusIndex < slots.size()) {
				slots.get(focusIndex).activate();
			}
		}

		private class BuffSlot extends Component {

			private Buff buff;

			Image icon;
			RenderedTextBlock txt;

			public BuffSlot( Buff buff ){
				super();
				this.buff = buff;

				icon = new BuffIcon(buff, true);
				icon.y = this.y;
				add( icon );

				txt = PixelScene.renderTextBlock( Messages.titleCase(buff.name()), 8 );
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
				add( txt );

			}

			@Override
			protected void layout() {
				super.layout();
				icon.y = this.y;
				txt.maxWidth((int)(width - icon.width()));
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
			}

			public void setFocused(boolean focused) {
				if (focused) {
					icon.brightness(1.5f);
					txt.hardlight(Window.TITLE_COLOR);
				} else {
					icon.resetColor();
					txt.resetColor();
				}
			}

			public void activate() {
				WndHero.this.active = false;
				GameScene.show(new WndInfoBuff(buff) {
					@Override
					public void destroy() {
						super.destroy();
						if (WndHero.this.parent != null) {
							WndHero.this.active = true;
						}
					}
				});
			}

			protected boolean onClick ( float x, float y ) {
				if (inside( x, y )) {
					activate();
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
