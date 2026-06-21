# 002. HTML5 폰트 시스템 초기 시도 (롤백됨)

**날짜**: 2026-03-08

## 개요

HTML5 폰트 시스템 초기 개선 시도. systemFont 설정 연동, 글리프 렌더링 높이 축소, Canvas2D 폰트 품질 개선, RenderedTextBlock 높이 보정, AboutScene 1열 레이아웃, 검은 여백 제거 등. (이후 롤백됨)

---

## 수정된 파일

| 파일 | 모듈 | 변경 내용 |
|------|------|-----------|
| `FreeTypeFontGenerator.java` (super-source) | html | 폰트 렌더링 전면 개선 |
| `HtmlPlatformSupport.java` | html | systemFont 설정 연동 |
| `RenderedTextBlock.java` | core | 텍스트 블록 높이 계산 보정 |
| `AboutScene.java` | core | 소개 화면 1열 레이아웃 |
| `index.html` | html/webapp | 캔버스 핸들러 정리 |
| `styles.css` | html/webapp | 캔버스 전체화면, 검은 여백 제거 |

---

## 변경 사항

### 1. systemFont 설정 HTML5 연동

### 파일
- `html/.../super/.../FreeTypeFontGenerator.java` (line 23-24)
- `html/.../HtmlPlatformSupport.java` (line 80-82)

### 내용
`SPDSettings.systemFont()` 설정이 HTML5에서 무시되던 문제 수정.

- GWT super-source 제약으로 static 필드 사용 불가 → 파일명 컨벤션으로 우회
- `systemfont=true` 시 `"fonts/pretendard_systemfont.otf"` 이름으로 생성자 호출
- 생성자에서 파일명에 `"systemfont"` 포함 여부로 분기:
  - `true`: `system-ui, -apple-system, 'Segoe UI', Roboto, 'PretendardStd', 'Noto Sans', sans-serif`
  - `false`: `'PretendardStd', 'Noto Sans', sans-serif` (기존과 동일)
- CJK 언어(한/중/일)는 기본 `systemFont=true` → 시스템 CJK 폰트 우선, PretendardStd fallback

---

## 2. 글리프 렌더링 높이 축소 (텍스트 오버플로우 수정)

### 파일
- `html/.../super/.../FreeTypeFontGenerator.java` (CanvasFontData 클래스)

### 문제
`glyph.height = cellH`로 설정되어, 실제 텍스처 쿼드가 레이아웃 높이보다 훨씬 큼.

| fontSize | cellH (변경 전) | glyphH (변경 후) | layout height | 오버플로우 |
|:---:|:---:|:---:|:---:|:---:|
| 6 (border) | 13px | 9px | 5px | 8px → 4px |
| 9 (no border) | 15px | 10px | 7px | 8px → 3px |

### 변경
```java
// 새로 추가
this.glyphH = pad + fontSize + (int) Math.ceil(borderWidth);

// 변경
glyph.height = glyphH;  // was: cellH
```
- `cellH`는 아틀라스 행 간격으로 유지 (텍스처 bleed 방지)
- `glyphH`는 UV 영역 전용 (실제 렌더링 쿼드 크기)

---

## 3. Canvas2D 폰트 렌더링 품질 개선

### 파일
- `html/.../super/.../FreeTypeFontGenerator.java` (jsRenderAllGlyphs, jsMeasureChar)

### 변경
```javascript
// 변경 전
ctx.font = fontSize + 'px ' + fontFamily;

// 변경 후
ctx.font = '200 ' + fontSize + 'px ' + fontFamily;
ctx.textRendering = 'geometricPrecision';
```

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| font-weight | 미지정 (기본 400/normal) | **200** (Extra Light) |
| textRendering | 미지정 | **geometricPrecision** |

- PretendardStd-**Thin** 폰트에 맞는 가벼운 굵기 적용
- `geometricPrecision`으로 글자 형태 정밀도 우선 렌더링
- Canvas2D에는 ClearType/서브픽셀 렌더링 불가, 이것이 현재 가능한 최선

---

## 4. RenderedTextBlock 높이 계산 보정

### 파일
- `core/.../ui/RenderedTextBlock.java` (line 265-268)

### 문제
`this.height = (y - this.y) + height`에서 `height = Math.round(size * 0.75f)` (capHeight만 반영).
글리프 디센더(g, p, y 꼬리)와 보더 영역이 미포함 → 텍스트가 컨테이너 아래로 넘침.

### 변경
```java
// nLines 기반 차등 패딩
float bottomPad = nLines > 1 ? Math.round(size * 0.5f) : Math.round(size * 0.15f);
this.height = (y - this.y) + height + bottomPad;
```

| 용도 | nLines | size | 패딩 | 효과 |
|------|:---:|:---:|:---:|------|
| 버튼 텍스트 | 1 | 9 | 1px | 수직 중앙 정렬 유지 |
| 제목 | 1 | 8-12 | 1-2px | 과도한 하단 여백 방지 |
| 설명 본문 | 3+ | 6 | 3px | 디센더 + 하단 여유 확보 |

---

## 5. 소개 화면 (AboutScene) 1열 레이아웃

### 파일
- `core/.../scenes/AboutScene.java`

### 문제
Portrait 모드에서 크레딧 블록이 `colWidth/2` 너비로 2개씩 한 줄에 배치 → 좁은 화면에서 겹침/가독성 저하.

### 변경
Portrait 모드에서:
- 블록 너비: `colWidth/2` → `colWidth` (전체 폭)
- 배치: 가로 나란히 → 세로 스택 (`bottom() + 5`)
- 영향 블록: alex/celesti, kristjan, arcnor/purigro, cube
- Landscape 모드는 기존 2열 유지

---

## 6. 검은 여백 제거 (캔버스 전체화면)

### 파일
- `html/webapp/styles.css`
- `html/webapp/index.html`

### 문제
LibGDX GWT가 생성하는 래퍼 요소(table, div)가 뷰포트를 채우지 않아 우측/하단에 검은 여백 발생.

### 변경 (CSS)
```css
#embed-html {
    position: fixed;
    top: 0; left: 0;
    width: 100vw;
    height: 100dvh;
}

#embed-html table, #embed-html td, #embed-html div {
    width: 100% !important;
    height: 100% !important;
}

canvas {
    position: fixed !important;
    top: 0 !important; left: 0 !important;
    width: 100vw !important;
    height: 100dvh !important;
}
```

### 변경 (JS)
- 이전에 추가했던 `resizeCanvas()` JS 핸들러 제거 (LibGDX 내부 해상도 관리와 충돌)
- 캔버스 크기는 CSS `position: fixed + 100vw/dvh`로 처리
- LibGDX가 내부 해상도(canvas.width/height 속성)를 관리

---

## 수치 요약

### 글리프 오버플로우 개선 (fontSize=9, border=0)

| 항목 | 원본 | 최종 |
|------|:---:|:---:|
| glyph.height (렌더링 쿼드) | 15px | 10px |
| RenderedTextBlock 보정 | 0px | +1px (1줄) / +3px (여러줄) |
| 오버플로우 | **8px** | **2px** |

### Canvas2D 폰트 설정

```
ctx.font = '200 {size}px PretendardStd, Noto Sans, sans-serif'
ctx.textBaseline = 'top'
ctx.textRendering = 'geometricPrecision'
```
