package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import org.teavm.jso.JSBody;

public class FreeTypeFontGenerator {

    public enum Hinting {
        None, Slight, Medium, Full, AutoSlight, AutoMedium, AutoFull
    }

    private String fontFamily;
    private boolean useSystemFont = false;
    private final java.util.ArrayList<CanvasFontData> myFontData = new java.util.ArrayList<>();

    // Constructor for file-based font (legacy)
    public FreeTypeFontGenerator(FileHandle fontFile) {
        String name = fontFile.name().toLowerCase();
        if (name.contains("pixel")) {
            fontFamily = "'Courier New', monospace";
        } else {
            fontFamily = "sans-serif";
        }
    }

    // Constructor with explicit CSS font-family string
    public FreeTypeFontGenerator(String cssFontFamily) {
        this.fontFamily = cssFontFamily;
    }

    public BitmapFont generateFont(FreeTypeFontParameter parameter) {
        int size = Math.max(6, parameter.size);
        boolean flip = parameter.flip;
        float borderWidth = parameter.borderWidth;

        CanvasFontData fontData = new CanvasFontData(size, flip, borderWidth, fontFamily);
        myFontData.add(fontData);

        // Batch mode: suppress per-glyph rebuilds during bulk pre-rendering
        fontData.batchMode = true;

        // Pre-render ASCII printable characters
        for (int i = 32; i < 127; i++) {
            fontData.ensureGlyph((char) i);
        }

        // Pre-render common Korean Hangul characters
        String commonKorean = "가각간갈감갑강개거건걸검게겐격견결경계고곡곤골공과관광괴교구국군굴권궁귀규균그극근글금급기긴길김깊까깡꺼꽃끄끌끝나난날남납낭내너널넓네녀년념노논놀높농놓누눈눌능니다단달담답당대더던덜덤덩데도독돈돌동두둘둥뒤드든들듬등디따딱땅때떠떤떨떻또뚫뜨뜻라란랄람량러런럽레력련렬령로론롭료루류르른를름릴림립링마만많말맑맛망매맥먹먼멀멋멍메며면멸명모목몬몸몽무묘문물뭐므미민밀밝밤밥방배백번벌범법벗변별병보복본볼봉부북분불붉비빈빌빛빠빨빠사삭산살삼상새색생서석선설섬섭성세소속손솔송수숙순술숨숲쉬스슬습승시식신실싸쌓아악안알암압앙애앞야약양어억언얻얼엄업없에여역연열염엽영예오온올옮완왕외요용우운울원월위유육율은을음읍의이인일읽임입있자작잔잠잡장재전절점접정제조족존졸종주죄주준줄중즈즉증지직진질짐집징차찬찰참창찾채처천철청체초총추축출춤충취츠측치침칠카칸큰킬타탈탑탄태택터턱테토통투특틀파판팔패팽펴편평폐포폭표푸풀품피필하한할합항해핵행향허헌험혁현혈형혜호혹혼홀화확환활황회횟효후훈훌훨휘흐흑흔흘흥히힘";
        for (int i = 0; i < commonKorean.length(); i++) {
            fontData.ensureGlyph(commonKorean.charAt(i));
        }

        // End batch mode, rebuild texture once for all pre-rendered glyphs
        fontData.batchMode = false;
        fontData.rebuildTexture();

        BitmapFont font = new BitmapFont(fontData, fontData.getRegions(), false);
        return font;
    }

    public void dispose() {
        //Remove only THIS generator's CanvasFontData instances from tracking.
        //Other generators' fonts remain tracked for context-loss invalidation.
        allFontData.removeAll(myFontData);
        myFontData.clear();
    }

    public static class FreeTypeFontParameter {
        public int size = 16;
        public boolean flip = false;
        public float borderWidth = 0;
        public int renderCount = 2;
        public Hinting hinting = Hinting.None;
        public int spaceX = 0;
        public boolean incremental = false;
        public String characters = "";
        public PixmapPacker packer = null;
    }

    //All live CanvasFontData instances, so we can invalidate and rebuild them
    //after a WebGL context loss (their textures are not in TextureCache).
    private static final java.util.ArrayList<CanvasFontData> allFontData = new java.util.ArrayList<>();

    /**
     * Invalidate all font atlas textures and force a full re-render + re-upload.
     * Called after WebGL context loss/restore to recover font rendering.
     */
    public static void invalidateAllFontAtlases() {
        for (CanvasFontData fd : new java.util.ArrayList<>(allFontData)) {
            fd.invalidateTexture();
        }
    }

    static class CanvasFontData extends BitmapFont.BitmapFontData {

        private int fontSize;
        private float borderWidth;
        private String fontFamily;
        private int pad;

        private int atlasW = 512;
        private int atlasH = 512;
        private int cursorX = 0;
        private int cursorY = 0;
        private int rowH = 0;
        private int glyphH;

        private java.util.HashSet<Character> renderedChars = new java.util.HashSet<>();

        private Texture texture;
        private Array<TextureRegion> regions = new Array<>();
        private boolean needsRebuild = false;
        private boolean inGetGlyph = false;
        boolean batchMode = false;

