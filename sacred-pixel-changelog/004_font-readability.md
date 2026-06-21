# 004. RenderedText 가독성 + 오버플로우 해결

**날짜**: 2026-03-10

## 개요

Canvas2D로 렌더링되는 한글 텍스트(RenderedText)가 두껍고 흐릿하며, 설명박스 하단을 넘치는 문제를 해결.
**방안 5(픽셀폰트 + 알파 이진화)** 를 적용하여 BitmapText급 선명도 달성.

---

## 수정된 파일

| 파일 | 모듈 | 변경 내용 |
|------|------|-----------|
| `NeoDunggeunmoPro-Regular.woff2` | html/webapp/fonts | 픽셀 스타일 한글 폰트 추가 |
| `styles.css` | html/webapp | @font-face → NeoDunggeunmoPro |
| `index.html` | html/webapp | FontFace 프리로드 → NeoDunggeunmoPro |
| `FreeTypeFontGenerator.java` | html (super-source) | fontFamily + Nearest 필터 + 글리프 높이 보정 + 알파 이진화 |
| `RenderedTextBlock.java` | core | 하단 패딩 보정 (오버플로우 해결) |

---

## 변경 사항

### 1. 폰트 교체: PretendardStd → NeoDunggeunmoPro

### 파일
- `html/webapp/fonts/NeoDunggeunmoPro-Regular.woff2` (신규)
- `html/webapp/styles.css`
- `html/webapp/index.html`
- `FreeTypeFontGenerator.java` (super-source)

### 문제
PretendardStd(고딕체)는 안티앨리어싱 적용 시 획이 두꺼워지고 흐려져서 BitmapText(pixel_font.png)와 시각적 괴리가 큼.

### 변경

**styles.css:**
```css
/* Before */
@font-face {
    font-family: 'PretendardStd';
    src: url('fonts/PretendardStd-Thin.otf') format('opentype');
}

/* After */
@font-face {
    font-family: 'NeoDunggeunmoPro';
    src: url('fonts/NeoDunggeunmoPro-Regular.woff2') format('woff2');
}
```

**index.html:**
```html
<!-- Before -->
<link rel="preload" href="fonts/PretendardStd-Thin.otf" as="font" type="font/otf" crossorigin>

<!-- After -->
<link rel="preload" href="fonts/NeoDunggeunmoPro-Regular.woff2" as="font" type="font/woff2" crossorigin>
```

```javascript
// Before
var font = new FontFace('PretendardStd', 'url(fonts/PretendardStd-Thin.otf)');

// After
var font = new FontFace('NeoDunggeunmoPro', 'url(fonts/NeoDunggeunmoPro-Regular.woff2)');
```

**FreeTypeFontGenerator.java:**
```java
// Before
fontFamily = "'PretendardStd', 'Noto Sans', sans-serif";

// After
fontFamily = "'NeoDunggeunmoPro', 'Noto Sans', sans-serif";
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 폰트 | PretendardStd-Thin (고딕체, .otf) | NeoDunggeunmoPro-Regular (픽셀폰트, .woff2) |
| 시각적 느낌 | 일반 UI 폰트, BitmapText와 괴리 | 레트로/픽셀아트 분위기, BitmapText와 통일 |
| 파일 크기 | ~150KB (otf) | ~600KB (woff2, 완성형 한글 11,172자) |

---

## 2. 텍스처 필터: Linear → Nearest

### 파일
- `FreeTypeFontGenerator.java` (super-source) — `rebuildTexture()` 메서드

### 문제
`texture.setFilter(Linear, Linear)` → 텍셀이 주변 픽셀과 블렌딩되어 글자가 흐려짐.

### 변경
```java
// Before
texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

// After
texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 텍스처 필터 | Linear (보간) | Nearest (픽셀 정확) |
| 효과 | 글자 테두리가 블러 처리 | 픽셀 단위로 선명한 렌더링 |

---

