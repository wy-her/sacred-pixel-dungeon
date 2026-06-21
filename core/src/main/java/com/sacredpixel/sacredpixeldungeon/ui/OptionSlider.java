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

import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

public abstract class OptionSlider extends Component implements Focusable {

	private PointerArea pointerArea;

	private RenderedTextBlock title;
	private RenderedTextBlock minTxt;
	private RenderedTextBlock maxTxt;

	//values are expressed internally as ints, but they can easily be interpreted as something else externally.
	private int minVal;
	private int maxVal;
	private int selectedVal;

	private NinePatch sliderNode;
	private NinePatch BG;
	private ColorBlock sliderBG;
	private ColorBlock[] sliderTicks;
	private float tickDist;

	// Keyboard focus support
	private boolean focused = false;
	private Signal.Listener<KeyEvent> keyListener;


	public OptionSlider(String title, String minTxt, String maxTxt, int minVal, int maxVal){
		super();

		//shouldn't function if this happens.
		if (minVal > maxVal){
			minVal = maxVal;
			active = false;
		}

		this.title.text(title);
		this.minTxt.text(minTxt);
		this.maxTxt.text(maxTxt);

		this.minVal = minVal;
		this.maxVal = maxVal;

		sliderTicks = new ColorBlock[(maxVal - minVal) + 1];
		for (int i = 0; i < sliderTicks.length; i++){
			add(sliderTicks[i] = new ColorBlock(1, 9, 0xFF222222));
		}
		add(sliderNode);
	}

	protected abstract void onChange();

	public int getSelectedValue(){
		return selectedVal;
	}

	public void setSelectedValue(int val) {
		this.selectedVal = val;
		sliderNode.x = (int)(x + tickDist*(selectedVal-minVal)) + 0.5f;
		sliderNode.y = sliderBG.y-4;
		PixelScene.align(sliderNode);
	}