        CanvasFontData(int fontSize, boolean flip, float borderWidth, String fontFamily) {
            this.fontSize = fontSize;
            this.borderWidth = borderWidth;
            this.fontFamily = fontFamily;
            this.pad = (int) Math.ceil(borderWidth) + 2;
            this.glyphH = pad + fontSize + pad + (int)Math.ceil(borderWidth) + 1;

            //Register for context-loss invalidation
            allFontData.add(this);

            jsMeasureFontMetrics(fontSize, fontFamily);
            int measuredAscent = jsFontAscent();
            int measuredDescent = jsFontDescent();

            this.flipped = flip;
            this.lineHeight = glyphH;
            this.capHeight = measuredAscent;
            // Compensate for top padding in glyph atlas.
            // Canvas renders with textBaseline='top', adding `pad` pixels of blank
            // space above the actual text content. Negative ascent shifts text up.
            this.ascent = -pad;
            this.descent = measuredDescent;
            this.down = flip ? lineHeight : -lineHeight;
            this.scaleX = 1f;
            this.scaleY = 1f;
            this.xHeight = measuredAscent * 0.65f;
            this.imagePaths = new String[]{""};

            regions.add(new TextureRegion());
        }

        void ensureGlyph(char c) {
            if (renderedChars.contains(c)) return;
            renderedChars.add(c);

            int charW = jsMeasureChar(String.valueOf(c), fontSize, fontFamily);
            int glyphW = charW + pad * 2 + 2;

            if (cursorX + glyphW > atlasW) {
                cursorX = 0;
                cursorY += rowH;
                rowH = 0;
            }

            if (cursorY + glyphH > atlasH) {
                atlasH = nextPow2(cursorY + glyphH);
            }

            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.id = c;
            glyph.srcX = cursorX;
            glyph.srcY = cursorY;
            glyph.width = glyphW;
            glyph.height = glyphH;
            glyph.xoffset = 0;
            glyph.yoffset = 0;
            glyph.xadvance = charW + (int) borderWidth;
            glyph.page = 0;

            setGlyph(c, glyph);

            if (c == ' ') {
                spaceXadvance = (int)(glyph.xadvance * 0.75f);
            }

            cursorX += glyphW;
            rowH = Math.max(rowH, glyphH);
            needsRebuild = true;
        }

        @Override
        public BitmapFont.Glyph getGlyph(char ch) {
            BitmapFont.Glyph glyph = super.getGlyph(ch);
            if (glyph == null && ch != 0) {
                ensureGlyph(ch);
                glyph = super.getGlyph(ch);
            }
            if (needsRebuild && !batchMode && !inGetGlyph) {
                inGetGlyph = true;
                rebuildTexture();
                inGetGlyph = false;
            }
            return glyph;
        }

        void rebuildTexture() {
            if (!needsRebuild && texture != null) return;
            needsRebuild = false;

            StringBuilder sb = new StringBuilder();
            int[] srcXs = new int[renderedChars.size()];
            int[] srcYs = new int[renderedChars.size()];
            int[] glyphWs = new int[renderedChars.size()];
            int idx = 0;
            for (char c : renderedChars) {
                sb.append(c);
                BitmapFont.Glyph g = super.getGlyph(c);
                if (g != null) {
                    srcXs[idx] = g.srcX;
                    srcYs[idx] = g.srcY;
                    glyphWs[idx] = g.width;
                }
                idx++;
            }

            jsRenderGlyphs(sb.toString(), srcXs, srcYs, glyphWs,
                    fontSize, fontFamily, borderWidth, pad, atlasW, atlasH, glyphH);

            // Recreate texture if atlas size changed
            if (texture != null && (texture.getWidth() != atlasW || texture.getHeight() != atlasH)) {
                texture.dispose();
                texture = null;
            }

            if (texture == null) {
                Pixmap blank = new Pixmap(atlasW, atlasH, Pixmap.Format.RGBA8888);
                texture = new Texture(blank);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                blank.dispose();
            }

            texture.bind();
            jsUploadCanvasToTexture(atlasW, atlasH);

            TextureRegion newRegion = new TextureRegion(texture);
            if (regions.size == 0) {
                regions.add(newRegion);
            } else {
                regions.set(0, newRegion);
            }

            TextureRegion r = regions.get(0);
            for (BitmapFont.Glyph[] page : glyphs) {
                if (page == null) continue;
                for (BitmapFont.Glyph glyph : page) {
                    if (glyph != null) {
                        setGlyphRegion(glyph, r);
                    }
                }
            }
            if (missingGlyph != null) {
                setGlyphRegion(missingGlyph, r);
            }
        }

        /**
         * Invalidate the GL texture after context loss. The old texture ID is
         * no longer valid — dispose it and force a full re-render + re-upload
         * of all glyphs on the next access.
         */
        void invalidateTexture() {
            if (texture != null) {
                //Don't call texture.dispose() — the GL texture name is already
                //invalid after context loss, and calling glDeleteTexture on it
                //would be a no-op or error. Just drop the reference.
                texture = null;
            }
            needsRebuild = true;
        }

