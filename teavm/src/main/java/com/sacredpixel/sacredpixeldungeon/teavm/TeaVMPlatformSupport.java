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

package com.sacredpixel.sacredpixeldungeon.teavm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.sacredpixel.sacredpixeldungeon.messages.Languages;
import com.sacredpixel.sacredpixeldungeon.messages.Messages;
import com.sacredpixel.sacredpixeldungeon.SPDSettings;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import org.teavm.jso.JSBody;

import java.util.HashMap;

public class TeaVMPlatformSupport extends PlatformSupport {

    @Override
    public void updateDisplaySize() {
        // On HTML5 the canvas size is controlled by the browser/CSS
    }

    @Override
    public boolean supportsFullScreen() {
        return true;
    }

    @Override
    public void updateSystemUI() {
        if (SPDSettings.fullscreen()) {
            jsRequestFullscreen();
        } else {
            jsExitFullscreen();
        }
    }

    @JSBody(script =
        "if (!document.fullscreenElement) {" +
        "  document.documentElement.requestFullscreen().catch(function(e){" +
        "    console.warn('Fullscreen request failed:', e);" +
        "  });" +
        "}")
    private static native void jsRequestFullscreen();

    @JSBody(script =
        "if (document.fullscreenElement) {" +
        "  document.exitFullscreen().catch(function(e){" +
        "    console.warn('Exit fullscreen failed:', e);" +
        "  });" +
        "}")
    private static native void jsExitFullscreen();

    @Override
    public boolean connectedToUnmeteredNetwork() {
        return true; // assume unmetered in browser
    }

    @Override
    public boolean supportsVibration() {
        return false;
    }

    /* FONT SUPPORT
     *
     * TeaVM/HTML5 uses Canvas 2D for font rendering, not real FreeType.
     * Each FreeTypeFontGenerator holds a CSS font-family string used by Canvas.
     * The BROWSER handles per-character font fallback via CSS font-family stacking.
     *
     * Architecture: single generator with CSS font-family stack
     *   mainGenerator — language-aware font priority order
     *   Browser handles per-character font fallback automatically
     */

    private static FreeTypeFontGenerator mainGenerator;

    // Build a CSS font-family string with language-aware CJK ordering.
    // The browser's Canvas automatically picks the first font that has each glyph.
    //
    // Principle: [language-specific font] → [SC as wide CJK fallback] → [remaining]
    // SC (Simplified Chinese) has the widest Han coverage among Noto Sans CJK variants.
    // Characters like 语(U+8BED), 设(U+8BBE) only exist properly in SC.
    // Putting KR/JP before SC would cause these to render incorrectly or as tofu.
    private static String buildFontFamily() {
        String latin = "'Inter', 'NotoSans'";

        String cjk;
        Languages lang = Messages.lang();
        if (lang == Languages.JAPANESE) {
            // JP first for JP-specific glyphs, SC fallback for wide Han coverage
            cjk = "'NotoSansJP', 'NotoSansSC', 'NotoSansTC', 'NotoSansKR'";
        } else if (lang == Languages.KOREAN) {
            // KR first for Hangul + Korean hanja, SC fallback for missing hanja
            cjk = "'NotoSansKR', 'NotoSansSC', 'NotoSansTC', 'NotoSansJP'";
        } else if (lang == Languages.CHI_TRAD) {
            // TC first, SC as close fallback for shared CJK
            cjk = "'NotoSansTC', 'NotoSansSC', 'NotoSansJP', 'NotoSansKR'";
        } else if (lang == Languages.CHI_SMPL) {
            // SC first — widest simplified Han coverage
            cjk = "'NotoSansSC', 'NotoSansTC', 'NotoSansJP', 'NotoSansKR'";
        } else {
            // Default (non-CJK languages): SC first for widest Han coverage
            cjk = "'NotoSansSC', 'NotoSansKR', 'NotoSansJP', 'NotoSansTC'";
        }

        return latin + ", " + cjk + ", sans-serif";
    }

    @Override
    public void resetGenerators(boolean setupAfter) {
        mainGenerator = null;
        super.resetGenerators(setupAfter);
    }

    @Override
    public void setupFontGenerators(int pageSize, boolean systemfont) {
        if (fonts != null && mainGenerator != null
                && this.pageSize == pageSize && this.systemfont == systemfont) {
            return;
        }
        this.pageSize = pageSize;
        this.systemfont = systemfont;

        resetGenerators(false);
        fonts = new HashMap<>();

        mainGenerator = new FreeTypeFontGenerator(buildFontFamily());
        fonts.put(mainGenerator, new HashMap<>());

        packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
    }

    @Override
    protected FreeTypeFontGenerator getGeneratorForString(String input) {
        // All text uses the main generator — CSS font-family handles per-glyph fallback
        return mainGenerator;
    }

    private static boolean isCJK(char c) {
        return (c >= '\u3040' && c <= '\u309F')   // Hiragana
            || (c >= '\u30A0' && c <= '\u30FF')   // Katakana
            || (c >= '\u4E00' && c <= '\u9FFF')   // CJK Unified Ideographs
            || (c >= '\u3400' && c <= '\u4DBF')   // CJK Unified Ideographs Extension A
            || (c >= '\uF900' && c <= '\uFAFF')   // CJK Compatibility Ideographs
            || (c >= '\u3000' && c <= '\u303F');   // CJK Symbols and Punctuation
    }

    private static boolean isSplitPoint(char c, boolean multiline) {
        if (c == '\n' || c == '_') return true;
        if (multiline && c == ' ') return true;
        return isCJK(c);
    }

    @Override
    public String[] splitforTextBlock(String text, boolean multiline) {
        java.util.ArrayList<String> parts = new java.util.ArrayList<>();
        int start = 0;
        int len = text.length();

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);

            if (c == '*' && i + 1 < len && text.charAt(i + 1) == '*') {
                if (i > start) parts.add(text.substring(start, i));
                parts.add("**");
                i++;
                start = i + 1;
                continue;
            }

            if (isSplitPoint(c, multiline)) {
                if (i > start) parts.add(text.substring(start, i));
                parts.add(text.substring(i, i + 1));
                start = i + 1;
            }
        }

        if (start < len) parts.add(text.substring(start));

        return parts.toArray(new String[0]);
    }
}
