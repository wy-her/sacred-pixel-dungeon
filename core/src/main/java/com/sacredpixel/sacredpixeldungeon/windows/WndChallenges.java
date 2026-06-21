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

import com.sacredpixel.sacredpixeldungeon.Challenges;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.ui.CheckBox;
import com.sacredpixel.sacredpixeldungeon.ui.IconButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndChallenges extends Window {

	private static final int WIDTH		= 149;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;

	private boolean editable;
	private ArrayList<CheckBox> boxes;
	private ArrayList<IconButton> infoButtons;

	private Window descriptionWindow = null;
	private int savedFocusIndex = -1;

	public WndChallenges( int checked, boolean editable ) {

		super();

		this.editable = editable;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 8 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				2
		);
		PixelScene.align(title);
		add( title );

		boxes = new ArrayList<>();
		infoButtons = new ArrayList<>();

		float pos = title.bottom() + 4;
		for (int i=0; i < Challenges.NAME_IDS.length; i++) {

			final String challenge = Challenges.NAME_IDS[i];

			CheckBox cb = new CheckBox( Messages.titleCase(Messages.get(Challenges.class, challenge)) );
			cb.checked( (checked & Challenges.MASKS[i]) != 0 );
			cb.active = editable;

			if (i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

			add( cb );
			boxes.add( cb );

			IconButton info = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					descriptionWindow = new WndMessage(Messages.get(Challenges.class, challenge+"_desc"));
					SacredPixelDungeon.scene().add(descriptionWindow);
					// Save focus position and clear keyboard focus while description is shown
					// Only save if keyboard focus exists (don't save for mouse clicks)
					if (focusIndex >= 0) {
						savedFocusIndex = focusIndex;
					}
					clearFocus();
				}
			};
			info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
			add(info);
			infoButtons.add(info);

			pos = cb.bottom();
		}

		resize( WIDTH, (int)pos );

		// Register focusable buttons for keyboard navigation
		// Add checkboxes and info buttons in pairs (checkbox, info, checkbox, info, ...)
		for (int i = 0; i < boxes.size(); i++) {
			addFocusableButton(boxes.get(i));
			addFocusableButton(infoButtons.get(i));
		}
	}

	@Override
	public void update() {
		super.update();
		// Restore focus when description window is closed (check parent == null instead of !alive)
		if (descriptionWindow != null && descriptionWindow.parent == null) {
			descriptionWindow = null;
			// Restore saved focus position
			if (savedFocusIndex >= 0 && savedFocusIndex < focusableButtons.size()) {
				focusIndex = savedFocusIndex;
				focusableButtons.get(focusIndex).saveFocusState();
				focusableButtons.get(focusIndex).setFocused(true);
			}
			savedFocusIndex = -1;
		}
	}

	@Override
	public void onBackPressed() {

		if (editable) {
			int value = 0;
			for (int i=0; i < boxes.size(); i++) {
				if (boxes.get( i ).checked()) {
					value |= Challenges.MASKS[i];
				}
			}
			SPDSettings.challenges( value );
		}

		super.onBackPressed();
	}
}