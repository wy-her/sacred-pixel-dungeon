# 020. 폰트 렌더링 폴리시 — 검은 점, 세로 정렬, 빌드 파이프라인

**날짜**: 2026-03-25

## 개요

버튼 아래 검은 점 제거, 텍스트 세로 정렬 수정, NeoDunggeunmoPro 완전 제거, 빌드 파이프라인 수정.

---

## 변경 사항

### 1. 버튼 아래 검은 점 (Black Dot Artifacts)

**증상:** 버튼 텍스트 아래에 검은 점(1-2px)이 나타남. 특히 bordered 텍스트(Test Zone, Vault Level 등)에서 발생.

**원인:** `jsRenderGlyphs`에서 `ctx.strokeText()`의 border anti-aliasing 픽셀이 글리프 영역의 패딩(pad)을 넘어감. `lineWidth = borderWidth * 2`의 stroke가 `pad = ceil(borderWidth) + 1`보다 커서 경계를 넘음.

**수정:** `FreeTypeFontGenerator.java`
```java
// Before
this.pad = (int) Math.ceil(borderWidth) + 1;

// After
this.pad = (int) Math.ceil(borderWidth) + 2;
```

### 2. 버튼 텍스트 세로 정렬

**증상:** 모든 버튼에서 텍스트가 세로 방향으로 약간 아래쪽에 치우침.

**원인:** Canvas `textBaseline='top'`으로 렌더링 시, 글리프 이미지 상단에 `pad` 픽셀의 빈 공간이 포함됨. 기존 `ascent` 계산식 `-(fontSize - measuredAscent - measuredDescent) * 0.5f`가 이 패딩을 정확히 보상하지 못함.

**수정:** `FreeTypeFontGenerator.java`
```java
// Before
this.ascent = -(fontSize - measuredAscent - measuredDescent) * 0.5f;

// After — Canvas top padding을 정확히 보상
this.ascent = -pad;
```

StyledButton의 세로 중앙 정렬 공식 `y + (height() - text.height()) / 2f`에서 `text.height()` = capHeight이고, `ascent = -pad`가 렌더링 시 텍스트를 pad만큼 위로 올려서 시각적 중앙에 맞춤.

### 3. NeoDunggeunmoPro 완전 제거

폰트 파일 삭제 및 모든 소스코드 참조 제거:

| 위치 | 변경 |
|------|------|
| `core/.../fonts/NeoDunggeunmoPro-Regular.woff2` | 삭제 |
| `teavm/webapp/fonts/NeoDunggeunmoPro-Regular.woff2` | 삭제 |
| `teavm/webapp/styles.css` | @font-face 삭제 |
| `teavm/webapp/index.html` | preload, FontFace API 삭제 |
| `FreeTypeFontGenerator.java` | 생성자에서 neodunggeunmo 분기 삭제 |
| `TeaVMPlatformSupport.java` | pixelGenerator 제거, font stack에서 NeoDunggeunmoPro 삭제 |
| `AboutScene.java` | 폰트 크레딧 섹션 삭제 |
| `SPDSettings.java` | 주석 업데이트 |

binarization(알파 이진화) 로직도 전면 삭제 — 모든 폰트가 안티앨리어싱 sans-serif이므로 불필요.

### 4. 빌드 파이프라인 수정

**문제:** `runDebug`의 `doFirst` 블록에서 커스텀 webapp 파일을 복사하지만, TeaVM 컴파일러가 나중에 기본 `index.html`(33줄)을 생성하여 덮어씀. 결과적으로 커스텀 index.html(293줄, 폰트 프리로드, min-height 등)이 무시됨.

**수정:** `TeaVMBuilder.java`에 `copyWebappFiles()` 메서드 추가 — `compiler.build()` 완료 후, Jetty 시작 전에 커스텀 webapp 파일을 복사. music preload 비활성화도 여기서 처리.

### 5. 미사용 폰트 파일 정리

9개 파일 삭제 (~64MB 절약):
- Latin-only 서브셋 5개: `inter-v20-latin-regular.woff2`, `noto-sans-*-latin-regular.woff2` (4개)
- noto-cjk OTF 2개: `noto-sans-sc.otf`, `noto-sans-tc.otf`
- webapp OTF 복사본 2개

### 6. 세로 최소 길이 시도 및 철회

CSS `min-height`와 JavaScript `enforceMinCanvasSize()`를 시도했으나:
- `position: fixed` + `height: 100dvh`가 min-height를 무시
- LibGDX가 JavaScript에서 canvas 크기를 직접 제어하여 CSS로 제어 불가
- 브라우저 창 크기는 OS/브라우저 영역

→ 세로 최소 길이 관련 코드 모두 제거.

## Final Font Architecture

```
mainGenerator (single) — CSS font-family stack:
  'Inter', 'NotoSans', [language-aware CJK], sans-serif

Canvas rendering:
  ctx.font = "400 12px 'Inter', 'NotoSans', 'NotoSansSC', ..."
  ctx.textBaseline = 'top'
  No binarization — all anti-aliased

Font metrics:
  capHeight = measuredAscent (from Canvas measureText)
  ascent = -pad (compensates for Canvas top padding)
  descent = measuredDescent
  lineHeight = glyphH (pad + fontSize + borderWidth + pad)
```

---

## 수정된 파일

| File | Changes |
|------|--------|
| `teavm/.../FreeTypeFontGenerator.java` | pad 증가, ascent=-pad, binarization 삭제 |
| `teavm/.../TeaVMPlatformSupport.java` | pixelGenerator 제거, 단일 mainGenerator |
| `teavm/.../TeaVMBuilder.java` | copyWebappFiles() 추가 |
| `SPD-classes/.../RenderedText.java` | height = capHeight 유지 (lineHeight 시도 후 원복) |
| `core/.../AboutScene.java` | 폰트 크레딧 삭제 |
| `core/.../SPDSettings.java` | 주석 업데이트 |
| `teavm/webapp/styles.css` | NeoDunggeunmoPro 삭제, min-height 삭제 |
| `teavm/webapp/index.html` | NeoDunggeunmoPro 삭제, min-height/JS 삭제 |