	public void enable( boolean value ) {
		active = value;
		title.alpha( value ? 1.0f : 0.3f );
		minTxt.alpha( value ? 1.0f : 0.3f );
		maxTxt.alpha( value ? 1.0f : 0.3f );
		sliderNode.alpha( value ? 1.0f : 0.3f );
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		add( BG = Chrome.get(Chrome.Type.RED_BUTTON));
		BG.alpha(0.5f);

		add(title = PixelScene.renderTextBlock(6));
		add(this.minTxt = PixelScene.renderTextBlock(6));
		add(this.maxTxt = PixelScene.renderTextBlock(6));

		add(sliderBG = new ColorBlock(1, 1, 0xFF222222));
		sliderNode = Chrome.get(Chrome.Type.RED_BUTTON);
		sliderNode.size(4, 7);

		pointerArea = new PointerArea(0, 0, 0, 0){
			boolean pressed = false;

			@Override
			protected void onPointerDown( PointerEvent event ) {
				pressed = true;
				PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
				sliderNode.x = GameMath.gate(sliderBG.x-2, p.x - sliderNode.width()/2, sliderBG.x+sliderBG.width()-2);
				sliderNode.brightness(1.5f);
			}

			@Override
			protected void onPointerUp( PointerEvent event ) {
				if (pressed) {
					PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
					sliderNode.x = GameMath.gate(sliderBG.x - 2, p.x - sliderNode.width()/2, sliderBG.x + sliderBG.width() - 2);
					sliderNode.resetColor();
					
					//sets the selected value
					selectedVal = minVal + Math.round((sliderNode.x - x) / tickDist);
					sliderNode.x = x + tickDist * (selectedVal - minVal) + 0.5f;
					PixelScene.align(sliderNode);
					onChange();
					pressed = false;
				}
			}

			@Override
			protected void onDrag( PointerEvent event ) {
				if (pressed) {
					PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
					sliderNode.x = GameMath.gate(sliderBG.x - 2, p.x - sliderNode.width()/2, sliderBG.x + sliderBG.width() - 2);
				}
			}
		};
		add(pointerArea);

		// Keyboard listener for arrow key adjustments when focused
		KeyEvent.addKeyListener(keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!focused || !active || !event.pressed) return false;

				GameAction action = KeyBindings.getActionForKey(event);

				// Left arrow: decrease value (W = left in SPD controls)
				if (action == SPDAction.W || action == SPDAction.NW || action == SPDAction.SW) {
					if (selectedVal > minVal) {
						selectedVal--;
						sliderNode.x = x + tickDist * (selectedVal - minVal) + 0.5f;
						PixelScene.align(sliderNode);
						onChange();
					}
					return true;
				}
				// Right arrow: increase value (E = right in SPD controls)
				else if (action == SPDAction.E || action == SPDAction.NE || action == SPDAction.SE) {
					if (selectedVal < maxVal) {
						selectedVal++;
						sliderNode.x = x + tickDist * (selectedVal - minVal) + 0.5f;
						PixelScene.align(sliderNode);
						onChange();
					}
					return true;
				}

				return false;
			}
		});
	}

	private String titleText; //cached for re-creation at smaller sizes
	private int titleSize = 6;

	@Override
	protected void layout() {

		//Title: force single line by shrinking font size if needed.
		//Try size 6, then 5, then 4 until it fits in one line.
		if (titleText == null) titleText = title.text();

		title.maxWidth((int)(width - 4));
		if (title.nLines > 1 && titleSize > 4) {
			//recreate title at smaller size
			remove(title);
			titleSize--;
			title = PixelScene.renderTextBlock(titleText, titleSize);
			title.maxWidth((int)(width - 4));
			add(title);
			//recurse to check if it still overflows
			if (title.nLines > 1 && titleSize > 4) {
				layout();
				return;
			}
		}

		title.setPos(
				x + (width - title.width()) / 2,
				y + 2
		);
		PixelScene.align(title);

		//Min/max labels: placed below title (where 2nd line would be)
		float minMaxY = y + 2 + title.height() + 1;
		minTxt.setPos(x + 1, minMaxY);
		maxTxt.setPos(x + width() - maxTxt.width(), minMaxY);

		//Slider bar and ticks: anchored to bottom of component
		sliderBG.y = y + height() - 7;
		sliderBG.x = x + 2;
		sliderBG.size(width - 5, 1);
		tickDist = sliderBG.width() / (maxVal - minVal);
		for (int i = 0; i < sliderTicks.length; i++) {
			sliderTicks[i].y = sliderBG.y - 4;
			sliderTicks[i].x = x + 2 + (tickDist * i);
			PixelScene.align(sliderTicks[i]);
		}

		sliderNode.x = x + tickDist * (selectedVal - minVal) + 0.5f;
		sliderNode.y = sliderBG.y - 3;
		PixelScene.align(sliderNode);

		pointerArea.x = x;
		pointerArea.y = y;
		pointerArea.width = width();
		pointerArea.height = height();

		BG.size(width(), height());
		BG.x = x;
		BG.y = y;

	}

	@Override
	public void destroy() {
		super.destroy();
		KeyEvent.removeKeyListener(keyListener);
	}

	// Focus support methods for keyboard navigation
	public void setFocused(boolean focused) {
		this.focused = focused;
		if (focused) {
			sliderNode.brightness(1.5f);
			BG.brightness(1.2f);
		} else {
			sliderNode.resetColor();
			BG.resetColor();
			BG.alpha(0.5f);
		}
	}

	public void saveFocusState() {
		// No state to save, we just use default colors
	}

	public void restoreFocusState() {
		setFocused(false);
	}

	public boolean isFocused() {
		return focused;
	}

	// Called when Enter is pressed while focused - can be used for confirmation
	public void click() {
		// Toggle focus off on click/enter, slider values are adjusted via arrow keys
		setFocused(false);
	}

	@Override
	public boolean isActive() {
		// IMPORTANT: Must call super.isActive() for parent chain check
		// This is required for PointerArea click detection to work properly
		return super.isActive();
	}
}
