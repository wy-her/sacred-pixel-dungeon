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

import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Hero;
import com.sacredpixel.sacredpixeldungeon.actors.hero.Talent;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.ClericSpell;
import com.sacredpixel.sacredpixeldungeon.actors.hero.spells.GuidingLight;
import com.sacredpixel.sacredpixeldungeon.items.artifacts.HolyTome;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.ui.HeroIcon;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.QuickSlotButton;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.RightClickMenu;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.sacredpixel.sacredpixeldungeon.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PointF;

import java.util.ArrayList;

public class WndClericSpells extends Window {

	protected static final int WIDTH    = 149;
	private static final int MARGIN     = 2;

	public static int BTN_SIZE = 20;

	public WndClericSpells(HolyTome tome, Hero cleric, boolean info){

		IconTitle title;
		if (!info){
			title = new IconTitle(new ItemSprite(tome), Messages.titleCase(Messages.get(this, "cast_title")));
		} else {
			title = new IconTitle(Icons.INFO.get(), Messages.titleCase(Messages.get(this, "info_title")));
		}

		title.setRect(0, 0, WIDTH, 0);
		add(title);

		IconButton btnInfo = new IconButton(info ? new ItemSprite(tome) : Icons.INFO.get()){
			@Override
			protected void onClick() {
				GameScene.show(new WndClericSpells(tome, cleric, !info));
				hide();
			}
		};
		btnInfo.setRect(WIDTH-16, 0, 16, 16);
		add(btnInfo);
		addFocusableButton(btnInfo);

		RenderedTextBlock msg;
		if (info){
			msg = PixelScene.renderTextBlock( Messages.get( this, "info_desc"), 6);
		} else if (DeviceCompat.isDesktop()){
			msg = PixelScene.renderTextBlock( Messages.get( this, "cast_desc_desktop"), 6);
		} else {
			msg = PixelScene.renderTextBlock( Messages.get( this, "cast_desc_mobile"), 6);
		}
		msg.maxWidth(WIDTH);
		msg.setPos(0, title.bottom() + 2*MARGIN);
		add(msg);

		int top = (int)msg.bottom() + 2*MARGIN;

		for (int i = 1; i <= Talent.MAX_TALENT_TIERS; i++) {

			ArrayList<ClericSpell> spells = ClericSpell.getSpellList(cleric, i);

			if (!spells.isEmpty() && i != 1){
				top += BTN_SIZE + 2;
				ColorBlock sep = new ColorBlock(WIDTH, 1, 0xFF000000);
				sep.y = top;
				add(sep);
				top += 3;
			}

			ArrayList<IconButton> spellBtns = new ArrayList<>();

			for (ClericSpell spell : spells) {
				IconButton spellBtn = new SpellButton(spell, tome, info);
				add(spellBtn);
				spellBtns.add(spellBtn);
				addFocusableButton(spellBtn);
			}

			int left = 2 + (WIDTH - spellBtns.size() * (BTN_SIZE + 4)) / 2;
			for (IconButton btn : spellBtns) {
				btn.setRect(left, top, BTN_SIZE, BTN_SIZE);
				left += btn.width() + 4;
			}

		}

		resize(WIDTH, top + BTN_SIZE);

		//if we are on mobile, offset the window down to just above the toolbar
		if (SPDSettings.interfaceSize() != 2){
			offset(0, (int) (GameScene.uiCamera.height/2 - 30 - height/2));
		}

	}

	public class SpellButton extends IconButton {

		ClericSpell spell;
		HolyTome tome;
		boolean info;

		NinePatch bg;

		public SpellButton(ClericSpell spell, HolyTome tome, boolean info){
			super(new HeroIcon(spell));

			this.spell = spell;
			this.tome = tome;
			this.info = info;

			if (!tome.canCast(Dungeon.hero, spell)){
				icon.alpha( 0.3f );
			} else if (spell == GuidingLight.INSTANCE && spell.chargeUse(Dungeon.hero) == 0){
				icon.brightness(3);
			}

			bg = Chrome.get(Chrome.Type.TOAST);
			addToBack(bg);
		}

