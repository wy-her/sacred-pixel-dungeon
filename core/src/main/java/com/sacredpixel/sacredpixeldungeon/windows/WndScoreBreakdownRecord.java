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

import com.sacredpixel.sacredpixeldungeon.Rankings;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.noosa.Group;
import com.watabou.utils.DateCompat;

/**
 * Shows score breakdown for a Rankings.Record (imported or historical).
 * Similar to WndScoreBreakdown but uses record data instead of Statistics.
 */
public class WndScoreBreakdownRecord extends Window {

	private static final int WIDTH = 149;
	private int GAP = 4;

	public WndScoreBreakdownRecord(Rankings.Record record) {

		IconTitle title = new IconTitle(Icons.get(Icons.INFO), Messages.get(WndScoreBreakdown.class, "title"));
		title.setRect(0, 0, WIDTH, 16);
		add(title);

		float pos = title.bottom() + 2;

		// Score breakdown from record
		pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "progress_title"),
				DateCompat.formatNumber(record.progressScore, Messages.locale()), pos, record.progressScore >= 50_000);
		pos = addInfo(this, Messages.get(WndScoreBreakdown.class, "progress_desc"), pos);

		pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "treasure_title"),
				DateCompat.formatNumber(record.treasureScore, Messages.locale()), pos, record.treasureScore >= 20_000);
		pos = addInfo(this, Messages.get(WndScoreBreakdown.class, "treasure_desc"), pos);

		pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "explore_title"),
				DateCompat.formatNumber(record.exploreScore, Messages.locale()), pos, record.exploreScore >= 20_000);
		pos = addInfo(this, Messages.get(WndScoreBreakdown.class, "explore_desc"), pos);

		pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "bosses_title"),
				DateCompat.formatNumber(record.totalBossScore, Messages.locale()), pos, record.totalBossScore >= 15_000);
		pos = addInfo(this, Messages.get(WndScoreBreakdown.class, "bosses_desc"), pos);

		pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "quests_title"),
				DateCompat.formatNumber(record.totalQuestScore, Messages.locale()), pos, record.totalQuestScore >= 10_000);
		pos = addInfo(this, Messages.get(WndScoreBreakdown.class, "quests_desc"), pos);

		if (record.winMultiplier > 1) {
			pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "win_multiplier"),
					Messages.decimalFormat("#.##", record.winMultiplier) + "x", pos, false);
		}
		if (record.chalMultiplier > 1) {
			pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "challenge_multiplier"),
					Messages.decimalFormat("#.##", record.chalMultiplier) + "x", pos, false);
		}
		pos = statSlot(this, Messages.get(WndScoreBreakdown.class, "total"),
				DateCompat.formatNumber(record.score, Messages.locale()), pos, false);

		resize(WIDTH, (int) pos);
	}

	private float statSlot(Group parent, String label, String value, float pos, boolean highlight) {
		// Label takes 67% of width
		RenderedTextBlock txt = PixelScene.renderTextBlock(label, 7);
		txt.maxWidth((int) (WIDTH * 0.67f) - 2);
		if (highlight) txt.hardlight(Window.TITLE_COLOR);
		txt.setPos(0, pos);
		parent.add(txt);

		// Value takes remaining 33%
		RenderedTextBlock valueTxt = PixelScene.renderTextBlock(value, 7);
		valueTxt.maxWidth((int) (WIDTH * 0.33f));
		if (highlight) valueTxt.hardlight(Window.TITLE_COLOR);
		valueTxt.setPos(WIDTH * 0.67f, pos);
		PixelScene.align(valueTxt);
		parent.add(valueTxt);

		float maxHeight = Math.max(txt.height(), valueTxt.height());
		return pos + GAP + maxHeight;
	}

	private float addInfo(Group parent, String info, float pos) {
		RenderedTextBlock txt = PixelScene.renderTextBlock(info, 5);
		txt.maxWidth(WIDTH);
		txt.hardlight(0x999999);
		txt.setPos(0, pos - 2);
		parent.add(txt);

		return pos - 2 + GAP + txt.height();
	}
}
