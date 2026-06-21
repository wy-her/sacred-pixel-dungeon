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

import com.sacredpixel.sacredpixeldungeon.messages.Languages;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class RenderedTextBlock extends Component {

	private int maxWidth = Integer.MAX_VALUE;
	public int nLines;

	private static final RenderedText SPACE = new RenderedText();
	private static final RenderedText NEWLINE = new RenderedText();
	private static final RenderedText HIGHLIGHT_BRIDGE = new RenderedText();
	
	protected String text;
	protected String[] tokens = null;
	protected ArrayList<RenderedText> words = new ArrayList<>();
	protected boolean multiline = false;

	private int size;
	private float zoom;
	private int color = -1;
	
	private int hightlightColor = Window.TITLE_COLOR;
	private boolean highlightingEnabled = true;

	public static final int LEFT_ALIGN = 1;
	public static final int CENTER_ALIGN = 2;
	public static final int RIGHT_ALIGN = 3;
	private int alignment = LEFT_ALIGN;
	
	public RenderedTextBlock(int size){
		this.size = size;
	}

	public RenderedTextBlock(String text, int size){
		this.size = size;
		text(text);
	}

	public void text(String text){

		if (text != null && !text.equals("")) {

			//strip trailing newlines to prevent empty trailing lines in layout
			//do this before assigning to this.text so getter and render are consistent
			while (text.endsWith("\n")) {
				text = text.substring(0, text.length() - 1);
			}

			this.text = text;

			//For Asian languages, always split on spaces so that scaledSpaceW
			//controls word spacing even for single-line text (titles, labels, etc.)
			boolean splitSpaces = true;
			tokens = Game.platform.splitforTextBlock(text, splitSpaces);

			build();
		} else {
			this.text = text;
		}
	}

	//for manual text block splitting, a space between each word is assumed
	public void tokens(String... words){
		StringBuilder fullText = new StringBuilder();
		for (String word : words) {
			fullText.append(word);
		}
		text = fullText.toString();

		tokens = words;
		build();
	}

	public void text(String text, int maxWidth){
		this.maxWidth = maxWidth;
		multiline = true;
		text(text);
	}

	public String text(){
		return text;
	}

	public void maxWidth(int maxWidth){
		if (this.maxWidth != maxWidth){
			this.maxWidth = maxWidth;
			multiline = true;
			text(text);
		}
	}

	public int maxWidth(){
		return maxWidth;
	}

	private synchronized void build(){
		if (tokens == null) return;

		clear();
		words = new ArrayList<>();
		boolean highlighting = false;
		boolean justEndedHighlight = false;
		for (String str : tokens){

			//if highlighting is enabled, '_' or '**' is used to toggle highlighting on or off
			// the actual symbols are not rendered
			if ((str.equals("_") || str.equals("**")) && highlightingEnabled){
				boolean wasHighlighting = highlighting;
				highlighting = !highlighting;
				//track when highlighting just ended (transition from highlighted to normal)
				justEndedHighlight = wasHighlighting && !highlighting;
			} else if (str.equals("\n")){
				words.add(NEWLINE);
				justEndedHighlight = false;
			} else if (str.equals(" ")){
				//For CJK languages, skip space after highlight to remove trailing space
				//and insert HIGHLIGHT_BRIDGE for proper line-break handling.
				//For Western languages (non-Asian), always preserve the space.
				//Korean uses spaces like Western languages, so only skip for CJK (Chinese/Japanese)
			boolean skipSpaceForHighlight = justEndedHighlight
				&& (Messages.lang() == Languages.CHI_SMPL
					|| Messages.lang() == Languages.CHI_TRAD
					|| Messages.lang() == Languages.JAPANESE);
				if (!skipSpaceForHighlight) {
					words.add(SPACE);
					justEndedHighlight = false;
				}
				//For CJK: If space was skipped, keep justEndedHighlight = true
				//so next word gets HIGHLIGHT_BRIDGE for line-break opportunity
			} else {
				//Insert bridge marker to close gap after highlight boundary
				if (justEndedHighlight) {
					words.add(HIGHLIGHT_BRIDGE);
				}
				RenderedText word = new RenderedText(str, size);
				
				if (highlighting) word.hardlight(hightlightColor);
				else if (color != -1) word.hardlight(color);
				word.scale.set(zoom);
				
				words.add(word);
				add(word);
				
				if (height < word.height()) height = word.height();
				justEndedHighlight = false;
			}
		}
		layout();
	}

	public synchronized void zoom(float zoom){
		this.zoom = zoom;
		for (RenderedText word : words) {
			if (word != null) word.scale.set(zoom);
		}
		layout();
	}

	//saved per-word color multipliers for focus highlight restore
	private float[][] savedWordColors;

	public synchronized void saveColors(){
		savedWordColors = new float[words.size()][];
		for (int i = 0; i < words.size(); i++){
			RenderedText word = words.get(i);
			if (word != null && word != SPACE && word != NEWLINE) {
				savedWordColors[i] = new float[]{word.rm, word.gm, word.bm};
			}
		}
	}

	public synchronized void restoreSavedColors(){
		if (savedWordColors != null && savedWordColors.length == words.size()){
			for (int i = 0; i < words.size(); i++){
				RenderedText word = words.get(i);
				if (word != null && savedWordColors[i] != null) {
					word.rm = savedWordColors[i][0];
					word.gm = savedWordColors[i][1];
					word.bm = savedWordColors[i][2];
				}
			}
		}
		this.color = -1;
		savedWordColors = null;
	}

	public synchronized void hardlight(int color){
		this.color = color;
		for (RenderedText word : words) {
			if (word != null) word.hardlight( color );
		}
	}

	public synchronized void resetColor(){
		this.color = -1;
		for (RenderedText word : words) {
			if (word != null) word.resetColor();
		}
	}
	
	public synchronized void alpha(float value){
		for (RenderedText word : words) {
			if (word != null) word.alpha( value );
		}
	}
	
	public synchronized void setHightlighting(boolean enabled){
		setHightlighting(enabled, Window.TITLE_COLOR);
	}
	
	public synchronized void setHightlighting(boolean enabled, int color){
		if (enabled != highlightingEnabled || color != hightlightColor) {
			hightlightColor = color;
			highlightingEnabled = enabled;
			build();
		}
	}

	public synchronized void invert(){
		if (words != null) {
			for (RenderedText word : words) {
				if (word != null) {
					word.ra = 0.77f;
					word.ga = 0.73f;
					word.ba = 0.62f;
					word.rm = -0.77f;
					word.gm = -0.73f;
					word.bm = -0.62f;
				}
			}
		}
	}

	public synchronized void align(int align){
		alignment = align;
		layout();
	}

	@Override
	protected synchronized void layout() {
		super.layout();
		float x = this.x;
		float y = this.y;
		float height = 0;
		nLines = 1;

		ArrayList<ArrayList<RenderedText>> lines = new ArrayList<>();
		ArrayList<RenderedText> curLine = new ArrayList<>();
		lines.add(curLine);

		//space width and char gap scale proportionally with logical font size
		//at size 6 (the baseline): spaceW = 1.667, charGap = 0.667
		float effectiveZoom = (zoom != 0 ? zoom : 1);
		float logicalSize = size * effectiveZoom;
		float charGap = logicalSize / 9f;
		boolean asianLang = Messages.lang().isAsian();

		//Each RenderedText word has invisible border padding (from borderWidth = size/10)
		//that inflates word.width() beyond the visual glyphs. The padding on the right side
		//of each word creates extra visual gap between words. We compensate for this.
		//  border excess ≈ (2 * borderWidth + 2) * zoom = logicalSize/5 + 2*zoom
		float borderExcess = logicalSize / 5f + 2f * effectiveZoom;

		//Asian languages use tighter word spacing than Western languages.
		//Visual gap = scaledSpaceW - charGap + borderExcess
		//  Western: keeps original proportional spacing (borderExcess absorbed naturally)
		//  Asian:   compensates borderExcess for tighter spacing
		//All languages use tight spacing (same as Asian/Korean)
		float scaledSpaceW = charGap - borderExcess;

		width = 0;
		for (int i = 0; i < words.size(); i++){
			RenderedText word = words.get(i);
			if (word == HIGHLIGHT_BRIDGE){
				if (Messages.lang() == Languages.JAPANESE || Messages.lang() == Languages.CHI_SMPL
						|| Messages.lang() == Languages.CHI_TRAD) {
					//For CJK: previous char CJK compensation already handles the gap
					//No additional subtraction needed
				} else {
					x -= (2*borderExcess - charGap);
				}
			} else if (word == SPACE){
				if (Messages.lang() == Languages.JAPANESE || Messages.lang() == Languages.CHI_SMPL
						|| Messages.lang() == Languages.CHI_TRAD) {
					//CJK chars already have tight spacing via borderExcess compensation.
					//scaledSpaceW is very negative and would collapse spaces between CJK and Latin.
					//Use a small positive gap instead.
					x += charGap;
				} else {
					x += scaledSpaceW;
				}
			} else if (word == NEWLINE) {
				//newline - system font gets slightly more line spacing for readability
				y += height+2f;
				x = this.x;
				nLines++;
				curLine = new ArrayList<>();
				lines.add(curLine);
			} else {
				if (word.height() > height) height = word.height();

				float fullWidth = word.width();
				int j = i+1;

				//this is so that words split only by highlighting are still grouped in layout
				//Chinese/Japanese must render every character separately for line wrapping
				// (they don't use spaces), but Korean uses spaces like Western languages
				//HIGHLIGHT_BRIDGE is treated as a line-break opportunity since it replaces
				//the skipped space after highlighting ends
				while (Messages.lang() != Languages.CHI_SMPL && Messages.lang() != Languages.CHI_TRAD
						&& Messages.lang() != Languages.JAPANESE
						&& j < words.size() && words.get(j) != SPACE && words.get(j) != NEWLINE
						&& words.get(j) != HIGHLIGHT_BRIDGE){
					fullWidth += words.get(j).width() - charGap;
					j++;
				}

				if ((x - this.x) + fullWidth - 0.001f > maxWidth && !curLine.isEmpty()){
					y += height+2f;
					x = this.x;
					nLines++;
					curLine = new ArrayList<>();
					lines.add(curLine);
				}

				word.x = x;
				word.y = y;
				PixelScene.align(word);
				x += word.width();
				curLine.add(word);

				if ((x - this.x) > width) width = (x - this.x);

				//Reduce gap between words/characters.
				//For single CJK characters (length 1, CJK codepoint), apply extra
				//border padding compensation since each char is its own RenderedText.
				//This is based on the actual character content, not the selected language,
				//so CJK text renders consistently regardless of which language is active.
				String wordText = word.text();
				if (wordText != null && wordText.length() == 1) {
					char ch = wordText.charAt(0);
					//Extra border compensation for CJK characters rendered individually.
					//Hangul excluded: Korean uses word-based splitting (like Western),
					//so single-Hangul words (e.g. "적") should use normal charGap.
					if ((ch >= '\u3040' && ch <= '\u309F')    // Hiragana
						|| (ch >= '\u30A0' && ch <= '\u30FF') // Katakana
						|| (ch >= '\u4E00' && ch <= '\u9FFF') // CJK Unified
						|| (ch >= '\u3000' && ch <= '\u303F')) { // CJK Symbols
						x -= charGap + borderExcess * 0.90f;
					} else {
						x -= charGap;
					}
				} else {
					x -= charGap;
				}

			}
		}
		float bottomPad = nLines > 1 ? Math.round(size * 0.25f) : Math.round(size * 0.15f);
		this.height = (y - this.y) + height + bottomPad;

		if (alignment != LEFT_ALIGN){
			for (ArrayList<RenderedText> line : lines){
				if (line.size() == 0) continue;
				float lineWidth = line.get(line.size()-1).width() + line.get(line.size()-1).x - this.x;
				if (alignment == CENTER_ALIGN){
					for (RenderedText text : line){
						text.x += (width() - lineWidth)/2f;
						PixelScene.align(text);
					}
				} else if (alignment == RIGHT_ALIGN) {
					for (RenderedText text : line){
						text.x += width() - lineWidth;
						PixelScene.align(text);
					}
				}
			}
		}
	}
}
