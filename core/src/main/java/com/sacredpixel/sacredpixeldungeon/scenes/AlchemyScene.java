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
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Belongings;
import com.sacredpixel.sacredpixeldungeon.effects.Speck;
import com.sacredpixel.sacredpixeldungeon.effects.particles.SparkParticle;
import com.sacredpixel.sacredpixeldungeon.items.EnergyCrystal;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.LiquidMetal;
import com.sacredpixel.sacredpixeldungeon.items.Recipe;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.sacredpixel.sacredpixeldungeon.items.bags.Bag;
import com.sacredpixel.sacredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.sacredpixel.sacredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.sacredpixel.sacredpixeldungeon.journal.Catalog;
import com.sacredpixel.sacredpixeldungeon.journal.Document;
import com.sacredpixel.sacredpixeldungeon.journal.Journal;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.Button;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.ItemSlot;
import com.sacredpixel.sacredpixeldungeon.ui.RadialMenu;
import com.sacredpixel.sacredpixeldungeon.ui.RedButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.StatusPane;
import com.sacredpixel.sacredpixeldungeon.ui.StyledButton;
import com.sacredpixel.sacredpixeldungeon.ui.Toolbar;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.windows.IconTitle;
import com.sacredpixel.sacredpixeldungeon.windows.WndBag;
import com.sacredpixel.sacredpixeldungeon.windows.WndEnergizeItem;
import com.sacredpixel.sacredpixeldungeon.windows.WndInfoItem;
import com.sacredpixel.sacredpixeldungeon.windows.WndJournal;
import com.sacredpixel.sacredpixeldungeon.windows.WndKeyBindings;
import com.sacredpixel.sacredpixeldungeon.windows.WndMessage;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.input.ControllerHandler;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;

import java.io.IOException;
import java.util.ArrayList;
import com.watabou.utils.Compat;
import com.watabou.utils.Signal;
import com.watabou.input.KeyEvent;
import com.sacredpixel.sacredpixeldungeon.ui.Focusable;
import com.badlogic.gdx.Input;

public class AlchemyScene extends PixelScene implements Signal.Listener<KeyEvent> {

	// Keyboard navigation support
	protected ArrayList<Focusable> focusableButtons = new ArrayList<>();
	protected int focusIndex = -1;

	//max of 3 inputs, and 3 potential recipe outputs
	private static final InputButton[] inputs = new InputButton[3];
	private static final CombineButton[] combines = new CombineButton[3];
	private static final OutputSlot[] outputs = new OutputSlot[3];

	private IconButton cancel;
	private IconButton repeat;
	private static ArrayList<Item> lastIngredients = new ArrayList<>();
	private static Recipe lastRecipe = null;

	private Emitter smokeEmitter;
	private Emitter bubbleEmitter;
	private Emitter sparkEmitter;
	
	private Emitter lowerBubbles;
	private SkinnedBlock water;

	private Image energyIcon;
	private RenderedTextBlock energyLeft;
	private IconButton energyAdd;
	private boolean energyAddBlinking = false;
	private StyledButton btnGuide;
	private ExitButton btnExit;

	private static boolean splitAlchGuide = false;
	private WndJournal.AlchemyTab alchGuide = null;
	private static int centerW;


	private static final int BTN_SIZE	= 28;

	{
		inGameScene = true;
	}
	
	@Override
	public void create() {
		super.create();

		int w = Camera.main.width;
		int h = Camera.main.height;
		RectF insets = getCommonInsets();

		water = new SkinnedBlock(
				w, h,
				Dungeon.level.waterTex() ){
			
			@Override
			protected NoosaScript script() {
				return NoosaScriptNoLighting.get();
			}
			
			@Override
			public void draw() {
				//water has no alpha component, this improves performance
				Blending.disable();
				super.draw();
				Blending.enable();
			}
		};
		water.autoAdjust = true;
		add(water);
		
		Image im = new Image(TextureCache.createGradient(0x66000000, 0x88000000, 0xAA000000, 0xCC000000, 0xFF000000));
		im.angle = 90;
		im.x = w;
		im.scale.x = h/5f;
		im.scale.y = w;
		add(im);

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;

		btnExit = new ExitButton(){
			@Override
			protected void onClick() {
				Game.switchScene(GameScene.class);
			}
		};
		btnExit.setPos( insets.left + w - btnExit.width(), insets.top );
		add( btnExit );

		bubbleEmitter = new Emitter();
		add(bubbleEmitter);

		lowerBubbles = new Emitter();
		add(lowerBubbles);
		
		IconTitle title = new IconTitle(Icons.ALCHEMY.get(), Messages.get(this, "title") );
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (20 - title.height()) / 2f
		);
		align(title);
		add(title);
		
		int pw = Math.min(50 + w/2, 150);
		int left = (int)(insets.left) + (w - pw)/2;

		centerW = left + pw/2;

		int pos = (int)(insets.top) + (h - 120)/2;

		// Split mode disabled - always use window mode for guide
		// Original condition: splitAlchGuide && w >= 300 && h >= MIN_HEIGHT_FULL
		if (false && splitAlchGuide &&
				w >= 300 &&
				h >= PixelScene.MIN_HEIGHT_FULL){
			pw = Math.min(150, w/2);
			left = (w/2 - pw);
			centerW = left + pw/2;

			NinePatch guideBG = Chrome.get(Chrome.Type.TOAST);
			guideBG.size(126 + guideBG.marginHor(), Math.min(Camera.main.height - 18, 191 + guideBG.marginVer()));
			guideBG.y = Math.max(17, insets.top + (h - guideBG.height())/2f);
			guideBG.x = insets.left + w - left - guideBG.width();
			add(guideBG);

			alchGuide = new WndJournal.AlchemyTab();
			add(alchGuide);
			alchGuide.setRect(guideBG.x + guideBG.marginLeft(),
					guideBG.y + guideBG.marginTop(),
					guideBG.width() - guideBG.marginHor(),
					guideBG.height() - guideBG.marginVer());

		} else {
			splitAlchGuide = false;
		}
		
