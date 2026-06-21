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

import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.watabou.input.ControllerHandler;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Signal;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;

public class Button extends Component implements Focusable {

	// DEBUG flag - set to true to enable hotArea debugging
	public static boolean DEBUG_HOTAREA = false;

	public static float longClick = 0.5f;

	protected PointerArea hotArea;
	protected Tooltip hoverTip;

	//only one button should be pressed at a time
	protected static Button pressedButton;
	protected float pressTime;
	protected boolean clickReady;

	@Override
	protected void createChildren() {
		hotArea = new PointerArea( 0, 0, 0, 0 ) {
			@Override
			protected void onPointerDown( PointerEvent event ) {
				if (DEBUG_HOTAREA) {
					debugPrintHotArea("onPointerDown");
				}
				// If this button is NOT inside a window, and a window is open,
				// ignore pointer events (let the window handle input)
				if (!isInsideWindow() && hasOpenWindowInScene()) {
					return;
				}
				pressedButton = Button.this;
				pressTime = 0;
				clickReady = true;
				Button.this.onPointerDown();
			}
			@Override
			protected void onPointerUp( PointerEvent event ) {
				if (pressedButton == Button.this){
					pressedButton = null;
				} else {
					//cancel any potential click, only one button can be activated at a time
					clickReady = false;
				}
				Button.this.onPointerUp();
			}
			@Override
			protected void onClick( PointerEvent event ) {
				if (DEBUG_HOTAREA) {
					debugPrintHotArea("onClick");
				}
				// If this button is NOT inside a window, and a window is open,
				// ignore clicks (let the window handle input)
				if (!isInsideWindow() && hasOpenWindowInScene()) {
					return;
				}
				if (clickReady) {
					killTooltip();
					switch (event.button){
						case PointerEvent.LEFT: default:
							Button.this.onClick();
							break;
						case PointerEvent.RIGHT:
							Button.this.onRightClick();
							break;
						case PointerEvent.MIDDLE:
							Button.this.onMiddleClick();
							break;
					}

				}
			}

			@Override
			protected void onHoverStart(PointerEvent event) {
				String text = hoverText();
				if (text != null){
					int key = 0;
					if (keyAction() != null){
						key = KeyBindings.getFirstKeyForAction(keyAction(), ControllerHandler.controllerActive);
					}

					if (key == 0 && secondaryTooltipAction() != null){
						key = KeyBindings.getFirstKeyForAction(secondaryTooltipAction(), ControllerHandler.controllerActive);
					}

					if (key != 0){
						text += " _(" + KeyBindings.getKeyName(key) + ")_";
					}
					hoverTip = new Tooltip(Button.this, text, 80);
					Button.this.parent.addToFront(hoverTip);
					hoverTip.camera = camera();
					alignTooltip(hoverTip);
				}
			}

			@Override
			protected void onHoverEnd(PointerEvent event) {
				killTooltip();
			}
		};
		add( hotArea );
		
		KeyEvent.addKeyListener( keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal ( KeyEvent event ) {
				if ( active && KeyBindings.getActionForKey( event ) == keyAction()){
					// If this button is NOT inside a window, and a window is open,
					// don't process keyboard shortcuts (let the window handle input)
					if (!isInsideWindow() && hasOpenWindowInScene()) {
						return false;
					}
					if (event.pressed){
						pressedButton = Button.this;
						pressTime = 0;
						clickReady = true;
						Button.this.onPointerDown();
					} else {
						Button.this.onPointerUp();
						if (pressedButton == Button.this) {
							pressedButton = null;
							if (clickReady) onClick();
						}
					}
					return true;
				}
				return false;
			}
		});
	}
	
	private Signal.Listener<KeyEvent> keyListener;
	
	public GameAction keyAction(){
		return null;
	}

	// Check if this button is a child of a Window
	protected boolean isInsideWindow() {
		Gizmo g = parent;
		while (g != null) {
			if (g instanceof Window) {
				return true;
			}
			g = g.parent;
		}
		return false;
	}

	// Check if the current scene has any open windows
	protected boolean hasOpenWindowInScene() {
		if (Game.scene() instanceof PixelScene) {
			return ((PixelScene) Game.scene()).hasOpenWindows();
		}
		return false;
	}

	//used in cases where the main key action isn't bound, but a secondary action can be used for the tooltip
	public GameAction secondaryTooltipAction(){
		return null;
	}

	@Override
	public void update() {
		super.update();

		hotArea.active = visible && active;
		
		if (pressedButton == this && (pressTime += Game.elapsed) >= longClick) {
			pressedButton = null;
			if (onLongClick()) {

				hotArea.reset();
				clickReady = false; //did a long click, can't do a regular one
				onPointerUp();

				if (SPDSettings.vibration()) {
					Game.vibrate(50);
				}
			}
		}
	}
	
	protected void onPointerDown() {}
	protected void onPointerUp() {}
	protected void onClick() {} //left click, default key type

	// Public method to programmatically trigger a click (for keyboard navigation)
	public void click() {
		onClick();
	}
	protected void onRightClick() {}
	protected void onMiddleClick() {}
	protected boolean onLongClick() {
		return false;
	}

	protected String hoverText() {
		return null;
	}

	// Focus indication methods for keyboard navigation
	// Subclasses should override these for visual focus feedback
	public void setFocused(boolean focused) {
		// Default implementation does nothing
	}

	public void saveFocusState() {
		// Default implementation does nothing
	}

	public void restoreFocusState() {
		// Default implementation does nothing
	}

	@Override
	public boolean isActive() {
		// Must call super to preserve the parent chain check from Gizmo.isActive()
		// This is required for PointerArea click detection to work properly
		return super.isActive();
	}

	//TODO might be nice for more flexibility here
	private void alignTooltip( Tooltip tip ){
		tip.setPos(x, y-tip.height()-1);
		Camera cam = camera();
		//shift left if there's no room on the right
		if (tip.right() > (cam.width+cam.scroll.x)){
			tip.setPos(tip.left() - (tip.right() - (cam.width+cam.scroll.x)), tip.top());
		}
		//move to the bottom if there's no room on top
		if (tip.top() < 0){
			tip.setPos(tip.left(), bottom()+1);
		}
	}

	public void killTooltip(){
		if (hoverTip != null){
			hoverTip.killAndErase();
			hoverTip = null;
		}
	}
	
	@Override
	protected void layout() {
		hotArea.x = x;
		hotArea.y = y;
		hotArea.width = width;
		hotArea.height = height;
	}
	
	@Override
	public synchronized void destroy () {
		super.destroy();
		KeyEvent.removeKeyListener( keyListener );
		killTooltip();
	}

	public void givePointerPriority(){
		hotArea.givePointerPriority();
	}

	// DEBUG: Print hotArea information
	protected void debugPrintHotArea(String trigger) {
		if (!DEBUG_HOTAREA) return;

		StringBuilder sb = new StringBuilder();
		sb.append("\n=== Button DEBUG (").append(trigger).append(") ===\n");
		sb.append("Button class: ").append(this.getClass().getSimpleName()).append("\n");
		sb.append("Button pos: (").append(x).append(", ").append(y).append(")\n");
		sb.append("Button size: ").append(width).append(" x ").append(height).append("\n");

		sb.append("\n-- hotArea --\n");
		sb.append("hotArea pos: (").append(hotArea.x).append(", ").append(hotArea.y).append(")\n");
		sb.append("hotArea size: ").append(hotArea.width).append(" x ").append(hotArea.height).append("\n");
		sb.append("hotArea.camera (direct): ").append(hotArea.camera).append("\n");
		sb.append("hotArea.camera() (resolved): ").append(hotArea.camera()).append("\n");

		Camera cam = hotArea.camera();
		if (cam != null) {
			sb.append("\n-- hotArea's resolved Camera --\n");
			sb.append("Camera screen pos: (").append(cam.x).append(", ").append(cam.y).append(")\n");
			sb.append("Camera size: ").append(cam.width).append(" x ").append(cam.height).append("\n");
			sb.append("Camera zoom: ").append(cam.zoom).append("\n");
			sb.append("Camera scroll: (").append(cam.scroll.x).append(", ").append(cam.scroll.y).append(")\n");
			sb.append("Camera isMain: ").append(cam == Camera.main).append("\n");
		}

		sb.append("\n-- Parent chain --\n");
		Gizmo current = hotArea;
		int depth = 0;
		while (current != null && depth < 10) {
			String name = current.getClass().getSimpleName();
			Camera c = current.camera;
			String camInfo = (c == null) ? "null" : "Camera@" + Integer.toHexString(c.hashCode());
			sb.append("  ").append(depth).append(": ").append(name);
			sb.append(" [camera=").append(camInfo).append("]");
			if (current instanceof Group) {
				sb.append(" (Group)");
			}
			sb.append("\n");

			if (current.parent != null) {
				current = current.parent;
			} else {
				break;
			}
			depth++;
		}

		sb.append("=== END DEBUG ===\n");
		Game.runOnRenderThread(() -> {
			System.out.println(sb.toString());
		});
	}

}
