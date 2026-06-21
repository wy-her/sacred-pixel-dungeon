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

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.actors.Char;
import com.sacredpixel.sacredpixeldungeon.actors.mobs.Mob;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.CharSprite;
import com.sacredpixel.sacredpixeldungeon.windows.WndKeyBindings;
import com.watabou.input.GameAction;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

//FIXME needs a refactor, lots of weird thread interaction here.
public class AttackIndicator extends Tag {
	
	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;

	private static float delay;

	private static AttackIndicator instance;

	// Tutorial flashing
	public static boolean tutorialFlashing = false;
	private static float tutorialFlashTime = 0;
	
	private CharSprite sprite = null;
	
	private Mob lastTarget;
	private ArrayList<Mob> candidates = new ArrayList<>();
	
	public AttackIndicator() {
		super( DangerIndicator.COLOR );

		synchronized (this) {
			instance = this;
			lastTarget = null;

			setSize(SIZE, SIZE);
			visible(false);
			enable(false);
		}
	}
	
	@Override
	public GameAction keyAction() {
		return SPDAction.TAG_ATTACK;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
	}
	
	@Override
	protected synchronized void layout() {
		super.layout();

		if (sprite != null) {
			if (!flipped)   sprite.x = x + (SIZE - sprite.width()) / 2f + 1;
			else            sprite.x = x + width - (SIZE + sprite.width()) / 2f - 1;
			sprite.y = y + (height - sprite.height()) / 2f;
			PixelScene.align(sprite);
		}
	}
	
	@Override
	public synchronized void update() {
		super.update();

		if (!bg.visible){
			if (sprite != null) sprite.visible = false;
			enable(false);
			if (delay > 0f) delay -= Game.elapsed;
			if (delay <= 0f) active = false;
		} else {
			delay = 0.75f;
			active = true;
			if (bg.width > 0 && sprite != null)sprite.visible = true;

			if (Dungeon.hero.isAlive()) {

				//re-check if the current target is still within attack range
				//this handles enemies that move out of range between updateState() calls
				if (lastTarget != null && !Dungeon.hero.canAttack(lastTarget)) {
					checkEnemies();
				}

				enable(Dungeon.hero.ready);

			} else {
				visible( false );
				enable( false );
			}
		}

		// Tutorial flashing
		if (tutorialFlashing && bg.visible) {
			tutorialFlashTime += Game.elapsed;
			float alpha = (float) Math.abs(Math.cos(tutorialFlashTime * StatusPane.FLASH_RATE));
			bg.brightness(1f + alpha * 0.5f);
		} else {
			tutorialFlashTime = 0;
		}
	}
	
	private synchronized void checkEnemies() {

		candidates.clear();
		int v = Dungeon.hero.visibleEnemies();
		for (int i=0; i < v; i++) {
			Mob mob = Dungeon.hero.visibleEnemy( i );
			if ( Dungeon.hero.canAttack( mob) ) {
				candidates.add( mob );
			}
		}
		
		if (lastTarget == null || !candidates.contains( lastTarget )) {
			if (candidates.isEmpty()) {
				lastTarget = null;
			} else {
				active = true;
				lastTarget = Random.element( candidates );
				updateImage();
				flash();
			}
		} else {
			active = true;
			if (!bg.visible) {
				flash();
			}
		}
		
		visible( lastTarget != null );
		enable( bg.visible );
	}
	
	private synchronized void updateImage() {
		
		if (sprite != null) {
			sprite.killAndErase();
			sprite = null;
		}
		
		sprite = Reflection.newInstance(lastTarget.spriteClass);
		active = true;
		sprite.linkVisuals(lastTarget);
		sprite.idle();
		sprite.paused = true;
		sprite.visible = bg.visible;

		if (sprite.width() > 20 || sprite.height() > 20){
			sprite.scale.set(PixelScene.align(20f/Math.max(sprite.width(), sprite.height())));
		}

		add( sprite );

		layout();
	}
	
	private boolean enabled = true;
	private synchronized void enable( boolean value ) {
		enabled = value;
		if (sprite != null) {
			sprite.alpha( value ? ENABLED : DISABLED );
		}
	}
	
	private synchronized void visible( boolean value ) {
		bg.visible = value;
	}
	
	@Override
	protected synchronized void onClick() {
		super.onClick();
		// Only check hero.ready, not enabled, to avoid race condition where
		// ready=true but enabled hasn't been updated yet by updateState()
		// Also validate lastTarget is still a valid attack target (not dead/out of range)
		if (Dungeon.hero.ready && lastTarget != null && Dungeon.hero.canAttack(lastTarget)) {
			// Cancel cell selector BEFORE attack to prevent Spirit Bow re-triggering
			GameScene.cancel();
			if (Dungeon.hero.handle( lastTarget.pos )) {
				Dungeon.hero.next();
				enable(false); //prevent rapid re-clicks until hero is ready again
				// Cancel any active targeting mode when attacking via this button
				QuickSlotButton.cancel();
				InventoryPane.cancelTargeting();
			}
		}
	}

	@Override
	protected String hoverText() {
		return Messages.titleCase(Messages.get(WndKeyBindings.class, "tag_attack"));
	}

	public static void target(Char target ) {
		if (target == null) return;
		synchronized (instance) {
			instance.lastTarget = (Mob) target;
			instance.updateImage();

			QuickSlotButton.target(target);
		}
	}
	
	public static void updateState() {
		instance.checkEnemies();
	}
}
