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

import com.badlogic.gdx.Gdx;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Attribute;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Uniform;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.utils.DeviceCompat;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class NoosaScript extends Script {

	public Uniform uCamera;
	public Uniform uModel;
	public Uniform uTex;
	public Uniform uColorM;
	public Uniform uColorA;
	public Attribute aXY;
	public Attribute aUV;

	private Camera lastCamera;

	// Temporary VBO/IBO for WebGL compatibility (WebGL doesn't support client-side vertex arrays)
	private int tempVbo = -1;
	private int tempIbo = -1;

	private void uploadToTempVbo( FloatBuffer vertices ) {
		if (tempVbo == -1) tempVbo = Gdx.gl.glGenBuffer();
		((Buffer)vertices).position(0);
		Gdx.gl.glBindBuffer( Gdx.gl.GL_ARRAY_BUFFER, tempVbo );
		Gdx.gl.glBufferData( Gdx.gl.GL_ARRAY_BUFFER, vertices.limit() * 4, vertices, Gdx.gl.GL_DYNAMIC_DRAW );
	}

	private void releaseTempVbo() {
		Gdx.gl.glBindBuffer( Gdx.gl.GL_ARRAY_BUFFER, 0 );
	}

	private void uploadToTempIbo( ShortBuffer indices, int size ) {
		if (tempIbo == -1) tempIbo = Gdx.gl.glGenBuffer();
		((Buffer)indices).position(0);
		Gdx.gl.glBindBuffer( Gdx.gl.GL_ELEMENT_ARRAY_BUFFER, tempIbo );
		Gdx.gl.glBufferData( Gdx.gl.GL_ELEMENT_ARRAY_BUFFER, size * 2, indices, Gdx.gl.GL_DYNAMIC_DRAW );
	}

	private void releaseTempIbo() {
		Gdx.gl.glBindBuffer( Gdx.gl.GL_ELEMENT_ARRAY_BUFFER, 0 );
	}
	
	public NoosaScript() {

		super();
		compile( shader() );
		
		uCamera	= uniform( "uCamera" );
		uModel	= uniform( "uModel" );
		uTex	= uniform( "uTex" );
		uColorM	= uniform( "uColorM" );
		uColorA	= uniform( "uColorA" );
		aXY		= attribute( "aXYZW" );
		aUV		= attribute( "aUV" );

		Quad.setupIndices();
		Quad.bindIndices();
		
	}
	
	@Override
	public void use() {
		
		super.use();
		
		aXY.enable();
		aUV.enable();
		
	}

	public void drawElements( FloatBuffer vertices, ShortBuffer indices, int size ) {

		uploadToTempVbo( vertices );
		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );
		releaseTempVbo();

		Quad.releaseIndices();
		uploadToTempIbo( indices, size );
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, size, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
		releaseTempIbo();
		Quad.bindIndices();
	}

	public void drawQuad( FloatBuffer vertices ) {

		uploadToTempVbo( vertices );
		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );
		releaseTempVbo();

		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	public void drawQuad( Vertexbuffer buffer ) {

		buffer.updateGLData();

		buffer.bind();

		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );

		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );

		buffer.release();
	}

	public void drawQuadSet( FloatBuffer vertices, int size ) {

		if (size == 0) {
			return;
		}

		uploadToTempVbo( vertices );
		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );
		releaseTempVbo();

		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE * size, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	public void drawQuadSet( Vertexbuffer buffer, int length, int offset ){

		if (length == 0) {
			return;
		}

		buffer.updateGLData();

		buffer.bind();

		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );

		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE * length, Gdx.gl20.GL_UNSIGNED_SHORT, Quad.SIZE * Short.SIZE/8 * offset );

		buffer.release();
	}
	
	public void lighting( float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa ) {
		uColorM.value4f( rm, gm, bm, am );
		uColorA.value4f( ra, ga, ba, aa );
	}
	
	public void resetCamera() {
		lastCamera = null;
	}
	
	public void camera( Camera camera ) {
		if (camera == null) {
			camera = Camera.main;
		}
		if (camera != lastCamera && camera.matrix != null) {
			lastCamera = camera;
			uCamera.valueM4( camera.matrix );

			if (!camera.fullScreen) {
				Gdx.gl20.glEnable( Gdx.gl20.GL_SCISSOR_TEST );

				//This fixes pixel scaling issues on some hidpi displays (mainly on macOS)
				// because for some reason all other openGL operations work on virtual pixels
				// but glScissor operations work on real pixels
				float xScale = DeviceCompat.getRealPixelScaleX();
				float yScale = DeviceCompat.getRealPixelScaleY();

				Gdx.gl20.glScissor(
						Math.round(camera.x * xScale),
						Math.round((Game.height - camera.screenHeight - camera.y) * yScale),
						Math.round(camera.screenWidth * xScale),
						Math.round(camera.screenHeight * yScale));
			} else {
				Gdx.gl20.glDisable( Gdx.gl20.GL_SCISSOR_TEST );
			}
		}
	}
	
	public static NoosaScript get() {
		return Script.use( NoosaScript.class );
	}
	
	
	protected String shader() {
		return SHADER;
	}
	
	private static final String SHADER =
		
		//vertex shader
		"uniform mat4 uCamera;\n" +
		"uniform mat4 uModel;\n" +
		"attribute vec4 aXYZW;\n" +
		"attribute vec2 aUV;\n" +
		"varying vec2 vUV;\n" +
		"void main() {\n" +
		"  gl_Position = uCamera * uModel * aXYZW;\n" +
		"  vUV = aUV;\n" +
		"}\n" +
		
		//this symbol separates the vertex and fragment shaders (see Script.compile)
		"//\n" +
		
		//fragment shader
		//preprocessor directives let us define precision on GLES platforms, and ignore it elsewhere
		"#ifdef GL_ES\n" +
		"  precision mediump float;\n" +
		"#endif\n" +
		"varying vec2 vUV;\n" +
		"uniform sampler2D uTex;\n" +
		"uniform vec4 uColorM;\n" +
		"uniform vec4 uColorA;\n" +
		"void main() {\n" +
		"  gl_FragColor = texture2D( uTex, vUV ) * uColorM + uColorA;\n" +
		"}\n";
}
