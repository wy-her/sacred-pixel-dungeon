# 017. 다국어 폰트 시스템 개편 (1부)

**날짜**: 2026-03-25

## 개요

기본 폰트 시스템을 단일 NeoDunggeunmoPro에서 7개 폰트 패밀리 스택으로 전환하고, CJK(한자/히라가나/가타카나) 렌더링이 정상 동작하도록 근본적인 아키텍처 변경을 수행함.

---

## 변경 사항

### Font Family Priority Order

1. **Inter** (Latin, Cyrillic, Greek, Vietnamese) — 342KB full version
2. **Noto Sans** (Latin extended) — 13KB
3. **Noto Sans KR** (Korean) — 3.8MB full Hangul + Hanja
4. **Noto Sans JP** (Japanese) — 11MB full CJK + Kana
5. **Noto Sans TC** (Traditional Chinese) — 16MB full
6. **Noto Sans SC** (Simplified Chinese) — 16MB full
7. **NeoDunggeunmoPro** (pixel font fallback) — 38KB

### 1. Font Files (Latin-only → Full CJK)

기존 13-14KB Latin-only 서브셋 폰트를 전체 글리프를 포함하는 풀 버전으로 교체:

| Font | Before | After |
|------|--------|-------|
| Inter | 24KB (Latin only) | 342KB (Latin+Cyrillic+Greek+Vietnamese) |
| Noto Sans KR | 14KB (Latin only) | 3.8MB (full Korean) |
| Noto Sans JP | 13KB (Latin only) | 11MB (full Japanese) |
| Noto Sans TC | 13KB (Latin only) | 16MB (full Traditional Chinese) |
| Noto Sans SC | 13KB (Latin only) | 16MB (full Simplified Chinese) |

### 2. TeaVM FreeTypeFontGenerator Architecture (핵심 변경)

**문제:** TeaVM의 `FreeTypeFontGenerator`는 실제 FreeType이 아닌 HTML5 Canvas 2D로 폰트를 렌더링하는 스텁. 기존에는 모든 폰트 파일에 대해 동일한 CSS font-family(`'NeoDunggeunmoPro', 'Noto Sans', sans-serif`)를 사용하여, 커스텀 폰트가 실제로 사용되지 않았음.

**해결:**

- `FreeTypeFontGenerator` 생성자에 CSS font-family 문자열을 직접 전달하는 오버로드 추가
- `TeaVMPlatformSupport`를 7개 개별 제너레이터 → **2개 제너레이터**(mainGenerator + pixelGenerator)로 단순화
- mainGenerator가 **CSS font-family 스택**으로 모든 스크립트를 처리 — 브라우저의 CSS 폰트 폴백이 글리프별로 최적 폰트 자동 선택
- 언어별 CJK 폰트 우선순위를 `buildFontFamily()`로 동적 생성 (일본어 선택 시 NotoSansJP가 먼저, 한국어 시 NotoSansKR이 먼저)

### 3. CSS @font-face & Font Preloading

- `webapp/styles.css` — 7개 폰트 모두 `@font-face`로 브라우저에 등록
- `webapp/index.html` — FontFace API로 7개 폰트 비동기 프리로드 + `document.fonts.ready` 대기 후 게임 시작
- `webapp/fonts/` — 풀 CJK 폰트 파일 배포

### 4. Atlas Texture Resize Bug Fix (핵심 버그 수정)

**문제:** 초기 512x512 아틀라스에 ASCII+한글 사전렌더링 후, CJK 글리프가 추가되면 아틀라스가 1024+ 높이로 커지지만, GL 텍스처는 512x512로 고정. UV 좌표가 텍스처 범위를 벗어나 CJK 글리프가 보이지 않음.

**해결:** `rebuildTexture()`에서 `atlasW`/`atlasH`가 기존 텍스처 크기와 다르면 텍스처를 재생성하도록 수정.

### 5. Glyph Rebuild Timing Fix

**문제:** `rebuildTextureThrottled()`가 프레임당 1회만 리빌드. `measure()` 중 100개 CJK 글리프 추가 시 첫 1개만 텍스처에 렌더링되고 나머지 99개는 빈 칸으로 캐시. `draw()`는 `getGlyph()`을 다시 호출하지 않아 리빌드 트리거 불가.

**해결:**
- 프레임 스로틀 제거, `needsRebuild` 시 `getGlyph()` 호출마다 즉시 리빌드
- `batchMode` 플래그 도입: `generateFont()`의 대량 사전렌더링 중에는 리빌드 억제, 완료 후 1회 리빌드

### 6. Generator Reset Safety

- `TeaVMPlatformSupport.resetGenerators()` 오버라이드: dispose 전에 static 참조를 null로 초기화
- `setupFontGenerators()`에 `mainGenerator != null` 체크 추가하여 disposed 제너레이터 감지
- `getFont()`에 null 안전성 가드 추가

### 7. Misc Fixes

- `desktop.ini` 파일 전체 삭제 — 프리로더 404 에러로 인한 게임 시작 행 방지
- `isCJK()` 함수에 CJK Extension A, Compatibility Ideographs 범위 추가
- 한글은 `isCJK()`에서 제외 (한글은 띄어쓰기 기반 분리, CJK처럼 글자별 분리하면 부자연스러움)

---

## 수정된 파일

| File | Description |
|------|-------------|
| `teavm/src/.../FreeTypeFontGenerator.java` | CSS font-family 생성자, 아틀라스 리사이즈, 리빌드 로직 |
| `teavm/src/.../TeaVMPlatformSupport.java` | 2-generator 아키텍처, 언어별 font-family, 리셋 안전성 |
| `teavm/webapp/styles.css` | 7개 @font-face 선언 |
| `teavm/webapp/index.html` | 폰트 프리로드, document.fonts.ready 대기 |
| `teavm/webapp/fonts/*` | 풀 CJK 폰트 파일 추가 |
| `core/src/main/assets/fonts/*` | 풀 CJK 폰트 파일 추가 |

## Rendering Approach by Font Type

| Font Type | Rendering | Details |
|-----------|-----------|---------|
| NeoDunggeunmoPro (pixel) | Hinting.None, alpha binarization @ threshold 64 | 크리스프 픽셀 엣지 |
| Sans-serif (Inter, Noto Sans) | CSS font-family fallback, natural anti-aliasing | 안티앨리어싱 보존 |
| CJK (NotoSansKR/JP/SC/TC) | CSS font-family fallback, natural anti-aliasing | 언어별 우선순위 동적 변경 |
