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

package com.watabou.glwrap;

import com.badlogic.gdx.Gdx;

public class Blending {

	private static boolean blendEnabled = false;
	private static int currentSrc = -1;
	private static int currentDst = -1;

	public static void useDefault(){
		enable();
		setNormalMode();
	}

	public static void enable(){
		if (!blendEnabled) {
			blendEnabled = true;
			Gdx.gl.glEnable( Gdx.gl.GL_BLEND );
		}
	}

	public static void disable(){
		if (blendEnabled) {
			blendEnabled = false;
			Gdx.gl.glDisable( Gdx.gl.GL_BLEND );
		}
	}

	//in this mode colors overwrite eachother, based on alpha value
	public static void setNormalMode(){
		setBlendFunc( Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA );
	}

	//in this mode colors add to eachother, eventually reaching pure white
	public static void setLightMode(){
		setBlendFunc( Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE );
	}

	private static void setBlendFunc( int src, int dst ){
		if (src != currentSrc || dst != currentDst) {
			currentSrc = src;
			currentDst = dst;
			Gdx.gl.glBlendFunc( src, dst );
		}
	}

	// Call when GL context is lost/recreated to reset tracked state
	public static void reset(){
		blendEnabled = false;
		currentSrc = -1;
		currentDst = -1;
	}

}
