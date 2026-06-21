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

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.items.ArcaneResin;
import com.sacredpixel.sacredpixeldungeon.items.Generator;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.LiquidMetal;
import com.sacredpixel.sacredpixeldungeon.items.Recipe;
import com.sacredpixel.sacredpixeldungeon.items.bombs.Bomb;
import com.sacredpixel.sacredpixeldungeon.items.food.Blandfruit;
import com.sacredpixel.sacredpixeldungeon.items.food.Food;
import com.sacredpixel.sacredpixeldungeon.items.food.MeatPie;
import com.sacredpixel.sacredpixeldungeon.items.food.MysteryMeat;
import com.sacredpixel.sacredpixeldungeon.items.food.Pasty;
import com.sacredpixel.sacredpixeldungeon.items.food.StewedMeat;
import com.sacredpixel.sacredpixeldungeon.items.potions.Potion;
import com.sacredpixel.sacredpixeldungeon.items.potions.brews.AquaBrew;
import com.sacredpixel.sacredpixeldungeon.items.potions.brews.BlizzardBrew;
import com.sacredpixel.sacredpixeldungeon.items.potions.brews.CausticBrew;
import com.sacredpixel.sacredpixeldungeon.items.potions.brews.InfernalBrew;
import com.sacredpixel.sacredpixeldungeon.items.potions.brews.ShockingBrew;
import com.sacredpixel.sacredpixeldungeon.items.potions.brews.UnstableBrew;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfIcyTouch;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.sacredpixel.sacredpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.sacredpixel.sacredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.Scroll;
import com.sacredpixel.sacredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.sacredpixel.sacredpixeldungeon.items.spells.Alchemize;
import com.sacredpixel.sacredpixeldungeon.items.spells.BeaconOfReturning;
import com.sacredpixel.sacredpixeldungeon.items.spells.CurseInfusion;
import com.sacredpixel.sacredpixeldungeon.items.spells.MagicalInfusion;
import com.sacredpixel.sacredpixeldungeon.items.spells.PhaseShift;
import com.sacredpixel.sacredpixeldungeon.items.spells.ReclaimTrap;
import com.sacredpixel.sacredpixeldungeon.items.spells.Recycle;
import com.sacredpixel.sacredpixeldungeon.items.spells.SummonElemental;
import com.sacredpixel.sacredpixeldungeon.items.spells.TelekineticGrab;
import com.sacredpixel.sacredpixeldungeon.items.spells.UnstableSpell;
import com.sacredpixel.sacredpixeldungeon.items.spells.WildEnergy;
import com.sacredpixel.sacredpixeldungeon.items.stones.Runestone;
import com.sacredpixel.sacredpixeldungeon.items.wands.Wand;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.plants.Plant;
import com.sacredpixel.sacredpixeldungeon.scenes.AlchemyScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.windows.WndBag;
import com.sacredpixel.sacredpixeldungeon.windows.WndInfoItem;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class QuickRecipe extends Component {

	private ArrayList<Item> ingredients;

	private ArrayList<ItemSlot> inputs;
	private QuickRecipe.arrow arrow;
	private ItemSlot output;

	// Keyboard navigation support
	private int focusedItemIndex = -1;
	private com.watabou.noosa.ColorBlock focusHighlight;

	// Get total number of focusable items (inputs + output)
	public int getItemCount() {
		return inputs.size() + 1; // +1 for output
	}

	// Set focus on item at index (0 to getItemCount()-1)
	public void setFocusedItem(int index) {
		if (focusedItemIndex == index) return;

		// Clear previous highlight
		clearItemFocus();

		if (index < 0 || index >= getItemCount()) {
			focusedItemIndex = -1;
			return;
		}

		focusedItemIndex = index;

		// Get the target ItemSlot
		ItemSlot target;
		if (index < inputs.size()) {
			target = inputs.get(index);
		} else {
			target = output;
		}

		// Create highlight if needed
		if (focusHighlight == null) {
			focusHighlight = new com.watabou.noosa.ColorBlock(1, 1, 0x44FFFFFF);
			add(focusHighlight);
		}

		// Position and size highlight around the item
		focusHighlight.size(target.width() + 2, target.height() + 2);
		focusHighlight.x = target.left() - 1;
		focusHighlight.y = target.top() - 1;
		focusHighlight.visible = true;
	}

	// Clear focus highlight
	public void clearItemFocus() {
		focusedItemIndex = -1;
		if (focusHighlight != null) {
			focusHighlight.visible = false;
		}
	}

	// Get currently focused item index
	public int getFocusedItemIndex() {
		return focusedItemIndex;
	}

	// Activate (click) the currently focused item
	public void activateFocusedItem() {
		if (focusedItemIndex < 0 || focusedItemIndex >= getItemCount()) return;

		ItemSlot target;
		if (focusedItemIndex < inputs.size()) {
			target = inputs.get(focusedItemIndex);
		} else {
			target = output;
		}

		if (target.active) {
			com.watabou.noosa.audio.Sample.INSTANCE.play(Assets.Sounds.CLICK);
			target.onClick();
		}
	}

	public QuickRecipe(Recipe.SimpleRecipe r){
		this(r, r.getIngredients(), r.sampleOutput(null));
	}
	
	public QuickRecipe(Recipe r, ArrayList<Item> inputs, final Item output) {
		
		ingredients = inputs;
		int cost = r.cost(inputs);
		boolean hasInputs = true;
		this.inputs = new ArrayList<>();
		for (final Item in : inputs) {
			anonymize(in);
			ItemSlot curr;
			curr = new ItemSlot(in) {
				{
					hotArea.blockLevel = PointerArea.NEVER_BLOCK;
				}

				@Override
				protected void onClick() {
					SacredPixelDungeon.scene().addToFront(new WndInfoItem(in));
				}
			};

			int quantity = 0;
			if (Dungeon.hero != null) {
				ArrayList<Item> similar = Dungeon.hero.belongings.getAllSimilar(in);
				for (Item sim : similar) {
					//if we are looking for a specific item, it must be IDed
					if (sim.getClass() != in.getClass() || sim.isIdentified())
						quantity += sim.quantity();
				}
				if (quantity < in.quantity()) {
					curr.sprite.alpha(0.3f);
					hasInputs = false;
				}
			} else {
				hasInputs = false;
			}

			curr.showExtraInfo(false);
			add(curr);
			this.inputs.add(curr);
		}
		
		if (cost > 0) {
			arrow = new arrow(Icons.get(Icons.ARROW), cost);
			arrow.hardlightText(0x44CCFF);
		} else {
			arrow = new arrow(Icons.get(Icons.ARROW));
		}
		if (hasInputs) {
			arrow.icon.tint(1, 1, 0, 1);
			if (!(SacredPixelDungeon.scene() instanceof AlchemyScene)) {
				arrow.enable(false);
			}
		} else {
			arrow.icon.color(0, 0, 0);
			arrow.enable(false);
		}
		add(arrow);
		
		anonymize(output);
		this.output = new ItemSlot(output){
			{
				hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			}

			@Override
			protected void onClick() {
				SacredPixelDungeon.scene().addToFront(new WndInfoItem(output));
			}
		};
		if (Dungeon.hero != null && !hasInputs){
			this.output.sprite.alpha(0.3f);
		}
		this.output.showExtraInfo(false);
		add(this.output);
		
		layout();
	}
	
	@Override
	protected void layout() {
		
		height = 16;
		width = 0;

		int padding = inputs.size() == 1 ? 8 : 0;

		for (ItemSlot item : inputs){
			item.setRect(x + width + padding, y, 16, 16);
			width += 16 + padding;
		}
		
		arrow.setRect(x + width, y, 14, 16);
		width += 14;
		
		output.setRect(x + width, y, 16, 16);
		width += 16;

		width += padding;
	}
	
	//used to ensure that un-IDed items are not spoiled
	private void anonymize(Item item){
		if (item instanceof Potion){
			((Potion) item).anonymize();
		} else if (item instanceof Scroll){
			((Scroll) item).anonymize();
		}
	}
	
	public class arrow extends IconButton {
		
		BitmapText text;
		
		public arrow(){
			super();
		}
		
		public arrow( Image icon ){
			super( icon );
		}
		
		public arrow( Image icon, int count ){
			super( icon );
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;

			text = new BitmapText( Integer.toString(count), PixelScene.pixelFont);
			text.measure();
			add(text);
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			if (text != null){
				text.x = x;
				text.y = y;
				PixelScene.align(text);
			}
		}
		
		@Override
		protected void onPointerUp() {
			icon.brightness(1f);
		}

		@Override
		protected void onClick() {
			super.onClick();
			
			//find the window this is inside of and close it
			Group parent = this.parent;
			while (parent != null){
				if (parent instanceof Window){
					((Window) parent).hide();
					break;
				} else {
					parent = parent.parent;
				}
			}
			
			((AlchemyScene)SacredPixelDungeon.scene()).populate(ingredients, Dungeon.hero.belongings);
		}
		
		public void hardlightText(int color ){
			if (text != null) text.hardlight(color);
		}
	}
	
	//gets recipes for a particular alchemy guide page
	//a null entry indicates a break in section
	public static ArrayList<QuickRecipe> getRecipes( int pageIdx ){
		ArrayList<QuickRecipe> result = new ArrayList<>();
		switch (pageIdx){
			case 0: default:
				result.add(new QuickRecipe( new Potion.SeedToPotion(), new ArrayList<>(Arrays.asList(new Plant.Seed.PlaceHolder().quantity(3))), new WndBag.Placeholder(ItemSpriteSheet.POTION_HOLDER){
					@Override
					public String name() {
						return Messages.get(Potion.SeedToPotion.class, "name");
					}

					@Override
					public String info() {
						return "";
					}
				}));
				return result;
			case 1:
				Recipe r = new Scroll.ScrollToStone();
				for (Class<?> cls : Generator.Category.SCROLL.classes){
					Scroll scroll = (Scroll) Reflection.newInstance(cls);
					if (!scroll.isKnown()) scroll.anonymize();
					ArrayList<Item> in = new ArrayList<Item>(Arrays.asList(scroll));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 2:
				result.add(new QuickRecipe( new StewedMeat.oneMeat() ));
				result.add(new QuickRecipe( new StewedMeat.twoMeat() ));
				result.add(new QuickRecipe( new StewedMeat.threeMeat() ));
				result.add(null);
				result.add(new QuickRecipe( new MeatPie.Recipe(),
						new ArrayList<Item>(Arrays.asList(new Pasty(), new Food(), new MysteryMeat.PlaceHolder())),
						new MeatPie()));
				result.add(null);
				result.add(new QuickRecipe( new Blandfruit.CookFruit(),
						new ArrayList<>(Arrays.asList(new Blandfruit(), new Plant.Seed.PlaceHolder())),
						new Blandfruit(){

							public String name(){
								return Messages.get(Blandfruit.class, "cooked");
							}
							
							@Override
							public String info() {
								return "";
							}
						}));
				return result;
			case 3:
				r = new ExoticPotion.PotionToExotic();
				for (Class<?> cls : Generator.Category.POTION.classes){
					Potion pot = (Potion) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(pot));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 4:
				r = new ExoticScroll.ScrollToExotic();
				for (Class<?> cls : Generator.Category.SCROLL.classes){
					Scroll scroll = (Scroll) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(scroll));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 5:
				r = new Bomb.EnhanceBomb();
				//Row 1: Frost, Fire, Smoke, Regrowth
				Class<?>[] row1 = {com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfFrost.class,
					com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfLiquidFlame.class,
					com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfInvisibility.class,
					com.sacredpixel.sacredpixeldungeon.items.potions.PotionOfHealing.class};
				for (Class<?> cls : row1){
					Item item = (Item) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(new Bomb(), item));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				result.add(null);
				//Row 2: Woolly, Noise, Flash, Holy
				Class<?>[] row2 = {com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfMirrorImage.class,
					com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfRage.class,
					com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfRecharging.class,
					com.sacredpixel.sacredpixeldungeon.items.scrolls.ScrollOfRemoveCurse.class};
				for (Class<?> cls : row2){
					Item item = (Item) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(new Bomb(), item));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				result.add(null);
				//Row 3: Arcane, Shrapnel
				Class<?>[] row3 = {com.sacredpixel.sacredpixeldungeon.items.quest.GooBlob.class,
					com.sacredpixel.sacredpixeldungeon.items.quest.MetalShard.class};
				for (Class<?> cls : row3){
					Item item = (Item) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(new Bomb(), item));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 6:
				result.add(new QuickRecipe( new LiquidMetal.Recipe(),
						new ArrayList<Item>(Arrays.asList(new MissileWeapon.PlaceHolder())),
						new LiquidMetal()));
				result.add(new QuickRecipe( new ArcaneResin.Recipe(),
						new ArrayList<Item>(Arrays.asList(new Wand.PlaceHolder())),
						new ArcaneResin()));
				return result;
			case 7:
				result.add(new QuickRecipe(new UnstableBrew.Recipe(), new ArrayList<>(Arrays.asList(new Potion.PlaceHolder(), new  Plant.Seed.PlaceHolder())), new UnstableBrew()));
				result.add(new QuickRecipe(new BlizzardBrew.Recipe()));
				result.add(new QuickRecipe(new InfernalBrew.Recipe()));
				result.add(new QuickRecipe(new AquaBrew.Recipe()));
				result.add(new QuickRecipe(new CausticBrew.Recipe()));
				result.add(new QuickRecipe(new ShockingBrew.Recipe()));
				result.add(null);
				result.add(new QuickRecipe(new ElixirOfHoneyedHealing.Recipe()));
				result.add(new QuickRecipe(new ElixirOfAquaticRejuvenation.Recipe()));
				result.add(new QuickRecipe(new ElixirOfArcaneArmor.Recipe()));
				result.add(new QuickRecipe(new ElixirOfIcyTouch.Recipe()));
				result.add(new QuickRecipe(new ElixirOfToxicEssence.Recipe()));
				result.add(new QuickRecipe(new ElixirOfDragonsBlood.Recipe()));
				result.add(new QuickRecipe(new ElixirOfFeatherFall.Recipe()));
				result.add(new QuickRecipe(new ElixirOfMight.Recipe()));
				return result;
			case 8:
				result.add(new QuickRecipe(new UnstableSpell.Recipe(), new ArrayList<>(Arrays.asList(new Scroll.PlaceHolder(), new  Runestone.PlaceHolder())), new UnstableSpell()));
				result.add(new QuickRecipe(new WildEnergy.Recipe()));
				result.add(new QuickRecipe(new TelekineticGrab.Recipe()));
				result.add(new QuickRecipe(new PhaseShift.Recipe()));
				result.add(null);
				result.add(new QuickRecipe(new Alchemize.Recipe(), new ArrayList<>(Arrays.asList(new Plant.Seed.PlaceHolder(), new Runestone.PlaceHolder())), new Alchemize().quantity(8)));
				result.add(new QuickRecipe(new CurseInfusion.Recipe()));
				result.add(new QuickRecipe(new MagicalInfusion.Recipe()));
				result.add(new QuickRecipe(new Recycle.Recipe()));
				result.add(null);
				result.add(new QuickRecipe(new ReclaimTrap.Recipe()));
				result.add(new QuickRecipe(new SummonElemental.Recipe()));
				result.add(new QuickRecipe(new BeaconOfReturning.Recipe()));
				return result;
		}
	}
	
}
