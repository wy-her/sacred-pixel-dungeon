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

package com.sacredpixel.sacredpixeldungeon.items.stones;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Buff;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.effects.Identification;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.potions.Potion;
import com.sacredpixel.sacredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.Scroll;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Reflection;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class StoneOfIntuition extends InventoryStone {
	
	{
		image = ItemSpriteSheet.STONE_INTUITION;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		if (item instanceof Ring){
			return !((Ring) item).isKnown();
		} else if (item instanceof Potion){
			return !((Potion) item).isKnown();
		} else if (item instanceof Scroll){
			return !((Scroll) item).isKnown();
		}
		return false;
	}
	
	@Override
	protected void onItemSelected(Item item) {

		GameScene.show( new WndGuess(item));
		
	}

	@Override
	public String desc() {
		String text = super.desc();
		if (Dungeon.hero != null){
			if (Dungeon.hero.buff(IntuitionUseTracker.class) == null){
				text += "\n\n" + Messages.get(this, "break_info");
			} else {
				text += "\n\n" + Messages.get(this, "break_warn");
			}
		}
		return text;
	}

	public static class IntuitionUseTracker extends Buff {{ revivePersists = true; }};
	
	private static Class curGuess = null;

	public class WndGuess extends Window implements Signal.Listener<KeyEvent> {

		private static final int WIDTH = 149;
		private static final int BTN_SIZE = 20;
		private static final int BTN_HEIGHT = 16;

		// Custom keyboard navigation
		private ArrayList<IconButton> iconButtons = new ArrayList<>();
		private RedButton guessButton;
		private ColorBlock focusHighlight;
		private int iconFocusIndex = -1;
		private boolean focusOnButton = false;

		public WndGuess(final Item item){

			IconTitle titlebar = new IconTitle();
			titlebar.icon( new ItemSprite(item) );
			titlebar.label( Messages.titleCase(item.name()) );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );

			RenderedTextBlock text = PixelScene.renderTextBlock(6);
			text.text( Messages.get(this, "text") );
			text.setPos(0, titlebar.bottom()+2);
			text.maxWidth( WIDTH );
			add(text);

			// Focus highlight overlay (white semi-transparent rectangle)
			focusHighlight = new ColorBlock(BTN_SIZE, BTN_SIZE, 0x44FFFFFF);
			focusHighlight.visible = false;
			add(focusHighlight);

			guessButton = new RedButton(""){
				@Override
				protected void onClick() {
					super.onClick();
					useAnimation();
					if (item.getClass() == curGuess){
						if (item instanceof Ring){
							((Ring) item).setKnown();
							Item.updateQuickslot();
						} else {
							item.identify();
						}
						GLog.p( Messages.get(WndGuess.class, "correct") );
						curUser.sprite.parent.add( new Identification( curUser.sprite.center().offset( 0, -16 ) ) );
					} else {
						GLog.w( Messages.get(WndGuess.class, "incorrect") );
					}
					if (!anonymous) {
						Catalog.countUse(StoneOfIntuition.class);
						if (curUser.buff(IntuitionUseTracker.class) == null) {
							Buff.affect(curUser, IntuitionUseTracker.class);
						} else {
							curItem.detach(curUser.belongings.backpack);
							curUser.buff(IntuitionUseTracker.class).detach();
						}
						Talent.onRunestoneUsed(curUser, curUser.pos, StoneOfIntuition.class);
					}
					curGuess = null;
					hide();
				}
			};
			guessButton.visible = false;
			guessButton.enable(false);
			add(guessButton);

			float left;
			float top = text.bottom() + 5;
			int rows;
			int placed = 0;

			final ArrayList<Class<?extends Item>> unIDed = new ArrayList<>();
			if (item.isIdentified()){
				hide();
				return;
			} else if (item instanceof Potion){
				if (item instanceof ExoticPotion) {
					for (Class<?extends Item> i : Potion.getUnknown()){
						unIDed.add(ExoticPotion.regToExo.get(i));
					}
				} else {
					unIDed.addAll(Potion.getUnknown());
				}
			} else if (item instanceof Scroll){
				if (item instanceof ExoticScroll) {
					for (Class<?extends Item> i : Scroll.getUnknown()){
						unIDed.add(ExoticScroll.regToExo.get(i));
					}
				} else {
					unIDed.addAll(Scroll.getUnknown());
				}
			} else if (item instanceof Ring) {
				unIDed.addAll(Ring.getUnknown());
			} else {
				hide();
				return;
			}

			if (unIDed.size() <= 5){
				rows = 1;
				top += BTN_SIZE/2f;
				left = (WIDTH - BTN_SIZE*unIDed.size())/2f;
			} else {
				rows = 2;
				left = (WIDTH - BTN_SIZE*((unIDed.size()+1)/2))/2f;
			}

			for (final Class<?extends Item> i : unIDed){

				IconButton btn = new IconButton(){
					@Override
					protected void onClick() {
						curGuess = i;
						guessButton.visible = true;
						guessButton.text( Messages.titleCase(Messages.get(curGuess, "name")) );
						guessButton.enable(true);
						super.onClick();
					}
				};
				Image im = new Image(Assets.Sprites.ITEM_ICONS);
				im.frame(ItemSpriteSheet.Icons.film.get(Reflection.newInstance(i).icon));
				im.scale.set(2f);
				btn.icon(im);
				btn.setRect(left + placed*BTN_SIZE, top, BTN_SIZE, BTN_SIZE);
				add(btn);
				iconButtons.add(btn);

				placed++;
				if (rows == 2 && placed == ((unIDed.size()+1)/2)){
					placed = 0;
					if (unIDed.size() % 2 == 1){
						left += BTN_SIZE/2f;
					}
					top += BTN_SIZE;
				}
			}

			// Bring focus highlight to front
			bringToFront(focusHighlight);

			float iconsBottom = top + BTN_SIZE;
			guessButton.setRect(0, iconsBottom + 2, WIDTH, BTN_HEIGHT);
			resize(WIDTH, (int)(iconsBottom + 2 + BTN_HEIGHT));

			// Setup keyboard navigation
			KeyEvent.addKeyListener(this);
			// No default focus - user must press arrow key to start navigating
		}

		@Override
		public boolean onSignal(KeyEvent event) {
			if (!event.pressed) return false;

			GameAction action = KeyBindings.getActionForKey(event);

			// ESC: if on button, go back to icons; if on icons, close window
			if (action == SPDAction.BACK) {
				if (focusOnButton) {
					// Move from button back to icons (same as pressing up)
					focusOnButton = false;
					guessButton.setFocused(false);
					if (iconFocusIndex == -1 && !iconButtons.isEmpty()) {
						iconFocusIndex = 0;
					}
					updateFocusHighlight();
				} else {
					hide();
				}
				return true;
			}
			// Left/Right: move focus among icons
			else if (action == SPDAction.W) {
				if (!focusOnButton && !iconButtons.isEmpty()) {
					moveIconFocus(-1);
				}
				return true;
			} else if (action == SPDAction.E) {
				if (!focusOnButton && !iconButtons.isEmpty()) {
					moveIconFocus(1);
				}
				return true;
			}
			// Up/Down: toggle between icon area and button
			else if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE) {
				if (focusOnButton) {
					// Move from button to icons
					focusOnButton = false;
					guessButton.setFocused(false);
					if (iconFocusIndex == -1 && !iconButtons.isEmpty()) {
						iconFocusIndex = 0;
					}
					updateFocusHighlight();
				}
				return true;
			} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE) {
				if (!focusOnButton && guessButton.visible && guessButton.active) {
					// Move from icons to button
					focusOnButton = true;
					focusHighlight.visible = false;
					guessButton.setFocused(true);
				}
				return true;
			}
			// Enter: activate focused element
			else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
					|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
				if (focusOnButton && guessButton.visible && guessButton.active) {
					Sample.INSTANCE.play(Assets.Sounds.CLICK);
					guessButton.click();
				} else if (!focusOnButton && iconFocusIndex >= 0 && iconFocusIndex < iconButtons.size()) {
					Sample.INSTANCE.play(Assets.Sounds.CLICK);
					iconButtons.get(iconFocusIndex).click();
					// After clicking icon, guess button becomes active
					// Focus moves to button automatically
					focusOnButton = true;
					focusHighlight.visible = false;
					guessButton.setFocused(true);
				}
				return true;
			}
			return false;
		}

		private void moveIconFocus(int direction) {
			if (iconButtons.isEmpty()) return;

			// If no current focus, start at first or last based on direction
			if (iconFocusIndex == -1) {
				iconFocusIndex = (direction > 0) ? 0 : iconButtons.size() - 1;
			} else {
				iconFocusIndex += direction;
				if (iconFocusIndex < 0) iconFocusIndex = iconButtons.size() - 1;
				if (iconFocusIndex >= iconButtons.size()) iconFocusIndex = 0;
			}

			updateFocusHighlight();
		}

		private void updateFocusHighlight() {
			if (iconFocusIndex >= 0 && iconFocusIndex < iconButtons.size()) {
				IconButton btn = iconButtons.get(iconFocusIndex);
				focusHighlight.x = btn.left();
				focusHighlight.y = btn.top();
				focusHighlight.size(btn.width(), btn.height());
				focusHighlight.visible = true;
			} else {
				focusHighlight.visible = false;
			}
		}

		@Override
		public void destroy() {
			KeyEvent.removeKeyListener(this);
			super.destroy();
		}

	}
}