		@Override
		protected void onPointerDown() {
			super.onPointerDown();
			if (spell == GuidingLight.INSTANCE && spell.chargeUse(Dungeon.hero) == 0){
				icon.brightness(4);
			}
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			if (!tome.canCast(Dungeon.hero, spell)){
				icon.alpha( 0.3f );
			} else if (spell == GuidingLight.INSTANCE && spell.chargeUse(Dungeon.hero) == 0){
				icon.brightness(3);
			}
		}

		@Override
		protected void layout() {
			super.layout();

			if (bg != null) {
				bg.size(width, height);
				bg.x = x;
				bg.y = y;
			}
		}

		@Override
		protected void onClick() {
			if (info){
				GameScene.show(new WndTitledMessage(new HeroIcon(spell), Messages.titleCase(spell.name()), spell.desc()));
			} else {
				hide();


				if(!tome.canCast(Dungeon.hero, spell)){
					GLog.w(Messages.get(HolyTome.class, "no_spell"));
				} else {
					spell.onCast(tome, Dungeon.hero);

					if (spell.targetingFlags() != -1 && Dungeon.quickslot.contains(tome)){
						tome.targetingSpell = spell;
						QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(tome));
					}
				}

			}
		}

		@Override
		protected boolean onLongClick() {
			hide();
			tome.setQuickSpell(spell);
			return true;
		}

		@Override
		protected void onRightClick() {
			super.onRightClick();
			RightClickMenu r = new RightClickMenu(new Image(icon),
					Messages.titleCase(spell.name()),
					Messages.get(WndClericSpells.class, "cast"),
					Messages.get(WndClericSpells.class, "info"),
					Messages.get(WndClericSpells.class, "quick_cast")){
				@Override
				public void onSelect(int index) {
					switch (index){
						default:
							//do nothing
							break;
						case 0:
							hide();
							if(!tome.canCast(Dungeon.hero, spell)){
								GLog.w(Messages.get(HolyTome.class, "no_spell"));
							} else {
								spell.onCast(tome, Dungeon.hero);

								if (spell.targetingFlags() != -1 && Dungeon.quickslot.contains(tome)){
									tome.targetingSpell = spell;
									QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(tome));
								}
							}
							break;
						case 1:
							GameScene.show(new WndTitledMessage(new HeroIcon(spell), Messages.titleCase(spell.name()), spell.desc()));
							break;
						case 2:
							hide();
							tome.setQuickSpell(spell);
							break;
					}
				}
			};
			parent.addToFront(r);
			r.camera = camera();
			PointF mousePos = PointerEvent.currentHoverPos();
			mousePos = camera.screenToCamera((int)mousePos.x, (int)mousePos.y);
			r.setPos(mousePos.x-3, mousePos.y-3);
		}

		@Override
		protected String hoverText() {
			return "_" + Messages.titleCase(spell.name()) + "_\n" + spell.shortDesc();
		}

		@Override
		public void setFocused(boolean focused) {
			if (icon != null) {
				if (focused) {
					// Special case for free GuidingLight - use brighter highlight
					if (spell == GuidingLight.INSTANCE && spell.chargeUse(Dungeon.hero) == 0) {
						icon.brightness(4);
					} else {
						icon.brightness(1.5f);
					}
				} else {
					restoreFocusState();
				}
			}
		}

		@Override
		public void restoreFocusState() {
			if (icon != null) {
				if (!tome.canCast(Dungeon.hero, spell)) {
					icon.resetColor();
					icon.alpha(0.3f);
				} else if (spell == GuidingLight.INSTANCE && spell.chargeUse(Dungeon.hero) == 0) {
					icon.brightness(3);
				} else {
					icon.resetColor();
				}
			}
		}
	}

}