        Array<TextureRegion> getRegions() {
            return regions;
        }

        // Renders all glyphs to an offscreen canvas. No binarization — all fonts
        // are anti-aliased sans-serif (Inter, Noto Sans family).
        @JSBody(params = {"chars", "srcXs", "srcYs", "glyphWs",
                "fontSize", "fontFamily", "borderWidth", "pad", "atlasW", "atlasH", "glyphH"},
            script =
            "if (!window.__spd_fc || window.__spd_fc.width !== atlasW || window.__spd_fc.height !== atlasH) {" +
            "  window.__spd_fc = document.createElement('canvas');" +
            "  window.__spd_fc.width = atlasW;" +
            "  window.__spd_fc.height = atlasH;" +
            "  window.__spd_fctx = window.__spd_fc.getContext('2d', {willReadFrequently: true});" +
            "}" +
            "var canvas = window.__spd_fc;" +
            "var ctx = window.__spd_fctx;" +
            "if (!ctx) return;" +
            "ctx.clearRect(0, 0, atlasW, atlasH);" +
            "ctx.globalCompositeOperation = 'source-over';" +
            "ctx.textBaseline = 'top';" +
            "ctx.font = '400 ' + fontSize + 'px ' + fontFamily;" +
            "ctx.fillStyle = 'rgba(255,255,255,1)';" +
            "if (borderWidth > 0) {" +
            "  ctx.strokeStyle = 'rgba(0,0,0,1)';" +
            "  ctx.lineJoin = 'round';" +
            "  ctx.lineWidth = Math.max(1, Math.round(borderWidth * 2));" +
            "}" +
            "for (var i = 0; i < chars.length; i++) {" +
            "  var x = srcXs[i] + pad;" +
            "  var y = srcYs[i] + pad;" +
            "  var ch = chars.charAt(i);" +
            "  if (borderWidth > 0) ctx.strokeText(ch, x, y);" +
            "  ctx.fillText(ch, x, y);" +
            "}")
        private static native void jsRenderGlyphs(String chars,
                int[] srcXs, int[] srcYs, int[] glyphWs,
                int fontSize, String fontFamily,
                float borderWidth, int pad, int atlasW, int atlasH, int glyphH);

        @JSBody(params = {"atlasW", "atlasH"}, script =
            "var fc = window.__spd_fc;" +
            "if (!fc) return;" +
            "var gameCanvas = document.getElementById('canvas');" +
            "if (!gameCanvas) return;" +
            "var gl = gameCanvas.getContext('webgl2') || gameCanvas.getContext('webgl');" +
            "if (!gl) return;" +
            "gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, fc);" +
            "gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);" +
            "gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);")
        private static native void jsUploadCanvasToTexture(int atlasW, int atlasH);

        @JSBody(params = {"ch", "fontSize", "fontFamily"}, script =
            "if (!window.__spd_mc) {" +
            "  window.__spd_mc = document.createElement('canvas');" +
            "  window.__spd_mc.width = 1;" +
            "  window.__spd_mc.height = 1;" +
            "  window.__spd_mctx = window.__spd_mc.getContext('2d');" +
            "}" +
            "window.__spd_mctx.font = '400 ' + fontSize + 'px ' + fontFamily;" +
            "return Math.ceil(window.__spd_mctx.measureText(ch).width);")
        private static native int jsMeasureChar(String ch, int fontSize, String fontFamily);

        @JSBody(params = {"fontSize", "fontFamily"}, script =
            "if (!window.__spd_mc) {" +
            "  window.__spd_mc = document.createElement('canvas');" +
            "  window.__spd_mc.width = 1;" +
            "  window.__spd_mc.height = 1;" +
            "  window.__spd_mctx = window.__spd_mc.getContext('2d');" +
            "}" +
            "var ctx = window.__spd_mctx;" +
            "ctx.font = '400 ' + fontSize + 'px ' + fontFamily;" +
            "var metrics = ctx.measureText('Hg');" +
            "if (typeof metrics.actualBoundingBoxAscent === 'number') {" +
            "  window.__spd_fontAscent = Math.ceil(metrics.actualBoundingBoxAscent);" +
            "  window.__spd_fontDescent = Math.ceil(metrics.actualBoundingBoxDescent);" +
            "} else {" +
            "  window.__spd_fontAscent = Math.ceil(fontSize * 0.75);" +
            "  window.__spd_fontDescent = Math.ceil(fontSize * 0.25);" +
            "}")
        private static native void jsMeasureFontMetrics(int fontSize, String fontFamily);

        @JSBody(script = "return window.__spd_fontAscent || 0;")
        private static native int jsFontAscent();

        @JSBody(script = "return window.__spd_fontDescent || 0;")
        private static native int jsFontDescent();

        private static int nextPow2(int v) {
            v--;
            v |= v >> 1;
            v |= v >> 2;
            v |= v >> 4;
            v |= v >> 8;
            v |= v >> 16;
            return v + 1;
        }
    }
}