		RenderedTextBlock desc = PixelScene.renderTextBlock(6);
		desc.maxWidth(pw);
		desc.text( Messages.get(AlchemyScene.class, "text") );
		desc.setPos(left + (pw - desc.width())/2, pos);
		add(desc);
		
		pos += desc.height() + 6;

		NinePatch inputBG = Chrome.get(Chrome.Type.TOAST_TR);
		inputBG.x = left + 6;
		inputBG.y = pos;
		inputBG.size(BTN_SIZE+8, 3*BTN_SIZE + 4 + 8);
		add(inputBG);

		pos += 4;

		synchronized (inputs) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] == null) {
					inputs[i] = new InputButton();
				} else {
					//in case the scene was reset without calling destroy() for some reason
					Item item = inputs[i].item();
					inputs[i] = new InputButton();
					if (item != null){
						inputs[i].item(item);
					}
				}
				inputs[i].setRect(left + 10, pos, BTN_SIZE, BTN_SIZE);
				add(inputs[i]);
				pos += BTN_SIZE + 2;
			}
		}

		Button invSelector = new Button(){
			@Override
			protected void onClick() {
						if (Dungeon.hero != null) {
							ArrayList<Bag> bags = Dungeon.hero.belongings.getBags();

							String[] names = new String[bags.size()];
							Image[] images = new Image[bags.size()];
							for (int i = 0; i < bags.size(); i++){
								names[i] = Messages.titleCase(bags.get(i).name());
								images[i] = new ItemSprite(bags.get(i));
							}
							String info = "";
							if (ControllerHandler.controllerActive){
								info += KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(GameAction.LEFT_CLICK, true)) + ": " + Messages.get(Toolbar.class, "container_select") + "\n";
								info += KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(GameAction.BACK, true)) + ": " + Messages.get(Toolbar.class, "container_cancel");
							} else {
								info += Messages.get(WndKeyBindings.class, SPDAction.LEFT_CLICK.name()) + ": " + Messages.get(Toolbar.class, "container_select") + "\n";
								info += KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(GameAction.BACK, false)) + ": " + Messages.get(Toolbar.class, "container_cancel");
							}

							Game.scene().addToFront(new RadialMenu(Messages.get(Toolbar.class, "container_prompt"), info, names, images){
								@Override
								public void onSelect(int idx, boolean alt) {
									super.onSelect(idx, alt);
									Bag bag = bags.get(idx);
									ArrayList<Item> items = (ArrayList<Item>) bag.items.clone();

									for(Item i : bag.items){
										if (Dungeon.hero.belongings.lostInventory() && !i.keptThroughLostInventory()) items.remove(i);
										if (!Recipe.usableInRecipe(i)) items.remove(i);
									}

									if (items.size() == 0){
										SacredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(AlchemyScene.class, "no_items")));
										return;
									}

									String[] itemNames = new String[items.size()];
									Image[] itemIcons = new Image[items.size()];
									for (int i = 0; i < items.size(); i++){
										itemNames[i] = Messages.titleCase(items.get(i).name());
										itemIcons[i] = new ItemSprite(items.get(i));
									}

									String info = "";
									if (ControllerHandler.controllerActive){
										info += KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(GameAction.LEFT_CLICK, true)) + ": " + Messages.get(Toolbar.class, "item_select") + "\n";
										info += KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(GameAction.BACK, true)) + ": " + Messages.get(Toolbar.class, "item_cancel");
									} else {
										info += Messages.get(WndKeyBindings.class, SPDAction.LEFT_CLICK.name()) + ": " + Messages.get(Toolbar.class, "item_select") + "\n";
										info += KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(GameAction.BACK, false)) + ": " + Messages.get(Toolbar.class, "item_cancel");
									}

									Game.scene().addToFront(new RadialMenu(Messages.get(Toolbar.class, "item_prompt"), info, itemNames, itemIcons){
										@Override
										public void onSelect(int idx, boolean alt) {
											super.onSelect(idx, alt);
											Item item = items.get(idx);
											synchronized (inputs) {
												if (item != null && inputs[0] != null) {
													for (int i = 0; i < inputs.length; i++) {
														if (inputs[i].item() == null) {
															if (item instanceof LiquidMetal || item instanceof MissileWeapon){
																inputs[i].item(item.detachAll(Dungeon.hero.belongings.backpack));
															} else {
																inputs[i].item(item.detach(Dungeon.hero.belongings.backpack));
															}
															break;
														}
													}
													updateState();
												}
											}

										}
									});
								}
							});
						}
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.INVENTORY_SELECTOR;
			}
		};
		add(invSelector);

		cancel = new IconButton(Icons.CLOSE.get()){
			@Override
			protected void onClick() {
				super.onClick();
				clearSlots();
				updateState();
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.BACK;
			}

			@Override
			protected String hoverText() {
				return Messages.get(AlchemyScene.class, "cancel");
			}
		};
		cancel.setRect(left + 8, pos + 2, 16, 16);
		cancel.enable(false);
		add(cancel);

		repeat = new IconButton(Icons.REPEAT.get()){
			@Override
			protected void onClick() {
				super.onClick();
				if (lastRecipe != null){
					populate(lastIngredients, Dungeon.hero.belongings);
				}
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.TAG_RESUME;
			}

			@Override
			protected String hoverText() {
				return Messages.get(AlchemyScene.class, "repeat");
			}
		};
		repeat.setRect(left + 24, pos + 2, 16, 16);
		repeat.enable(false);
		add(repeat);

		lastIngredients.clear();
		lastRecipe = null;

		for (int i = 0; i < inputs.length; i++){
			combines[i] = new CombineButton(i);
			combines[i].enable(false);

			outputs[i] = new OutputSlot();
			outputs[i].item(null);

			if (i == 0){
				//first ones are always visible
				combines[i].setRect(left + (pw-30)/2f, inputs[1].top()+5, 30, inputs[1].height()-10);
				outputs[i].setRect(left + pw - BTN_SIZE - 10, inputs[1].top(), BTN_SIZE, BTN_SIZE);
			} else {
				combines[i].visible = false;
				outputs[i].visible = false;
			}

			add(combines[i]);
			add(outputs[i]);
		}

		smokeEmitter = new Emitter();
		smokeEmitter.pos(outputs[0].left() + (BTN_SIZE-16)/2f, outputs[0].top() + (BTN_SIZE-16)/2f, 16, 16);
		smokeEmitter.autoKill = false;
		add(smokeEmitter);
		
		pos += 10;

		if (Camera.main.height >= 280){
			//last elements get centered even with a split alch guide UI, as long as there's enough height
			centerW = (int)(insets.left) + w/2;
		}

		bubbleEmitter.pos(0,
				0,
				2*centerW,
				Camera.main.height);
		bubbleEmitter.autoKill = false;

		lowerBubbles.pos(0,
				pos,
				2*centerW,
				Math.max(0, h-pos));
		lowerBubbles.pour(Speck.factory( Speck.BUBBLE ), 0.1f );

		String energyText = Messages.get(AlchemyScene.class, "energy") + " " + Dungeon.energy;
		if (toolkit != null){
			energyText += "+" + toolkit.availableEnergy();
		}

		energyLeft = PixelScene.renderTextBlock(energyText, 8);
		energyLeft.setPos(
				centerW - energyLeft.width()/2,
				insets.top + h - 8 - energyLeft.height()
		);
		energyLeft.hardlight(0x44CCFF);
		add(energyLeft);

		energyIcon = new ItemSprite( toolkit != null ? ItemSpriteSheet.ARTIFACT_TOOLKIT : ItemSpriteSheet.ENERGY);
		energyIcon.x = energyLeft.left() - energyIcon.width();
		energyIcon.y = energyLeft.top() - (energyIcon.height() - energyLeft.height())/2;
		align(energyIcon);
		add(energyIcon);

		energyAdd = new IconButton(Icons.get(Icons.PLUS)){

			private float time = 0;

			@Override
			public void update() {
				super.update();
				if (energyAddBlinking){
					icon.brightness( 0.5f + (float)Math.abs(Math.cos( StatusPane.FLASH_RATE * (time += Game.elapsed) )));
				} else {
					if (time > 0){
						icon.resetColor();
					}
					time = 0;
				}
			}

			@Override
			protected void onClick() {
				WndEnergizeItem.openItemSelector();
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.TAG_ACTION;
			}

			@Override
			protected String hoverText() {
				return Messages.get(AlchemyScene.class, "energize");
			}
		};
		energyAdd.setRect(energyLeft.right(), energyLeft.top() - (16 - energyLeft.height())/2, 16, 16);
		align(energyAdd);
		add(energyAdd);

		sparkEmitter = new Emitter();
		sparkEmitter.pos(energyLeft.left(), energyLeft.top(), energyLeft.width(), energyLeft.height());
		sparkEmitter.autoKill = false;
		add(sparkEmitter);

		btnGuide = new StyledButton( Chrome.Type.TOAST_TR, Messages.get(AlchemyScene.class, "guide")){
			@Override
			protected void onClick() {
				super.onClick();
				// Split mode disabled - always use window mode
				// Original: if (Camera.main.width >= 300 && Camera.main.height >= MIN_HEIGHT_FULL) toggle split mode
				clearSlots();
				updateState();
				clearFocus();  // Disable keyboard navigation while guide is open
				Window guideWnd = new WndAlchemyGuide();
				guideWnd.setOnHideCallback(() -> {
					// Re-enable keyboard navigation when guide closes
					// Focus will be set on next navigation key press
				});
				AlchemyScene.this.addToFront(guideWnd);
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.JOURNAL;
			}

			@Override
			protected String hoverText() {
				return Messages.titleCase(Document.ALCHEMY_GUIDE.title());
			}
		};
		btnGuide.icon(new ItemSprite(ItemSpriteSheet.ALCH_PAGE));
		btnGuide.setSize(btnGuide.reqWidth()+4, 18);
		btnGuide.setPos(centerW - btnGuide.width()/2f, energyAdd.top()- btnGuide.height()-2);
		align(btnGuide);
		add(btnGuide);

		// Register focusable buttons for keyboard navigation
		// Order: inputs -> cancel/repeat -> combines/outputs -> guide -> energyAdd -> exit
		for (int i = 0; i < inputs.length; i++) {
			focusableButtons.add(inputs[i]);
		}
		focusableButtons.add(cancel);
		focusableButtons.add(repeat);
		for (int i = 0; i < combines.length; i++) {
			focusableButtons.add(combines[i]);
			focusableButtons.add(outputs[i]);
		}
		focusableButtons.add(btnGuide);
		focusableButtons.add(energyAdd);
		focusableButtons.add(btnExit);

		// Register for keyboard events
		KeyEvent.addKeyListener(this);

		TrinketCatalyst cata = Dungeon.hero.belongings.getItem(TrinketCatalyst.class);
		if (cata != null && cata.hasRolledTrinkets()){
			addToFront(new TrinketCatalyst.WndTrinket(cata));
		}

		fadeIn();

		saveNeeded = false;
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
			Journal.saveGlobal();
		} catch (IOException e) {
			SacredPixelDungeon.reportException(e);
		}
	}
	
	@Override
	protected void onBackPressed() {
		Game.switchScene(GameScene.class);
	}
	
	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(AlchemyScene.class, "select");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return Recipe.usableInRecipe(item);
		}

		@Override
		public void onSelect( Item item ) {
			synchronized (inputs) {
				if (item != null && inputs[0] != null) {
					for (int i = 0; i < inputs.length; i++) {
						if (inputs[i].item() == null) {
							if (item instanceof LiquidMetal || item instanceof MissileWeapon){
								inputs[i].item(item.detachAll(Dungeon.hero.belongings.backpack));
							} else {
								inputs[i].item(item.detach(Dungeon.hero.belongings.backpack));
							}
							break;
						}
					}
					updateState();
				}
			}
		}
	};
	
	private<T extends Item> ArrayList<T> filterInput(Class<? extends T> itemClass){
		ArrayList<T> filtered = new ArrayList<>();
		for (int i = 0; i < inputs.length; i++){
			Item item = inputs[i].item();
			if (item != null && Compat.isInstance(itemClass, item)){
				filtered.add((T)item);
			}
		}
		return filtered;
	}
	
	private void updateState(){

		repeat.enable(false);

		ArrayList<Item> ingredients = filterInput(Item.class);
		ArrayList<Recipe> recipes = Recipe.findRecipes(ingredients);

		//disables / hides unneeded buttons
		for (int i = recipes.size(); i < combines.length; i++){
			combines[i].enable(false);
			outputs[i].item(null);

			if (i != 0){
				combines[i].visible = false;
				outputs[i].visible = false;
			}
		}

		cancel.enable(!ingredients.isEmpty());

		if (recipes.isEmpty()){
			combines[0].setPos(combines[0].left(), inputs[1].top()+5);
			outputs[0].setPos(outputs[0].left(), inputs[1].top());
			energyAddBlinking = false;
			return;
		}

		//positions active buttons
		float gap = recipes.size() == 2 ? 6 : 2;

		float height = inputs[2].bottom() - inputs[0].top();
		height -= recipes.size()*BTN_SIZE + (recipes.size()-1)*gap;
		float top = inputs[0].top() + height/2;

		//positions and enables active buttons
		boolean promptToAddEnergy = false;
		for (int i = 0; i < recipes.size(); i++){

			Recipe recipe = recipes.get(i);

			int cost = recipe.cost(ingredients);

			outputs[i].visible = true;
			outputs[i].setRect(outputs[0].left(), top, BTN_SIZE, BTN_SIZE);
			outputs[i].item(recipe.sampleOutput(ingredients));
			top += BTN_SIZE+gap;

			int availableEnergy = Dungeon.energy;
			if (toolkit != null){
				availableEnergy += toolkit.availableEnergy();
			}

			combines[i].visible = true;
			combines[i].setRect(combines[0].left(), outputs[i].top()+5, 30, 20);
			combines[i].enable(cost <= availableEnergy, cost);

			if (cost > availableEnergy && recipe instanceof TrinketCatalyst.Recipe){
				promptToAddEnergy = true;
			}

		}

		energyAddBlinking = promptToAddEnergy;

		if (alchGuide != null){
			alchGuide.updateList();
		}

	}
	
	private void combine( int slot ){
		
		ArrayList<Item> ingredients = filterInput(Item.class);
		if (ingredients.isEmpty()) return;

		lastIngredients.clear();
		for (Item i : ingredients){
			lastIngredients.add(i.duplicate());
		}

		ArrayList<Recipe> recipes = Recipe.findRecipes(ingredients);
		if (recipes.size() <= slot) return;

		Recipe recipe = recipes.get(slot);
		
		Item result = null;
		
		if (recipe != null){
			int cost = recipe.cost(ingredients);
			if (toolkit != null){
				cost = toolkit.consumeEnergy(cost);
			}
			Catalog.countUses(EnergyCrystal.class, cost);
			Dungeon.energy -= cost;

			String energyText = Messages.get(AlchemyScene.class, "energy") + " " + Dungeon.energy;
			if (toolkit != null){
				energyText += "+" + toolkit.availableEnergy();
			}
			energyLeft.text(energyText);
			energyLeft.setPos(
					centerW - energyLeft.width()/2,
					energyLeft.top()
			);

			energyIcon.x = energyLeft.left() - energyIcon.width();
			align(energyIcon);

			energyAdd.setPos(energyLeft.right(), energyAdd.top());
			align(energyAdd);
			
			result = recipe.brew(ingredients);
		}
		
		if (result != null){

			craftItem(ingredients, result);

		}

		boolean foundItems = true;
		for (Item i : lastIngredients){
			Item found = Dungeon.hero.belongings.getSimilar(i);
			if (found == null){ //atm no quantity check as items are always loaded individually
				//currently found can be true if we need, say, 3x of an item but only have 2x of it
				foundItems = false;
			}
		}

		lastRecipe = recipe;
		repeat.enable(foundItems);

		cancel.enable(false);
		synchronized (inputs) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] != null && inputs[i].item() != null) {
					cancel.enable(true);
					break;
				}
			}
		}

		if (alchGuide != null){
			alchGuide.updateList();
		}
	}

	public void craftItem( ArrayList<Item> ingredients, Item result ){
		bubbleEmitter.start(Speck.factory( Speck.BUBBLE ), 0.01f, 100 );
		smokeEmitter.burst(Speck.factory( Speck.WOOL ), 10 );
		Sample.INSTANCE.play( Assets.Sounds.PUFF );

		int resultQuantity = result.quantity();
		if (!result.collect()){
			Dungeon.level.drop(result, Dungeon.hero.pos);
		}

		Statistics.itemsCrafted++;
		Badges.validateItemsCrafted();

		saveNeeded = false;
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
			Journal.saveGlobal();
		} catch (IOException e) {
			SacredPixelDungeon.reportException(e);
		}

		synchronized (inputs) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] != null && inputs[i].item() != null) {
					Item item = inputs[i].item();
					if (item.quantity() <= 0) {
						inputs[i].item(null);
					} else {
						inputs[i].slot.updateText();
					}
				}
			}
		}

		updateState();
		//we reset the quantity in case the result was merged into another stack in the backpack
		result.quantity(resultQuantity);
		outputs[0].item(result);
	}
	
	public void populate(ArrayList<Item> toFind, Belongings inventory){
		clearSlots();
		
		int curslot = 0;
		for (Item finding : toFind){
			int needed = finding.quantity();
			ArrayList<Item> found = inventory.getAllSimilar(finding);
			while (!found.isEmpty() && needed > 0){
				Item detached;
				if (finding instanceof LiquidMetal || finding instanceof MissileWeapon) {
					detached = found.get(0).detachAll(inventory.backpack);
				} else {
					detached = found.get(0).detach(inventory.backpack);
				}
				inputs[curslot].item(detached);
				curslot++;
				needed -= detached.quantity();
				if (detached == found.get(0)) {
					found.remove(0);
				}
			}
		}
		updateState();
	}

	private boolean saveNeeded = false;

	@Override
	public void onPause() {
		if (saveNeeded) {
			saveNeeded = false;
			clearSlots();
			updateState();
			try {
				Dungeon.saveAll();
				Badges.saveGlobal();
				Journal.saveGlobal();
			} catch (IOException e) {
				SacredPixelDungeon.reportException(e);
			}
		}
	}

	@Override
	public void destroy() {
		// Remove keyboard listener
		KeyEvent.removeKeyListener(this);
		focusableButtons.clear();
		focusIndex = -1;

		//clear slots inline, handle each item individually to prevent one failure from blocking others
		synchronized ( inputs ) {
			for (int i = 0; i < inputs.length; i++) {
				try {
					if (inputs[i] != null && inputs[i].item() != null) {
						Item item = inputs[i].item();
						if (!item.collect()) {
							if (Dungeon.level != null && Dungeon.hero != null) {
								Dungeon.level.drop(item, Dungeon.hero.pos);
							}
						}
						inputs[i].item(null);
					}
				} catch (Exception e) {
					SacredPixelDungeon.reportException(e);
				}
				inputs[i] = null;
			}
		}

		saveNeeded = false;
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
			Journal.saveGlobal();
		} catch (Exception e) {
			SacredPixelDungeon.reportException(e);
		}
		super.destroy();
	}
	
	public void clearSlots(){
		synchronized ( inputs ) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] != null && inputs[i].item() != null) {
					Item item = inputs[i].item();
					if (!item.collect()) {
						if (Dungeon.level != null && Dungeon.hero != null) {
							Dungeon.level.drop(item, Dungeon.hero.pos);
						}
					}
					inputs[i].item(null);
				}
			}
		}
		cancel.enable(false);
		repeat.enable(lastRecipe != null);
		if (alchGuide != null){
			alchGuide.updateList();
		}
	}

	public void createEnergy(){
		String energyText = Messages.get(AlchemyScene.class, "energy") + " " + Dungeon.energy;
		if (toolkit != null){
			energyText += "+" + toolkit.availableEnergy();
		}
		energyLeft.text(energyText);
		energyLeft.setPos(
				centerW - energyLeft.width()/2,
				energyLeft.top()
		);

		energyIcon.x = energyLeft.left() - energyIcon.width();
		align(energyIcon);

		energyAdd.setPos(energyLeft.right(), energyAdd.top());
		align(energyAdd);

		bubbleEmitter.start(Speck.factory( Speck.BUBBLE ), 0.01f, 100 );
		sparkEmitter.burst(SparkParticle.FACTORY, 20);
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );

		//queue a save here, as items may be in the input windows and we don't want to clear them
		// but if the game becomes paused we do this to prevent exploits
		saveNeeded = true;
		updateState();
	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed) {
			// Don't handle navigation if a window is open (e.g., guide window)
			if (hasOpenWindows()) {
				return false;
			}

			GameAction action = KeyBindings.getActionForKey(event);

			// Handle navigation keys
			if (action == SPDAction.N || action == SPDAction.NW
					|| action == SPDAction.W || action == SPDAction.SW) {
				moveFocus(-1);
				return true;
			} else if (action == SPDAction.S || action == SPDAction.SE
					|| action == SPDAction.E || action == SPDAction.NE) {
				moveFocus(1);
				return true;
			} else if (event.code == Input.Keys.ENTER || event.code == Input.Keys.NUMPAD_ENTER) {
				if (focusIndex >= 0) {
					activateFocused();
					return true;
				}
			}
		}
		return false;
	}


	protected void moveFocus(int direction) {
		if (focusableButtons.isEmpty()) return;

		// Restore focus state on previously focused element
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			focusableButtons.get(focusIndex).restoreFocusState();
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

		// Skip disabled/invisible elements
		focusIndex = startIndex;
		int checked = 0;
		while (!focusableButtons.get(focusIndex).isActive() && checked < focusableButtons.size()) {
			focusIndex += direction;
			if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
			if (focusIndex >= focusableButtons.size()) focusIndex = 0;
			checked++;
		}

		// Save focus state then highlight new element
		focusableButtons.get(focusIndex).saveFocusState();
		focusableButtons.get(focusIndex).setFocused(true);
	}

	protected void activateFocused() {
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			Focusable focusable = focusableButtons.get(focusIndex);
			if (focusable.isActive()) {
				Sample.INSTANCE.play(Assets.Sounds.CLICK);
				focusable.click();
			}
		}
	}

	public void clearFocus() {
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			focusableButtons.get(focusIndex).restoreFocusState();
		}
		focusIndex = -1;
	}

	// Pending identify window data (to show after WndBag opens)
	private static Item pendingIdentifyItem = null;
	private static String pendingOldName = null;

	public void showIdentify(Item item){
		if (item.isIdentified()) return;

		// Store old name and icon before identifying
		pendingOldName = Messages.titleCase(item.title());

		item.identify();

		// Store the item for delayed window creation
		pendingIdentifyItem = item;
	}

	@Override
	public void update() {
		super.update();
		water.offset( 0, -5 * Game.elapsed );

		// Show pending identify window (delayed so it appears on top of WndBag)
		if (pendingIdentifyItem != null) {
			String newName = Messages.titleCase(pendingIdentifyItem.title());
			addToFront(new WndAlchemyIdentify(pendingIdentifyItem, pendingOldName, newName));
			pendingIdentifyItem = null;
			pendingOldName = null;
		}
	}

	// Window for showing item identification in alchemy
	// This window cannot be closed by user (ESC, outside click, etc.) - it closes automatically
	public static class WndAlchemyIdentify extends Window {
		private static final float PHASE_DURATION = 0.5f;
		private static final float HOLD_DURATION = 1.5f;
		private static final int WIDTH_P = 149; // Portrait max width
		private static final int WIDTH_L = 251; // Landscape max width
		private static final int MARGIN = 6;

		private IconTitle title;
		private String oldName;
		private String newName;
		private int contentWidth;
		private float elapsed = 0;
		private int phase = 0; // 0=show old, 1=transition, 2=show new, 3=fade out

		// Prevent closing by ESC key or clicking outside the window
		@Override
		public void onBackPressed() {
			// Do nothing - window closes automatically after animation completes
		}

		public WndAlchemyIdentify(Item item, String oldName, String newName) {
			super();
			this.oldName = oldName;
			this.newName = newName;

			int maxWidth = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

			// Create title with item icon
			title = new IconTitle(new ItemSprite(item), oldName);
			title.color(TITLE_COLOR);
			add(title);

			// Calculate dynamic width based on longer name (old or new)
			// First measure old name width (use large width for single-line measurement)
			title.setSize(maxWidth, 0);
			float oldWidth = title.reqWidth();

			// Measure new name width
			title.label(newName);
			title.setSize(maxWidth, 0);
			float newWidth = title.reqWidth();

			// Use the larger of the two, capped at maxWidth
			// +8 padding to account for measurement vs rendering differences
			contentWidth = (int)Math.ceil(Math.max(oldWidth, newWidth)) + 8;
			contentWidth = Math.min(contentWidth, maxWidth - MARGIN);

			// Reset to old name and set final size
			title.label(oldName);
			title.setSize(contentWidth, 0);
			float oldHeight = title.height();

			// Also measure new name height (in case it wraps differently)
			title.label(newName);
			title.setSize(contentWidth, 0);
			float newHeight = title.height();

			// Use the larger height to prevent window resize during animation
			float maxHeight = Math.max(oldHeight, newHeight);

			// Reset to old name for initial display
			title.label(oldName);
			title.setSize(contentWidth, 0);

			int windowWidth = contentWidth + MARGIN;
			int windowHeight = (int)maxHeight + MARGIN;
			resize(windowWidth, windowHeight);

			// Center title vertically within the window
			float titleY = (windowHeight - title.height()) / 2f;
			title.setPos(MARGIN / 2f, titleY);
		}

		// Recenter title when text changes (for multi-line cases)
		private void recenterTitle() {
			int windowHeight = height;
			float titleY = (windowHeight - title.height()) / 2f;
			title.setPos(MARGIN / 2f, Math.max(MARGIN / 2f, titleY));
		}

		@Override
		public void update() {
			super.update();
			elapsed += Game.elapsed;

			switch (phase) {
				case 0: // Show old name
					if (elapsed >= PHASE_DURATION) {
						elapsed = 0;
						phase = 1;
					}
					break;
				case 1: // Transition - fade out old, change text, fade in new
					float progress = elapsed / PHASE_DURATION;
					if (progress < 0.5f) {
						// Fade out
						title.alpha(1f - progress * 2);
					} else {
						// Change text at midpoint
						if (progress >= 0.5f && title.alpha() < 0.1f) {
							title.label(newName);
							title.setSize(contentWidth, 0);
							recenterTitle();
						}
						// Fade in
						title.alpha((progress - 0.5f) * 2);
					}
					if (elapsed >= PHASE_DURATION) {
						elapsed = 0;
						phase = 2;
						title.alpha(1f);
						title.label(newName);
						title.setSize(contentWidth, 0);
						recenterTitle();
					}
					break;
				case 2: // Hold new name
					if (elapsed >= HOLD_DURATION) {
						elapsed = 0;
						phase = 3;
					}
					break;
				case 3: // Fade out and close
					float fadeAlpha = 1f - (elapsed / PHASE_DURATION);
					title.alpha(Math.max(0, fadeAlpha));
					if (fadeAlpha <= 0) {
						hide();
					}
					break;
			}
		}
	}
	
	private class InputButton extends Component implements Focusable {

		protected NinePatch bg;
		protected ItemSlot slot;

		private Item item = null;
		private float savedBrightness = 1f;
		private boolean focused = false;

		@Override
		protected void createChildren() {
			super.createChildren();

			bg = Chrome.get( Chrome.Type.RED_BUTTON);
			add( bg );
			
			slot = new ItemSlot() {
				@Override
				protected void onPointerDown() {
					bg.brightness( 1.2f );
					Sample.INSTANCE.play( Assets.Sounds.CLICK );
				}
				@Override
				protected void onPointerUp() {
					bg.resetColor();
				}
				@Override
				protected void onClick() {
					super.onClick();
					Item item = InputButton.this.item;
					if (item != null) {
						if (!item.collect()) {
							Dungeon.level.drop(item, Dungeon.hero.pos);
						}
						InputButton.this.item(null);
						updateState();
					}
					AlchemyScene.this.addToFront(WndBag.getBag( itemSelector ));
				}

				@Override
				protected boolean onLongClick() {
					Item item = InputButton.this.item;
					if (item != null){
						AlchemyScene.this.addToFront(new WndInfoItem(item));
						return true;
					}
					return false;
				}

				@Override
				//only the first empty button accepts key input
				public GameAction keyAction() {
					for (InputButton i : inputs){
						if (i.item == null || i.item instanceof WndBag.Placeholder) {
							if (i == InputButton.this) {
								return SPDAction.INVENTORY;
							} else {
								return super.keyAction();
							}
						}
					}
					return super.keyAction();
				}

				@Override
				protected String hoverText() {
					if (item == null || item instanceof WndBag.Placeholder){
						return Messages.get(AlchemyScene.class, "add");
					}
					return super.hoverText();
				}

				@Override
				public GameAction secondaryTooltipAction() {
					return SPDAction.INVENTORY_SELECTOR;
				}
			};
			slot.enable(true);
			add( slot );
		}

		@Override
		protected void layout() {
			super.layout();
			
			bg.x = x;
			bg.y = y;
			bg.size( width, height );
			
			slot.setRect( x + 2, y + 2, width - 4, height - 4 );
		}

		public Item item(){
			return item;
		}

		public void item( Item item ) {
			if (item == null){
				this.item = null;
				slot.item(new WndBag.Placeholder(ItemSpriteSheet.SOMETHING));
			} else {
				slot.item(this.item = item);
			}
		}

		@Override
		public void setFocused(boolean focused) {
			this.focused = focused;
			if (focused) {
				bg.brightness(1.3f);
			} else {
				bg.brightness(savedBrightness);
			}
		}

		@Override
		public void saveFocusState() {
			savedBrightness = 1f;
		}

		@Override
		public void restoreFocusState() {
			bg.brightness(savedBrightness);
			focused = false;
		}

		@Override
		public void click() {
			slot.click();
		}

		@Override
		public boolean isActive() {
			return visible && active;
		}
	}

	private class CombineButton extends Component implements Focusable {

		protected int slot;

		protected RedButton button;
		protected RenderedTextBlock costText;

		private CombineButton(int slot){
			super();

			this.slot = slot;
		}

		@Override
		public void setFocused(boolean focused) {
			button.setFocused(focused);
		}

		@Override
		public void saveFocusState() {
			button.saveFocusState();
		}

		@Override
		public void restoreFocusState() {
			button.restoreFocusState();
		}

		@Override
		public void click() {
			button.click();
		}

		@Override
		public boolean isActive() {
			return visible && active && button.active;
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			button = new RedButton(""){
				@Override
				protected void onClick() {
					super.onClick();
					combine(slot);
				}

				@Override
				protected String hoverText() {
					return Messages.get(AlchemyScene.class, "craft");
				}

				@Override
				public GameAction keyAction() {
					if (slot == 0 && !combines[1].active && !combines[2].active){
						return SPDAction.TAG_LOOT;
					}
					return super.keyAction();
				}
			};
			button.icon(Icons.get(Icons.ARROW));
			add(button);

			costText = PixelScene.renderTextBlock(6);
			add(costText);
		}

		@Override
		protected void layout() {
			super.layout();

			button.setRect(x, y, width(), height());

			costText.setPos(
					left() + (width() - costText.width())/2,
					top() - costText.height()
			);
		}

		public void enable( boolean enabled ){
			enable(enabled, 0);
		}

		public void enable( boolean enabled, int cost ){
			button.enable(enabled);
			if (enabled) {
				button.icon().tint(1, 1, 0, 1);
				button.alpha(1f);
				costText.hardlight(0x44CCFF);
			} else {
				button.icon().color(0, 0, 0);
				button.alpha(0.6f);
				costText.hardlight(0xFF0000);
			}

			if (cost == 0){
				costText.visible = false;
			} else {
				costText.visible = true;
				costText.text(Messages.get(AlchemyScene.class, "energy") + " " + cost);
			}

			layout();
			active = enabled;
		}

	}

	private class OutputSlot extends Component implements Focusable {

		protected NinePatch bg;
		protected ItemSlot slot;
		private float savedBrightness = 1f;
		private boolean focused = false;

		@Override
		protected void createChildren() {

			bg = Chrome.get(Chrome.Type.TOAST_TR);
			add(bg);

			slot = new ItemSlot() {
				@Override
				protected void onClick() {
					super.onClick();
					if (visible && item != null && item.trueName() != null){
						AlchemyScene.this.addToFront(new WndInfoItem(item));
					}
				}
			};
			slot.item(null);
			add( slot );
		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;
			bg.size(width(), height());

			slot.setRect(x+2, y+2, width()-4, height()-4);
		}

		public void item( Item item ) {
			slot.item(item);
		}

		@Override
		public void setFocused(boolean focused) {
			this.focused = focused;
			if (focused) {
				bg.brightness(1.3f);
			} else {
				bg.brightness(savedBrightness);
			}
		}

		@Override
		public void saveFocusState() {
			savedBrightness = 1f;
		}

		@Override
		public void restoreFocusState() {
			bg.brightness(savedBrightness);
			focused = false;
		}

		@Override
		public void click() {
			slot.click();
		}

		@Override
		public boolean isActive() {
			return visible && active && slot.item() != null;
		}
	}

	private static AlchemistsToolkit toolkit;

	public static void assignToolkit( AlchemistsToolkit toolkit ){
		AlchemyScene.toolkit = toolkit;
	}

	public static void clearToolkit(){
		AlchemyScene.toolkit = null;
	}

	// Dedicated window class for Alchemy Guide with keyboard navigation support
	public static class WndAlchemyGuide extends Window implements Signal.Listener<KeyEvent> {

		private WndJournal.AlchemyTab alchemyTab;
		private boolean contentFocusMode = false;

		public WndAlchemyGuide() {
			super();

			alchemyTab = new WndJournal.AlchemyTab();
			int w, h;
			if (PixelScene.landscape()) {
				w = WndJournal.WIDTH_L;
				h = WndJournal.HEIGHT_L + 8;
			} else {
				w = WndJournal.WIDTH_P;
				h = WndJournal.HEIGHT_P + 10;
			}
			resize(w, h);
			add(alchemyTab);
			alchemyTab.setRect(0, 0, w, h);

			// Register page buttons for keyboard navigation
			for (int i = 0; i < alchemyTab.pageButtons.length; i++) {
				addFocusableButton(alchemyTab.pageButtons[i]);
			}

			// Register for keyboard events
			KeyEvent.addKeyListener(this);
		}

		@Override
		public void hide() {
			KeyEvent.removeKeyListener(this);
			super.hide();
		}

		@Override
		public void destroy() {
			KeyEvent.removeKeyListener(this);
			super.destroy();
		}

		private void enterContentFocusMode() {
			contentFocusMode = true;
			clearFocus();
			alchemyTab.activateRecipeNavigation();
		}

		private void exitContentFocusMode() {
			contentFocusMode = false;
			alchemyTab.deactivateRecipeNavigation();
			// Re-enable button focus
			if (!focusableButtons.isEmpty()) {
				focusIndex = WndJournal.AlchemyTab.currentPageIdx;
				if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
					focusableButtons.get(focusIndex).setFocused(true);
				}
			}
		}

		@Override
		public boolean onSignal(KeyEvent event) {
			// Don't process keyboard events if window is not active
			if (!active) return false;

			// Handle ESC/BACK key
			if (event.pressed && KeyBindings.getActionForKey(event) == SPDAction.BACK) {
				if (contentFocusMode) {
					exitContentFocusMode();
					return true;
				}
				onBackPressed();
				return true;
			}

			// Handle JOURNAL key to close
			if (event.pressed && KeyBindings.getActionForKey(event) == SPDAction.JOURNAL) {
				onBackPressed();
				return true;
			}

			// Handle Enter key
			if (event.pressed && (event.code == Input.Keys.ENTER || event.code == Input.Keys.NUMPAD_ENTER)) {
				if (contentFocusMode) {
					if (alchemyTab.isRecipeNavigationActive()) {
						alchemyTab.activateFocusedItem();
						return true;
					}
				} else if (focusIndex >= 0) {
					// Activate focused page button
					activateFocused();
					// Enter content focus mode for recipe navigation
					enterContentFocusMode();
					return true;
				}
				return true;
			}

			// Handle arrow key navigation
			if (event.pressed) {
				GameAction action = KeyBindings.getActionForKey(event);

				if (contentFocusMode && alchemyTab.isRecipeNavigationActive()) {
					// Navigate within recipes
					if (action == SPDAction.W || action == SPDAction.NW || action == SPDAction.SW) {
						alchemyTab.moveFocusHorizontal(-1);
						return true;
					} else if (action == SPDAction.E || action == SPDAction.NE || action == SPDAction.SE) {
						alchemyTab.moveFocusHorizontal(1);
						return true;
					} else if (action == SPDAction.N) {
						alchemyTab.moveFocusVertical(-1);
						return true;
					} else if (action == SPDAction.S) {
						alchemyTab.moveFocusVertical(1);
						return true;
					}
				} else {
					// Navigate page buttons
					if (action == SPDAction.W || action == SPDAction.NW || action == SPDAction.SW) {
						moveFocus(-1);
						return true;
					} else if (action == SPDAction.E || action == SPDAction.NE || action == SPDAction.SE) {
						moveFocus(1);
						return true;
					} else if (action == SPDAction.S) {
						// Down arrow enters content mode
						enterContentFocusMode();
						return true;
					}
				}
			}

			return false;
		}
	}

}
