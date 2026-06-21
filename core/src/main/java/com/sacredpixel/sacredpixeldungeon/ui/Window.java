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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.Chrome;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.effects.ShadowBox;
import com.sacredpixel.sacredpixeldungeon.scenes.PixelScene;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Point;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class Window extends Group implements Signal.Listener<KeyEvent> {

	protected int width;
	protected int height;

	protected int xOffset;
	protected int yOffset;

	protected ArrayList<Focusable> focusableButtons = new ArrayList<>();
	protected int focusIndex = -1;
	
	protected PointerArea blocker;
	protected ShadowBox shadow;
	protected NinePatch chrome;

	// Callback to run when window is hidden/destroyed
	protected Runnable onHideCallback;

	// Skip next click to prevent cascading window closures
	private boolean skipNextClick = false;

	// Track if blocker handled a closing click (to stop event propagation)
	private boolean blockerHandledClose = false;

	// Track if this window has been initialized in the scene (for window activation stack)
	private boolean sceneInitialized = false;

	public static final int WHITE = 0xFFFFFF;
	public static final int TITLE_COLOR = 0xFFFF44;
	public static final int SHPX_COLOR = 0x33BB33;
	
	public Window() {
		this( 0, 0, Chrome.get( Chrome.Type.WINDOW ) );
	}
	
	public Window( int width, int height ) {
		this( width, height, Chrome.get( Chrome.Type.WINDOW ) );
	}

	public Window( int width, int height, NinePatch chrome ) {
		super();
		
		blocker = new PointerArea( 0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height ) {
			@Override
			public boolean onSignal( PointerEvent event ) {
				// Reset the handled flag before processing
				blockerHandledClose = false;

				// Check if click is inside the window's interactive area BEFORE processing
				if (event != null && event.type == PointerEvent.Type.DOWN) {
					boolean insideWindowArea = isInsideWindowArea((int)event.current.x, (int)event.current.y);
					// If click is inside window area, let child components handle it.
					// Return false to continue event propagation to buttons inside window.
					if (insideWindowArea && Window.this.active) {
						return false;
					}
				}

				boolean result = super.onSignal(event);

				// If this blocker handled a window close, return true to stop event propagation
				if (blockerHandledClose) {
					return true;
				}
				return result;
			}

			@Override
			protected void onClick( PointerEvent event ) {
				// Skip this click if flagged (to prevent cascading window closures)
				if (skipNextClick) {
					skipNextClick = false;
					blockerHandledClose = true;
					return;
				}

				// Use isInsideWindowArea instead of just chrome check
				if (Window.this.parent != null && !isInsideWindowArea(
					(int) event.current.x,
					(int) event.current.y )) {
					blockerHandledClose = true;
					onBackPressed();
				}
			}
		};
		blocker.camera = PixelScene.uiCamera;
		add( blocker );
		
		this.chrome = chrome;

		this.width = width;
		this.height = height;

		shadow = new ShadowBox();
		shadow.am = 0.5f;
		shadow.camera = PixelScene.uiCamera.visible ?
				PixelScene.uiCamera : Camera.main;
		add( shadow );

		chrome.x = -chrome.marginLeft();
		chrome.y = -chrome.marginTop();
		chrome.size(
			width - chrome.x + chrome.marginRight(),
			height - chrome.y + chrome.marginBottom() );
		add( chrome );

		RectF insets = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK);
		int screenW = (int)(Game.width - insets.left - insets.right);
		int screenH = (int)(Game.height - insets.top - insets.bottom);
		
		camera = new Camera( 0, 0,
			(int)chrome.width,
			(int)chrome.height,
			PixelScene.defaultZoom );
		camera.x = (int)(insets.left + (screenW - camera.width * camera.zoom) / 2);
		camera.y = (int)(insets.top + (screenH - camera.height * camera.zoom) / 2);
		camera.y -= yOffset * camera.zoom;
		camera.scroll.set( chrome.x, chrome.y );
		Camera.add( camera );

		shadow.boxRect(
				camera.x / camera.zoom,
				camera.y / camera.zoom,
				chrome.width(), chrome.height );

		KeyEvent.addKeyListener( this );
	}

	@Override
	public void update() {
		super.update();
		// Initialize window activation stack on first update after being added to scene
		if (!sceneInitialized && parent != null) {
			sceneInitialized = true;
			onAddedToScene();
		}
	}

	// Called once when window is first added to the scene.
	// Deactivates all other windows to implement a window stack.
	// DO NOT REMOVE - this is essential for preventing clicks on background windows.
	protected void onAddedToScene() {
		if (parent == null) return;

		// Deactivate all other windows
		for (int i = 0; i < parent.length; i++) {
			Gizmo g = parent.getMember(i);
			if (g instanceof Window && g != this && g.exists) {
				g.active = false;
			}
		}
	}

	public void resize( int w, int h ) {
		this.width = w;
		this.height = h;
		
		chrome.size(
			width + chrome.marginHor(),
			height + chrome.marginVer() );
		
		camera.resize( (int)chrome.width, (int)chrome.height );

		RectF insets = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK);
		int screenW = (int)(Game.width - insets.left - insets.right);
		int screenH = (int)(Game.height - insets.top - insets.bottom);

		camera.x = (int)(screenW - camera.screenWidth()) / 2;
		camera.x += insets.left;
		camera.x += xOffset * camera.zoom;

		camera.y = (int)(screenH - camera.screenHeight()) / 2;
		camera.y += insets.top;
		camera.y += yOffset * camera.zoom;

		shadow.boxRect( camera.x / camera.zoom, camera.y / camera.zoom, chrome.width(), chrome.height );
	}

	public Point getOffset(){
		return new Point(xOffset, yOffset);
	}

	public final void offset( Point offset ){
		offset(offset.x, offset.y);
	}

	//windows with scroll panes will likely need to override this and refresh them when offset changes
	public void offset( int xOffset, int yOffset ){
		camera.x -= this.xOffset * camera.zoom;
		this.xOffset = xOffset;
		camera.x += xOffset * camera.zoom;

		camera.y -= this.yOffset * camera.zoom;
		this.yOffset = yOffset;
		camera.y += yOffset * camera.zoom;

		shadow.boxRect( camera.x / camera.zoom, camera.y / camera.zoom, chrome.width(), chrome.height );
	}

	//ensures the window, with offset, does not go beyond a given margin
	public void boundOffsetWithMargin( int margin ){
		float x = camera.x / camera.zoom;
		float y = camera.y / camera.zoom;

		Camera sceneCam = PixelScene.uiCamera.visible ? PixelScene.uiCamera : Camera.main;

		int newXOfs = xOffset;
		if (x < margin){
			newXOfs += margin - x;
		} else if (x + camera.width > sceneCam.width - margin){
			newXOfs += (sceneCam.width - margin) - (x + camera.width);
		}

		int newYOfs = yOffset;
		if (y < margin){
			newYOfs += margin - y;
		} else if (y + camera.height > sceneCam.height - margin){
			newYOfs += (sceneCam.height - margin) - (y + camera.height);
		}

		offset(newXOfs, newYOfs);
	}
	
	public void hide() {
		// Reactivate the previous topmost window BEFORE erasing from parent.
		// DO NOT REMOVE - this is essential for restoring background window functionality.
		if (parent != null) {
			Window toReactivate = null;
			for (int i = parent.length - 1; i >= 0; i--) {
				Gizmo g = parent.getMember(i);
				if (g instanceof Window && g != this && g.exists) {
					toReactivate = (Window) g;
					break;
				}
			}
			if (toReactivate != null) {
				toReactivate.active = true;
				// Note: Dont call refreshBlockerPriority() here.
				// The blocker should remain behind the windows own buttons in the listener order.
			}
			parent.erase(this);
		}
		destroy();
	}

	// Check if this window is the topmost window in the scene
	public boolean isTopmost() {
		if (parent == null) return false;
		// Check if there are any other Windows after this one in the parent's children
		boolean foundSelf = false;
		for (int i = 0; i < parent.length; i++) {
			com.watabou.noosa.Gizmo g = parent.getMember(i);
			if (g == this) {
				foundSelf = true;
			} else if (foundSelf && g instanceof Window && g.exists) {
				return false; // Another window exists after us
			}
		}
		return foundSelf;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		Camera.remove( camera );
		KeyEvent.removeKeyListener( this );

		if (onHideCallback != null) {
			onHideCallback.run();
			onHideCallback = null;
		}
	}

	public void setOnHideCallback(Runnable callback) {
		this.onHideCallback = callback;
	}

	// Restore blocker's pointer priority when window becomes active again
	// This is needed after a child window closes to ensure click-outside-to-close works
	public void refreshBlockerPriority() {
		if (blocker != null) {
			blocker.reset();
			blocker.givePointerPriority();
		}
	}

	@Override
	public boolean onSignal( KeyEvent event ) {
		if (event.pressed) {
			// Only handle keyboard events if this window is active and topmost
			if (!active || !isTopmost()) {
				return false;
			}

			GameAction action = KeyBindings.getActionForKey(event);
			if (action == SPDAction.BACK) {
				onBackPressed();
				return true;
			} else if (event.code == Input.Keys.ENTER || event.code == Input.Keys.NUMPAD_ENTER) {
				// Only handle Enter if there are focusable buttons
				if (!focusableButtons.isEmpty()) {
					onConfirm();
					return true;
				}
			} else if (action == SPDAction.N || action == SPDAction.NW
					|| action == SPDAction.W || action == SPDAction.SW) {
				// Only handle arrow keys if there are focusable buttons
				if (!focusableButtons.isEmpty()) {
					moveFocus(-1);
					return true;
				}
			} else if (action == SPDAction.S || action == SPDAction.SE
					|| action == SPDAction.E || action == SPDAction.NE) {
				// Only handle arrow keys if there are focusable buttons
				if (!focusableButtons.isEmpty()) {
					moveFocus(1);
					return true;
				}
			}
		}

		// Return false to allow other listeners (like WndTabbed's tabListener) to handle the event
		return false;
	}

	public void onConfirm() {
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			activateFocused();
		}
		// If no button is focused, do nothing (same as WndOptions behavior)
	}

	public void onBackPressed() {
		hide();
	}

	// Check if a screen point is inside the window's interactive area
	// Subclasses (like WndTabbed) can override to include additional areas like tabs
	protected boolean isInsideWindowArea(int screenX, int screenY) {
		return chrome.overlapsScreenPoint(screenX, screenY);
	}

	public void addFocusableButton(Button btn) {
		focusableButtons.add(btn);
	}

	public void addFocusable(Focusable focusable) {
		focusableButtons.add(focusable);
	}

	public void clearFocus() {
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			focusableButtons.get(focusIndex).restoreFocusState();
		}
		focusIndex = -1;
	}

	public int getFocusIndex() {
		return focusIndex;
	}

	public void restoreFocusToIndex(int index) {
		if (index >= 0 && index < focusableButtons.size()) {
			focusIndex = index;
			focusableButtons.get(focusIndex).saveFocusState();
			focusableButtons.get(focusIndex).setFocused(true);
		}
	}

	protected void moveFocus(int direction) {
		if (focusableButtons.isEmpty()) return;

		// Restore focus state on previously focused element
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			focusableButtons.get(focusIndex).restoreFocusState();
		}

		// Move index
		int startIndex;
		if (focusIndex == -1) {
			startIndex = direction > 0 ? 0 : focusableButtons.size() - 1;
		} else {
			startIndex = focusIndex + direction;
			if (startIndex < 0) startIndex = focusableButtons.size() - 1;
			if (startIndex >= focusableButtons.size()) startIndex = 0;
		}

		// Skip disabled elements
		focusIndex = startIndex;
		int checked = 0;
		while (!focusableButtons.get(focusIndex).isActive() && checked < focusableButtons.size()) {
			focusIndex += direction;
			if (focusIndex < 0) focusIndex = focusableButtons.size() - 1;
			if (focusIndex >= focusableButtons.size()) focusIndex = 0;
			checked++;
		}

		// Save focus state then highlight new element
		focusableButtons.get(focusIndex).saveFocusState();
		focusableButtons.get(focusIndex).setFocused(true);
	}

	protected void activateFocused() {
		if (focusIndex >= 0 && focusIndex < focusableButtons.size()) {
			Focusable focusable = focusableButtons.get(focusIndex);
			if (focusable.isActive()) {
				Sample.INSTANCE.play(Assets.Sounds.CLICK);
				focusable.click();
			}
		}
	}

}
