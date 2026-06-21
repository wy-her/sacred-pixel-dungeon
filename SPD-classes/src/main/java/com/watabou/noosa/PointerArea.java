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

package com.watabou.noosa;

import com.watabou.input.PointerEvent;
import com.watabou.utils.Signal;

public class PointerArea extends Visual implements Signal.Listener<PointerEvent> {
	
	// Its target can be pointerarea itself
	public Visual target;
	
	protected PointerEvent curEvent = null;
	protected boolean hovered = false;

	public int blockLevel = BLOCK_WHEN_ACTIVE;
	public static final int ALWAYS_BLOCK = 0;       //Always block input to overlapping elements
	public static final int BLOCK_WHEN_ACTIVE = 1;  //Only block when active (default)
	public static final int NEVER_BLOCK = 2;        //Never block (handy for buttons in scroll areas)
	
	public PointerArea( Visual target ) {
		super( 0, 0, 0, 0 );
		this.target = target;
		
		PointerEvent.addPointerListener( this );
	}
	
	public PointerArea( float x, float y, float width, float height ) {
		super( x, y, width, height );
		this.target = this;
		
		visible = false;
		
		PointerEvent.addPointerListener( this );
	}
	
	// DEBUG flag for tracing click detection
	public static boolean DEBUG_CLICK_TRACE = false;

	@Override
	public boolean onSignal( PointerEvent event ) {

		boolean hit = event != null && target.overlapsScreenPoint( (int)event.current.x, (int)event.current.y );

		// Debug trace for ItemSlot and BadgeButton clicks
		if (DEBUG_CLICK_TRACE && event != null && event.type == PointerEvent.Type.DOWN) {
			// Check if this PointerArea belongs to an ItemSlot or BadgeButton
			String parentName = (parent != null) ? parent.getClass().getSimpleName() : "null";
			boolean isBadgeButton = parentName.contains("BadgeButton");
			boolean isItemSlot = parentName.contains("ItemSlot") || (parent != null && parent.parent != null &&
				parent.parent.getClass().getSimpleName().contains("QuickRecipe"));

			if (isBadgeButton || isItemSlot) {
				Camera c = camera();
				System.out.println("[PointerArea.onSignal] " + parentName + " DOWN event:" +
					" screenPos=(" + (int)event.current.x + "," + (int)event.current.y + ")" +
					" hit=" + hit +
					" isActive=" + isActive() +
					" target.pos=(" + target.x + "," + target.y + ")" +
					" target.size=(" + target.width + "x" + target.height + ")" +
					" blockLevel=" + blockLevel +
					" camera=" + (c != null ? "(x=" + c.x + ",y=" + c.y + ",scroll=" + c.scroll + ",zoom=" + c.zoom + ")" : "NULL"));
				// Also trace why hit might be false
				if (!hit && c != null) {
					boolean hitTest = c.hitTest((int)event.current.x, (int)event.current.y);
					System.out.println("  -> Camera hitTest result: " + hitTest);
					if (hitTest) {
						com.watabou.utils.PointF p = c.screenToCamera((int)event.current.x, (int)event.current.y);
						System.out.println("  -> screenToCamera: (" + p.x + ", " + p.y + ")");
						System.out.println("  -> overlapsPoint would check: x>=" + target.x + " && x<" + (target.x + target.width * target.scale.x) +
							" && y>=" + target.y + " && y<" + (target.y + target.height * target.scale.y));
					}
				}
				// Trace parent chain for camera debugging
				if (isBadgeButton) {
					System.out.println("  -> BadgeButton parent chain:");
					Group p = parent;
					int depth = 0;
					while (p != null && depth < 10) {
						Camera pc = p.camera;
						System.out.println("     " + depth + ": " + p.getClass().getSimpleName() +
							" (camera=" + (pc != null ? "Camera@" + Integer.toHexString(pc.hashCode()) : "null") +
							", active=" + p.active + ")");
						p = p.parent;
						depth++;
					}
				}
			}
		}

		if (!isActive()) {
			if (DEBUG_CLICK_TRACE && event != null && event.type == PointerEvent.Type.DOWN) {
				String parentName = (parent != null) ? parent.getClass().getSimpleName() : "null";
				if (parentName.contains("ItemSlot")) {
					System.out.println("  -> BLOCKED: isActive()=false, returning hit=" + hit + " && blockLevel==ALWAYS_BLOCK=" + (blockLevel == ALWAYS_BLOCK));
				}
			}
			return (hit && blockLevel == ALWAYS_BLOCK);
		}

		if (hit) {

			boolean returnValue = (event.type == PointerEvent.Type.DOWN || event == curEvent);

			if (event.type == PointerEvent.Type.DOWN) {

				if (curEvent == null) {
					curEvent = event;
				}
				onPointerDown( event );

			} else if (event.type == PointerEvent.Type.UP) {

				onPointerUp( event );

				if (curEvent == event) {
					curEvent = null;
					onClick( event );
				}

			//similar to up, but no click
			} else if (event.type == PointerEvent.Type.CANCEL) {

				onPointerUp( event );

				if (curEvent == event) {
					curEvent = null;
				}

			} else if (event.type == PointerEvent.Type.HOVER) {
				if (event.handled && hovered){
					hovered = false;
					onHoverEnd(event);
				} else if (!event.handled && !hovered){
					hovered = true;
					onHoverStart(event);
				}
				event.handle();
			}

			return returnValue && blockLevel != NEVER_BLOCK;
			
		} else {
			
			if (event == null && curEvent != null) {
				onDrag(curEvent);

			} else if (curEvent != null &&
					(event.type == PointerEvent.Type.UP || event.type == PointerEvent.Type.CANCEL)) {
				onPointerUp( event );
				curEvent = null;

			} else if (event != null && event.type == PointerEvent.Type.HOVER && hovered){
				hovered = false;
				onHoverEnd(event);
			}
			
			return false;
			
		}
	}
	
	protected void onPointerDown( PointerEvent event ) { }
	
	protected void onPointerUp( PointerEvent event) { }
	
	protected void onClick( PointerEvent event ) { }
	
	protected void onDrag( PointerEvent event ) { }

	protected void onHoverStart( PointerEvent event ) { }

	protected void onHoverEnd( PointerEvent event ) { }
	
	public void reset() {
		curEvent = null;
	}

	//moves this pointer area to the front of the pointer event order
	public void givePointerPriority(){
		PointerEvent.removePointerListener( this );
		PointerEvent.addPointerListener( this );
	}
	
	@Override
	public void destroy() {
		PointerEvent.removePointerListener( this );
		super.destroy();
	}
}
