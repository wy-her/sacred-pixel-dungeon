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

import com.sacredpixel.sacredpixeldungeon.Badges;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroClass;
import com.sacredpixel.sacredpixeldungeon.actors.hero.HeroSubClass;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.TalentButton;
import com.sacredpixel.sacredpixeldungeon.ui.TalentsPane;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndHeroInfo extends WndTabbed {

	private HeroInfoTab heroInfo;
	private TalentInfoTab talentInfo;
	private SubclassInfoTab subclassInfo;
	private ArmorAbilityInfoTab abilityInfo;

	private static final int WIDTH_MIN = 149;
	private static final int WIDTH_MAX = 251;
	private static final int MIN_HEIGHT = 125;
	private static final int TARGET_HEIGHT = 152;  // Same as WndRanking HEIGHT
	private static final int MARGIN = 2;

	public WndHeroInfo( HeroClass cl ){

		Image tabIcon;
		switch (cl){
			case WARRIOR: default:
				tabIcon = new ItemSprite(ItemSpriteSheet.SEAL, null);
				break;
			case MAGE:
				tabIcon = new ItemSprite(ItemSpriteSheet.MAGES_STAFF, null);
				break;
			case ROGUE:
				tabIcon = new ItemSprite(ItemSpriteSheet.ARTIFACT_CLOAK, null);
				break;
			case HUNTRESS:
				tabIcon = new ItemSprite(ItemSpriteSheet.SPIRIT_BOW, null);
				break;
			case DUELIST:
				tabIcon = new ItemSprite(ItemSpriteSheet.RAPIER, null);
				break;
			case CLERIC:
				tabIcon = new ItemSprite(ItemSpriteSheet.ARTIFACT_TOME, null);
				break;
		}

		int width = WIDTH_MIN;
		int finalHeight = MIN_HEIGHT;

		heroInfo = new HeroInfoTab(cl);
		add(heroInfo);
		heroInfo.setSize(width, MIN_HEIGHT);
		heroInfo.visible = heroInfo.active = false;
		finalHeight = (int)Math.max(finalHeight, heroInfo.height());

		add( new IconTab( tabIcon ){
			@Override
			protected void select(boolean value) {
				super.select(value);
				heroInfo.visible = heroInfo.active = value;
				if (value) {
					// Clear TalentsPane focus when switching to other tabs
					if (talentInfo != null && talentInfo.talentPane != null) {
						talentInfo.talentPane.clearFocus();
					}
					clearFocus();
					rebuildFocusableButtons();
				}
			}
		});

		talentInfo = new TalentInfoTab(cl);
		add(talentInfo);
		talentInfo.setSize(width, MIN_HEIGHT);
		talentInfo.visible = talentInfo.active = false;
		finalHeight = (int)Math.max(finalHeight, talentInfo.height());

		add( new IconTab( Icons.get(Icons.TALENT) ){
			@Override
			protected void select(boolean value) {
				super.select(value);
				talentInfo.visible = talentInfo.active = value;
				if (value) {
					clearFocus();
					rebuildFocusableButtons();
					// Start with no focus initially - user must navigate to focus
					if (talentInfo.talentPane != null) {
						talentInfo.talentPane.clearFocus();
					}
				} else {
					// Clear TalentsPane focus when leaving talents tab
					if (talentInfo.talentPane != null) {
						talentInfo.talentPane.clearFocus();
					}
				}
			}
		});

		if (Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2) || DeviceCompat.isDebug()) {
			subclassInfo = new SubclassInfoTab(cl);
			add(subclassInfo);
			subclassInfo.setSize(width, MIN_HEIGHT);
			subclassInfo.visible = subclassInfo.active = false;
			finalHeight = (int)Math.max(finalHeight, subclassInfo.height());

			add(new IconTab(new ItemSprite(ItemSpriteSheet.MASK, null)) {
				@Override
				protected void select(boolean value) {
					super.select(value);
					subclassInfo.visible = subclassInfo.active = value;
					if (value) {
						// Clear TalentsPane focus when switching to other tabs
						if (talentInfo != null && talentInfo.talentPane != null) {
							talentInfo.talentPane.clearFocus();
						}
						clearFocus();
						rebuildFocusableButtons();
					}
				}
			});
		}

		if (Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4) || DeviceCompat.isDebug()) {
			abilityInfo = new ArmorAbilityInfoTab(cl);
			add(abilityInfo);
			abilityInfo.setSize(width, MIN_HEIGHT);
			abilityInfo.visible = abilityInfo.active = false;
			finalHeight = (int)Math.max(finalHeight, abilityInfo.height());

			add(new IconTab(new ItemSprite(ItemSpriteSheet.CROWN, null)) {
				@Override
				protected void select(boolean value) {
					super.select(value);
					abilityInfo.visible = abilityInfo.active = value;
					if (value) {
						// Clear TalentsPane focus when switching to other tabs
						if (talentInfo != null && talentInfo.talentPane != null) {
							talentInfo.talentPane.clearFocus();
						}
						clearFocus();
						rebuildFocusableButtons();
					}
				}
			});
		}

		// Dynamic width expansion in landscape mode
		while (PixelScene.landscape()
				&& finalHeight > TARGET_HEIGHT
				&& width < WIDTH_MAX) {
			width += 20;

			// Recalculate heights with new width
			finalHeight = MIN_HEIGHT;

			heroInfo.setSize(width, MIN_HEIGHT);
			finalHeight = (int)Math.max(finalHeight, heroInfo.height());

			talentInfo.setSize(width, MIN_HEIGHT);
			finalHeight = (int)Math.max(finalHeight, talentInfo.height());

			if (subclassInfo != null) {
				subclassInfo.setSize(width, MIN_HEIGHT);
				finalHeight = (int)Math.max(finalHeight, subclassInfo.height());
			}

			if (abilityInfo != null) {
				abilityInfo.setSize(width, MIN_HEIGHT);
				finalHeight = (int)Math.max(finalHeight, abilityInfo.height());
			}
		}

		resize(width, finalHeight);

		layoutTabs();
		talentInfo.layout();

		select(0);

		rebuildFocusableButtons();
	}

	private void rebuildFocusableButtons() {
		focusableButtons.clear();
		focusIndex = -1;

		// Add subclass info buttons if visible
		if (subclassInfo != null && subclassInfo.visible && subclassInfo.subClsInfos != null) {
			for (IconButton btn : subclassInfo.subClsInfos) {
				addFocusableButton(btn);
			}
		}

		// Add armor ability info buttons if visible
		if (abilityInfo != null && abilityInfo.visible && abilityInfo.abilityInfos != null) {
			for (IconButton btn : abilityInfo.abilityInfos) {
				addFocusableButton(btn);
			}
		}
	}

	@Override
	public void offset(int xOffset, int yOffset) {
		super.offset(xOffset, yOffset);
		talentInfo.layout();
	}

	private static class HeroInfoTab extends Component {

		private RenderedTextBlock title;
		private RenderedTextBlock[] info;
		private Image[] icons;

		public HeroInfoTab(HeroClass cls){
			super();
			title = PixelScene.renderTextBlock(Messages.titleCase(cls.title()), 8);
			title.hardlight(TITLE_COLOR);
			add(title);

			String[] desc_entries = cls.desc().split("\n\n");

			info = new RenderedTextBlock[desc_entries.length];

			for (int i = 0; i < desc_entries.length; i++){
				info[i] = PixelScene.renderTextBlock(desc_entries[i], 6);
				add(info[i]);
			}

			switch (cls){
				case WARRIOR: default:
					icons = new Image[]{ new ItemSprite(ItemSpriteSheet.SEAL),
							new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD),
							new ItemSprite(ItemSpriteSheet.SCROLL_ISAZ)};
					break;
				case MAGE:
					icons = new Image[]{ new ItemSprite(ItemSpriteSheet.MAGES_STAFF),
							new ItemSprite(ItemSpriteSheet.WAND_MAGIC_MISSILE),
							new ItemSprite(ItemSpriteSheet.SCROLL_ISAZ)};
					break;
				case ROGUE:
					icons = new Image[]{ new ItemSprite(ItemSpriteSheet.ARTIFACT_CLOAK),
							Icons.get(Icons.STAIRS),
							new ItemSprite(ItemSpriteSheet.DAGGER),
							new ItemSprite(ItemSpriteSheet.SCROLL_ISAZ)};
					break;
				case HUNTRESS:
					icons = new Image[]{ new ItemSprite(ItemSpriteSheet.SPIRIT_BOW),
							Icons.GRASS.get(),
							new ItemSprite(ItemSpriteSheet.GLOVES),
							new ItemSprite(ItemSpriteSheet.SCROLL_ISAZ)};
					break;
				case DUELIST:
					icons = new Image[]{ new ItemSprite(ItemSpriteSheet.RAPIER),
							new ItemSprite(ItemSpriteSheet.WAR_HAMMER),
							new ItemSprite(ItemSpriteSheet.THROWING_SPIKE),
							new ItemSprite(ItemSpriteSheet.SCROLL_ISAZ)};
					break;
				case CLERIC:
					icons = new Image[]{ new ItemSprite(ItemSpriteSheet.ARTIFACT_TOME),
							Icons.TALENT.get(),
							new ItemSprite(ItemSpriteSheet.CUDGEL),
							new ItemSprite(ItemSpriteSheet.SCROLL_ISAZ)};
					break;
			}
			for (Image im : icons) {
				add(im);
			}

		}

		@Override
		protected void layout() {
			super.layout();

			title.setPos((width-title.width())/2, MARGIN);

			float pos = title.bottom()+4*MARGIN;

			for (int i = 0; i < info.length; i++){
				info[i].maxWidth((int)width - 20);
				info[i].setPos(20, pos);

				icons[i].x = (20-icons[i].width())/2;
				icons[i].y = info[i].top() + (info[i].height() - icons[i].height())/2;
				PixelScene.align(icons[i]);

				pos = info[i].bottom() + 4*MARGIN;
			}

			height = Math.max(height, pos - 4*MARGIN);

		}
	}

	private static class TalentInfoTab extends Component {

		private RenderedTextBlock title;
		private RenderedTextBlock message;
		TalentsPane talentPane;

		public TalentInfoTab( HeroClass cls ){
			super();
			title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(WndHeroInfo.class, "talents")), 8);
			title.hardlight(TITLE_COLOR);
			add(title);

			message = PixelScene.renderTextBlock(Messages.get(WndHeroInfo.class, "talents_msg"), 6);
			add(message);

			ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
			Talent.initClassTalents(cls, talents);
			talents.get(2).clear(); //we show T3 talents with subclasses

			talentPane = new TalentsPane(TalentButton.Mode.INFO, talents);
			add(talentPane);
		}

		@Override
		protected void layout() {
			super.layout();

			title.setPos((width-title.width())/2, MARGIN);
			message.maxWidth((int)width);
			message.setPos(0, title.bottom()+4*MARGIN);

			talentPane.setRect(0, message.bottom() + 3*MARGIN, width, 0);
			talentPane.setSize(width, talentPane.content().height());

			height = Math.max(height, talentPane.bottom());
		}
	}

	private static class SubclassInfoTab extends Component {

		private RenderedTextBlock title;
		private RenderedTextBlock message;
		private RenderedTextBlock[] subClsDescs;
		IconButton[] subClsInfos;

		public SubclassInfoTab( HeroClass cls ){
			super();
			title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(WndHeroInfo.class, "subclasses")), 8);
			title.hardlight(TITLE_COLOR);
			add(title);

			message = PixelScene.renderTextBlock(Messages.get(WndHeroInfo.class, "subclasses_msg"), 6);
			add(message);

			HeroSubClass[] subClasses = cls.subClasses();

			subClsDescs = new RenderedTextBlock[subClasses.length];
			subClsInfos = new IconButton[subClasses.length];

			for (int i = 0; i < subClasses.length; i++){
				subClsDescs[i] = PixelScene.renderTextBlock(subClasses[i].shortDesc(), 6);
				int finalI = i;
				subClsInfos[i] = new IconButton( Icons.get(Icons.INFO) ){
					@Override
					protected void onClick() {
						com.sacredpixel.sacredpixeldungeon.ui.Window childWindow = new WndInfoSubclass(cls, subClasses[finalI]);
						// Find and deactivate parent WndHeroInfo
						final com.sacredpixel.sacredpixeldungeon.ui.Window parentWindow = findParentWindow();
						if (parentWindow != null) {
							final int savedFocus = parentWindow.getFocusIndex();
							parentWindow.clearFocus();
							parentWindow.active = false;
							childWindow.setOnHideCallback(new Runnable() {
								@Override
								public void run() {
									if (parentWindow.parent != null) {
										parentWindow.active = true;
										parentWindow.refreshBlockerPriority();
										parentWindow.restoreFocusToIndex(savedFocus);
									}
								}
							});
						}
						Game.scene().addToFront(childWindow);
					}

					private com.sacredpixel.sacredpixeldungeon.ui.Window findParentWindow() {
						com.watabou.noosa.Group p = parent;
						while (p != null) {
							if (p instanceof com.sacredpixel.sacredpixeldungeon.ui.Window) {
								return (com.sacredpixel.sacredpixeldungeon.ui.Window) p;
							}
							if (p instanceof com.watabou.noosa.Group) {
								p = ((com.watabou.noosa.Group) p).parent;
							} else {
								break;
							}
						}
						return null;
					}
				};
				add(subClsDescs[i]);
				add(subClsInfos[i]);
			}

		}

		@Override
		protected void layout() {
			super.layout();

			title.setPos((width-title.width())/2, MARGIN);
			message.maxWidth((int)width);
			message.setPos(0, title.bottom()+4*MARGIN);

			float pos = message.bottom()+4*MARGIN;

			for (int i = 0; i < subClsDescs.length; i++){
				subClsDescs[i].maxWidth((int)width - 20);
				subClsDescs[i].setPos(0, pos);

				subClsInfos[i].setRect(width-20, subClsDescs[i].top() + (subClsDescs[i].height()-20)/2, 20, 20);

				pos = subClsDescs[i].bottom() + 4*MARGIN;
			}

			height = Math.max(height, pos - 4*MARGIN);

		}
	}

	private static class ArmorAbilityInfoTab extends Component {

		private RenderedTextBlock title;
		private RenderedTextBlock message;
		private RenderedTextBlock[] abilityDescs;
		IconButton[] abilityInfos;

		public ArmorAbilityInfoTab(HeroClass cls){
			super();
			title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(WndHeroInfo.class, "abilities")), 8);
			title.hardlight(TITLE_COLOR);
			add(title);

			message = PixelScene.renderTextBlock(Messages.get(WndHeroInfo.class, "abilities_msg"), 6);
			add(message);

			ArmorAbility[] abilities = cls.armorAbilities();

			abilityDescs = new RenderedTextBlock[abilities.length];
			abilityInfos = new IconButton[abilities.length];

			for (int i = 0; i < abilities.length; i++){
				abilityDescs[i] = PixelScene.renderTextBlock(abilities[i].shortDesc(), 6);
				int finalI = i;
				abilityInfos[i] = new IconButton( Icons.get(Icons.INFO) ){
					@Override
					protected void onClick() {
						com.sacredpixel.sacredpixeldungeon.ui.Window childWindow = new WndInfoArmorAbility(cls, abilities[finalI]);
						// Find and deactivate parent WndHeroInfo
						final com.sacredpixel.sacredpixeldungeon.ui.Window parentWindow = findParentWindow();
						if (parentWindow != null) {
							final int savedFocus = parentWindow.getFocusIndex();
							parentWindow.clearFocus();
							parentWindow.active = false;
							childWindow.setOnHideCallback(new Runnable() {
								@Override
								public void run() {
									if (parentWindow.parent != null) {
										parentWindow.active = true;
										parentWindow.refreshBlockerPriority();
										parentWindow.restoreFocusToIndex(savedFocus);
									}
								}
							});
						}
						Game.scene().addToFront(childWindow);
					}

					private com.sacredpixel.sacredpixeldungeon.ui.Window findParentWindow() {
						com.watabou.noosa.Group p = parent;
						while (p != null) {
							if (p instanceof com.sacredpixel.sacredpixeldungeon.ui.Window) {
								return (com.sacredpixel.sacredpixeldungeon.ui.Window) p;
							}
							if (p instanceof com.watabou.noosa.Group) {
								p = ((com.watabou.noosa.Group) p).parent;
							} else {
								break;
							}
						}
						return null;
					}
				};
				add(abilityDescs[i]);
				add(abilityInfos[i]);
			}

		}

		@Override
		protected void layout() {
			super.layout();

			title.setPos((width-title.width())/2, MARGIN);
			message.maxWidth((int)width);
			message.setPos(0, title.bottom()+4*MARGIN);

			float pos = message.bottom()+4*MARGIN;

			for (int i = 0; i < abilityDescs.length; i++){
				abilityDescs[i].maxWidth((int)width - 20);
				abilityDescs[i].setPos(0, pos);

				abilityInfos[i].setRect(width-20, abilityDescs[i].top() + (abilityDescs[i].height()-20)/2, 20, 20);

				pos = abilityDescs[i].bottom() + 4*MARGIN;
			}

			height = Math.max(height, pos - 4*MARGIN);

		}
	}

}
