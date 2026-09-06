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
import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.effects.Flare;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.Button;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ColorBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.sacredpixel.sacredpixeldungeon.windows.WndRanking;
import com.sacredpixel.sacredpixeldungeon.windows.WndVictoryCongrats;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.GameMath;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class RankingsScene extends PixelScene {

	private static final float ROW_HEIGHT_MAX	= 20;
	private static final float ROW_HEIGHT_MIN	= 12;

	private static final float MAX_ROW_WIDTH    = 160;

	private static final float GAP	= 4;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private ArrayList<Button> focusableButtons = new ArrayList<>();
	private int focusIndex = -1;

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
		add( BG );

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;

		Rankings.INSTANCE.load();

		IconTitle title = new IconTitle( Icons.RANKINGS.get(), Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (20 - title.height()) / 2f
		);
		align(title);
		add(title);
		
		if (Rankings.INSTANCE.records.size() > 0) {

			//attempts to give each record as much space as possible, ideally as much space as portrait mode
			float rowHeight = GameMath.gate(ROW_HEIGHT_MIN, (h - 26)/Rankings.INSTANCE.records.size(), ROW_HEIGHT_MAX);

			float left = (w - Math.min( MAX_ROW_WIDTH, w )) / 2 + GAP;
			float top = (h - rowHeight  * Rankings.INSTANCE.records.size()) / 2;

			int pos = 0;

			for (Rankings.Record rec : Rankings.INSTANCE.records) {
				Record row = new Record( pos, pos == Rankings.INSTANCE.lastRecord, rec );
				float offset = 0;
				if (rowHeight <= 14){
					offset = (pos % 2 == 1) ? 5 : -5;
				}
				row.setRect( insets.left + left+offset, insets.top + top + pos * rowHeight, w - left * 2, rowHeight );
				add(row);
				focusableButtons.add(row);

				pos++;
			}
			
			if (Rankings.INSTANCE.totalNumber >= Rankings.TABLE_SIZE) {
				
				RenderedTextBlock label = PixelScene.renderTextBlock( 8 );
				label.hardlight( 0xCCCCCC );
				label.setHightlighting(true, Window.SHPX_COLOR);
				label.text( Messages.get(this, "total") + " _" + Rankings.INSTANCE.wonNumber + "_/" + Rankings.INSTANCE.totalNumber );
				add( label );
				
				label.setPos(
						insets.left + (w - label.width()) / 2,
						insets.top + h - label.height() - 2*GAP
				);
				align(label);

			}
			
		} else {

			RenderedTextBlock noRec = PixelScene.renderTextBlock(Messages.get(this, "no_games"), 8);
			noRec.hardlight( 0xCCCCCC );
			noRec.setPos(
					insets.left + (w - noRec.width()) / 2,
					insets.top + (h - noRec.height()) / 2
			);
			align(noRec);
			add(noRec);
			
		}

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width() - insets.right, insets.top );
		add( btnExit );
		focusableButtons.add( btnExit );

		if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		// Setup keyboard navigation
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

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
						Button btn = focusableButtons.get(focusIndex);
						if (btn.active) {
							Sample.INSTANCE.play(Assets.Sounds.CLICK);
							btn.click();
						}
					}
					return true;
				} else if (action == SPDAction.BACK) {
					onBackPressed();
					return true;
				}
				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);

		fadeIn();
	}

	private void moveFocus(int direction) {
		if (focusableButtons.isEmpty()) return;

		// Clear previous highlight
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			Button btn = focusableButtons.get(focusIndex);
			if (btn instanceof Record) {
				((Record) btn).setFocused(false);
			} else if (btn instanceof ExitButton) {
				((ExitButton) btn).icon().resetColor();
			}
		}

		// Move index
		if (focusIndex == -1) {
			focusIndex = direction > 0 ? 0 : focusableButtons.size() - 1;
		} else {
			focusIndex += direction;
			if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
			if (focusIndex >= focusableButtons.size()) focusIndex = 0;
		}

		// Apply highlight to new focused button
		Button btn = focusableButtons.get(focusIndex);
		if (btn instanceof Record) {
			((Record) btn).setFocused(true);
		} else if (btn instanceof ExitButton) {
			((ExitButton) btn).icon().brightness(1.5f);
		}
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		super.destroy();
	}

	@Override
	protected void onBackPressed() {
		SacredPixelDungeon.switchNoFade(TitleScene.class);
	}
	
	public static class Record extends Button {

		private static final float GAP	= 4;

		private static final int TEXT_WIN	= 0xFFFF88;
		private static final int TEXT_LOSE	= 0xDDDDDD;
		private static final int FLARE_WIN	= 0x888866;
		private static final int FLARE_LOSE	= 0x666666;

		private Rankings.Record rec;
		private boolean focused = false;
		private int posIndex;
		
		protected Image shield;
		private Flare flare;
		private BitmapText position;
		private RenderedTextBlock desc;
		private Image steps;
		private BitmapText depth;
		private Image classIcon;
		private BitmapText level;
		
		public Record( int pos, boolean latest, Rankings.Record rec ) {
			super();

			this.rec = rec;
			this.posIndex = pos;
			
			if (latest) {
				flare = new Flare( 6, 24 );
				flare.angularSpeed = 90;
				flare.color( rec.win ? FLARE_WIN : FLARE_LOSE );
				addToBack( flare );
			}

			if (pos != Rankings.TABLE_SIZE-1) {
				position.text(Integer.toString(pos + 1));
			} else
				position.text(" ");
			position.measure();
			
			desc.text( Messages.titleCase(rec.desc()) );

			if (rec.win) {
				shield.copy( new ItemSprite(ItemSpriteSheet.AMULET, null) );
				position.hardlight( TEXT_WIN );
				desc.hardlight( TEXT_WIN );
				depth.hardlight( TEXT_WIN );
				level.hardlight( TEXT_WIN );
			} else {
				position.hardlight( TEXT_LOSE );
				desc.hardlight( TEXT_LOSE );
				depth.hardlight( TEXT_LOSE );
				level.hardlight( TEXT_LOSE );

				if (rec.depth != 0){
					depth.text( Integer.toString(rec.depth) );
					depth.measure();
					steps.copy(Icons.STAIRS.get());

					add(steps);
					add(depth);
				}

				if (rec.ascending){
					shield.copy( new ItemSprite(ItemSpriteSheet.AMULET, null) );
					shield.hardlight(0.4f, 0.4f, 0.7f);
				}

			}

			if (rec.customSeed != null && !rec.customSeed.isEmpty()){
				shield.copy( Icons.get(Icons.SEED) );
				shield.hardlight(1f, 1.5f, 0.67f);
			}

			if (rec.herolevel != 0){
				level.text( Integer.toString(rec.herolevel) );
				level.measure();
				add(level);
			}
			
			classIcon.copy( Icons.get( rec.heroClass ) );
			if (rec.heroClass == HeroClass.ROGUE){
				//cloak of shadows needs to be brightened a bit
				classIcon.brightness(2f);
			}
		}
		
		@Override
		protected void createChildren() {
			
			super.createChildren();
			
			shield = new Image(new ItemSprite( ItemSpriteSheet.TOMB, null ));
			add( shield );
			
			position = new BitmapText( PixelScene.pixelFont);
			add( position );
			
			desc = renderTextBlock( 7 );
			add( desc );

			depth = new BitmapText( PixelScene.pixelFont);

			steps = new Image();
			
			classIcon = new Image();
			add( classIcon );

			level = new BitmapText( PixelScene.pixelFont);
		}
		
		@Override
		protected void layout() {
			
			super.layout();
			
			shield.x = x + (16 - shield.width) / 2f;
			shield.y = y + (height - shield.height) / 2f;
			align(shield);
			
			position.x = shield.x + (shield.width - position.width()) / 2f;
			position.y = shield.y + (shield.height - position.height()) / 2f + 1;
			align(position);
			
			if (flare != null) {
				flare.point( shield.center() );
			}

			classIcon.x = x + width - 16 + (16 - classIcon.width())/2f;
			classIcon.y = shield.y + (16 - classIcon.height())/2f;
			align(classIcon);

			level.x = classIcon.x + (classIcon.width - level.width()) / 2f;
			level.y = classIcon.y + (classIcon.height - level.height()) / 2f + 1;
			align(level);

			steps.x = x + width - 32 + (16 - steps.width())/2f;
			steps.y = shield.y + (16 - steps.height())/2f;
			align(steps);

			depth.x = steps.x + (steps.width - depth.width()) / 2f;
			depth.y = steps.y + (steps.height - depth.height()) / 2f + 1;
			align(depth);

			desc.maxWidth((int)(steps.x - (x + 16 + GAP)));
			desc.setPos(x + 16 + GAP, shield.y + (shield.height - desc.height()) / 2f + 1);
			align(desc);
		}
		
		@Override
		protected void onClick() {
			parent.add( new WndRanking( rec ) );
		}

		public void setFocused(boolean focused) {
			this.focused = focused;
			if (focused) {
				shield.brightness(1.5f);
				classIcon.brightness(1.5f);
				steps.brightness(1.5f);
				// Brighten text without changing color
				desc.alpha(1f);
			} else {
				shield.resetColor();
				classIcon.resetColor();
				steps.resetColor();
				// Handle special cases for shield/classIcon
				if (rec.customSeed != null && !rec.customSeed.isEmpty()){
					shield.hardlight(1f, 1.5f, 0.67f);
				} else if (!rec.win && rec.ascending){
					shield.hardlight(0.4f, 0.4f, 0.7f);
				}
				if (rec.heroClass == com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass.ROGUE){
					classIcon.brightness(2f);
				}
				desc.alpha(1f);
			}
		}
	}
}
