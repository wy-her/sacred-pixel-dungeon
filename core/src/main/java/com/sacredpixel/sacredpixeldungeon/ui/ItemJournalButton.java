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

import com.sacredpixel.sacredpixeldungeon.items.EquipableItem;
import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.trinkets.Trinket;
import com.sacredpixel.sacredpixeldungeon.items.wands.Wand;
import com.sacredpixel.sacredpixeldungeon.journal.Notes;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.GameScene;
import com.sacredpixel.sacredpixeldungeon.windows.WndTextInput;
import com.sacredpixel.sacredpixeldungeon.windows.WndTitledMessage;
import com.sacredpixel.sacredpixeldungeon.windows.WndUseItem;

public class ItemJournalButton extends IconButton {

	Item item;
	Window parentWnd;

	public ItemJournalButton(Item item, Window parentWnd){
		super(Icons.JOURNAL.get());
		this.item = item;
		this.parentWnd = parentWnd;
	}

	@Override
	protected void onClick() {

		customNote();

	}

	private void customNote(){
		Notes.CustomRecord note = null;
		if (item instanceof EquipableItem || item instanceof Wand || item instanceof Trinket){
			note = Notes.findCustomRecord(item.customNoteID);
		} else {
			note = Notes.findCustomRecord(item.getClass());
		}
		if (note == null){
			if (Notes.getRecords(Notes.CustomRecord.class).size() >= Notes.customRecordLimit()){
				GameScene.show(new WndTitledMessage(Icons.INFO.get(),
						Messages.get(CustomNoteButton.class, "limit_title"),
						Messages.get(CustomNoteButton.class, "limit_text")));
			} else {

				if (item instanceof EquipableItem || item instanceof Wand || item instanceof Trinket) {
					note = new Notes.CustomRecord(item, "", "");
					note.assignID();
					item.customNoteID = note.ID();
				} else {
					note = new Notes.CustomRecord(item.getClass(), "", "");
					note.assignID();
				}

				addNote(parentWnd, note, Messages.get(CustomNoteButton.class, "new_inv"),
						Messages.get(CustomNoteButton.class, "new_item_title", Messages.titleCase(item.name())));
			}
		} else {
			GameScene.show(new CustomNoteButton.CustomNoteWindow(note, parentWnd));
		}
	}

	private static void addNote(Window parentWindow, Notes.CustomRecord note, String promptTitle, String prompttext){
		GameScene.show(new WndTextInput(promptTitle,
				prompttext,
				"",
				50,
				false,
				Messages.get(CustomNoteButton.CustomNoteWindow.class, "confirm"),
				Messages.get(CustomNoteButton.CustomNoteWindow.class, "cancel")){
			@Override
			public void onSelect(boolean positive, String text) {
				if (positive && !text.isEmpty()){
					Notes.add(note);
					note.editText(text, "");
					if (parentWindow != null) {
						parentWindow.hide();
					}

					hide();
					if (parentWindow instanceof WndUseItem){
						GameScene.show(new WndUseItem(((WndUseItem) parentWindow).owner, ((WndUseItem) parentWindow).item));
					}
				}
			}
		});
	}
}
