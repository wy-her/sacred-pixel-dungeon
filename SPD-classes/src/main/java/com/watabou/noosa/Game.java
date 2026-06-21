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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.TimeUtils;
import com.watabou.glscripts.Script;
import com.watabou.utils.GameSettings;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.input.ControllerHandler;
import com.watabou.input.InputHandler;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Reflection;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

public class Game implements ApplicationListener {

	public static Game instance;
	
	// Size of the EGL surface view
	public static int width;
	public static int height;

	// Density: mdpi=1, hdpi=1.5, xhdpi=2...
	public static float density = 1;
	
	public static String version;
	public static int versionCode;
	
	// Current scene
	protected Scene scene;
	// New scene we are going to switch to
	protected Scene requestedScene;
	// true if scene switch is requested
	protected boolean requestedReset = true;
	// true while switchScene() is executing (destroy old → create new)
	protected boolean inSwitchScene = false;
	// callback to perform logic during scene change
	protected SceneChangeCallback onChange;
	// New scene class
	protected static Class<? extends Scene> sceneClass;
	
	public static float timeScale = 1.0f;
	public static float elapsed = 0f;
	public static float timeTotal = 0f;
	public static long realTime = 0;

	public static InputHandler inputHandler;
	
	public static PlatformSupport platform;
	
	public Game(Class<? extends Scene> c, PlatformSupport platform) {
		sceneClass = c;
		
		instance = this;
		this.platform = platform;
	}
	
	@Override
	public void create() {
		// Set clear color to black to prevent garbage pixels during scene transitions
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);

		density = Gdx.graphics.getDensity();
		if (density == Float.POSITIVE_INFINITY){
			density = 100f / 160f; //assume 100PPI if density can't be found
		} else if (DeviceCompat.isDesktop()) {
			int dispWidth = Gdx.graphics.getDisplayMode().width;
			int dispHeight = Gdx.graphics.getDisplayMode().height;
			float reportedWidth = dispWidth / Gdx.graphics.getPpiX();
			float reportedHeight = dispHeight / Gdx.graphics.getPpiY();

			//this exists because Steam deck reports its display size as 4"x6.3" for some reason
			// as if in portrait, instead of 6.3"x4". This results in incorrect PPI measurements.
			// So we check that the orientation of the resolution and the display dimensions match.
			// If they don't, re-calculate density assuming reported dimensions are flipped.
			if (dispWidth > dispHeight != reportedWidth > reportedHeight){
				float realPpiX = dispWidth / reportedHeight;
				density = realPpiX / 160f;
			}
		}

		inputHandler = new InputHandler( Gdx.input );
		ControllerHandler.setupControllerListener();

