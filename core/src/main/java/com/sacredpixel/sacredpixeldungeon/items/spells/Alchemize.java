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

package com.sacredpixel.sacredpixeldungeon.items.spells;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.stones.Runestone;
import com.sacredpixel.sacredpixeldungeon.items.trinkets.Trinket;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.plants.Plant;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.CharSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.windows.WndBag;
import com.sacredpixel.sacredpixeldungeon.windows.WndEnergizeItem;
import com.sacredpixel.sacredpixeldungeon.windows.WndInfoItem;
import com.sacredpixel.sacredpixeldungeon.windows.WndOptions;
import com.sacredpixel.sacredpixeldungeon.windows.WndTradeItem;
import com.sacredpixel.sacredpixeldungeon.windows.WndUpgrade;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Alchemize extends Spell {
	
	{
		image = ItemSpriteSheet.ALCHEMIZE;

		talentChance = 1/(float)Recipe.OUT_QUANTITY;
	}

	private static WndBag parentWnd;
	
	@Override
	protected void onCast(Hero hero) {
		parentWnd = GameScene.selectItem( itemSelector );
	}
	
	@Override
	public int value() {
		//lower value, as it's very cheap to make (and also sold at shops)
		return (int)(20 * (quantity/(float)Recipe.OUT_QUANTITY));
	}

	@Override
	public int energyVal() {
		return (int)(4 * (quantity/(float)Recipe.OUT_QUANTITY));
	}

	public static class Recipe extends com.sacredpixel.sacredpixeldungeon.items.Recipe {

		private static final int OUT_QUANTITY = 8;

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients.size() != 2) return false;

			if (ingredients.get(0) instanceof Plant.Seed && ingredients.get(1) instanceof Runestone){
				return true;
			}

			if (ingredients.get(0) instanceof Runestone && ingredients.get(1) instanceof Plant.Seed){
				return true;
			}

			return false;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 2;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			ingredients.get(0).quantity(ingredients.get(0).quantity()-1);
			ingredients.get(1).quantity(ingredients.get(1).quantity()-1);
			return sampleOutput(null);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new Alchemize().quantity(OUT_QUANTITY);
		}
	}

	private static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(Alchemize.class, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return !(item instanceof Alchemize)
					&& (Shopkeeper.canSell(item) || item.energyVal() > 0);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				if (parentWnd != null) {
					parentWnd = GameScene.selectItem(itemSelector);
				}
				GameScene.show( new WndAlchemizeItem( item, parentWnd ) );
			}
		}
	};


	public static class WndAlchemizeItem extends WndInfoItem {

		private static final float GAP		= 2;
		private static final int BTN_HEIGHT	= 16;

		private WndBag owner;

		public WndAlchemizeItem(Item item, WndBag owner) {
			super(item);

			this.owner = owner;

			float pos = height;

			if (Shopkeeper.canSell(item)) {
				if (item.quantity() == 1 || (item instanceof MissileWeapon && item.isUpgradable())) {

					if (item instanceof MissileWeapon && ((MissileWeapon) item).extraThrownLeft){
						RenderedTextBlock warn = PixelScene.renderTextBlock(Messages.get(WndUpgrade.class, "thrown_dust"), 6);
						warn.hardlight(CharSprite.WARNING);
						warn.maxWidth(this.width);
						warn.setPos(0, pos + GAP);
						add(warn);
						pos = warn.bottom();
					}

					RedButton btnSell = new RedButton(Messages.get(this, "sell", item.value())) {
						@Override
						protected void onClick() {
							WndTradeItem.sell(item);
							hide();
							consumeAlchemize();
						}
					};
					btnSell.setRect(0, pos + GAP, width, BTN_HEIGHT);
					add(btnSell);
					addFocusableButton(btnSell);

					pos = btnSell.bottom();

				} else {

					int priceAll = item.value();
					RedButton btnSell1 = new RedButton(Messages.get(this, "sell_1", priceAll / item.quantity())) {
						@Override
						protected void onClick() {
							WndTradeItem.sellOne(item);
							hide();
							consumeAlchemize();
						}
					};
					btnSell1.setRect(0, pos + GAP, width, BTN_HEIGHT);
					add(btnSell1);
					addFocusableButton(btnSell1);
					RedButton btnSellAll = new RedButton(Messages.get(this, "sell_all", priceAll)) {
						@Override
						protected void onClick() {
							WndTradeItem.sell(item);
							hide();
							consumeAlchemize();
						}
					};
					btnSellAll.setRect(0, btnSell1.bottom() + 1, width, BTN_HEIGHT);
					add(btnSellAll);
					addFocusableButton(btnSellAll);

					pos = btnSellAll.bottom();

				}
			}

			if (item.energyVal() > 0) {
				if (item.quantity() == 1) {

					RedButton btnEnergize = new RedButton(Messages.get(this, "energize", item.energyVal())) {
						@Override
						protected void onClick() {
							if (item instanceof Trinket){
								GameScene.show(new WndOptions(new ItemSprite(item), Messages.titleCase(item.name()),
										Messages.get(WndEnergizeItem.class, "trinket_warn"),
										Messages.get(WndEnergizeItem.class, "trinket_yes"),
										Messages.get(WndEnergizeItem.class, "trinket_no")){

									@Override
									protected void onSelect(int index) {
										if (index == 0) {
											WndEnergizeItem.energizeAll(item);
										}
									}

									@Override
									public void hide() {
										super.hide();
										WndAlchemizeItem.this.hide();
									}
								});
							} else {
								WndEnergizeItem.energizeAll(item);
								hide();
								consumeAlchemize();
							}
						}
					};
					btnEnergize.setRect(0, pos + GAP, width, BTN_HEIGHT);
					add(btnEnergize);
					addFocusableButton(btnEnergize);

					pos = btnEnergize.bottom();

				} else {

					int energyAll = item.energyVal();
					RedButton btnEnergize1 = new RedButton(Messages.get(this, "energize_1", energyAll / item.quantity())) {
						@Override
						protected void onClick() {
							WndEnergizeItem.energizeOne(item);
							hide();
							consumeAlchemize();
						}
					};
					btnEnergize1.setRect(0, pos + GAP, width, BTN_HEIGHT);
					add(btnEnergize1);
					addFocusableButton(btnEnergize1);
					RedButton btnEnergizeAll = new RedButton(Messages.get(this, "energize_all", energyAll)) {
						@Override
						protected void onClick() {
							WndEnergizeItem.energizeAll(item);
							hide();
							consumeAlchemize();
						}
					};
					btnEnergizeAll.setRect(0, btnEnergize1.bottom() + 1, width, BTN_HEIGHT);
					add(btnEnergizeAll);
					addFocusableButton(btnEnergizeAll);

					pos = btnEnergizeAll.bottom();

				}
			}

			resize( width, (int)pos );

		}

		private void consumeAlchemize(){
			Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
			if (curItem.quantity() <= 1){
				curItem.detachAll(Dungeon.hero.belongings.backpack);
				if (owner != null) {
					owner.hide();
				}
			} else {
				curItem.detach(Dungeon.hero.belongings.backpack);
				if (owner != null){
					owner.hide();
				}
				GameScene.selectItem(itemSelector);
			}
			Catalog.countUse(getClass());
			if (curItem instanceof Alchemize && Random.Float() < ((Alchemize)curItem).talentChance){
				Talent.onScrollUsed(curUser, curUser.pos, ((Alchemize) curItem).talentFactor, curItem.getClass());
			}
		}

	}
}