## 3. 알파 이진화 (AA 완전 제거)

### 파일
- `FreeTypeFontGenerator.java` (super-source) — `jsRenderAllGlyphs()` JSNI 메서드

### 문제
Canvas2D의 `ctx.fillText()`는 기본적으로 서브픽셀 안티앨리어싱을 적용 → 글자 테두리에 반투명 픽셀이 생겨 번짐.

### 변경
```javascript
// 글리프 렌더링 루프 뒤에 추가
// Alpha binarization: remove anti-aliasing
var imageData = ctx.getImageData(0, 0, atlasW, atlasH);
var data = imageData.data;
for (var i = 3; i < data.length; i += 4) {
    data[i] = data[i] > 128 ? 255 : 0;
}
ctx.putImageData(imageData, 0, 0);
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 알파값 | 0~255 연속 (AA 번짐) | 0 또는 255만 (이진) |
| 시각적 효과 | 글자 테두리 반투명 번짐 | BitmapText급 선명한 경계 |
| 성능 | — | 텍스처 빌드 시 1회 처리 (무시 가능) |

---

## 4. 글리프 높이 보정 (오버플로우 근본 해결)

### 파일
- `FreeTypeFontGenerator.java` (super-source) — `CanvasFontData` 클래스

### 문제
`glyph.height = cellH`에서 `cellH = fontSize * 1.5 + pad * 2` → UV 쿼드가 논리 높이(capHeight = fontSize * 0.75)보다 훨씬 커서 텍스트가 설명박스를 넘침.

### 변경
```java
// Before: cellH만 존재
this.cellH = (int)(fontSize * 1.5f) + pad * 2;  // 매우 큰 높이
glyph.height = cellH;
this.lineHeight = cellH;

// After: glyphH 필드 추가, 실제 필요 높이만 사용
this.glyphH = pad + fontSize + (int)Math.ceil(borderWidth) + pad;
glyph.height = glyphH;      // was: cellH
this.lineHeight = glyphH;   // was: cellH
rowH = Math.max(rowH, glyphH);  // was: cellH
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| `glyph.height` | `cellH` = `fontSize*1.5 + pad*2` | `glyphH` = `pad + fontSize + ceil(border) + pad` |
| `lineHeight` | `cellH` (과대) | `glyphH` (정확) |
| atlas rowH | `cellH` | `glyphH` |
| 예시 (size=7, border=0) | cellH = 14, pad=1 | glyphH = 9 (35% 축소) |

---

## 5. 하단 패딩 보정 (레이아웃 오버플로우 해결)

### 파일
- `core/.../ui/RenderedTextBlock.java` — `layout()` 메서드

### 문제
`this.height = (y - this.y) + height` — 디센더, 보더, 행간 여백이 미반영 → 텍스트가 컴포넌트 높이를 초과.

### 변경
```java
// Before
this.height = (y - this.y) + height;

// After
float bottomPad = nLines > 1 ? Math.round(size * 0.5f) : Math.round(size * 0.15f);
this.height = (y - this.y) + height + bottomPad;
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 싱글라인 하단 패딩 | 0 | `size * 0.15` (약 1px) |
| 멀티라인 하단 패딩 | 0 | `size * 0.5` (약 3-4px) |
| 오버플로우 | 텍스트가 설명박스 하단 넘침 | 패딩으로 여유 확보 |

---

## 변경 전후 비교 요약

| 문제 | 원인 | 해결 |
|------|------|------|
| 글씨가 두껍고 흐림 | Canvas2D AA + Linear 필터 | NeoDunggeunmoPro 픽셀폰트 + Nearest 필터 + 알파 이진화 |
| 텍스트 설명박스 넘침 | glyph.height = cellH (과대) + 하단 패딩 없음 | glyphH (정확한 높이) + bottomPad 추가 |
| BitmapText와 시각적 괴리 | 고딕체 + AA | 픽셀폰트 + AA 제거 → BitmapText급 통일감 |