		//refreshes texture and vertex data stored on the gpu
		versionContextRef = Gdx.graphics.getGLVersion();
		Blending.useDefault();
		TextureCache.reload();
		Vertexbuffer.reload();
	}

	private GLVersion versionContextRef;
	
	@Override
	public void resize(int width, int height) {
		if (width == 0 || height == 0){
			return;
		}

		//If the EGL context was destroyed, we need to refresh some data stored on the GPU.
		// This checks that by seeing if GLVersion has a new object reference
		if (versionContextRef != Gdx.graphics.getGLVersion()) {
			versionContextRef = Gdx.graphics.getGLVersion();
			Gdx.gl.glClearColor(0f, 0f, 0f, 1f); // Reset clear color after context restore
			Script.reset();
			Quad.reset(); //invalidate index buffer ID so setupIndices() regenerates it
			Blending.useDefault();
			TextureCache.reload();
			Vertexbuffer.reload();
			//On HTML5, font atlas textures are managed outside TextureCache
			//by CanvasFontData. Invalidate them so they re-render and re-upload.
			if (onGLContextRestore != null) {
				onGLContextRestore.call();
			}
		}

		if (height != Game.height || width != Game.width) {

			Game.width = width;
			Game.height = height;
			
			resetScene();
		}
	}

	//justResumed is a bit of a hack to improve start time metrics on Android,
	// as texture refreshing leads to slow warm starts. TODO would be nice to fix this properly
	private boolean justResumed = true;

	@Override
	public void render() {
		//prevents weird rare cases where the app is running twice
		if (instance != this){
			finish();
			return;
		}

		if (justResumed){
			justResumed = false;
			if (DeviceCompat.isAndroid()) return;
		}
		//On HTML5, a deferred GL reload may have been requested by error recovery
		//or by the resume() path detecting context loss. Force the GLVersion
		//reference to null so the context-loss check below triggers a full reload.
		if (DeviceCompat.isHTML5() && glReloadPending) {
			glReloadPending = false;
			versionContextRef = null;
		}

		//Also check for EGL context loss during render, not just resize.
		//This is needed for HTML5/WebGL where the context can be lost when
		//the browser tab is hidden and restored without triggering a resize.
		if (versionContextRef != Gdx.graphics.getGLVersion()) {
			versionContextRef = Gdx.graphics.getGLVersion();
			Gdx.gl.glClearColor(0f, 0f, 0f, 1f); // Reset clear color after context restore
			Script.reset();
			Quad.reset(); //invalidate index buffer ID so setupIndices() regenerates it
			Blending.useDefault();
			TextureCache.reload();
			Vertexbuffer.reload();
			//On HTML5, font atlas textures are managed outside TextureCache
			//by CanvasFontData. Invalidate them so they re-render and re-upload.
			if (onGLContextRestore != null) {
				onGLContextRestore.call();
			}
		}

		try {
			NoosaScript.get().resetCamera();
			NoosaScriptNoLighting.get().resetCamera();
			Gdx.gl.glDisable(Gdx.gl.GL_SCISSOR_TEST);
			Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
			draw();
		} catch (Throwable e) {
			//On HTML5/WebGL, rendering can fail after context loss.
			//Catch both Exception and Error (e.g. "java.lang.Error: null" from
			//null GL context or invalid texture operations after context loss).
			//Force a full context refresh on the next frame.
			if (DeviceCompat.isHTML5()) {
				glReloadPending = true;
				return;
			} else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException("render() failed", e);
			}
		}

		step();

		//Flush any batched preference writes (avoids per-put localStorage writes on HTML5)
		GameSettings.flushIfDirty();
	}
	
	@Override
	public void pause() {
		if (scene != null) {
			scene.onPause();
		}
		
		//On HTML5/WebGL, don't delete shader programs during pause.
		//The WebGL context handles invalidation automatically on context loss,
		//and eagerly deleting programs here causes null references in uniforms/attributes
		//when render() tries to recreate scripts before the context is fully restored.
		if (!DeviceCompat.isHTML5()) {
			Script.reset();
		}
	}
	
	//Flag set by JavaScript webglcontextlost event handler via TeaVMLauncher.
	//Only when this is true should resume() force a full GPU state reload.
	public static volatile boolean webglContextLost = false;

	//Tracks whether a full GPU resource reload is needed on the next render frame.
	//Set to true when context loss is detected; cleared after reload completes.
	//This is separate from webglContextLost because it can also be set by
	//error recovery in render() when GL operations fail unexpectedly.
	public static volatile boolean glReloadPending = false;

	//Hook for HTML5 font atlas invalidation after GL context loss.
	//Set by TeaVMLauncher to call CanvasFontData.invalidateAllFontAtlases().
	//Font atlas textures are managed outside TextureCache, so TextureCache.reload()
	//does not cover them. This callback re-renders glyphs to canvas and re-uploads.
	public static volatile Callback onGLContextRestore = null;

	@Override
	public void resume() {
		justResumed = true;
		//On HTML5/WebGL, only invalidate GL state if the context was actually lost.
		//Simple tab switches (visibilitychange) do NOT lose the context.
		//The webglcontextlost flag is set by JavaScript and cleared here after reload.
		if (DeviceCompat.isHTML5() && webglContextLost) {
			versionContextRef = null;
			glReloadPending = true;
			webglContextLost = false;
		}
	}
	
	public void finish(){
		Gdx.app.exit();
		
	}
	
	public void destroy(){
		if (scene != null) {
			scene.destroy();
			scene = null;
		}
		
		sceneClass = null;
		Music.INSTANCE.stop();
		Sample.INSTANCE.reset();
	}
	
	@Override
	public void dispose() {
		destroy();
	}
	
	public static void resetScene() {
		switchScene( instance.sceneClass );
	}

	public static void switchScene(Class<? extends Scene> c) {
		switchScene(c, null);
	}
	
	public static void switchScene(Class<? extends Scene> c, SceneChangeCallback callback) {
		instance.sceneClass = c;
		instance.requestedReset = true;
		instance.onChange = callback;
	}
	
	public static Scene scene() {
		return instance.scene;
	}

	public static boolean switchingScene() {
		return instance.requestedReset || instance.inSwitchScene;
	}
	
	protected void step() {
		
		if (requestedReset) {
			requestedReset = false;
			
			requestedScene = Reflection.newInstance(sceneClass);
			if (requestedScene != null){
				switchScene();
			}

		}
		
		update();
	}
	
	protected void draw() {
		if (scene != null) scene.draw();
	}
	
	protected void switchScene() {

		inSwitchScene = true;
		try {
			Camera.reset();

			if (scene != null) {
				scene.destroy();
			}
			//clear any leftover vertex buffers
			Vertexbuffer.clear();
			scene = requestedScene;
			if (onChange != null) onChange.beforeCreate();
			scene.create();
			if (onChange != null) onChange.afterCreate();
			onChange = null;

			Game.elapsed = 0f;
			Game.timeScale = 1.0f;
			Game.timeTotal = 0f;
		} catch (Exception e) {
			Game.reportException(e);
			//if scene creation fails, try to recover by resetting to a fresh scene
			//the scene variable is already set to requestedScene, so at minimum
			//the game loop won't crash on null scene references
			onChange = null;
			Game.elapsed = 0f;
			Game.timeScale = 1.0f;
			Game.timeTotal = 0f;
		} finally {
			inSwitchScene = false;
		}
	}

	protected void update() {
		//game will not process more than 200ms of graphics time per frame
		float frameDelta = Math.min(0.2f, Gdx.graphics.getDeltaTime());
		Game.elapsed = Game.timeScale * frameDelta;
		Game.timeTotal += Game.elapsed;
		
		Game.realTime = TimeUtils.millis();

		inputHandler.processAllEvents();

		Music.INSTANCE.update();
		Sample.INSTANCE.update();
		scene.update();
		Camera.updateAll();
	}
	
	public static void reportException( Throwable tr ) {
		if (instance != null && Gdx.app != null) {
			instance.logException(tr);
		} else {
			//fallback if error happened in initialization
			System.err.println(getStackTraceString(tr));
		}
	}

	protected void logException( Throwable tr ){
		Gdx.app.error("GAME", getStackTraceString(tr));
	}

	private static String getStackTraceString( Throwable tr ){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		tr.printStackTrace(ps);
		ps.flush();
		return baos.toString();
	}
	
	public static void runOnRenderThread(Callback c){
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				c.call();
			}
		});
	}
	
	public static void vibrate( int milliseconds ) {
		if (platform.supportsVibration()) {
			platform.vibrate(milliseconds);
		}
	}

	public interface SceneChangeCallback{
		void beforeCreate();
		void afterCreate();
	}
	
}
