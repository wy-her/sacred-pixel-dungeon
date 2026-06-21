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

package com.sacredpixel.sacredpixeldungeon.scenes;

import com.sacredpixel.sacredpixeldungeon.Assets;
import com.sacredpixel.sacredpixeldungeon.SPDAction;
import com.sacredpixel.sacredpixeldungeon.SacredPixelDungeon;
import com.sacredpixel.sacredpixeldungeon.effects.Flare;
import com.sacredpixel.sacredpixeldungeon.ui.ExitButton;
import com.sacredpixel.sacredpixeldungeon.ui.Icons;
import com.sacredpixel.sacredpixeldungeon.ui.RenderedTextBlock;
import com.sacredpixel.sacredpixeldungeon.ui.ScrollPane;
import com.sacredpixel.sacredpixeldungeon.ui.Window;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

public class AboutScene extends PixelScene {

	private Signal.Listener<KeyEvent> keyListener;
	private ScrollPane scrollPane;
	private ExitButton exitButton;
	private boolean exitButtonFocused = false;
	private static final float SCROLL_SPEED = 30f;

	@Override
	public void create() {
		super.create();

		final float colWidth = 180;

		int w = Camera.main.width;
		int h = Camera.main.height;

		RectF insets = getCommonInsets();

		ColorBlock BG = new ColorBlock(w, h, 0xFF000000);
		add( BG );

		scrollPane = new ScrollPane( new Component() );
		add( scrollPane );
		ScrollPane list = scrollPane;

		Component content = list.content();
		content.clear();

		float left = (w - colWidth) / 2f;
		float curY = insets.top + 6;

		//*** Sacred Pixel Dungeon Credits ***

		final int SACRED_COLOR = 0xFF6633;
		Image sacredIcon = new Image("interfaces/sacred-logo.png");
		CreditsBlock sacred = new CreditsBlock(true, SACRED_COLOR,
				"Sacred Pixel Dungeon",
				sacredIcon,
				"Developed by: _AI SOFT(WY-HER)_\nBased on Shattered Pixel Dungeon's\nopen source code",
				"github.com/wy-her/sacred-pixel-dungeon",
				"https://github.com/wy-her/sacred-pixel-dungeon");
		sacred.setRect(left, curY, colWidth, 0);
		content.add(sacred);
		curY = sacred.bottom() + 5;

		ColorBlock fontsIcon = new ColorBlock(16, 13, 0xFF000000);
		CreditsBlock fonts = new CreditsBlock(false, SACRED_COLOR,
				"Fonts:",
				fontsIcon,
				"Inter by Rasmus Andersson\nNoto Sans by Google",
				null,
				null);
		fonts.setRect(left, curY, colWidth, 0);
		content.add(fonts);
		curY = fonts.bottom() + 5;

		addLine(curY, content);
		curY += 8;

		//*** Shattered Pixel Dungeon Credits ***

		CreditsBlock shpx = new CreditsBlock(true, Window.SHPX_COLOR,
				"Shattered Pixel Dungeon",
				Icons.SHPX.get(),
				"Developed by: _Evan Debenham_\nBased on Pixel Dungeon's\nopen source code",
				"ShatteredPixel.com",
				"https://ShatteredPixel.com");
		shpx.setRect(left, curY, colWidth, 0);
		content.add(shpx);
		curY = shpx.bottom() + 5;

		CreditsBlock alex = new CreditsBlock(false, Window.SHPX_COLOR,
				"Splash Art & Design:",
				Icons.ALEKS.get(),
				"Aleksandar Komitov",
				"alekskomitov.com",
				"https://www.alekskomitov.com/");
		alex.setRect(left, curY, colWidth, 0);
		content.add(alex);
		curY = alex.bottom() + 5;

		CreditsBlock celesti = new CreditsBlock(false, Window.SHPX_COLOR,
				"Sound Effects:",
				Icons.CELESTI.get(),
				"Celesti",
				"celesti-whispers.itch.io",
				"https://celesti-whispers.itch.io/");
		celesti.setRect(left, curY, colWidth, 0);
		content.add(celesti);
		curY = celesti.bottom() + 5;

		CreditsBlock kristjan = new CreditsBlock(false, Window.SHPX_COLOR,
				"Music:",
				Icons.KRISTJAN.get(),
				"Kristjan Haaristo",
				"youtube.com/@kristjan...",
				"https://www.youtube.com/@kristjanthomashaaristo");
		kristjan.setRect(left, curY, colWidth, 0);
		content.add(kristjan);
		curY = kristjan.bottom() + 4;

		addLine(curY, content);
		curY += 8;

		//*** Pixel Dungeon Credits ***

		final int WATA_COLOR = 0x55AAFF;
		CreditsBlock wata = new CreditsBlock(true, WATA_COLOR,
				"Pixel Dungeon",
				Icons.WATA.get(),
				"Developed by: _Watabou_\nInspired by Brian Walker's Brogue",
				"watabou.itch.io",
				"https://watabou.itch.io/");
		wata.setRect(left, curY, colWidth, 0);
		content.add(wata);
		curY = wata.bottom() + 5;

		CreditsBlock cube = new CreditsBlock(false, WATA_COLOR,
				"Music:",
				Icons.CUBE_CODE.get(),
				"Cube Code",
				null,
				null);
		cube.setRect(left, curY, colWidth, 0);
		content.add(cube);
		curY = cube.bottom() + 4;

		addLine(curY, content);
		curY += 8;

		//*** libGDX Credits ***

		final int GDX_COLOR = 0xE44D3C;
		CreditsBlock gdx = new CreditsBlock(true,
				GDX_COLOR,
				"libGDX",
				Icons.LIBGDX.get(),
				"SacredPD is powered by _libGDX_",
				"libgdx.com",
				"https://libgdx.com/");
		gdx.setRect(left, curY, colWidth, 0);
		content.add(gdx);
		curY = gdx.bottom() + 5;

		CreditsBlock arcnor = new CreditsBlock(false, GDX_COLOR,
				"Pixel Dungeon GDX:",
				Icons.ARCNOR.get(),
				"Edu García",
				"gamedev.place/@arcnor",
				"https://mastodon.gamedev.place/@arcnor");
		arcnor.setRect(left, curY, colWidth, 0);
		content.add(arcnor);
		curY = arcnor.bottom() + 5;

		CreditsBlock purigro = new CreditsBlock(false, GDX_COLOR,
				"Shattered GDX Help:",
				Icons.PURIGRO.get(),
				"Kevin MacMartin",
				"github.com/prurigro",
				"https://github.com/prurigro/");
		purigro.setRect(left, curY, colWidth, 0);
		content.add(purigro);
		curY = purigro.bottom() + 4;

		addLine(curY, content);
		curY += 8;

		//*** Transifex Credits ***

		CreditsBlock transifex = new CreditsBlock(true,
				Window.TITLE_COLOR,
				null,
				null,
				"Community-translated via _Transifex_\nThank you so much to all of Shattered's volunteer translators!",
				"transifex.com/shattered-pixel/...",
				"https://explore.transifex.com/shattered-pixel/shattered-pixel-dungeon/");
		transifex.setRect(left, curY, colWidth, 0);
		content.add(transifex);
		curY = transifex.bottom() + 4;

		addLine(curY, content);
		curY += 8;

		//*** Freesound Credits ***

		CreditsBlock freesound = new CreditsBlock(true,
				Window.TITLE_COLOR,
				null,
				null,
				"Shattered Pixel Dungeon uses the following sound samples from _freesound.org_:\n\n" +

				"Creative Commons Attribution License:\n" +
				"_SFX ATTACK SWORD 001.wav_ by _JoelAudio_\n" +
				"_Pack: Slingshots and Longbows_ by _saturdaysoundguy_\n" +
				"_Cracking/Crunching, A.wav_ by _InspectorJ_\n" +
				"_Extracting a sword.mp3_ by _Taira Komori_\n" +
				"_Pack: Uni Sound Library_ by _timmy h123_\n\n" +

				"Creative Commons Zero License:\n" +
				"_Pack: Movie Foley: Swords_ by _Black Snow_\n" +
				"_machine gun shot 2.flac_ by _qubodup_\n" +
				"_m240h machine gun burst 4.flac_ by _qubodup_\n" +
				"_Pack: Onomatopoeia_ by _Adam N_\n" +
				"_Pack: Watermelon_ by _lolamadeus_\n" +
				"_metal chain_ by _Mediapaja2009_\n" +
				"_Pack: Sword Clashes Pack_ by _JohnBuhr_\n" +
				"_Pack: Metal Clangs and Pings_ by _wilhellboy_\n" +
				"_Pack: Stabbing Stomachs & Crushing Skulls_ by _TheFilmLook_\n" +
				"_Sheep bleating_ by _zachrau_\n" +
				"_Lemon,Juicy,Squeeze,Fruit.wav_ by _Filipe Chagas_\n" +
				"_Lemon,Squeeze,Squishy,Fruit.wav_ by _Filipe Chagas_",
				"freesound.org",
				"https://www.freesound.org");
		freesound.setRect(left - 10, curY, colWidth + 20, 0);
		content.add(freesound);

		content.setSize( colWidth, freesound.bottom()+10 + insets.bottom );

		list.setRect( 0, 0, w, h );
		list.scrollTo(0, 0);

		exitButton = new ExitButton();
		int ofs = 4;
		exitButton.setPos( Camera.main.width - exitButton.width() - ofs, ofs );
		add( exitButton );

		// Setup keyboard navigation
		keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent event) {
				if (!event.pressed) return false;

				GameAction action = KeyBindings.getActionForKey(event);

				// Up/Down for scrolling
				if (action == SPDAction.N || action == SPDAction.NW || action == SPDAction.NE) {
					// Scroll up
					if (scrollPane != null) {
						float currentY = scrollPane.content().camera.scroll.y;
						if (currentY <= 0) {
							// At top, focus exit button
							if (!exitButtonFocused) {
								exitButtonFocused = true;
								exitButton.icon().brightness(1.5f);
							}
						} else {
							// Scroll up
							if (exitButtonFocused) {
								exitButtonFocused = false;
								exitButton.icon().resetColor();
							}
							scrollPane.scrollTo(0, Math.max(0, currentY - SCROLL_SPEED));
						}
					}
					return true;
				} else if (action == SPDAction.S || action == SPDAction.SW || action == SPDAction.SE) {
					// Scroll down
					if (scrollPane != null) {
						float currentY = scrollPane.content().camera.scroll.y;
						float maxScroll = scrollPane.content().height() - scrollPane.height();
						if (currentY >= maxScroll) {
							// At bottom, focus exit button
							if (!exitButtonFocused) {
								exitButtonFocused = true;
								exitButton.icon().brightness(1.5f);
							}
						} else {
							// Scroll down
							if (exitButtonFocused) {
								exitButtonFocused = false;
								exitButton.icon().resetColor();
							}
							scrollPane.scrollTo(0, Math.min(maxScroll, currentY + SCROLL_SPEED));
						}
					}
					return true;
				} else if (event.code == com.badlogic.gdx.Input.Keys.ENTER
						|| event.code == com.badlogic.gdx.Input.Keys.NUMPAD_ENTER) {
					// If exit button is focused, click it
					if (exitButtonFocused) {
						Sample.INSTANCE.play(Assets.Sounds.CLICK);
						onBackPressed();
					}
					return true;
				} else if (action == SPDAction.BACK) {
					onBackPressed();
					return true;
				}
				return false;
			}
		};
		KeyEvent.addKeyListener(keyListener);

		//fadeIn();
	}

	@Override
	public void destroy() {
		if (keyListener != null) {
			KeyEvent.removeKeyListener(keyListener);
			keyListener = null;
		}
		super.destroy();
	}
	
	@Override
	protected void onBackPressed() {
		SacredPixelDungeon.switchScene(TitleScene.class);
	}

	private void addLine( float y, Group content ){
		ColorBlock line = new ColorBlock(Camera.main.width, 1, 0xFF333333);
		line.y = y;
		content.add(line);
	}

	private static class CreditsBlock extends Component {

		boolean large;
		RenderedTextBlock title;
		Image avatar;
		Flare flare;
		RenderedTextBlock body;

		RenderedTextBlock link;
		ColorBlock linkUnderline;
		PointerArea linkButton;

		//many elements can be null, but body is assumed to have content.
		private CreditsBlock(boolean large, int highlight, String title, Image avatar, String body, String linkText, String linkUrl){
			super();

			this.large = large;

			if (title != null) {
				this.title = PixelScene.renderTextBlock(title, large ? 8 : 6);
				if (highlight != -1) this.title.hardlight(highlight);
				add(this.title);
			}

			if (avatar != null){
				this.avatar = avatar;
				add(this.avatar);
			}

			//flare effect removed - caused scroll desync issues

			this.body = PixelScene.renderTextBlock(body, 6);
			if (highlight != -1) this.body.setHightlighting(true, highlight);
			if (large) this.body.align(RenderedTextBlock.CENTER_ALIGN);
			add(this.body);

			if (linkText != null && linkUrl != null){

				int color = 0xFFFFFFFF;
				if (highlight != -1) color = 0xFF000000 | highlight;
				this.linkUnderline = new ColorBlock(1, 1, color);
				add(this.linkUnderline);

				this.link = PixelScene.renderTextBlock(linkText, 6);
				if (highlight != -1) this.link.hardlight(highlight);
				add(this.link);

				linkButton = new PointerArea(0, 0, 0, 0){
					@Override
					protected void onClick( PointerEvent event ) {
						SacredPixelDungeon.platform.openURI( linkUrl );
					}
				};
				add(linkButton);
			}

		}

		@Override
		public void update() {
			super.update();
		}

		@Override
		protected void layout() {
			super.layout();

			float topY = top();

			if (title != null){
				title.maxWidth((int)width());
				title.setPos( x + (width() - title.width())/2f, topY);
				topY += title.height() + (large ? 2 : 1);
			}

			if (large){

				if (avatar != null){
					avatar.x = x + (width()-avatar.width())/2f;
					avatar.y = topY;
					PixelScene.align(avatar);
					topY = avatar.y + avatar.height() + 2;
				}

				body.maxWidth((int)width());
				body.setPos( x + (width() - body.width())/2f, topY);
				topY += body.height() + 2;

			} else {

				float indent = 6;
				if (avatar != null){
					avatar.x = x + indent;
					body.maxWidth((int)(width() - avatar.width - 1 - indent));

					float fullAvHeight = Math.max(avatar.height(), 16);
					if (fullAvHeight > body.height()){
						avatar.y = topY + (fullAvHeight - avatar.height())/2f;
						PixelScene.align(avatar);
						body.setPos( avatar.x + avatar.width() + 1, topY + (fullAvHeight - body.height())/2f);
						topY += fullAvHeight + 1;
					} else {
						avatar.y = topY + (body.height() - fullAvHeight)/2f;
						PixelScene.align(avatar);
						body.setPos( avatar.x + avatar.width() + 1, topY);
						topY += body.height() + 2;
					}

				} else {
					topY += 1;
					body.maxWidth((int)width());
					body.setPos( x, topY);
					topY += body.height()+2;
				}

			}

			if (link != null){
				if (large) topY += 1;
				link.maxWidth((int)width());
				link.setPos( x + (width() - link.width())/2f, topY);
				topY += link.height() + 2;

				linkButton.x = link.left()-1;
				linkButton.y = link.top()-1;
				linkButton.width = link.width()+2;
				linkButton.height = link.height()+2;

				linkUnderline.size(link.width(), PixelScene.align(0.49f));
				linkUnderline.x = link.left();
				linkUnderline.y = link.bottom()+1;

			}

			topY -= 2;

			height = Math.max(height, topY - top());
		}
	}
}
