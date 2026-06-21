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
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.GamesInProgress;
import com.sacredpixel.sacredpixeldungeon.effects.BadgeBanner;
import com.sacredpixel.sacredpixeldungeon.effects.Flare;
import com.sacredpixel.sacredpixeldungeon.effects.Speck;
import com.sacredpixel.sacredpixeldungeon.items.Amulet;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Camera;
import com.watabou.utils.Signal;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

public class AmuletScene extends PixelScene {
	
	private static final int WIDTH			= 149;
	private static final int BTN_HEIGHT		= 20;
	private static final float SMALL_GAP	= 2;
	private static final float LARGE_GAP	= 8;
	
	public static boolean noText = false;
	
	private Image amulet;

	{
		inGameScene = true;
	}

	StyledButton btnExit = null;
	StyledButton btnStay = null;

	// Keyboard navigation
	private Signal.Listener<KeyEvent> keyListener;
	private java.util.ArrayList<StyledButton> focusableButtons = new java.util.ArrayList<>();
	private int focusIndex = -1;
	
	@Override
	public void create() {
		super.create();
		
		RenderedTextBlock text = null;
		if (!noText) {
			text = renderTextBlock( Messages.get(this, "text"), 7 );
			text.maxWidth( PixelScene.landscape() ? 2*WIDTH-4 : WIDTH);
		}
		
		amulet = new Image( Assets.Sprites.AMULET );
		add( amulet );

		btnExit = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "exit") ) {
			@Override
			protected void onClick() {
				Dungeon.win( Amulet.class );
				Dungeon.deleteGame( GamesInProgress.curSlot, true );
				Badges.saveGlobal();
				btnExit.enable(false);
				btnStay.enable(false);

				AmuletScene.this.add(new Delayer(0.1f){
					@Override
					protected void onComplete() {
						if (BadgeBanner.isShowingBadges()){
							AmuletScene.this.add(new Delayer(3f){
								@Override
								protected void onComplete() {
									Game.switchScene( RankingsScene.class );
								}
							});
						} else {
							Game.switchScene( RankingsScene.class );
						}
					}
				});
				Music.INSTANCE.playTracks(
						new String[]{Assets.Music.THEME_2, Assets.Music.THEME_1},
						new float[]{1, 1},
						false);
			}
		};
		btnExit.icon(new ItemSprite(ItemSpriteSheet.AMULET));
		btnExit.setSize( WIDTH, BTN_HEIGHT );
		add( btnExit );
		
		btnStay = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "stay") ) {
			@Override
			protected void onClick() {
				btnExit.enable(false);
				btnStay.enable(false);
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
				Game.switchScene(InterlevelScene.class);
			}
		};
		btnStay.icon(Icons.CLOSE.get());
		btnStay.setSize( WIDTH, BTN_HEIGHT );
		add( btnStay );

		RectF insets = getCommonInsets();
		int w = (int) (Camera.main.width - insets.left + insets.right);
		int h = (int) (Camera.main.height - insets.top + insets.bottom);

		float height;
		if (noText) {
			height = amulet.height + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height();
			
			amulet.x = insets.left + (w - amulet.width) / 2;
			amulet.y = insets.top + (h - height) / 2;
			align(amulet);

			btnExit.setPos( insets.left + (w - btnExit.width()) / 2, amulet.y + amulet.height + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
			
		} else {
			height = amulet.height + LARGE_GAP + text.height() + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height();

			amulet.x = insets.left + (w - amulet.width) / 2;
			amulet.y = insets.top + (h - height) / 2;
			align(amulet);

			text.setPos(insets.left + (w - text.width()) / 2, amulet.y + amulet.height + LARGE_GAP);
			align(text);
			add(text);

			btnExit.setPos( insets.left + (w - btnExit.width()) / 2, text.top() + text.height() + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
		}

		new Flare( 8, 48 ).color( 0xFFDDBB, true ).show( amulet, 0 ).angularSpeed = +30;

		// Setup keyboard navigation
		focusableButtons.clear();
		focusableButtons.add(btnExit);
		focusableButtons.add(btnStay);

		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

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
					} else if (btnExit.active) {
						// Default to Exit button if nothing selected
						com.watabou.noosa.audio.Sample.INSTANCE.play(Assets.Sounds.CLICK);
						btnExit.click();
					}
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
		if (btnExit.isActive()) {
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
		}
	}
	
	private float timer = 0;
	
	@Override
	public void update() {
		super.update();
		
		if ((timer -= Game.elapsed) < 0) {
			timer = Random.Float( 0.5f, 5f );
			
			Speck star = (Speck)recycle( Speck.class );
			star.reset( 0, amulet.x + 10.5f, amulet.y + 5.5f, Speck.DISCOVER );
			add( star );
		}
	}
}
