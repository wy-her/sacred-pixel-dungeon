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

import com.sacredpixel.sacredpixeldungeon.items.Heap;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSprite;
import com.sacredpixel.sacredpixeldungeon.ui.HealthBar;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class IconTitle extends Component {

	private static final float FONT_SIZE = 8;

	private static final float GAP = 2;

	protected Image imIcon;
	protected RenderedTextBlock tfLabel;
	protected HealthBar health;

	private float healthLvl = Float.NaN;

	public IconTitle() {
		super();
	}

	public IconTitle( Item item ) {
		ItemSprite icon = new ItemSprite();
		icon( icon );
		label( Messages.titleCase( item.title() ) );
		icon.view( item );
		layout();
	}
	
	public IconTitle( Heap heap ){
		ItemSprite icon = new ItemSprite();
		icon( icon );
		label( Messages.titleCase( heap.title() ) );
		icon.view( heap );
		layout();
	}

	public IconTitle( Image icon, String label ) {
		icon( icon );
		label( label );
		layout();
	}

	@Override
	protected void createChildren() {
		imIcon = new Image();
		add( imIcon );

		tfLabel = PixelScene.renderTextBlock( (int)FONT_SIZE );
		tfLabel.hardlight( Window.TITLE_COLOR );
		tfLabel.setHightlighting(false);
		add( tfLabel );

		health = new HealthBar();
		add( health );
	}

	@Override
	protected void layout() {

		health.visible = !Float.isNaN( healthLvl );

		// Check if we have a real icon (not empty/default)
		boolean hasIcon = imIcon.width() > 0 && imIcon.height() > 0;

		if (hasIcon) {
			imIcon.x = x + (Math.max(0, 8 - imIcon.width()/2));
			imIcon.y = y + (Math.max(0, 8 - imIcon.height()/2));
			PixelScene.align(imIcon);

			int imWidth = (int)Math.max(imIcon.width(), 16);
			int imHeight = (int)Math.max(imIcon.height(), 16);

			tfLabel.maxWidth((int)(width - (imWidth + GAP)));
			tfLabel.setPos(x + imWidth + GAP,
							imHeight > tfLabel.height() ? y +(imHeight - tfLabel.height()) / 2 : y);
			PixelScene.align(tfLabel);

			if (health.visible) {
				health.setRect( tfLabel.left(), tfLabel.bottom(), tfLabel.maxWidth(), 0 );
				height = Math.max( imHeight, health.bottom() );
			} else {
				height = Math.max( imHeight, tfLabel.height() );
			}
		} else {
			// No icon - position text at left edge without gap
			tfLabel.maxWidth((int)width);
			tfLabel.setPos(x, y);
			PixelScene.align(tfLabel);

			if (health.visible) {
				health.setRect( tfLabel.left(), tfLabel.bottom(), tfLabel.maxWidth(), 0 );
				height = health.bottom();
			} else {
				height = tfLabel.height();
			}
		}
	}

	public float reqWidth(){
		return imIcon.width() + tfLabel.width() + GAP;
	}

	public void icon( Image icon ) {
		if (icon != null) {
			remove(imIcon);
			add(imIcon = icon);
		}
	}

	public void label( String label ) {
		tfLabel.text( label );
	}

	public void label( String label, int color ) {
		tfLabel.text( label );
		tfLabel.hardlight( color );
	}

	public void color( int color ) {
		tfLabel.hardlight( color );
	}

	public float alpha(){
		return imIcon.alpha();
	}

	public void alpha( float value ){
		tfLabel.alpha(value);
		imIcon.alpha(value);
	}

	public void health( float value ) {
		health.level( healthLvl = value );
		layout();
	}
}
