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
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

public class IconButton extends Button {

	protected Image icon;

	// For preserving icon color during focus
	private float savedRm = 1f, savedGm = 1f, savedBm = 1f;
	private boolean colorSaved = false;
	
	public IconButton(){
		super();
	}
	
	public IconButton( Image icon ){
		super();
		icon( icon );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		if (icon != null) {
			icon.x = x + (width - icon.width()) / 2f;
			icon.y = y + (height - icon.height()) / 2f;
			PixelScene.align(icon);
		}
	}
	
	@Override
	protected void onPointerDown() {
		if (icon != null) icon.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
	}
	
	@Override
	protected void onPointerUp() {
		if (icon != null) icon.resetColor();
	}
	
	public void enable( boolean value ) {
		active = value;
		if (icon != null) icon.alpha( value ? 1.0f : 0.3f );
	}
	
	public void icon( Image icon ) {
		if (this.icon != null) {
			remove( this.icon );
		}
		this.icon = icon;
		if (this.icon != null) {
			add( this.icon );
			layout();
		}
	}
	
	public Image icon(){
		return icon;
	}

	@Override
	public void setFocused(boolean focused) {
		if (icon != null) {
			if (focused) {
				// Save current color if not already saved
				if (!colorSaved) {
					savedRm = icon.rm;
					savedGm = icon.gm;
					savedBm = icon.bm;
					colorSaved = true;
				}
				// Apply brightness multiplier to current color (preserves gold/custom colors)
				icon.rm = savedRm * 1.5f;
				icon.gm = savedGm * 1.5f;
				icon.bm = savedBm * 1.5f;
			} else {
				// Restore saved color
				if (colorSaved) {
					icon.rm = savedRm;
					icon.gm = savedGm;
					icon.bm = savedBm;
					colorSaved = false;
				}
			}
		}
	}

	@Override
	public void saveFocusState() {
		// Save current color state
		if (icon != null && !colorSaved) {
			savedRm = icon.rm;
			savedGm = icon.gm;
			savedBm = icon.bm;
			colorSaved = true;
		}
	}

	@Override
	public void restoreFocusState() {
		if (icon != null && colorSaved) {
			icon.rm = savedRm;
			icon.gm = savedGm;
			icon.bm = savedBm;
			colorSaved = false;
		}
	}
}
