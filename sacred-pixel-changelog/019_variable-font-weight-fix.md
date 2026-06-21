# 019. 간체 중국어 가변 폰트 굵기 수정

**날짜**: 2026-03-25

## 개요

간체 중국어(简体中文) 글자가 깨지거나 이상하게 렌더링되던 문제 수정. Variable font의 weight가 Canvas 2D에서 기본값 100(극세)으로 적용되던 것이 원인.

---

## 변경 사항

### Root Cause

### 문제
NotoSansSC, NotoSansKR, NotoSansJP 등 Google Fonts에서 다운로드한 폰트는 **Variable Font** (가변 폰트)로, weight 축이 100~900 범위를 가짐.

Canvas 2D의 `ctx.font` 설정에서 weight를 명시하지 않으면:
```javascript
// Before (broken)
ctx.font = "12px 'NotoSansSC', 'NotoSansKR', sans-serif";
```
브라우저가 variable font의 기본 weight를 **100 (Thin/극세)**으로 적용하여, 글자가 극도로 얇거나 보이지 않게 됨.

### 추가 문제
CSS `@font-face`에서 `font-weight: normal`로 선언하면 variable font의 weight 범위가 제대로 인식되지 않음.

## Fix

### 1. Canvas font 문자열에 weight 400 명시

**File:** `teavm/src/.../FreeTypeFontGenerator.java`

```javascript
// After (fixed)
ctx.font = "400 12px 'NotoSansSC', 'NotoSansKR', sans-serif";
```

3곳 수정: `jsRenderGlyphs`, `jsMeasureChar`, `jsMeasureFontMetrics`

### 2. CSS @font-face에 variable font weight range 선언

**File:** `teavm/webapp/styles.css`

```css
/* Before */
@font-face {
    font-family: 'NotoSansSC';
    font-weight: normal;
}

/* After */
@font-face {
    font-family: 'NotoSansSC';
    font-weight: 100 900;  /* Variable font range */
}
```

Inter, NotoSansKR, NotoSansJP, NotoSansTC, NotoSansSC 모두 `font-weight: 100 900`으로 변경.
NotoSans (Latin, static font)만 `font-weight: normal` 유지.

### 3. SC/TC woff2 파일 google/fonts 버전으로 교체

이전: noto-cjk 레포의 CFF OTF → fonttools로 woff2 변환 (CFF→TrueType 변환 과정에서 글리프 손실 가능)
이후: google/fonts 레포의 TTF(TrueType) → woff2 변환 (네이티브 TrueType, 안정적)

| Font | Before | After |
|------|--------|-------|
| NotoSansSC | 10.9MB (noto-cjk CFF→woff2) | 7.8MB (google/fonts TTF→woff2) |
| NotoSansTC | 10.9MB (noto-cjk CFF→woff2) | 5.4MB (google/fonts TTF→woff2) |

### 4. Build pipeline 수정

**File:** `teavm/src/.../TeaVMBuilder.java`

TeaVM 컴파일러가 기본 index.html을 생성한 후, 커스텀 webapp 파일(index.html, styles.css, fonts/)을 덮어쓰도록 `copyWebappFiles()` 메서드 추가. 이전에는 Gradle `doFirst` 블록에서 복사했지만, TeaVM이 나중에 덮어쓰는 문제가 있었음.

## Why KR Worked But SC Didn't

- NotoSansKR woff2 (3.8MB)도 variable font이지만, 한글 글리프는 weight 100에서도 상대적으로 가독성이 유지됨
- NotoSansSC의 간체 한자(语, 设 등)는 획이 복잡하여 weight 100에서 획이 사라지거나 뭉개짐
- 결과적으로 동일한 버그이지만 SC에서만 시각적으로 심각하게 나타남

## Lesson Learned

- **Variable font + Canvas 2D = 반드시 weight 명시** (`ctx.font = "400 12px ..."`)
- **CSS @font-face에서 variable font은 `font-weight: 100 900` range 선언** 필수
- **google/fonts의 TTF가 noto-cjk의 CFF OTF보다 Canvas 호환성이 높음**
- **빌드 파이프라인에서 TeaVM 기본 HTML 덮어쓰기 타이밍** 주의 필요

---

## 수정된 파일

| File | Changes |
|------|---------|
| `teavm/src/.../FreeTypeFontGenerator.java` | Canvas font 문자열에 weight 400 명시 (3곳) |
| `teavm/webapp/styles.css` | Variable font weight range 선언 (100 900) |
| `teavm/src/.../TeaVMBuilder.java` | copyWebappFiles() 메서드 추가 |
| `teavm/webapp/fonts/noto-sans-sc.woff2` | google/fonts TTF→woff2 교체 |
| `teavm/webapp/fonts/noto-sans-tc.woff2` | google/fonts TTF→woff2 교체 |
