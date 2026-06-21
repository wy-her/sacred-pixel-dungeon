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

import com.sacredpixel.sacredpixeldungeon.Dungeon;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.Statistics;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.noosa.Group;

import com.watabou.utils.DateCompat;

public class WndScoreBreakdown extends Window {

	private static final int WIDTH			= 149;

	private int GAP	= 4;

	public WndScoreBreakdown(){

		IconTitle title = new IconTitle( Icons.get(Icons.INFO), Messages.get(this, "title"));
		title.setRect(0, 0, WIDTH, 16);
		add(title);

		float pos = title.bottom()+2;

		//using a lambda-like helper to format numbers via DateCompat
		if (Dungeon.initialVersion > SacredPixelDungeon.v1_2_3) {
			pos = statSlot(this, Messages.get(this, "progress_title"),
					DateCompat.formatNumber(Statistics.progressScore, Messages.locale()), pos, Statistics.progressScore >= 50_000);
			pos = addInfo(this, Messages.get(this, "progress_desc"), pos);
			pos = statSlot(this, Messages.get(this, "treasure_title"),
					DateCompat.formatNumber(Statistics.treasureScore, Messages.locale()), pos, Statistics.treasureScore >= 20_000);
			pos = addInfo(this, Messages.get(this, "treasure_desc"), pos);
			pos = statSlot(this, Messages.get(this, "explore_title"),
					DateCompat.formatNumber(Statistics.exploreScore, Messages.locale()), pos, Statistics.exploreScore >= 20_000);
			pos = addInfo(this, Messages.get(this, "explore_desc"), pos);
			pos = statSlot(this, Messages.get(this, "bosses_title"),
					DateCompat.formatNumber(Statistics.totalBossScore, Messages.locale()), pos, Statistics.totalBossScore >= 15_000);
			pos = addInfo(this, Messages.get(this, "bosses_desc"), pos);
			pos = statSlot(this, Messages.get(this, "quests_title"),
					DateCompat.formatNumber(Statistics.totalQuestScore, Messages.locale()), pos, Statistics.totalQuestScore >= 10_000);
			pos = addInfo(this, Messages.get(this, "quests_desc"), pos);
		} else {
			pos = statSlot(this, Messages.get(this, "progress_title"),
					DateCompat.formatNumber(Statistics.progressScore, Messages.locale()), pos, Statistics.progressScore >= 78_000);
			pos = addInfo(this, Messages.get(this, "progress_desc"), pos);
			pos = statSlot(this, Messages.get(this, "treasure_title"),
					DateCompat.formatNumber(Statistics.treasureScore, Messages.locale()), pos, Statistics.treasureScore >= 30_000);
			pos = addInfo(this, Messages.get(this, "treasure_desc_old"), pos);
		}

		if (Statistics.winMultiplier > 1) {
			pos = statSlot(this, Messages.get(this, "win_multiplier"), Messages.decimalFormat("#.##", Statistics.winMultiplier) + "x", pos, false);
		}
		if (Statistics.chalMultiplier > 1) {
			pos = statSlot(this, Messages.get(this, "challenge_multiplier"), Messages.decimalFormat("#.##", Statistics.chalMultiplier) + "x", pos, false);
		}
		pos = statSlot(this, Messages.get(this, "total"), DateCompat.formatNumber(Statistics.totalScore, Messages.locale()), pos, false);

		if (Dungeon.initialVersion <= SacredPixelDungeon.v1_2_3){
			pos = addInfo(this, Messages.get(this, "old_score_desc"), pos);
		}

		resize(WIDTH, (int)pos);

	}

	private float statSlot(Group parent, String label, String value, float pos, boolean highlight ) {

		// Label takes 67% of width
		RenderedTextBlock txt = PixelScene.renderTextBlock( label, 7 );
		txt.maxWidth((int)(WIDTH * 0.67f) - 2);
		if (highlight) txt.hardlight(Window.TITLE_COLOR);
		txt.setPos(0, pos);
		parent.add( txt );

		// Value takes remaining 33%
		RenderedTextBlock valueTxt = PixelScene.renderTextBlock( value, 7 );
		valueTxt.maxWidth((int)(WIDTH * 0.33f));
		if (highlight) valueTxt.hardlight(Window.TITLE_COLOR);
		valueTxt.setPos(WIDTH * 0.67f, pos);
		PixelScene.align(valueTxt);
		parent.add( valueTxt );

		float maxHeight = Math.max(txt.height(), valueTxt.height());
		return pos + GAP + maxHeight;
	}

	private float addInfo(Group parent, String info, float pos){

		RenderedTextBlock txt = PixelScene.renderTextBlock( info, 5 );
		txt.maxWidth(WIDTH);
		txt.hardlight(0x999999);
		txt.setPos(0, pos-2);
		parent.add( txt );

		return pos - 2 + GAP + txt.height();

	}


}
